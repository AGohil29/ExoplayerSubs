package com.example.arunr.exoplayersubs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
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
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements TextRenderer.Output {

    private static final int PICK_FILE_REQUEST_CODE = 2;

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private SubtitleView subtitleView;

    private DefaultTrackSelector trackSelector;
    private DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

    private Uri uri;
    private int streamType;
    private Handler mainHandler = new Handler();

    private ArrayList<SubtitleList> subtitleData = new ArrayList<>();
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
    private ImageView subsSize1, subsSize2, subsSize3, subsSize4, subsSize5;
    private boolean isSelected;
    // for subtitles background
    private ImageView subsBlackBackground, subsGrayBackground, subsTransparentBackground, subsWhiteBackground;
    private Dialog mFullScreenDialog;
    private boolean mExoPlayerFullscreen = false;
    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private boolean playWhenReady;

    private SubtitleList subtitles;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FrameLayout subtitleBtn;
    private ImageView subtitleBtnOn;
    private ImageView subtitleBtnOff;

    // string values to be used as keys in sharedPreference
    public static final String SHARED_PREF_NAME = "my_pref";
    public static final String SELECTED_SUBTITLE = "subtitleName";
    public static final String SUBTITLE_SIZE = "subtitleSize";
    public static final String SUBTITLE_FORMAT = "subtitleFormat";
    // values of subtitle background in int
    public static final int SUBTITLE_BACKGROUND_BLACK = 1;
    public static final int SUBTITLE_BACKGROUND_GRAY = 2;
    public static final int SUBTITLE_BACKGROUND_TRANSPARENT = 3;
    public static final int SUBTITLE_BACKGROUND_WHITE = 4;

    private int subsSizeInt;
    private int subsFormat;

    private int lastSubtitleSelected;

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
        //playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        // for toggling subtitles
        subtitleView = findViewById(R.id.exo_subtitles);
        subtitleBtn = findViewById(R.id.subtitleBtn);
        subtitleBtnOn = findViewById(R.id.subs_on);
        subtitleBtnOff = findViewById(R.id.subs_off);

        // to save the last selected subtitle
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        int subsSelected = sharedPreferences.getInt(SELECTED_SUBTITLE, 0);
        lastSubtitlePref(subsSelected);

        // Todo - save the last subtitle size selected
        subsSizeInt = sharedPreferences.getInt(SUBTITLE_SIZE, 13);
        lastSubtitleSizePref(subsSizeInt);

        // Todo - save the last subtitle format selected
        subsFormat = sharedPreferences.getInt(SUBTITLE_FORMAT, 1);
        lastSubtitleFormatPref(subsFormat);

        // settings btn include multiple subtitle settings
        subtitleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectedSubtitle();
                player.setPlayWhenReady(false);
            }
        });
        //requestFullScreenIfLandscape();

        //Todo save the player state when resumed again
        initFullscreenDialog();
        initFullscreenButton();
    }

    // methods for playing the video on fullscreen
    private void initFullscreenDialog() {

        mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }

    private void openFullscreenDialog() {

        ((ViewGroup) playerView.getParent()).removeView(playerView);
        mFullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }

    private void closeFullscreenDialog() {

        ((ViewGroup) playerView.getParent()).removeView(playerView);
        ((FrameLayout) findViewById(R.id.main_media_frame)).addView(playerView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fullscreen_expand));
    }

    private void initFullscreenButton() {

        PlaybackControlView controlView = playerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });
    }
    /*private void requestFullScreenIfLandscape() {
        if (getResources().getBoolean(R.bool.landscape)) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }*/

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checking the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //First Hide other objects (listview or recyclerview), better hide them using Gone.
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
            //to remove "information bar" above the action bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().hide();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getSupportActionBar().show();
        }
    }*/

    // method to toggle subtitles on and off
   /* public void onClick(View view) {
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
    }*/

    private void showSelectedSubtitle() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.track_selection_dialog, null);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        // set the height and width of dialog
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER;

        alertDialog.getWindow().setAttributes(layoutParams);

        // to prevent dialog box from getting dismissed on outside touch
        alertDialog.setCanceledOnTouchOutside(false);

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
                subtitles.setCheckedImage(false);
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

        subsAdapter = new SubtitleAdapter(subtitleData, this);
        recyclerView.setAdapter(subsAdapter);
        subsAdapter.notifyDataSetChanged();
        subsAdapter.setOnItemClickListener(new SubtitleAdapter.onRecyclerViewItemClickListener() {
            @Override
            public void onItemClickListener(RecyclerView.ViewHolder holder, int position) {
                //Completed if view is clicked its related imageview is displayed
                // prepare the player when ok button is clicked
                // Store value in sharedpreference for mediasource

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

            }

        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mergedSource != null) {
                    player.prepare(mergedSource, false, true);
                    alertDialog.dismiss();
                } else {
                    initializePlayer();
                    alertDialog.dismiss();
                }
                player.setPlayWhenReady(true);
            }
        });

        subsSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSubsSettingsDialog();
                subsSizeInt = sharedPreferences.getInt(SUBTITLE_SIZE, 13);
                lastSubtitleSizePref(subsSizeInt);
                // save the last subtitle format selected
                subsFormat = sharedPreferences.getInt(SUBTITLE_FORMAT, 1);
                lastSubtitleFormatPref(subsFormat);

                alertDialog.dismiss();
            }
        });
    }

    public void showSubsSettingsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.subtitle_settings_dialog, null);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        // to prevent dialog box from getting dismissed on outside touch
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.show();

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        Button btnOk = view.findViewById(R.id.subtitle_settings_dialog_btnOk);
        final TextView sampleSubtitleText = view.findViewById(R.id.sample_subtitle_text);

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
                // play when the ok button of dialog is clicked
                player.setPlayWhenReady(true);
                alertDialog.dismiss();
            }
        });
        //1. Done show a sample text of selected subtitle size and format
        //2. Completed selects only one size and one format
        // setting subtitles text size
        subsSize1 = view.findViewById(R.id.subs_size_1);
        subsSize2 = view.findViewById(R.id.subs_size_2);
        subsSize3 = view.findViewById(R.id.subs_size_3);
        subsSize4 = view.findViewById(R.id.subs_size_4);
        subsSize5 = view.findViewById(R.id.subs_size_5);

        // create your sharedpreferences
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, 0);
        editor = sharedPreferences.edit();

        subsSize1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 0.8f);
                // to change the border on selection
                subsSizeSelected(subsSize1);
                // deselect the remaining subsSize
                ImageView[] subSizesToDeselect = {subsSize2, subsSize3, subsSize4, subsSize5};
                subsSizeDeselectRemainingSizes(subSizesToDeselect);
                // change the sample subtitle text
                sampleSubtitleText.setTextSize(11f);

                // save some value in sharedpreference
                editor.putInt(SUBTITLE_SIZE, 11).apply();
            }
        });

        subsSize2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 0.9f);
                // to change the border on selection
                subsSizeSelected(subsSize2);
                // deselect the remaining subsSize
                ImageView[] subSizesToDeselect = {subsSize1, subsSize3, subsSize4, subsSize5};
                subsSizeDeselectRemainingSizes(subSizesToDeselect);
                // change the sample subtitle text
                sampleSubtitleText.setTextSize(12f);

                // save some value in sharedpreference
                editor.putInt(SUBTITLE_SIZE, 12).apply();
            }
        });

        subsSize3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.0f);
                // to change the border on selection
                subsSizeSelected(subsSize3);
                // deselect the remaining subsSize
                ImageView[] subSizesToDeselect = {subsSize1, subsSize2, subsSize4, subsSize5};
                subsSizeDeselectRemainingSizes(subSizesToDeselect);
                // change the sample subtitle text
                sampleSubtitleText.setTextSize(13f);

                // save some value in sharedpreference
                editor.putInt(SUBTITLE_SIZE, 13).apply();

            }
        });

        subsSize4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.1f);
                // to change the border on selection
                subsSizeSelected(subsSize4);
                // deselect the remaining subsSize
                ImageView[] subSizesToDeselect = {subsSize1, subsSize2, subsSize3, subsSize5};
                subsSizeDeselectRemainingSizes(subSizesToDeselect);
                // change the sample subtitle text
                sampleSubtitleText.setTextSize(14f);

                // save some value in sharedpreference
                editor.putInt(SUBTITLE_SIZE, 14).apply();

            }
        });

        subsSize5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.2f);
                // to change the border on selection
                subsSizeSelected(subsSize5);
                // deselect the remaining subsSize
                ImageView[] subSizesToDeselect = {subsSize1, subsSize2, subsSize3, subsSize4};
                subsSizeDeselectRemainingSizes(subSizesToDeselect);
                // change the sample subtitle text
                sampleSubtitleText.setTextSize(15f);

                // save some value in sharedpreference
                editor.putInt(SUBTITLE_SIZE, 15).apply();

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
                // to change the border on selection
                subtitleFormatSelected(subsBlackBackground);

                // deselect remaining subtitle format
                ImageView subtileFormats[] = {subsGrayBackground, subsTransparentBackground, subsWhiteBackground};
                deselectOtherSubtitleFormat(subtileFormats);
                // set the background and font color of sample subtitle text
                sampleSubtitleText.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                sampleSubtitleText.setTextColor(getResources().getColor(R.color.colorWhite));

                // save value of subtitle background
                editor.putInt(SUBTITLE_FORMAT, SUBTITLE_BACKGROUND_BLACK).apply();

            }
        });

        // sets gray background and white text color for subtitles
        subsGrayBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captionStyleCompat = new CaptionStyleCompat(Color.WHITE, Color.LTGRAY, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.LTGRAY, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
                // to change the border on selection
                subtitleFormatSelected(subsGrayBackground);

                // deselect remaining subtitle format
                ImageView subtileFormats[] = {subsBlackBackground, subsTransparentBackground, subsWhiteBackground};
                deselectOtherSubtitleFormat(subtileFormats);
                // set the background and font color of sample subtitle text
                sampleSubtitleText.setBackgroundColor(Color.parseColor("#696969"));
                sampleSubtitleText.setTextColor(getResources().getColor(R.color.colorWhite));

                // save value of subtitle background
                editor.putInt(SUBTITLE_FORMAT, SUBTITLE_BACKGROUND_GRAY).apply();

            }
        });

        // sets transparent background and yellow text color for subtitles
        subsTransparentBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captionStyleCompat = new CaptionStyleCompat(Color.YELLOW, Color.TRANSPARENT, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.TRANSPARENT, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
                // to change the border on selection
                subtitleFormatSelected(subsTransparentBackground);

                // deselect remaining subtitle format
                ImageView subtileFormats[] = {subsGrayBackground, subsBlackBackground, subsWhiteBackground};
                deselectOtherSubtitleFormat(subtileFormats);
                // set the background and font color of sample subtitle text
                sampleSubtitleText.setBackgroundColor(Color.parseColor("#696969"));
                sampleSubtitleText.setTextColor(getResources().getColor(R.color.colorYellow));

                // save value of subtitle background
                editor.putInt(SUBTITLE_FORMAT, SUBTITLE_BACKGROUND_TRANSPARENT).apply();

            }
        });

        // sets white background and black text color for subtitles
        subsWhiteBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captionStyleCompat = new CaptionStyleCompat(Color.BLACK, Color.WHITE, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.TRANSPARENT, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
                // to change the border on selection
                subtitleFormatSelected(subsWhiteBackground);

                // deselect remaining subtitle format
                ImageView subtileFormats[] = {subsGrayBackground, subsTransparentBackground, subsBlackBackground};
                deselectOtherSubtitleFormat(subtileFormats);
                // set the background and font color of sample subtitle text
                sampleSubtitleText.setBackgroundColor(Color.parseColor("#696969"));
                sampleSubtitleText.setTextColor(getResources().getColor(R.color.colorBlack));

                // save value of subtitle background
                editor.putInt(SUBTITLE_FORMAT, SUBTITLE_BACKGROUND_WHITE).apply();

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initFullscreenDialog();
            initFullscreenButton();
            initializePlayer();

            lastSubtitlePref(lastSubtitleSelected);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT >= 23 && player == null) {
            initFullscreenDialog();
            initFullscreenButton();
            initializePlayer();

            lastSubtitlePref(lastSubtitleSelected);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayers();

            if (mFullScreenDialog != null)
                mFullScreenDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayers();

            if (mFullScreenDialog != null)
                mFullScreenDialog.dismiss();
        }
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

    private void lastSubtitlePref(int lastSubtitleSelected) {
        // plays the player with the last with users preferred subtitle
        switch (lastSubtitleSelected) {
            case 0:
                initializePlayer();
                // you can pass single or multiple sources but not null
                // if null is passed it crashes
                mergedSource = new MergingMediaSource(videoSource);
                player.prepare(mergedSource, false, true);
                showSubtitle(false);
                break;

            case 1:
                initializePlayer();
                MediaSource subtitleSourceEng = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-en.srt"),
                        dataSourceFactory, format, C.TIME_UNSET);
                mergedSource = new MergingMediaSource(videoSource, subtitleSourceEng);
                player.prepare(mergedSource, false, true);
                showSubtitle(true);
                break;

            case 2:
                initializePlayer();
                MediaSource subtitleSourceSp = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-es.srt"),
                        dataSourceFactory, format, C.TIME_UNSET);
                mergedSource = new MergingMediaSource(videoSource, subtitleSourceSp);
                player.prepare(mergedSource, false, true);
                showSubtitle(true);
                break;

            case 3:
                initializePlayer();
                MediaSource subtitleSourceFr = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-fr-Goofy.srt"),
                        dataSourceFactory, format, C.TIME_UNSET);

                mergedSource = new MergingMediaSource(videoSource, subtitleSourceFr);
                player.prepare(mergedSource, false, true);
                showSubtitle(true);
                break;
        }
    }

    // method to change and highlight the size of subtitle as per saved preference
    private void lastSubtitleSizePref(int subsSizeInt) {
        if (subsSizeInt == 11) {
            if (subsSize1 != null) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 0.8f);
                subsSize1.setBackgroundResource(R.drawable.border_style_selected);
                subsSize1.setSelected(true);
            } else {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 0.8f);
            }

        } else if (subsSizeInt == 12) {
            if (subsSize2 != null) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 0.9f);
                subsSize2.setBackgroundResource(R.drawable.border_style_selected);
                subsSize2.setSelected(true);
            } else {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 0.9f);
            }

        } else if (subsSizeInt == 13) {
            if (subsSize3 != null) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1f);
                subsSize3.setBackgroundResource(R.drawable.border_style_selected);
                subsSize3.setSelected(true);
            } else {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1f);
            }

        } else if (subsSizeInt == 14) {
            if (subsSize4 != null) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.1f);
                subsSize4.setBackgroundResource(R.drawable.border_style_selected);
                subsSize4.setSelected(true);
            } else {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.1f);
            }

        } else {
            if (subsSize5 != null) {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.2f);
                subsSize5.setBackgroundResource(R.drawable.border_style_selected);
                subsSize5.setSelected(true);
            } else {
                playerView.getSubtitleView().setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 1.2f);
            }

        }
    }

    // method for changing the subtitle format as per saved preference
    private void lastSubtitleFormatPref(int subsFormat) {
        if (subsFormat == SUBTITLE_BACKGROUND_BLACK) {
            if (subsBlackBackground != null) {
                captionStyleCompat = new CaptionStyleCompat(Color.WHITE, Color.BLACK, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.LTGRAY, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
                subtitleFormatSelected(subsBlackBackground);
            } else {
                captionStyleCompat = new CaptionStyleCompat(Color.WHITE, Color.BLACK, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.LTGRAY, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
            }

        } else if (subsFormat == SUBTITLE_BACKGROUND_GRAY) {
            if (subsGrayBackground != null) {
                captionStyleCompat = new CaptionStyleCompat(Color.WHITE, Color.LTGRAY, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.LTGRAY, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
                // to change the border on selection
                subtitleFormatSelected(subsGrayBackground);
            } else {
                captionStyleCompat = new CaptionStyleCompat(Color.WHITE, Color.LTGRAY, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.LTGRAY, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
            }

        } else if (subsFormat == SUBTITLE_BACKGROUND_TRANSPARENT) {
            if (subsTransparentBackground != null) {
                captionStyleCompat = new CaptionStyleCompat(Color.YELLOW, Color.TRANSPARENT, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.TRANSPARENT, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
                // to change the border on selection
                subtitleFormatSelected(subsTransparentBackground);
            } else {
                captionStyleCompat = new CaptionStyleCompat(Color.YELLOW, Color.TRANSPARENT, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.TRANSPARENT, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
            }

        } else {
            if (subsWhiteBackground != null) {
                captionStyleCompat = new CaptionStyleCompat(Color.BLACK, Color.WHITE, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.TRANSPARENT, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
                // to change the border on selection
                subtitleFormatSelected(subsWhiteBackground);
            } else {
                captionStyleCompat = new CaptionStyleCompat(Color.BLACK, Color.WHITE, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.TRANSPARENT, null);
                playerView.getSubtitleView().setStyle(captionStyleCompat);
            }
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
        }
    }

   /* private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }*/

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
        return new DefaultDataSourceFactory(this, bandwidthMeter,
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

    private void subsSizeSelected(ImageView subsSize) {
        if (subsSize.isSelected()) {
            subsSize.setBackgroundResource(R.drawable.border_style);
            subsSize.setSelected(false);
        } else {
            subsSize.setBackgroundResource(R.drawable.border_style_selected);
            subsSize.setSelected(true);
        }
    }

    private void subsSizeDeselectRemainingSizes(ImageView subsSizeToDeselect[]) {
        for (ImageView aSubsSizeToDeselect : subsSizeToDeselect) {
            aSubsSizeToDeselect.setBackgroundResource(R.drawable.border_style);
            aSubsSizeToDeselect.setSelected(false);
        }
    }

    private void subtitleFormatSelected(ImageView subtitleFormat) {
        if (subtitleFormat.isSelected()) {
            subtitleFormat.setBackgroundResource(R.drawable.border_style);
            subtitleFormat.setSelected(false);
        } else {
            subtitleFormat.setBackgroundResource(R.drawable.border_style_selected);
            subtitleFormat.setSelected(true);
        }
    }

    private void deselectOtherSubtitleFormat(ImageView subtitleFormat[]) {
        for (ImageView aSubtitleFormat : subtitleFormat) {
            aSubtitleFormat.setBackgroundResource(R.drawable.border_style);
            aSubtitleFormat.setSelected(false);
        }
    }


    private class PlayerEventListener extends Player.DefaultEventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            super.onPlayerStateChanged(playWhenReady, playbackState);

        }
    }
}
