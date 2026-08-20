package com.novasphere.apexoverlay.overlay

import androidx.compose.runtime.mutableStateOf

/**
 * Tracks whether the in-game Quick Controls panel is currently open.
 * Session-only, not persisted. Single writer: CrosshairOverlayService.
 */
object QuickControlsState {
    var isPanelOpen: Boolean by mutableStateOf(false)
}
