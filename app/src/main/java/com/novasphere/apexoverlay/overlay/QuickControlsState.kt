package com.novasphere.apexoverlay.overlay

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Tracks whether the in-game Quick Controls panel is currently open.
 * Session-only, not persisted. Single writer: CrosshairOverlayService.
 */
object QuickControlsState {
    var isPanelOpen: Boolean by mutableStateOf(false)
}
