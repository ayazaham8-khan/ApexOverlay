package com.novasphere.apexoverlay.ui.crosshair

data class CrosshairPreset(
    val id: String,
    val label: String,
    val shape: CrosshairShape
)

val crosshairPresets: List<CrosshairPreset> = listOf(
    CrosshairPreset(id = "classic_cross", label = "Classic", shape = CrosshairShape.CROSS),
    CrosshairPreset(id = "plus", label = "Plus", shape = CrosshairShape.PLUS),
    CrosshairPreset(id = "dot", label = "Dot", shape = CrosshairShape.DOT),
    CrosshairPreset(id = "circle", label = "Circle", shape = CrosshairShape.CIRCLE),
    CrosshairPreset(id = "circle_dot", label = "Circle Dot", shape = CrosshairShape.CIRCLE_DOT),
    CrosshairPreset(id = "x_shape", label = "X Shape", shape = CrosshairShape.X_SHAPE)
)
