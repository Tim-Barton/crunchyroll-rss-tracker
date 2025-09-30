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
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.appbar.MaterialToolbar
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var episodeAdapter: RssEpisodeAdapter
    private lateinit var notificationPermissionHelper: NotificationPermissionHelper

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
    private lateinit var toolbar: MaterialToolbar

    // Chips
    private lateinit var chipAll: Chip
    private lateinit var chipNew: Chip
    private lateinit var chipDubs: Chip
    private lateinit var chipSubs: Chip

    // Permission launcher for notifications
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handleNotificationPermissionResult(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ViewModel with factory
        val viewModelFactory = ViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

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

        // Setup toolbar
        setupToolbar()

        // Setup notification permission helper
        setupNotificationPermissions()

        // Setup background sync
        setupBackgroundSync()

        // Load RSS feed on app start
        viewModel.loadRssEpisodes()

        // Handle intent extras (like search query from SavedTitlesActivity)
        handleIntentExtras()
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
        toolbar = findViewById(R.id.toolbar)

        // Initialize chips
        chipAll = findViewById(R.id.chipAll)
        chipNew = findViewById(R.id.chipNew)
        chipDubs = findViewById(R.id.chipDubs)
        chipSubs = findViewById(R.id.chipSubs)
    }

    private fun setupRecyclerView() {
        episodeAdapter = RssEpisodeAdapter(
            onEpisodeClick = { episode -> onEpisodeClicked(episode) },
            onFavoriteClick = { episode -> onFavoriteClicked(episode) },
            isTitleSaved = { seriesTitle -> viewModel.isTitleSaved(seriesTitle) }
        )

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

        viewModel.savedTitles.observe(this) { savedTitles ->
            // Update adapter when saved titles change
            episodeAdapter.notifyDataSetChanged()
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

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun setupNotificationPermissions() {
        notificationPermissionHelper = NotificationPermissionHelper(this) { isGranted ->
            handleNotificationPermissionResult(isGranted)
        }
        notificationPermissionHelper.initialize(notificationPermissionLauncher)
    }

    private fun setupBackgroundSync() {
        // Check if we have saved titles and notification permission
        viewModel.savedTitles.observe(this) { savedTitles ->
            if (savedTitles.isNotEmpty() && !notificationPermissionHelper.hasNotificationPermission()) {
                // Delay the permission request to avoid showing it immediately on app start
                window.decorView.post {
                    showNotificationPermissionPrompt()
                }
            }
        }

        // Setup background sync if permission is granted
        if (notificationPermissionHelper.hasNotificationPermission()) {
            viewModel.setupBackgroundSync()
        }
    }

    private fun showNotificationPermissionPrompt() {
        // Only show if user has saved titles
        val savedTitles = viewModel.savedTitles.value
        if (!savedTitles.isNullOrEmpty()) {
            notificationPermissionHelper.requestNotificationPermission()
        }
    }

    private fun handleNotificationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            // Permission granted - setup background sync
            viewModel.setupBackgroundSync()
            viewModel.enableBackgroundSync(true)
            Toast.makeText(this, "Notifications enabled! You'll receive alerts for new episodes.", Toast.LENGTH_LONG).show()
        } else {
            // Permission denied - still allow manual checking
            notificationPermissionHelper.showPermissionDeniedDialog()
            viewModel.enableBackgroundSync(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_saved_titles -> {
                openSavedTitlesActivity()
                true
            }
            R.id.action_refresh -> {
                viewModel.refreshRssFeed()
                Toast.makeText(this, "Refreshing anime RSS feed...", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_sync_now -> {
                viewModel.forceSyncNow()
                Toast.makeText(this, "Starting background sync...", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_notifications -> {
                showNotificationSettingsDialog()
                true
            }
            R.id.action_settings -> {
                showSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupFab() {
        refreshFab.setOnClickListener {
            viewModel.refreshRssFeed()
            Toast.makeText(this, "Refreshing anime RSS feed...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onEpisodeClicked(episode: CrunchyrollEpisode) {
        // Show options dialog
        showEpisodeOptionsDialog(episode)
    }

    private fun onFavoriteClicked(episode: CrunchyrollEpisode) {
        if (viewModel.isTitleSaved(episode.seriesTitle)) {
            // Remove from favorites
            val titleId = episode.seriesTitle.lowercase().replace(Regex("[^a-z0-9]"), "_")
            viewModel.removeSavedTitle(titleId)
            Toast.makeText(this, "Removed ${episode.seriesTitle} from favorites", Toast.LENGTH_SHORT).show()
        } else {
            // Add to favorites
            viewModel.saveTitle(episode)
            Toast.makeText(this, "Added ${episode.seriesTitle} to favorites", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEpisodeOptionsDialog(episode: CrunchyrollEpisode) {
        val options = arrayOf(
            "Open in Browser",
            if (episode.isWatched) "Mark as Unwatched" else "Mark as Watched",
            "Share Episode",
            "View Saved Titles"
        )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(episode.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openInBrowser(episode.link)
                    1 -> viewModel.markEpisodeAsWatched(episode.id, !episode.isWatched)
                    2 -> shareEpisode(episode)
                    3 -> openSavedTitlesActivity()
                }
            }
            .show()
    }

    private fun openSavedTitlesActivity() {
        val intent = Intent(this, SavedTitlesActivity::class.java)
        startActivity(intent)
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

    private fun handleIntentExtras() {
        intent?.getStringExtra("search_query")?.let { searchQuery ->
            searchEditText.setText(searchQuery)
            viewModel.searchEpisodes(searchQuery)
        }

        // Handle notification permission request from SavedTitlesActivity
        if (intent?.getBooleanExtra("request_notifications", false) == true) {
            window.decorView.post {
                notificationPermissionHelper.requestNotificationPermission()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentExtras()
    }

    override fun onResume() {
        super.onResume()
        // Refresh anime RSS data when returning to the app
        viewModel.loadRssEpisodes()

        // Check if notifications were enabled/disabled in settings
        if (notificationPermissionHelper.hasNotificationPermission() && !viewModel.isBackgroundSyncEnabled()) {
            viewModel.setupBackgroundSync()
        }
    }

    private fun showSettingsDialog() {
        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        // Background sync toggle
        val syncEnabled = viewModel.isBackgroundSyncEnabled()
        options.add(if (syncEnabled) "Disable Background Sync" else "Enable Background Sync")
        actions.add {
            if (syncEnabled) {
                viewModel.enableBackgroundSync(false)
                Toast.makeText(this, "Background sync disabled", Toast.LENGTH_SHORT).show()
            } else {
                if (notificationPermissionHelper.hasNotificationPermission()) {
                    viewModel.enableBackgroundSync(true)
                    Toast.makeText(this, "Background sync enabled", Toast.LENGTH_SHORT).show()
                } else {
                    notificationPermissionHelper.requestNotificationPermission()
                }
            }
        }

        // Last sync time
        val lastSync = viewModel.getLastSyncTime()
        val lastSyncText = if (lastSync > 0) {
            val format = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            "Last sync: ${format.format(Date(lastSync))}"
        } else {
            "Never synced"
        }
        options.add("Force Sync Now")
        actions.add {
            viewModel.forceSyncNow()
            Toast.makeText(this, "Starting background sync...", Toast.LENGTH_SHORT).show()
        }

        // Test notification
        options.add("Send Test Notification")
        actions.add {
            if (notificationPermissionHelper.hasNotificationPermission()) {
                viewModel.testNotification()
                Toast.makeText(this, "Test notification sent!", Toast.LENGTH_SHORT).show()
            } else {
                notificationPermissionHelper.showNotificationsDisabledDialog()
            }
        }

        // Notification settings
        if (!notificationPermissionHelper.areNotificationsEnabled()) {
            options.add("Enable Notifications")
            actions.add {
                notificationPermissionHelper.showNotificationsDisabledDialog()
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Settings")
            .setMessage(lastSyncText)
            .setItems(options.toTypedArray()) { _, which ->
                actions[which].invoke()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showNotificationSettingsDialog() {
        val hasPermission = notificationPermissionHelper.hasNotificationPermission()
        val isEnabled = notificationPermissionHelper.areNotificationsEnabled()
        val syncEnabled = viewModel.isBackgroundSyncEnabled()

        val message = buildString {
            append("Notification Status:\n")
            append("• Permission: ${if (hasPermission) "Granted" else "Denied"}\n")
            append("• System Enabled: ${if (isEnabled) "Yes" else "No"}\n")
            append("• Background Sync: ${if (syncEnabled) "Enabled" else "Disabled"}\n\n")

            if (!hasPermission || !isEnabled) {
                append("Enable notifications to receive alerts about new episodes of your favorite anime series.")
            } else {
                append("You'll receive notifications when new episodes are available for your saved titles.")
            }
        }

        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        if (!hasPermission) {
            options.add("Request Permission")
            actions.add {
                notificationPermissionHelper.requestNotificationPermission()
            }
        }

        if (!isEnabled) {
            options.add("Open Settings")
            actions.add {
                notificationPermissionHelper.showNotificationsDisabledDialog()
            }
        }

        if (hasPermission && isEnabled) {
            options.add("Send Test Notification")
            actions.add {
                viewModel.testNotification()
                Toast.makeText(this, "Test notification sent!", Toast.LENGTH_SHORT).show()
            }
        }

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Notification Settings")
            .setMessage(message)
            .setNegativeButton("Close", null)

        if (options.isNotEmpty()) {
            builder.setItems(options.toTypedArray()) { _, which ->
                actions[which].invoke()
            }
        }

        builder.show()
    }
}
