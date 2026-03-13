package com.appusage.monitor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.appusage.monitor.data.AppUsageInfo
import com.appusage.monitor.data.SortOrder
import com.appusage.monitor.data.TimeFrame
import com.appusage.monitor.util.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val usageStatsHelper = UsageStatsHelper(application)

    private val _usageData = MutableLiveData<List<AppUsageInfo>>()
    val usageData: LiveData<List<AppUsageInfo>> = _usageData

    private val _totalScreenTime = MutableLiveData<String>()
    val totalScreenTime: LiveData<String> = _totalScreenTime

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _hasPermission = MutableLiveData<Boolean>()
    val hasPermission: LiveData<Boolean> = _hasPermission

    var currentTimeFrame: TimeFrame = TimeFrame.TODAY
        private set

    var currentSortOrder: SortOrder = SortOrder.LEAST_USED
        private set

    var showSystemApps: Boolean = false
        private set

    fun checkPermission() {
        _hasPermission.value = usageStatsHelper.hasUsagePermission()
    }

    fun loadUsageData(
        timeFrame: TimeFrame = currentTimeFrame,
        sortOrder: SortOrder = currentSortOrder,
        includeSystemApps: Boolean = showSystemApps
    ) {
        currentTimeFrame = timeFrame
        currentSortOrder = sortOrder
        showSystemApps = includeSystemApps

        _isLoading.value = true

        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                usageStatsHelper.getAppUsageStats(timeFrame, sortOrder, includeSystemApps)
            }
            val screenTime = withContext(Dispatchers.IO) {
                usageStatsHelper.getTotalScreenTime(timeFrame)
            }

            _usageData.value = data
            _totalScreenTime.value = formatTotalTime(screenTime)
            _isLoading.value = false
        }
    }

    fun toggleSortOrder() {
        val newOrder = if (currentSortOrder == SortOrder.LEAST_USED)
            SortOrder.MOST_USED else SortOrder.LEAST_USED
        loadUsageData(sortOrder = newOrder)
    }

    private fun formatTotalTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}
