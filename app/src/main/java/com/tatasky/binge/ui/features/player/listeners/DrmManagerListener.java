package com.tatasky.binge.ui.features.player.listeners;

import com.google.android.exoplayer2.drm.DefaultDrmSessionEventListener;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Varun Mishra on 10/8/18.
 */
final public class DrmManagerListener implements DefaultDrmSessionEventListener {
    private PlayerListener.DefaultDrmSessionEventListener defaultDrmSessionEventListener = null;

    public DrmManagerListener(@NotNull PlayerListener.DefaultDrmSessionEventListener defaultDrmSessionEventListener) {
        this.defaultDrmSessionEventListener =defaultDrmSessionEventListener;
    }

    @Override
        public void onDrmKeysLoaded() {
        defaultDrmSessionEventListener.onDrmKeysLoaded();
        }

        @Override
        public void onDrmSessionManagerError(Exception error) {
        defaultDrmSessionEventListener.onDrmSessionManagerError(error);
        }

        @Override
        public void onDrmKeysRestored() {
        defaultDrmSessionEventListener.onDrmKeysRestored();
        }

        @Override
        public void onDrmKeysRemoved() {
        defaultDrmSessionEventListener.onDrmKeysRemoved();
        }
}
