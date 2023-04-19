package com.tatasky.binge.utils

object PlayerUtils {

    fun getCurrentSeekBarProgressInPercentage(
        currentDuration: Long?,
        totalDuration: Long?,
        decimalFormat: String = "%.2f"
    ): String {
        val currentDurationInSeconds = (currentDuration ?: 0) / 1000
        val totalDurationInSeconds = (totalDuration ?: 0) / 1000
        val seekbarProgress = ((currentDurationInSeconds.toFloat() / totalDurationInSeconds) * 100)
        return "${String.format(decimalFormat, seekbarProgress)}%"
    }
}