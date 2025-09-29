package com.example.crunchyrollwatcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var episodeAdapter: RssEpisodeAdapter

    // Views
    private lateinit var searchEditText: TextInputEditText
    private lateinit var filterButton: MaterialButton
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var episodesRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var errorStateLayout: LinearLayout
    private lateinit var errorMessage: TextView
    private lateinit var retryButton: MaterialButton
    private lateinit var refreshFab: FloatingActionButton

    // Chips
    private lateinit var chipAll: Chip
    private lateinit var chipNew: Chip
    private lateinit var chipDubs: Chip
    private lateinit var chipSubs: Chip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Initialize views
        initViews()

        // Setup RecyclerView
        setupRecyclerView()

        // Setup observers
        setupObservers()

        // Setup click listeners
        setupClickListeners()

        // Setup search
        setupSearch()

        // Setup FAB
        setupFab()

        // Load RSS feed on app start
        viewModel.loadRssEpisodes()
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        filterButton = findViewById(R.id.filterButton)
        filterChipGroup = findViewById(R.id.filterChipGroup)
        episodesRecyclerView = findViewById(R.id.episodesRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        errorStateLayout = findViewById(R.id.errorStateLayout)
        errorMessage = findViewById(R.id.errorMessage)
        retryButton = findViewById(R.id.retryButton)
        refreshFab = findViewById(R.id.refreshFab)

        // Initialize chips
        chipAll = findViewById(R.id.chipAll)
        chipNew = findViewById(R.id.chipNew)
        chipDubs = findViewById(R.id.chipDubs)
        chipSubs = findViewById(R.id.chipSubs)
    }

    private fun setupRecyclerView() {
        episodeAdapter = RssEpisodeAdapter { episode ->
            onEpisodeClicked(episode)
        }

        episodesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = episodeAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Hide FAB when scrolling down, show when scrolling up
                    if (dy > 0) {
                        refreshFab.hide()
                    } else {
                        refreshFab.show()
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        viewModel.rssEpisodes.observe(this) { episodes ->
            episodeAdapter.submitList(episodes)
            updateUI(episodes)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                showError(error)
            } else {
                hideError()
            }
        }
    }

    private fun setupClickListeners() {
        // Filter chips
        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val filter = when (checkedIds[0]) {
                    R.id.chipAll -> RssFilter(RssFilterType.ALL)
                    R.id.chipNew -> RssFilter(RssFilterType.NEW_EPISODES)
                    R.id.chipDubs -> RssFilter(RssFilterType.DUBS)
                    R.id.chipSubs -> RssFilter(RssFilterType.SUBS)
                    else -> RssFilter(RssFilterType.ALL)
                }
                viewModel.applyFilter(filter)
            }
        }

        // Retry button
        retryButton.setOnClickListener {
            viewModel.refreshRssFeed()
        }

        // Filter button (for future advanced filtering)
        filterButton.setOnClickListener {
            // TODO: Implement advanced filter dialog
            Toast.makeText(this, "Advanced filtering coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener { text ->
            val query = text.toString()
            viewModel.searchEpisodes(query)
        }
    }

    private fun setupFab() {
        refreshFab.setOnClickListener {
            viewModel.refreshRssFeed()
            Toast.makeText(this, "Refreshing anime RSS feed...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onEpisodeClicked(episode: CrunchyrollEpisode) {
        // Mark as watched/unwatched
        val newWatchedState = !episode.isWatched
        viewModel.markEpisodeAsWatched(episode.id, newWatchedState)

        // Show options dialog
        showEpisodeOptionsDialog(episode)
    }

    private fun showEpisodeOptionsDialog(episode: CrunchyrollEpisode) {
        val options = arrayOf(
            "Open in Browser",
            if (episode.isWatched) "Mark as Unwatched" else "Mark as Watched",
            "Share Episode"
        )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(episode.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openInBrowser(episode.link)
                    1 -> viewModel.markEpisodeAsWatched(episode.id, !episode.isWatched)
                    2 -> shareEpisode(episode)
                }
            }
            .show()
    }

    private fun openInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareEpisode(episode: CrunchyrollEpisode) {
        val shareText = "${episode.seriesTitle} - ${episode.title}\n${episode.link}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, episode.title)
        }
        startActivity(Intent.createChooser(intent, "Share Episode"))
    }

    private fun updateUI(episodes: List<CrunchyrollEpisode>) {
        when {
            episodes.isEmpty() -> showEmptyState()
            else -> showContent()
        }
    }

    private fun showContent() {
        episodesRecyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
        errorStateLayout.visibility = View.GONE
    }

    private fun showEmptyState() {
        episodesRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        errorStateLayout.visibility = View.GONE
    }

    private fun showError(error: String) {
        episodesRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.GONE
        errorStateLayout.visibility = View.VISIBLE
        errorMessage.text = error
    }

    private fun hideError() {
        errorStateLayout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        // Refresh anime RSS data when returning to the app
        viewModel.loadRssEpisodes()
    }
}
