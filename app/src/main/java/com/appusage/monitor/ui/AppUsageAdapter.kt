package com.appusage.monitor.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appusage.monitor.R
import com.appusage.monitor.data.AppUsageInfo

class AppUsageAdapter : RecyclerView.Adapter<AppUsageAdapter.ViewHolder>() {

    private var items: List<AppUsageInfo> = emptyList()
    private var maxUsageTime: Long = 1L

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<AppUsageInfo>) {
        items = newItems
        maxUsageTime = newItems.maxOfOrNull { it.usageTimeMs } ?: 1L
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_usage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position + 1)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAppIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
        private val tvUsageTime: TextView = itemView.findViewById(R.id.tvUsageTime)
        private val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        private val tvPackageName: TextView = itemView.findViewById(R.id.tvPackageName)
        private val progressUsage: ProgressBar = itemView.findViewById(R.id.progressUsage)
        private val tvLaunchCount: TextView = itemView.findViewById(R.id.tvLaunchCount)

        fun bind(info: AppUsageInfo, rank: Int) {
            tvRank.text = "#$rank"
            tvAppName.text = info.appName
            tvUsageTime.text = info.usageTimeFormatted
            tvPackageName.text = info.packageName

            if (info.appIcon != null) {
                ivAppIcon.setImageDrawable(info.appIcon)
            } else {
                ivAppIcon.setImageResource(R.drawable.ic_default_app)
            }

            // Usage bar relative to max
            val progress = ((info.usageTimeMs.toFloat() / maxUsageTime) * 100).toInt()
            progressUsage.progress = progress.coerceIn(1, 100)

            if (info.launchCount > 0) {
                tvLaunchCount.visibility = View.VISIBLE
                tvLaunchCount.text = "${info.launchCount} opens"
            } else {
                tvLaunchCount.visibility = View.GONE
            }
        }
    }
}
