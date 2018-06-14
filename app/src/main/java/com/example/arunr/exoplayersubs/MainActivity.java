package com.example.arunr.exoplayersubs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arunr.exoplayersubs.Exoplayer.ExoPlayerManager;
import com.example.arunr.exoplayersubs.Exoplayer.ExoPlayerMediaSourceBuilder;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.RandomTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextRenderer.Output {

    private static final int PICK_FILE_REQUEST_CODE = 2;

    private ExoPlayerManager exoPlayerManager;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private SubtitleView subtitleView;
    private ImageButton subtitleBtnOn;
    private ImageButton subtitleBtnOff;
    private ImageButton settingsBtn;

    private static final TrackSelection.Factory FIXED_FACTORY = new FixedTrackSelection.Factory();
    private static final TrackSelection.Factory RANDOM_FACTORY = new RandomTrackSelection.Factory();

    private TrackSelection.Factory adaptiveTrackSelectionFactory;
    private MappingTrackSelector selector;

    private DefaultTrackSelector trackSelector;
    private MappingTrackSelector.MappedTrackInfo trackInfo;
    private int rendererIndex;
    private TrackGroupArray trackGroups;
    private boolean[] trackGroupsAdaptive;
    private MappingTrackSelector.SelectionOverride override;

    // Options for subtitles
    private CharSequence languages[] = new CharSequence[]{"None", "English", "हिन्दी", "मराठी"};

    private DialogActivity dialogActivity;

    private CheckedTextView disableView;
    private CheckedTextView defaultView;
    private CheckedTextView[][] trackViews;
    private CheckedTextView englishSubs;
    private CheckedTextView hindiSubs;
    private CheckedTextView marathiSubs;
    private ExoPlayerMediaSourceBuilder mediaSourceBuilder;

    private List<SubtitleList> subtitleList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SubtitleAdapter subsAdapter;

    DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
    private Format format;

    public void showSubtitle(boolean show) {
        if (playerView != null && playerView.getSubtitleView() != null)
            if (show) {
                playerView.getSubtitleView().setVisibility(View.VISIBLE);
            } else {
                playerView.getSubtitleView().setVisibility(View.GONE);
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.video_view);

        // for toggling subtitles
        subtitleView = findViewById(R.id.exo_subtitles);
        subtitleBtnOn = findViewById(R.id.subs_on);
        subtitleBtnOff = findViewById(R.id.subs_off);

        // settings btn include multiple subtitle settings
        settingsBtn = findViewById(R.id.settings);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showSelectedSubtitle();
            }
        });

        exoPlayerManager = new ExoPlayerManager(playerView, player);
        mediaSourceBuilder = new ExoPlayerMediaSourceBuilder(playerView.getContext());
        exoPlayerManager.play(Uri.parse(getString(R.string.media_url_dash)));
        showSubtitle(false);
        requestFullScreenIfLandscape();
    }

    private void requestFullScreenIfLandscape() {
        if (getResources().getBoolean(R.bool.landscape)) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            exoPlayerManager.play(data.getData());
        }
    }

    // method to toggle subtitles on and off
    public void onClick(View view) {
        if (playerView != null && subtitleBtnOn.getVisibility() == View.GONE) {
            showSubtitle(true);
            subtitleBtnOn.setVisibility(View.VISIBLE);
            subtitleBtnOff.setVisibility(View.GONE);
        } else {
            showSubtitle(false);
            subtitleBtnOff.setVisibility(View.VISIBLE);
            subtitleBtnOn.setVisibility(View.GONE);
        }
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("Subtitles")
        //.setItems(languages, new DialogInterface.OnClickListener() {
        //  @Override
        //public void onClick(DialogInterface dialogInterface, int i) {
        //  Toast.makeText(getApplicationContext(), "Subs in " + getString(i), Toast.LENGTH_SHORT).show();
        //}
        //})
        //.setView(buildView(builder.getContext()))
        // .setPositiveButton(android.R.string.ok, null)
        //.setNegativeButton(android.R.string.cancel, null)
        //.create()
        // .show();
    }

    private void showSelectedSubtitle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        // To align the title in center
        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("Subtitles");
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        //title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setView(buildView(builder.getContext()));

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create();
        builder.show();
    }

    private View buildView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.track_selection_dialog, null);
        ViewGroup root = view.findViewById(R.id.root);

        recyclerView = findViewById(R.id.recycler_view);

        subsAdapter = new SubtitleAdapter(subtitleList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(subsAdapter);

        prepareSubtitleData();

        root.addView(recyclerView);

        return view;
    }

    private void prepareSubtitleData() {
        SubtitleList subtitles = new SubtitleList("None");
        subtitleList.add(subtitles);

        SubtitleList subtitleEng = new SubtitleList("English");
        subtitleList.add(subtitleEng);

        SubtitleList subtitleHindi = new SubtitleList("Hindi");
        subtitleList.add(subtitleHindi);

        SubtitleList subtitleMarathi = new SubtitleList("Marathi");
        subtitleList.add(subtitleMarathi);

        subsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        exoPlayerManager.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        exoPlayerManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        exoPlayerManager.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        exoPlayerManager.onStop();
    }

    @Override
    public void onCues(List<Cue> cues) {
        if (subtitleView != null) {
            subtitleView.onCues(cues);
        }
    }
}
