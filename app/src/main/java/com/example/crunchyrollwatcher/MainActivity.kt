package com.example.crunchyrollwatcher

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var refreshFab: FloatingActionButton
    private lateinit var pagerAdapter: MainPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Initialize views
        initViews()

        // Setup ViewPager with tabs
        setupViewPager()

        // Setup FAB
        setupFab()

        // Setup observers
        setupObservers()
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        refreshFab = findViewById(R.id.refreshFab)
    }

    private fun setupViewPager() {
        pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Anime RSS Feed"
                1 -> "Search"
                else -> "Tab ${position + 1}"
            }
        }.attach()

        // Set default tab to Anime RSS Feed
        viewPager.currentItem = 0
    }

    private fun setupFab() {
        refreshFab.setOnClickListener {
            when (viewPager.currentItem) {
                0 -> {
                    // Refresh anime RSS feed
                    viewModel.refreshRssFeed()
                    Toast.makeText(this, "Refreshing anime RSS feed...", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    // Refresh popular anime in search tab
                    viewModel.loadPopularAnime()
                    Toast.makeText(this, "Loading popular anime...", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Show/hide FAB based on current tab
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        // Anime RSS Feed tab - show refresh FAB
                        refreshFab.show()
                    }
                    1 -> {
                        // Search tab - hide FAB or show different action
                        refreshFab.show()
                    }
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            // Handle loading state globally if needed
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getCurrentRssFeedFragment(): RssFeedFragment? {
        return if (viewPager.currentItem == 0) {
            supportFragmentManager.fragments
                .filterIsInstance<RssFeedFragment>()
                .firstOrNull()
        } else null
    }

    fun getCurrentSearchFragment(): SearchFragment? {
        return if (viewPager.currentItem == 1) {
            supportFragmentManager.fragments
                .filterIsInstance<SearchFragment>()
                .firstOrNull()
        } else null
    }

    override fun onResume() {
        super.onResume()
        // Refresh anime RSS data when returning to the app
        if (viewPager.currentItem == 0) {
            viewModel.loadRssEpisodes()
        }
    }
}
