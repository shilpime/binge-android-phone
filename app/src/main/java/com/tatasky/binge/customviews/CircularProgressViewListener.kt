package com.tatasky.binge.customviews

interface CircularProgressViewListener {
    /**
     * Called when setProgress(float currentProgress) is called (determinate only)
     *
     * @param currentProgress The progress that was set.
     */
    fun onProgressUpdate(currentProgress: Float)

    /**
     * Called when this view finishes animating to the updated progress. (Determinate only)
     *
     * @param currentProgress The progress that was set and this view has reached in its animation.
     */
    fun onProgressUpdateEnd(currentProgress: Float)

    /**
     * Called when resetAnimation() is called.
     */
    fun onAnimationReset()

    /**
     * Called when you switch between indeterminate and determinate modes.
     *
     * @param isIndeterminate true if mode was set to indeterminate, false otherwise.
     */
    fun onModeChanged(isIndeterminate: Boolean)
}
