package com.novasphere.apexoverlay.overlay

import android.content.Context

object SetupPreferences {

    private const val PREFS_NAME = "apexoverlay_setup_prefs"
    private const val KEY_INTRO_SEEN = "setup_intro_seen"

    fun hasSeenSetupIntro(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_INTRO_SEEN, false)
    }

    fun markSetupIntroSeen(context: Context) {
        prefs(context).edit().putBoolean(KEY_INTRO_SEEN, true).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
