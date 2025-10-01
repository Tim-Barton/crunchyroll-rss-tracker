package com.example.crunchyrollwatcher

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener

class AdManager(private val context: Context) {

    companion object {
        private const val TAG = "AdManager"
        private const val AD_REFRESH_INTERVAL_MS = 30000L // 30 seconds
        private const val AD_HEIGHT_DP = 50

        // Use BuildConfig to determine if we should use real ads or demo mode
        private val USE_REAL_ADS = BuildConfig.ADS_ENABLED && !BuildConfig.IS_DEBUG_BUILD
    }

    private var adContainer: FrameLayout? = null
    private var currentAdView: View? = null
    private var adRefreshTimer: android.os.Handler? = null
    private var isAdLoaded = false
    private var adMobAdView: AdView? = null
    private var isMobileAdsInitialized = false

    /**
     * Initialize the ad manager with the container from the layout
     */
    fun initialize(adContainer: FrameLayout) {
        this.adContainer = adContainer
        setupAdContainer()

        if (USE_REAL_ADS) {
            initializeMobileAds()
        } else {
            Log.d(TAG, "Using demo ads mode")
            loadDemoAd()
        }
    }

    /**
     * Initialize Google Mobile Ads SDK
     */
    private fun initializeMobileAds() {
        if (isMobileAdsInitialized) {
            loadRealAd()
            return
        }

        Log.d(TAG, "Initializing Google Mobile Ads SDK...")
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "Mobile Ads SDK initialized: ${initializationStatus.adapterStatusMap}")
            isMobileAdsInitialized = true
            loadRealAd()
        }
    }

    /**
     * Setup the ad container with proper styling
     */
    private fun setupAdContainer() {
        adContainer?.apply {
            // Ensure container is properly sized
            layoutParams = layoutParams?.apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

            // Add some elevation and background
            elevation = 8f
            setBackgroundColor(ContextCompat.getColor(context, R.color.background_secondary))
        }
    }

    /**
     * Load a demo ad for testing/development
     */
    private fun loadDemoAd() {
        val demoAds = listOf(
            "🎬 Premium Anime Streaming - Try Free!",
            "📱 New Anime App - Download Now!",
            "🎮 Anime Games - Play Free!",
            "📚 Manga Collection - Read Now!",
            "🎵 Anime Music - Listen Free!"
        )

        val randomAd = demoAds.random()
        createDemoAdView(randomAd)

        // Start auto-refresh for demo
        startAdRefreshTimer()
    }

    /**
     * Create a demo ad view
     */
    private fun createDemoAdView(adText: String) {
        val demoAdView = LinearLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dpToPx(AD_HEIGHT_DP)
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setBackgroundResource(R.drawable.ad_placeholder_background)
            isClickable = true
            isFocusable = true

            // Add click ripple effect
            val typedValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
            setBackgroundResource(typedValue.resourceId)
        }

        val adTextView = TextView(context).apply {
            text = adText
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.primary))
            gravity = android.view.Gravity.CENTER
        }

        val adLabel = TextView(context).apply {
            text = "Ad"
            textSize = 10f
            setTextColor(ContextCompat.getColor(context, R.color.text_tertiary))
            setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2))
            setBackgroundResource(R.drawable.episode_number_background)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dpToPx(8), 0, 0, 0)
            }
        }

        demoAdView.addView(adTextView)
        demoAdView.addView(adLabel)

        // Add click listener for demo
        demoAdView.setOnClickListener {
            handleAdClick(adText)
        }

        replaceCurrentAd(demoAdView)
    }

    /**
     * Load real advertisements using AdMob SDK
     */
    private fun loadRealAd() {
        if (!isMobileAdsInitialized) {
            Log.w(TAG, "Mobile Ads SDK not initialized yet, deferring ad load")
            return
        }

        if (!BuildConfig.ADS_ENABLED) {
            Log.d(TAG, "Ads are disabled in BuildConfig")
            hideAd()
            return
        }

        // Validate AdMob IDs
        val appId = BuildConfig.ADMOB_APP_ID
        val bannerUnitId = BuildConfig.ADMOB_BANNER_UNIT_ID

        if (appId.contains("REPLACE_WITH_YOUR") || bannerUnitId.contains("REPLACE_WITH_YOUR")) {
            Log.w(TAG, "AdMob IDs not configured properly, using demo ads instead")
            loadDemoAd()
            return
        }

        try {
            // Clean up existing AdView
            adMobAdView?.destroy()

            // Create new AdView
            adMobAdView = AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = bannerUnitId

                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d(TAG, "AdMob banner ad loaded successfully")
                        isAdLoaded = true
                        showAd()
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e(TAG, "AdMob banner ad failed to load: ${adError.message} (Code: ${adError.code})")
                        isAdLoaded = false
                        // Fallback to demo ad on failure
                        loadDemoAd()
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "AdMob banner ad clicked")
                        handleAdClick("AdMob Banner")
                    }

                    override fun onAdOpened() {
                        Log.d(TAG, "AdMob banner ad opened")
                    }

                    override fun onAdClosed() {
                        Log.d(TAG, "AdMob banner ad closed")
                    }
                }
            }

            // Create ad request
            val adRequest = AdRequest.Builder()
                .build()

            Log.d(TAG, "Loading AdMob banner ad with Unit ID: $bannerUnitId")
            adMobAdView?.loadAd(adRequest)

            // Add to container
            adMobAdView?.let { replaceCurrentAd(it) }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading real ad: ${e.message}", e)
            loadDemoAd() // Fallback to demo ad
        }
    }

    /**
     * Replace the current ad with a new one
     */
    private fun replaceCurrentAd(newAdView: View) {
        adContainer?.let { container ->
            // Remove current ad
            currentAdView?.let { container.removeView(it) }

            // Hide placeholder
            container.findViewById<LinearLayout>(R.id.adPlaceholder)?.visibility = View.GONE

            // Add new ad
            container.addView(newAdView)
            currentAdView = newAdView
            isAdLoaded = true
        }
    }

    /**
     * Handle ad click events
     */
    private fun handleAdClick(adContent: String) {
        // Track ad clicks, open links, etc.
        println("AdManager: Ad clicked - $adContent")

        // TODO: Add analytics tracking
        // TODO: Open ad destination
    }

    /**
     * Start auto-refresh timer for ads
     */
    private fun startAdRefreshTimer() {
        adRefreshTimer = android.os.Handler(android.os.Looper.getMainLooper()).apply {
            postDelayed(object : Runnable {
                override fun run() {
                    if (USE_REAL_ADS) {
                        loadRealAd()
                    } else {
                        loadDemoAd()
                    }
                    postDelayed(this, AD_REFRESH_INTERVAL_MS)
                }
            }, AD_REFRESH_INTERVAL_MS)
        }
    }

    /**
     * Stop ad refresh timer
     */
    private fun stopAdRefreshTimer() {
        adRefreshTimer?.removeCallbacksAndMessages(null)
        adRefreshTimer = null
    }

    /**
     * Show the ad container
     */
    fun showAd() {
        adContainer?.visibility = View.VISIBLE
    }

    /**
     * Hide the ad container
     */
    fun hideAd() {
        adContainer?.visibility = View.GONE
    }

    /**
     * Check if ad is currently loaded
     */
    fun isAdLoaded(): Boolean = isAdLoaded

    /**
     * Refresh the current ad
     */
    fun refreshAd() {
        if (USE_REAL_ADS) {
            loadRealAd()
        } else {
            loadDemoAd()
        }
    }

    /**
     * Clean up resources when activity is destroyed
     */
    fun destroy() {
        stopAdRefreshTimer()
        adMobAdView?.destroy()
        adMobAdView = null
        currentAdView = null
        adContainer = null
    }

    /**
     * Pause ad refresh (call in onPause)
     */
    fun pause() {
        stopAdRefreshTimer()
    }

    /**
     * Resume ad refresh (call in onResume)
     */
    fun resume() {
        adMobAdView?.resume()
        if (isAdLoaded) {
            startAdRefreshTimer()
        }
    }

    /**
     * Pause AdMob ads (call in onPause)
     */
    fun pauseAds() {
        adMobAdView?.pause()
        stopAdRefreshTimer()
    }

    /**
     * Utility function to convert dp to pixels
     */
    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    /**
     * Set ad visibility based on user preferences
     */
    fun setAdEnabled(enabled: Boolean) {
        if (enabled) {
            showAd()
            if (!isAdLoaded) {
                refreshAd()
            }
        } else {
            hideAd()
        }
    }

    /**
     * Check if ads are enabled in app settings
     */
    fun areAdsEnabled(): Boolean {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("ads_enabled", true) // Default to enabled
    }

    /**
     * Enable or disable ads in app settings
     */
    fun setAdsEnabled(enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("ads_enabled", enabled).apply()
        setAdEnabled(enabled)
    }

    /**
     * Check if AdMob IDs are properly configured
     */
    fun isAdMobConfigured(): Boolean {
        val appId = BuildConfig.ADMOB_APP_ID
        val bannerUnitId = BuildConfig.ADMOB_BANNER_UNIT_ID

        return !appId.contains("REPLACE_WITH_YOUR") &&
               !bannerUnitId.contains("REPLACE_WITH_YOUR") &&
               appId.startsWith("ca-app-pub-") &&
               bannerUnitId.startsWith("ca-app-pub-")
    }

    /**
     * Get current ad configuration info for debugging
     */
    fun getAdConfigInfo(): String {
        return buildString {
            appendLine("AdManager Configuration:")
            appendLine("- USE_REAL_ADS: $USE_REAL_ADS")
            appendLine("- ADS_ENABLED: ${BuildConfig.ADS_ENABLED}")
            appendLine("- IS_DEBUG_BUILD: ${BuildConfig.IS_DEBUG_BUILD}")
            appendLine("- AdMob Configured: ${isAdMobConfigured()}")
            appendLine("- Mobile Ads Initialized: $isMobileAdsInitialized")
            appendLine("- Ad Loaded: $isAdLoaded")
            if (BuildConfig.IS_DEBUG_BUILD) {
                appendLine("- App ID: ${BuildConfig.ADMOB_APP_ID}")
                appendLine("- Banner Unit ID: ${BuildConfig.ADMOB_BANNER_UNIT_ID}")
            }
        }
    }
}
