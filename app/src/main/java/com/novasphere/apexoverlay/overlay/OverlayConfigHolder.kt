package com.novasphere.apexoverlay.overlay

import androidx.compose.runtime.mutableStateOf
import com.novasphere.apexoverlay.ui.crosshair.CrosshairConfig

/**
 * In-memory bridge between the Crosshair settings UI (app process) and the
 * overlay window rendered by CrosshairOverlayService (same process, separate
 * window). No persistence yet - that arrives with the DataStore milestone.
 */
object OverlayConfigHolder {
    var crosshairConfig: CrosshairConfig by mutableStateOf(CrosshairConfig())
}
