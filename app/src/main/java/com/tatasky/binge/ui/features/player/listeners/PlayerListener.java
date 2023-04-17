package com.tatasky.binge.ui.features.player.listeners;

import android.media.MediaCodec;
import android.net.NetworkInfo;
import android.view.Surface;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Varun Mishra on 10/8/18.
 */
public interface PlayerListener {

    /**
     * Custom Listener for getting the current and total time updates
     */
    interface TimeChangeListener{
        void onTimeChanged(@NotNull long currentTime, @NotNull long totalTime);
    }
    /**
     * A listener for core events of Player.
     */
    interface EventListener {
        void onTimelineChanged(Timeline timeline, Object manifest, @Player.TimelineChangeReason int reason);

        void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections);

        void onLoadingChanged(boolean isLoading);
        void onBuffering();
        void onBufferingEnded();
        void onPlayerStateChanged(boolean playWhenReady, int playbackState);

        void onRepeatModeChanged(@Player.RepeatMode int repeatMode);

        void onShuffleModeEnabledChanged(boolean shuffleModeEnabled);

        void onPlayerError(ExoPlaybackException error);

        void onPositionDiscontinuity(@Player.DiscontinuityReason int reason);

        void onPlaybackParametersChanged(PlaybackParameters playbackParameters);

        void onSeekProcessed();
    }


    interface AnalyticsEventListener {
        void onPlayerStateChanged(AnalyticsListener.EventTime eventTime, boolean playWhenReady, int playbackState);

        void onTimelineChanged(AnalyticsListener.EventTime eventTime, @Player.TimelineChangeReason int reason);

        void onPositionDiscontinuity(AnalyticsListener.EventTime eventTime, @Player.DiscontinuityReason int reason);

        void onSeekStarted(AnalyticsListener.EventTime eventTime);

        void onSeekProcessed(AnalyticsListener.EventTime eventTime);

        void onPlaybackParametersChanged(AnalyticsListener.EventTime eventTime, PlaybackParameters playbackParameters);

        void onRepeatModeChanged(AnalyticsListener.EventTime eventTime, @Player.RepeatMode int repeatMode);

        void onShuffleModeChanged(AnalyticsListener.EventTime eventTime, boolean shuffleModeEnabled);

        void onLoadingChanged(AnalyticsListener.EventTime eventTime, boolean isLoading);

        void onPlayerError(AnalyticsListener.EventTime eventTime, ExoPlaybackException error);

        void onTracksChanged(AnalyticsListener.EventTime eventTime, TrackGroupArray trackGroups, TrackSelectionArray trackSelections);

        void onLoadStarted(AnalyticsListener.EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData);

        void onLoadCompleted(AnalyticsListener.EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData);

        void onLoadCanceled(AnalyticsListener.EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData);

        void onLoadError(AnalyticsListener.EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled);

        void onDownstreamFormatChanged(AnalyticsListener.EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData);

        void onUpstreamDiscarded(AnalyticsListener.EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData);

        void onMediaPeriodCreated(AnalyticsListener.EventTime eventTime);

        void onMediaPeriodReleased(AnalyticsListener.EventTime eventTime);

        void onReadingStarted(AnalyticsListener.EventTime eventTime);

        void onBandwidthEstimate(AnalyticsListener.EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate);

        void onViewportSizeChange(AnalyticsListener.EventTime eventTime, int width, int height);

        void onNetworkTypeChanged(AnalyticsListener.EventTime eventTime, @Nullable NetworkInfo networkInfo);

        void onMetadata(AnalyticsListener.EventTime eventTime, Metadata metadata);

        void onDecoderEnabled(AnalyticsListener.EventTime eventTime, int trackType, DecoderCounters decoderCounters);

        void onDecoderInitialized(AnalyticsListener.EventTime eventTime, int trackType, String decoderName, long initializationDurationMs);

        void onDecoderInputFormatChanged(AnalyticsListener.EventTime eventTime, int trackType, Format format);

        void onDecoderDisabled(AnalyticsListener.EventTime eventTime, int trackType, DecoderCounters decoderCounters);

        void onAudioSessionId(AnalyticsListener.EventTime eventTime, int audioSessionId);

        void onAudioUnderrun(AnalyticsListener.EventTime eventTime, int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs);

        void onDroppedVideoFrames(AnalyticsListener.EventTime eventTime, int droppedFrames, long elapsedMs);

        void onVideoSizeChanged(AnalyticsListener.EventTime eventTime, int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio);

        void onRenderedFirstFrame(AnalyticsListener.EventTime eventTime, Surface surface);

        void onDrmKeysLoaded(AnalyticsListener.EventTime eventTime);

        void onDrmSessionManagerError(AnalyticsListener.EventTime eventTime, Exception error);

        void onDrmKeysRestored(AnalyticsListener.EventTime eventTime);

        void onDrmKeysRemoved(AnalyticsListener.EventTime eventTime);
    }

    interface DefaultDrmSessionEventListener {

        void onDrmKeysLoaded();

        void onDrmSessionManagerError(Exception error);

        void onDrmKeysRestored();

        void onDrmKeysRemoved();
    }

    interface BandwidthMeterEventListener {

        void onBandwidthSample(int elapsedMs, long bytes, long bitrate);
    }




    /**
     * A listener for internal errors.
     * <p>
     * These errors are not visible to the user, and hence this listener is provided for
     * informational purposes only. Note however that an internal error may cause a fatal
     * error if the player fails to recover. If this happens, {@link EventListener#onPlayerError(ExoPlaybackException)}
     * will be invoked.
     */
    interface InternalErrorListener {
        void onRendererInitializationError(Exception e);
        void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs);
        void onDecoderInitializationError(MediaCodecRenderer.DecoderInitializationException e);
        void onCryptoError(MediaCodec.CryptoException e);
        void onLoadError(int sourceId, IOException e);
        void onDrmSessionManagerError(Exception e);
    }

    /**
     * A listener for debugging information.
     */
    interface InfoListener {
        void onVideoFormatEnabled(Format format, int trigger, long mediaTimeMs);
        void onAudioFormatEnabled(Format format, int trigger, long mediaTimeMs);
        void onDroppedFrames(int count, long elapsed);
        void onBandwidthSample(int elapsedMs, long bytes, long bitrateEstimate);
        void onLoadStarted(int sourceId, long length, int type, int trigger, Format format,
                           long mediaStartTimeMs, long mediaEndTimeMs);
        void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format,
                             long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs);
        void onDecoderInitialized(String decoderName, long elapsedRealtimeMs,
                                  long initializationDurationMs);
        //void onAvailableRangeChanged(int sourceId, TimeRange availableRange);
    }

    /**
     * A listener for receiving notifications of timed text.
     */
    interface CaptionListener {
        void onCues(List<Cue> cues);
    }

    /**
     * A listener for receiving ID3 metadata parsed from the media stream.
     */
    interface Id3MetadataListener {
        void onId3Metadata(Map<String, Object> metadata);
    }

}
