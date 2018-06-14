package com.example.arunr.exoplayersubs.Exoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.arunr.exoplayersubs.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.RandomTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by arun.r on 11-06-2018.
 */

public class ExoPlayerManager {

    private ExoPlayerMediaSourceBuilder mediaSourceBuilder;
    private PlayerView playerView;
    private SimpleExoPlayer player;

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

    private CheckedTextView disableView;
    private CheckedTextView defaultView;
    private CheckedTextView[][] trackViews;

    private Uri subtitleUri;
    private Player.EventListener eventListener = new Player.DefaultEventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_READY) {

            }
        }
    };
    private Format format;

    public Format getFormat() {
        return format;
    }

    public Uri getSubtitleUri() {
        return subtitleUri;
    }

    public void setSubtitleUri(Uri subtitleUri) {
        this.subtitleUri = subtitleUri;
    }

    public ExoPlayerManager(PlayerView playerView, SimpleExoPlayer player) {
        this.playerView = playerView;
        this.player = player;
        this.mediaSourceBuilder = new ExoPlayerMediaSourceBuilder(playerView.getContext());
    }

    public void play(Uri uri) {
        mediaSourceBuilder.setUri(uri);
    }

    public void onStart() {
        if (Util.SDK_INT > 23) {
            createPlayers();
        }
    }

    public void onResume() {
        if (Util.SDK_INT <= 23) {
            createPlayers();
        }
    }

    public void onPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayers();
        }
    }

    public void onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayers();
        }
    }

    private void releasePlayers() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void createPlayers() {
        if (player != null) {
            player.release();
        }
        player = createFullPlayer();
        playerView.setPlayer(player);
    }

    public SimpleExoPlayer createFullPlayer() {
        TrackSelection.Factory videoTrackSelectionFactory
                = new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(playerView.getContext()),
                trackSelector, loadControl);

        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");

        // Build the subtitle mediasource
        format = Format.createTextSampleFormat(
                null, // An identifier for the track. May be null.
                MimeTypes.APPLICATION_SUBRIP, // The mime type. Must be set correctly.
                Format.NO_VALUE,
                null); // The subtitle language. May be null.

        MediaSource subtitleSourceEng = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-en.srt"),
                dataSourceFactory, format, C.TIME_UNSET);
        MediaSource subtitleSourceSp = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-es.srt"),
                dataSourceFactory, format, C.TIME_UNSET);
        MediaSource subtitleSourceFr = new SingleSampleMediaSource(Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-fr-Goofy.srt"),
                dataSourceFactory, format, C.TIME_UNSET);

        MergingMediaSource mergedSource = new MergingMediaSource(mediaSourceBuilder.getMediaSource(), subtitleSourceEng);

        player.setPlayWhenReady(true);
        player.prepare(mergedSource, false, false);
        player.addListener(eventListener);
        return player;
    }
}
