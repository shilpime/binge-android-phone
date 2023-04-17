package com.tatasky.binge.ui.features.player.listeners;

import android.os.Handler;
import com.google.android.exoplayer2.SimpleExoPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Varun Mishra on 28/8/18.
 */
final public class PlayerDurationWatcher implements Runnable {

    private static final int REFRESH_INTERVAL_MS = 1000;
    private final SimpleExoPlayer player;
    private final Handler handler;
    private final PlayerListener.TimeChangeListener timeChangeListener;
    private boolean started;

    /**
     * @param player The {@link SimpleExoPlayer} from which duration information should be obtained.
     * @param timeChangeListener The {@link PlayerListener.TimeChangeListener} that should be updated to display the information.
     */
    public PlayerDurationWatcher(@NotNull SimpleExoPlayer player, @NotNull PlayerListener.TimeChangeListener timeChangeListener) {
        this.player = player;
        this.handler= new Handler();
        this.timeChangeListener = timeChangeListener;
    }

    /**
     * Starts periodic updates of the {@link PlayerListener.TimeChangeListener}.
     * thread.
     */
    public final void start() {
        if (started) {
            return;
        }
        started = true;
        updateAndPost();
    }

    /**
     * Stops periodic updates of the {@link PlayerListener.TimeChangeListener}.
     */
    public final void stop() {
        if (!started) {
            return;
        }
        started = false;
        handler.removeCallbacks(this);
    }

    // Runnable implementation.

    @Override
    public final void run() {
        updateAndPost();
    }

    protected final void updateAndPost() {
        timeChangeListener.onTimeChanged(player.getCurrentPosition(),player.getDuration());
        handler.removeCallbacks(this);
        handler.postDelayed(this, REFRESH_INTERVAL_MS);
    }
}
