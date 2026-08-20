package com.novasphere.apexoverlay.overlay

import androidx.compose.runtime.mutableStateOf
import com.novasphere.apexoverlay.ui.crosshair.CrosshairConfig

/**
 * Central bridge for the active CrosshairConfig, shared by the main
 * ApexOverlay Crosshair screen, the in-game Quick Controls panel, and
 * CrosshairOverlayService's rendered overlay. Backed by Compose's own
 * mutableStateOf - the mechanism already proven to deliver live updates
 * reliably inside a WindowManager-hosted ComposeView on this device.
 * No persistence involved - in-memory only, cleared on process death.
 */
object OverlayConfigHolder {
    var crosshairConfig: CrosshairConfig by mutableStateOf(CrosshairConfig())
}
