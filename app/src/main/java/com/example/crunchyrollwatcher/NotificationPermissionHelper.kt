package com.example.crunchyrollwatcher

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class NotificationPermissionHelper(
    private val activity: Activity,
    private val onPermissionResult: (Boolean) -> Unit
) {

    private var permissionLauncher: ActivityResultLauncher<String>? = null

    companion object {
        const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
    }

    fun initialize(permissionLauncher: ActivityResultLauncher<String>) {
        this.permissionLauncher = permissionLauncher
    }

    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                NOTIFICATION_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissions not required below API 33
        }
    }

    /**
     * Request notification permission with rationale dialog if needed
     */
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onPermissionResult(true)
            return
        }

        when {
            hasNotificationPermission() -> {
                onPermissionResult(true)
            }
            shouldShowRationale() -> {
                showPermissionRationaleDialog()
            }
            else -> {
                launchPermissionRequest()
            }
        }
    }

    /**
     * Check if we should show rationale for notification permission
     */
    private fun shouldShowRationale(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                NOTIFICATION_PERMISSION
            )
        } else {
            false
        }
    }

    /**
     * Show dialog explaining why we need notification permission
     */
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Enable Notifications")
            .setMessage(
                "To keep you updated with new episodes of your favorite anime series, " +
                "this app needs permission to send notifications.\n\n" +
                "You'll receive notifications when:\n" +
                "• New episodes of saved series are available\n" +
                "• Daily RSS feed updates are complete\n\n" +
                "You can always change this in your device settings later."
            )
            .setPositiveButton("Allow Notifications") { _, _ ->
                launchPermissionRequest()
            }
            .setNegativeButton("Not Now") { _, _ ->
                onPermissionResult(false)
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Launch the system permission request
     */
    private fun launchPermissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher?.launch(NOTIFICATION_PERMISSION)
        } else {
            onPermissionResult(true)
        }
    }

    /**
     * Handle permission result from launcher
     */
    fun handlePermissionResult(isGranted: Boolean) {
        onPermissionResult(isGranted)
    }

    /**
     * Show dialog when permission is denied permanently
     */
    fun showPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Notifications Disabled")
            .setMessage(
                "You won't receive notifications about new anime episodes. " +
                "You can still manually check for updates in the app.\n\n" +
                "To enable notifications later, go to:\n" +
                "Settings > Apps > Anime RSS Tracker > Notifications"
            )
            .setPositiveButton("OK") { _, _ -> }
            .setNegativeButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .show()
    }

    /**
     * Open app settings page
     */
    private fun openAppSettings() {
        try {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {
                data = android.net.Uri.fromParts("package", activity.packageName, null)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings
            val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
            activity.startActivity(intent)
        }
    }

    /**
     * Check if notifications are enabled in system settings
     */
    fun areNotificationsEnabled(): Boolean {
        return androidx.core.app.NotificationManagerCompat.from(activity).areNotificationsEnabled()
    }

    /**
     * Show dialog to guide user to enable notifications in settings
     */
    fun showNotificationsDisabledDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Notifications Are Disabled")
            .setMessage(
                "Notifications are currently disabled in your device settings. " +
                "Enable them to receive updates about new anime episodes."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    /**
     * Request permission and setup background sync based on result
     */
    fun requestPermissionAndSetupSync(
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        requestNotificationPermission()

        // The callback will be handled in the main activity/fragment
        // This is just a helper method to chain the operations
    }
}
