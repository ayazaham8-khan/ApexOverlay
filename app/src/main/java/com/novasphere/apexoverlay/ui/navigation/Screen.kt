package com.novasphere.apexoverlay.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "ApexOverlay")
    data object Crosshair : Screen("crosshair", "Crosshair")
    data object TouchVisualizer : Screen("touch_visualizer", "Touch Visualizer")
    data object Grid : Screen("grid", "Grid & Ruler")
    data object FpsPing : Screen("fps_ping", "FPS & Ping")
    data object Presets : Screen("presets", "Presets")
    data object PointsStore : Screen("points_store", "Points & Store")
    data object Settings : Screen("settings", "Settings")
}
