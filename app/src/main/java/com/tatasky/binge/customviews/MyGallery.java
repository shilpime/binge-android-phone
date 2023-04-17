package com.tatasky.binge.customviews;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

import static com.tatasky.binge.utils.UtilityKt.getRealDisplayPoint;

final public class MyGallery extends Gallery {
    private GalleryListener mlistener;
    public static final int RIGHT = 1;
    int velocity = 4050;
    private boolean stuck = false;

    public MyGallery(Context context) {
        super(context);
    }

    public MyGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyGallery(Context context, AttributeSet attrs) {
        super(context, attrs);

        int x = getRealDisplayPoint(getContext()).x;
        velocity = (int) (x * 3.8);
        setAnimationDuration(500);
    }

    public void setListener(GalleryListener mlistener) {
        this.mlistener = mlistener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && mlistener != null) {
            mlistener.onStartSlide();
        }
        return stuck || super.onTouchEvent(event);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        int kEvent;

        if (isScrollingLeft(e1, e2)) { // Check if scrolling left
            kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
        } else { // Otherwise scrolling right
            kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
        }

        onKeyDown(kEvent, null);
        return false;
    }

    private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
        return e2.getX() > e1.getX();
    }

    /**
     * Slide direction=1 to the right, the left -1
     */
    public void slide(int direction) {
        MotionEvent e1 = MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN,
                300.0f, 265.33334f, 0);

        MotionEvent e2 = MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 89.333336f,
                238.00003f, 0);

        if (direction == RIGHT) this.onFling(e1, e2, -velocity, 0);
        else this.onFling(e1, e2, velocity, 0);
    }

    public void setScrollingEnabled(boolean enabled) {
        stuck = !enabled;
    }
}
