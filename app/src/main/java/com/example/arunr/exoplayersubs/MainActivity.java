package com.example.arunr.exoplayersubs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.RandomTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextRenderer.Output {

    private static final int PICK_FILE_REQUEST_CODE = 2;

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private SubtitleView subtitleView;
    private ImageButton subtitleBtnOn;
    private ImageButton subtitleBtnOff;
    private ImageButton settingsBtn;

    private DefaultTrackSelector trackSelector;
    private DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

    private Uri uri;
    private int streamType;
    private Handler mainHandler = new Handler();

    private ArrayList<SubtitleList> subtitleData = new ArrayList<SubtitleList>();
    private RecyclerView recyclerView;
    private SubtitleAdapter subsAdapter;

    DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private Format format;
    private EventLogger eventLogger;
    private Player.EventListener eventListener;
    private MergingMediaSource mergedSource;
    private MediaSource videoSource;
    private CaptionStyleCompat captionStyleCompat;
    // for subtitles text size
    private TextView subsSize1, subsSize2, subsSize3, subsSize4, subsSize5;
    // for subtitles background
    private TextView subsBlackBackground, subsGrayBackground, subsTransparentBackground, subsWhiteBackground;
    private SubtitleList subtitles;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public void showSubtitle(boolean show) {
        if (playerView != null && playerView.getSubtitleView() != null)
            if (show) {
                playerView.getSubtitleView().setVisibility(View.VISIBLE);
                subtitleBtnOn.setVisibility(View.VISIBLE);
                subtitleBtnOff.setVisibility(View.GONE);
            } else {
                playerView.getSubtitleView().setVisibility(View.GONE);
                subtitleBtnOn.setVisibility(View.GONE);
                subtitleBtnOff.setVisibility(View.VISIBLE);
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

        prefs = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = prefs.edit();

        // settings btn include multiple subtitle settings
        settingsBtn = findViewById(R.id.settings);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectedSubtitle();
            }
        });

        initializePlayer();
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

    // method to toggle subtitles on and off
    public void onClick(View view) {
        if (playerView != null && subtitleBtnOn.getVisibility() == View.GONE) {
            initializePlayer();
            MediaSource subtitleSourceEng = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-en.srt"),
                    dataSourceFactory, format, C.TIME_UNSET);
            mergedSource = new MergingMediaSource(videoSource, subtitleSourceEng);
            player.prepare(mergedSource, false, false);
            showSubtitle(true);
        } else {
            showSubtitle(false);
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        /*// To align the title in center
        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("Subtitles");
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        //title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);*/

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.track_selection_dialog, null);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button subsSettingBtn, btnOk;
        subsSettingBtn = view.findViewById(R.id.subtitle_settings);
        btnOk = view.findViewById(R.id.btnOk);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        String subs[] = {"None", "English", "Spanish", "French"};
        if (subtitleData != null && subtitleData.size() < subs.length) {
            //for (int i = 0; i < subs.length; i++)
            for (String sub : subs) {
                subtitles = new SubtitleList();
                subtitles.setSubtitleLanguage(sub);
                this.subtitleData.add(subtitles);
            }
        }
        // adds subs data only once
        /*if (subtitleData != null && subtitleData.size() < SubtitleData.languages.length) {
            // list of different subtitle languages
            for (int i = 0; i < SubtitleData.languages.length; i++) {
                if (subtitleData != null && i < languages.length)
                    subtitleData.add(new SubtitleList(
                            SubtitleData.languages[i]
                    ));
            }
        }*/
        subsAdapter = new SubtitleAdapter(subtitleData);
        recyclerView.setAdapter(subsAdapter);
        subsAdapter.notifyDataSetChanged();
        subsAdapter.setOnItemClickListener(new SubtitleAdapter.onRecyclerViewItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                //Todo if view is clicked its related checkbox is checked
                if (position == 0) {
                    initializePlayer();
                    // you can pass single or multiple sources but not null
                    // if null is passed it crashes
                    mergedSource = new MergingMediaSource(videoSource);
                    showSubtitle(false);
                } else if (position == 1) {
                    initializePlayer();
                    MediaSource subtitleSourceEng = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-en.srt"),
                            dataSourceFactory, format, C.TIME_UNSET);
                    mergedSource = new MergingMediaSource(videoSource, subtitleSourceEng);
                    showSubtitle(true);

                } else if (position == 2) {
                    initializePlayer();
                    MediaSource subtitleSourceSp = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-es.srt"),
                            dataSourceFactory, format, C.TIME_UNSET);
                    mergedSource = new MergingMediaSource(videoSource, subtitleSourceSp);
                    showSubtitle(true);
                } else {
                    initializePlayer();
                    MediaSource subtitleSourceFr = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-fr-Goofy.srt"),
                            dataSourceFactory, format, C.TIME_UNSET);

                    mergedSource = new MergingMediaSource(videoSource, subtitleSourceFr);
                    showSubtitle(true);
                }
                player.prepare(mergedSource, false, true);
            }

        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        subsSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSubsSettingsDialog();
                alertDialog.dismiss();
            }
        });
    }

    private void showSubsSettingsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.subtitle_settings_dialog, null);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        Button btnOk = view.findViewById(R.id.subtitle_settings_dialog_btnOk);

        // show the 1st dialog again on back pressed
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                showSelectedSubtitle();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        // setting subtitles text size
        subsSize1 = view.findViewById(R.id.subs_size_1);
        subsSize2 = view.findViewById(R.id.subs_size_2);
        subsSize3 = view.findViewById(R.id.subs_size_3);
        subsSize4 = view.findViewById(R.id.subs_size_4);
        subsSize5 = view.findViewById(R.id.subs_size_5);

        subsSize1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 0.8f);
            }
        });

        subsSize2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 0.9f);
            }
        });

        subsSize3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.0f);
            }
        });

        subsSize4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.1f);
            }
        });

        subsSize5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.2f);
            }
        });

        // setting subtitles background and font color
        subsBlackBackground = view.findViewById(R.id.format_black);
        subsGrayBackground = view.findViewById(R.id.format_light_grey);
        subsTransparentBackground = view.findViewById(R.id.format_transparent);
        subsWhiteBackground = view.findViewById(R.id.format_white);

        // sets black background and white text color for subtitles
        subsBlackBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captionStyleCompat = new CaptionStyleCompat(Color.WHITE, Color.BLACK, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.LTGRAY, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
            }
        });

        // sets gray background and white text color for subtitles
        subsGrayBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captionStyleCompat = new CaptionStyleCompat(Color.WHITE, Color.LTGRAY, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.LTGRAY, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
            }
        });

        // sets transparent background and yellow text color for subtitles
        subsTransparentBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captionStyleCompat = new CaptionStyleCompat(Color.YELLOW, Color.TRANSPARENT, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.TRANSPARENT, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
            }
        });

        // sets white background and black text color for subtitles
        subsWhiteBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captionStyleCompat = new CaptionStyleCompat(Color.BLACK, Color.WHITE, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.TRANSPARENT, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        initializePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayers();
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
        player.removeListener(null);
    }

    @Override
    public void onCues(List<Cue> cues) {
        if (subtitleView != null) {
            subtitleView.onCues(cues);
        }
    }

    private void initializePlayer() {
        if (player == null) {
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
            eventLogger = new EventLogger(trackSelector);

            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(playerView.getContext());

            player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
            player.addListener(new PlayerEventListener());
            player.addListener(eventLogger);
            player.setPlayWhenReady(true);
            playerView.setPlayer(player);

            // Build the subtitle mediasource
            format = Format.createTextSampleFormat(
                    null, // An identifier for the track. May be null.
                    MimeTypes.APPLICATION_SUBRIP, // The mime type. Must be set correctly.
                    Format.NO_VALUE,
                    null); // The subtitle language. May be null.

            videoSource = buildMediaSource(Uri.parse(getString(R.string.media_url_dash)), mainHandler, eventLogger);
            player.prepare(videoSource, false, true);
            // use if for different mediasource
            /*if (position == 1) {
                MediaSource subtitleSourceEng = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-en.srt"),
                        dataSourceFactory, format, C.TIME_UNSET);
                mergedSource = new MergingMediaSource(videoSource, subtitleSourceEng);
                player.prepare(mergedSource, false, true);

            } else if (position == 2) {
                MediaSource subtitleSourceSp = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-es.srt"),
                        dataSourceFactory, format, C.TIME_UNSET);
                mergedSource = new MergingMediaSource(videoSource, subtitleSourceSp);
                player.prepare(mergedSource, false, true);
            } else if (position == 3) {
                MediaSource subtitleSourceFr = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-fr-Goofy.srt"),
                        dataSourceFactory, format, C.TIME_UNSET);

                mergedSource = new MergingMediaSource(videoSource, subtitleSourceFr);
                player.prepare(mergedSource, false, true);
            } else {
                MediaSource subtitleSourceEng = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-en.srt"),
                        dataSourceFactory, format, C.TIME_UNSET);
                mergedSource = new MergingMediaSource(videoSource, subtitleSourceEng);
                player.prepare(mergedSource, false, true);
            }*/
        }
    }

    private MediaSource buildMediaSource(
            Uri uri,
            @Nullable Handler handler,
            @Nullable MediaSourceEventListener listener) {
        @C.ContentType int type = Util.inferContentType(uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(getDataSourceFactory()),
                        getHttpDataSourceFactory())
                        .createMediaSource(uri, handler, listener);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(getDataSourceFactory()),
                        getHttpDataSourceFactory())
                        .createMediaSource(uri, handler, listener);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(getDataSourceFactory())
                        .createMediaSource(uri, handler, listener);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(getDataSourceFactory())
                        .createMediaSource(uri, handler, listener);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private DataSource.Factory getDataSourceFactory() {
        return new DefaultDataSourceFactory(this,bandwidthMeter,
                getHttpDataSourceFactory());
    }

    private DataSource.Factory getHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this,
                "ExoPlayerDemo"), bandwidthMeter);
    }

    private void releasePlayers() {
        if (player != null) {
            player.release();
            player = null;
        }
    }


    private class PlayerEventListener extends Player.DefaultEventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            super.onPlayerStateChanged(playWhenReady, playbackState);

        }
    }
}
