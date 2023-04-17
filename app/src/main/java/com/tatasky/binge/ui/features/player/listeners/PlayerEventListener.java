package com.tatasky.binge.ui.features.player.listeners;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Varun Mishra on 10/8/18.
 */
final public class PlayerEventListener implements Player.EventListener {
    private PlayerListener.EventListener eventListener =null;

    public PlayerEventListener(@NotNull PlayerListener.EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        eventListener.onTimelineChanged(timeline,manifest,reason);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        eventListener.onTracksChanged(trackGroups,trackSelections);
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        eventListener.onLoadingChanged(isLoading);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        eventListener.onPlayerStateChanged(playWhenReady,playbackState);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        eventListener.onRepeatModeChanged(repeatMode);
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    eventListener.onShuffleModeEnabledChanged(shuffleModeEnabled);
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        eventListener.onPlayerError(error);
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        eventListener.onPositionDiscontinuity(reason);
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        eventListener.onPlaybackParametersChanged(playbackParameters);
    }

    @Override
    public void onSeekProcessed() {
        eventListener.onSeekProcessed();
    }
}
