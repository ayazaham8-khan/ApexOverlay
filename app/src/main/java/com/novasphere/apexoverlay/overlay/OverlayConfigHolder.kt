package com.novasphere.apexoverlay.overlay

import androidx.compose.runtime.mutableStateOf
import com.novasphere.apexoverlay.ui.crosshair.CrosshairConfig

object OverlayConfigHolder {

    private val _crosshairConfig = mutableStateOf(CrosshairConfig())

    var crosshairConfig: CrosshairConfig
        get() = _crosshairConfig.value
        set(value) {
            _crosshairConfig.value = value
        }
}
