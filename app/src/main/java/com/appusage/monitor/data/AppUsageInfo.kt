package com.appusage.monitor.data

import android.graphics.drawable.Drawable

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageTimeMs: Long,
    val lastTimeUsed: Long,
    val launchCount: Int,
    val appIcon: Drawable?
) {
    val usageTimeFormatted: String
        get() {
            val totalSeconds = usageTimeMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m ${seconds}s"
                else -> "${seconds}s"
            }
        }

    val usageTimeMinutes: Float
        get() = usageTimeMs / 60000f
}

enum class TimeFrame(val label: String, val days: Int) {
    TODAY("Today", 0),
    YESTERDAY("Yesterday", 1),
    WEEK("Last 7 Days", 7),
    MONTH("Last 30 Days", 30);

    companion object {
        fun fromPosition(position: Int): TimeFrame {
            return entries.getOrElse(position) { TODAY }
        }
    }
}

enum class SortOrder(val label: String) {
    LEAST_USED("Least → Most Used"),
    MOST_USED("Most → Least Used");
}
