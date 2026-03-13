package com.appusage.monitor.util

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.appusage.monitor.data.AppUsageInfo
import com.appusage.monitor.data.SortOrder
import com.appusage.monitor.data.TimeFrame
import java.util.Calendar

class UsageStatsHelper(private val context: Context) {

    private val usageStatsManager: UsageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val packageManager: PackageManager = context.packageManager

    /**
     * Check if the app has usage stats permission
     */
    fun hasUsagePermission(): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            calendar.timeInMillis,
            System.currentTimeMillis()
        )
        return stats != null && stats.isNotEmpty()
    }

    /**
     * Get app usage data for the specified timeframe
     */
    fun getAppUsageStats(
        timeFrame: TimeFrame,
        sortOrder: SortOrder = SortOrder.LEAST_USED,
        includeSystemApps: Boolean = false
    ): List<AppUsageInfo> {

        val (startTime, endTime) = getTimeRange(timeFrame)

        val intervalType = when (timeFrame) {
            TimeFrame.TODAY, TimeFrame.YESTERDAY -> UsageStatsManager.INTERVAL_DAILY
            TimeFrame.WEEK -> UsageStatsManager.INTERVAL_WEEKLY
            TimeFrame.MONTH -> UsageStatsManager.INTERVAL_MONTHLY
        }

        val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(
            intervalType, startTime, endTime
        ) ?: emptyList()

        // Aggregate stats by package name (multiple entries can exist)
        val aggregated = mutableMapOf<String, AggregatedStats>()

        for (stat in usageStatsList) {
            val pkg = stat.packageName
            val existing = aggregated[pkg]
            if (existing != null) {
                aggregated[pkg] = AggregatedStats(
                    totalTime = existing.totalTime + stat.totalTimeInForeground,
                    lastUsed = maxOf(existing.lastUsed, stat.lastTimeUsed),
                    launchCount = existing.launchCount + getLaunchCount(stat)
                )
            } else {
                aggregated[pkg] = AggregatedStats(
                    totalTime = stat.totalTimeInForeground,
                    lastUsed = stat.lastTimeUsed,
                    launchCount = getLaunchCount(stat)
                )
            }
        }

        // Convert to AppUsageInfo list
        val appUsageList = aggregated
            .filter { it.value.totalTime > 0 } // Only include apps that were actually used
            .mapNotNull { (packageName, stats) ->
                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)

                    // Filter system apps if needed
                    if (!includeSystemApps && isSystemApp(appInfo)) {
                        return@mapNotNull null
                    }

                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val appIcon = try {
                        packageManager.getApplicationIcon(packageName)
                    } catch (e: Exception) {
                        null
                    }

                    AppUsageInfo(
                        packageName = packageName,
                        appName = appName,
                        usageTimeMs = stats.totalTime,
                        lastTimeUsed = stats.lastUsed,
                        launchCount = stats.launchCount,
                        appIcon = appIcon
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    null // App was uninstalled
                }
            }

        // Sort based on user preference (default: least to most)
        return when (sortOrder) {
            SortOrder.LEAST_USED -> appUsageList.sortedBy { it.usageTimeMs }
            SortOrder.MOST_USED -> appUsageList.sortedByDescending { it.usageTimeMs }
        }
    }

    /**
     * Get total screen time for a timeframe
     */
    fun getTotalScreenTime(timeFrame: TimeFrame): Long {
        return getAppUsageStats(timeFrame, includeSystemApps = true)
            .sumOf { it.usageTimeMs }
    }

    private fun getTimeRange(timeFrame: TimeFrame): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime: Long
        val startTime: Long

        when (timeFrame) {
            TimeFrame.TODAY -> {
                endTime = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTime = calendar.timeInMillis
            }
            TimeFrame.YESTERDAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                endTime = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                startTime = calendar.timeInMillis
            }
            TimeFrame.WEEK -> {
                endTime = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                startTime = calendar.timeInMillis
            }
            TimeFrame.MONTH -> {
                endTime = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                startTime = calendar.timeInMillis
            }
        }

        return Pair(startTime, endTime)
    }

    @Suppress("DEPRECATION")
    private fun getLaunchCount(stats: UsageStats): Int {
        return try {
            // API 28+ has this method
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                stats.javaClass.getMethod("getAppLaunchCount").invoke(stats) as? Int ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
               (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
    }

    private data class AggregatedStats(
        val totalTime: Long,
        val lastUsed: Long,
        val launchCount: Int
    )
}
