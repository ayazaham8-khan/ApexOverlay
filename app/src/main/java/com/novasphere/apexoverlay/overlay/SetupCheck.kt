package com.novasphere.apexoverlay.overlay

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

enum class SetupCheckStatus {
    READY,
    ACTION_REQUIRED,
    MANUAL_CHECK_REQUIRED
}

data class SetupCheckItem(
    val id: String,
    val title: String,
    val description: String,
    val status: SetupCheckStatus,
    val isRequired: Boolean,
    val fixIntent: Intent? = null,
    val fixLabel: String? = null,
    val opensInstructionScreen: Boolean = false
)

object SetupCheckEvaluator {

    fun evaluate(context: Context): List<SetupCheckItem> {
        return listOf(
            overlayPermissionCheck(context),
            batteryOptimizationCheck(context),
            backgroundRestrictionCheck(context),
            oemBackgroundCheck()
        )
    }

    private fun overlayPermissionCheck(context: Context): SetupCheckItem {
        val ready = OverlayPermission.hasOverlayPermission(context)
        return SetupCheckItem(
            id = "overlay_permission",
            title = "Display over other apps",
            description = "Required for the crosshair to appear above your game.",
            status = if (ready) SetupCheckStatus.READY else SetupCheckStatus.ACTION_REQUIRED,
            isRequired = true,
            fixIntent = if (ready) null else OverlayPermission.overlayPermissionIntent(context),
            fixLabel = "Grant Permission"
        )
    }

    private fun batteryOptimizationCheck(context: Context): SetupCheckItem {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val ready = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        return SetupCheckItem(
            id = "battery_optimization",
            title = "Battery optimization",
            description = "Prevents standard Android from pausing the overlay to save battery.",
            status = if (ready) SetupCheckStatus.READY else SetupCheckStatus.ACTION_REQUIRED,
            isRequired = false,
            fixIntent = if (ready) null else Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            fixLabel = "Open Battery Settings"
        )
    }

    private fun backgroundRestrictionCheck(context: Context): SetupCheckItem {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val restricted = activityManager.isBackgroundRestricted
        return SetupCheckItem(
            id = "background_restriction",
            title = "Background activity",
            description = "A separate Android-level restriction that can stop the overlay even with battery optimization off.",
            status = if (!restricted) SetupCheckStatus.READY else SetupCheckStatus.ACTION_REQUIRED,
            isRequired = false,
            fixIntent = appInfoIntent(context),
            fixLabel = "Open App Info"
        )
    }

    private fun oemBackgroundCheck(): SetupCheckItem {
        val guidance = OemGuidance.forCurrentDevice()
        return SetupCheckItem(
            id = "oem_background",
            title = "${guidance.manufacturerLabel} background settings",
            description = guidance.shortDescription,
            status = SetupCheckStatus.MANUAL_CHECK_REQUIRED,
            isRequired = false,
            fixIntent = null,
            fixLabel = "View Instructions",
            opensInstructionScreen = true
        )
    }

    private fun appInfoIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        )
    }
}
