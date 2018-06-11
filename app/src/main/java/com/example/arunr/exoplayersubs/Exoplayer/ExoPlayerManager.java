package com.example.arunr.exoplayersubs.Exoplayer;

import android.net.Uri;
import android.widget.ImageView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
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
    private Player.EventListener eventListener = new Player.DefaultEventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_READY) {

            }
        }
    };

    public ExoPlayerManager() {

    }

    public ExoPlayerManager(PlayerView playerView) {
        this.playerView = playerView;
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

    public void stopPreview() {
        player.setPlayWhenReady(true);
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

        Uri subtitleUri = Uri.parse("https://download.blender.org/demo/movies/ToS/subtitles/TOS-en.srt");
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");

        // Build the subtitle mediasource
        Format subtitleFormat = Format.createTextSampleFormat(
                null, // An identifier for the track. May be null.
                MimeTypes.APPLICATION_SUBRIP, // The mime type. Must be set correctly.
                Format.NO_VALUE,
                "en"); // The subtitle language. May be null.

        MediaSource subtitleSource = new SingleSampleMediaSource(subtitleUri, dataSourceFactory, subtitleFormat, C.TIME_UNSET);

        MergingMediaSource mergedSource = new MergingMediaSource(mediaSourceBuilder.getMediaSource(), subtitleSource);

        player.setPlayWhenReady(true);
        player.prepare(mergedSource, true, false);
        player.addListener(eventListener);
        return player;
    }

}
