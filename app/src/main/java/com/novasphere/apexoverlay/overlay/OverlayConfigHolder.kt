package com.novasphere.apexoverlay.overlay

import com.novasphere.apexoverlay.ui.crosshair.CrosshairConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central bridge for the active CrosshairConfig, shared by the main
 * ApexOverlay Crosshair screen, the in-game Quick Controls panel, and
 * CrosshairOverlayService's rendered overlay. Backed by a StateFlow so
 * multiple writers and multiple readers all observe a single source of
 * truth. No persistence involved - in-memory only, cleared on process death.
 */
object OverlayConfigHolder {

    private val _configFlow = MutableStateFlow(CrosshairConfig())
    val configFlow: StateFlow<CrosshairConfig> = _configFlow.asStateFlow()

    var crosshairConfig: CrosshairConfig
        get() = _configFlow.value
        set(value) {
            _configFlow.value = value
        }
}
