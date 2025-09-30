package com.example.crunchyrollwatcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class SavedTitlesActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var savedTitlesAdapter: SavedTitlesAdapter

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var savedTitlesRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var errorStateLayout: LinearLayout
    private lateinit var errorMessage: TextView
    private lateinit var retryButton: MaterialButton
    private lateinit var savedTitlesCount: TextView
    private lateinit var clearAllButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_titles)

        // Initialize ViewModel with factory
        val viewModelFactory = ViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        // Initialize views
        initViews()

        // Setup toolbar
        setupToolbar()

        // Setup RecyclerView
        setupRecyclerView()

        // Setup observers
        setupObservers()

        // Setup click listeners
        setupClickListeners()

        // Load saved titles
        viewModel.loadSavedTitles()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        savedTitlesRecyclerView = findViewById(R.id.savedTitlesRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        errorStateLayout = findViewById(R.id.errorStateLayout)
        errorMessage = findViewById(R.id.errorMessage)
        retryButton = findViewById(R.id.retryButton)
        savedTitlesCount = findViewById(R.id.savedTitlesCount)
        clearAllButton = findViewById(R.id.clearAllButton)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupRecyclerView() {
        savedTitlesAdapter = SavedTitlesAdapter(
            onTitleClick = { savedTitle -> onTitleClicked(savedTitle) },
            onRemoveClick = { savedTitle -> onRemoveClicked(savedTitle) }
        )

        savedTitlesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SavedTitlesActivity)
            adapter = savedTitlesAdapter
        }
    }

    private fun setupObservers() {
        viewModel.savedTitles.observe(this) { savedTitles ->
            savedTitlesAdapter.submitList(savedTitles)
            updateUI(savedTitles)
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
        retryButton.setOnClickListener {
            viewModel.loadSavedTitles()
        }

        clearAllButton.setOnClickListener {
            showClearAllConfirmationDialog()
        }

        // Show notification status if notifications are disabled
        checkNotificationStatus()
    }

    private fun onTitleClicked(savedTitle: SavedTitle) {
        showTitleOptionsDialog(savedTitle)
    }

    private fun onRemoveClicked(savedTitle: SavedTitle) {
        showRemoveConfirmationDialog(savedTitle)
    }

    private fun showTitleOptionsDialog(savedTitle: SavedTitle) {
        val options = arrayOf(
            "Search for Episodes",
            "Share Title",
            "Remove from Favorites"
        )

        AlertDialog.Builder(this)
            .setTitle(savedTitle.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> searchForEpisodes(savedTitle.title)
                    1 -> shareTitle(savedTitle)
                    2 -> showRemoveConfirmationDialog(savedTitle)
                }
            }
            .show()
    }

    private fun showRemoveConfirmationDialog(savedTitle: SavedTitle) {
        AlertDialog.Builder(this)
            .setTitle("Remove from Favorites")
            .setMessage("Are you sure you want to remove \"${savedTitle.title}\" from your favorites?")
            .setPositiveButton("Remove") { _, _ ->
                viewModel.removeSavedTitle(savedTitle.id)
                Toast.makeText(this, "Removed ${savedTitle.title} from favorites", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearAllConfirmationDialog() {
        val savedTitles = viewModel.savedTitles.value ?: emptyList()
        if (savedTitles.isEmpty()) {
            Toast.makeText(this, "No saved titles to clear", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Clear All Favorites")
            .setMessage("Are you sure you want to remove all ${savedTitles.size} saved titles from your favorites? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                clearAllSavedTitles(savedTitles)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllSavedTitles(savedTitles: List<SavedTitle>) {
        // Remove all saved titles one by one
        savedTitles.forEach { savedTitle ->
            viewModel.removeSavedTitle(savedTitle.id)
        }
        Toast.makeText(this, "Cleared all saved titles", Toast.LENGTH_SHORT).show()
    }

    private fun searchForEpisodes(seriesTitle: String) {
        // Return to main activity and search for this series
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("search_query", seriesTitle)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun shareTitle(savedTitle: SavedTitle) {
        val shareText = buildString {
            append("Check out this anime: ${savedTitle.title}\n")
            if (!savedTitle.description.isNullOrEmpty()) {
                append("${savedTitle.description}\n")
            }
            append("Episodes tracked: ${savedTitle.episodeCount}")
            if (!savedTitle.lastEpisodeSeen.isNullOrEmpty()) {
                append("\nLast episode seen: ${savedTitle.lastEpisodeSeen}")
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Anime Recommendation: ${savedTitle.title}")
        }
        startActivity(Intent.createChooser(intent, "Share Anime Title"))
    }

    private fun updateUI(savedTitles: List<SavedTitle>) {
        when {
            savedTitles.isEmpty() -> showEmptyState()
            else -> showContent()
        }

        // Update titles count
        val count = savedTitles.size
        val notificationHelper = NotificationHelper(this)
        val hasNotificationPermission = notificationHelper.hasNotificationPermission()

        savedTitlesCount.text = buildString {
            if (count == 1) {
                append("1 saved title")
            } else {
                append("$count saved titles")
            }

            if (count > 0 && !hasNotificationPermission) {
                append(" • Notifications disabled")
            }
        }

        // Show/hide clear all button based on content
        clearAllButton.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    private fun showContent() {
        savedTitlesRecyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
        errorStateLayout.visibility = View.GONE
    }

    private fun showEmptyState() {
        savedTitlesRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        errorStateLayout.visibility = View.GONE
    }

    private fun showError(error: String) {
        savedTitlesRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.GONE
        errorStateLayout.visibility = View.VISIBLE
        errorMessage.text = error
    }

    private fun hideError() {
        errorStateLayout.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh saved titles when returning to the activity
        viewModel.loadSavedTitles()
    }

    private fun checkNotificationStatus() {
        val notificationHelper = NotificationHelper(this)
        val savedTitles = viewModel.savedTitles.value

        // Show info about notifications if user has saved titles but notifications are disabled
        if (!savedTitles.isNullOrEmpty() && !notificationHelper.hasNotificationPermission()) {
            showNotificationInfoDialog()
        }
    }

    private fun showNotificationInfoDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Notifications?")
            .setMessage(
                "You have ${viewModel.savedTitles.value?.size} saved anime series!\n\n" +
                "Enable notifications to get alerts when new episodes are available for your favorite shows."
            )
            .setPositiveButton("Enable") { _, _ ->
                // Return to MainActivity where notification permission can be requested
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("request_notifications", true)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .show()
    }
}
