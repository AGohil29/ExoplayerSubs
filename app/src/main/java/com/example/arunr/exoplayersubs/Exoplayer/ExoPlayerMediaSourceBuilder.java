package com.example.arunr.exoplayersubs.Exoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by arun.r on 11-06-2018.
 */

public class ExoPlayerMediaSourceBuilder {

    private DefaultBandwidthMeter bandwidthMeter;
    private Context context;
    private Uri uri;
    private int streamType;
    private Handler mainHandler = new Handler();

    public ExoPlayerMediaSourceBuilder(Context context) {
        this.context = context;
        this.bandwidthMeter = new DefaultBandwidthMeter();
    }

    public void setUri(Uri uri) {
        this.uri = uri;
        this.streamType = Util.inferContentType(uri.getLastPathSegment());
    }

    public MediaSource getMediaSource() {
        switch (streamType) {
            case C.TYPE_SS:
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(getDataSourceFactory()),
                        getHttpDataSourceFactory())
                        .createMediaSource(uri, mainHandler, null);
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(getDataSourceFactory()),
                        getHttpDataSourceFactory()).createMediaSource(uri, mainHandler, null);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(getDataSourceFactory())
                        .createMediaSource(uri, mainHandler, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(getDataSourceFactory())
                        .createMediaSource(uri, mainHandler, null);
            default: {
                throw new IllegalStateException("Unsupported type: " + streamType);
            }
        }
    }

    private DataSource.Factory getDataSourceFactory() {
        return new DefaultDataSourceFactory(context,bandwidthMeter,
                getHttpDataSourceFactory());
    }

    private DataSource.Factory getHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(context,
                "ExoPlayerDemo"), bandwidthMeter);
    }

}
