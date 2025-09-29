package com.example.crunchyrollwatcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.LinearLayout
import android.widget.ProgressBar

class SearchFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var animeAdapter: AnimeAdapter

    // Views
    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchButton: MaterialButton
    private lateinit var animeRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // Load initial popular anime
        viewModel.loadPopularAnime()
    }

    private fun initViews(view: View) {
        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)
        animeRecyclerView = view.findViewById(R.id.animeRecyclerView)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
    }

    private fun setupRecyclerView() {
        animeAdapter = AnimeAdapter { anime ->
            onAnimeClicked(anime)
        }

        animeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = animeAdapter
        }
    }

    private fun setupObservers() {
        viewModel.animeList.observe(viewLifecycleOwner) { animeList ->
            animeAdapter.updateList(animeList)
            updateUI(animeList)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                showError(errorMessage)
            }
        }
    }

    private fun setupClickListeners() {
        searchButton.setOnClickListener {
            performSearch()
        }

        searchEditText.addTextChangedListener { text ->
            if (text.isNullOrEmpty()) {
                viewModel.loadPopularAnime()
            }
        }

        searchEditText.setOnEditorActionListener { _, _, _ ->
            performSearch()
            true
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            viewModel.searchAnime(query)
        } else {
            Toast.makeText(context, "Please enter a search term", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onAnimeClicked(anime: Anime) {
        // Handle anime item click
        Toast.makeText(context, "Clicked: ${anime.title}", Toast.LENGTH_SHORT).show()

        // Show options dialog
        showAnimeOptionsDialog(anime)
    }

    private fun showAnimeOptionsDialog(anime: Anime) {
        val options = arrayOf(
            "Add to Watchlist",
            "View Details",
            "Mark as Watched"
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(anime.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        viewModel.addToWatchlist(anime)
                        Toast.makeText(context, "Added to watchlist", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        // TODO: Navigate to anime details screen
                        Toast.makeText(context, "Details view coming soon!", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        viewModel.updateWatchProgress(anime.id, anime.episodeCount)
                        Toast.makeText(context, "Marked as watched", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun updateUI(animeList: List<Anime>) {
        when {
            animeList.isEmpty() -> showEmptyState()
            else -> showContent()
        }
    }

    private fun showContent() {
        animeRecyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
    }

    private fun showEmptyState() {
        animeRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
    }

    companion object {
        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }
}
