package com.example.crunchyrollwatcher

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var animeAdapter: AnimeAdapter
    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchButton: MaterialButton
    private lateinit var animeRecyclerView: RecyclerView

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

        // Load initial data
        loadInitialData()
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        animeRecyclerView = findViewById(R.id.animeRecyclerView)
    }

    private fun setupRecyclerView() {
        animeAdapter = AnimeAdapter { anime ->
            onAnimeClicked(anime)
        }

        animeRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = animeAdapter
        }
    }

    private fun setupObservers() {
        viewModel.animeList.observe(this) { animeList ->
            animeAdapter.updateList(animeList)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // Show/hide loading indicator
            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                showError(errorMessage)
            }
        }
    }

    private fun setupClickListeners() {
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchAnime(query)
            } else {
                Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadInitialData() {
        viewModel.loadPopularAnime()
    }

    private fun onAnimeClicked(anime: Anime) {
        // Handle anime item click
        Toast.makeText(this, "Clicked: ${anime.title}", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to anime details screen
    }

    private fun showLoading() {
        // TODO: Show loading indicator
    }

    private fun hideLoading() {
        // TODO: Hide loading indicator
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
    }
}
