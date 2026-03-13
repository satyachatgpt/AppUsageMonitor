package com.appusage.monitor.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.appusage.monitor.R
import com.appusage.monitor.data.SortOrder
import com.appusage.monitor.data.TimeFrame
import com.appusage.monitor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: AppUsageAdapter

    private val usagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "App Usage Monitor"

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupRecyclerView()
        setupTimeFrameSpinner()
        setupSwipeRefresh()
        setupSortButton()
        observeViewModel()

        viewModel.checkPermission()
    }

    private fun setupRecyclerView() {
        adapter = AppUsageAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupTimeFrameSpinner() {
        val timeFrames = TimeFrame.entries.map { it.label }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeFrames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimeFrame.adapter = spinnerAdapter

        binding.spinnerTimeFrame.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.loadUsageData(timeFrame = TimeFrame.fromPosition(position))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadUsageData()
        }
    }

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener {
            viewModel.toggleSortOrder()
        }
        updateSortButtonText()
    }

    private fun updateSortButtonText() {
        binding.btnSort.text = viewModel.currentSortOrder.label
    }

    private fun observeViewModel() {
        viewModel.hasPermission.observe(this) { hasPermission ->
            if (hasPermission) {
                binding.layoutPermission.visibility = View.GONE
                binding.layoutContent.visibility = View.VISIBLE
                viewModel.loadUsageData()
            } else {
                binding.layoutPermission.visibility = View.VISIBLE
                binding.layoutContent.visibility = View.GONE
                binding.btnGrantPermission.setOnClickListener {
                    requestUsagePermission()
                }
            }
        }

        viewModel.usageData.observe(this) { data ->
            adapter.submitList(data)
            binding.tvAppCount.text = "${data.size} apps"
            binding.tvEmptyState.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.totalScreenTime.observe(this) { time ->
            binding.tvTotalScreenTime.text = time
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun requestUsagePermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        usagePermissionLauncher.launch(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_system -> {
                item.isChecked = !item.isChecked
                viewModel.loadUsageData(includeSystemApps = item.isChecked)
                true
            }
            R.id.action_refresh -> {
                viewModel.loadUsageData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermission()
    }
}
