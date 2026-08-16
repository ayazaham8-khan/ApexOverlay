package com.novasphere.apexoverlay.overlay

import android.content.Context
import android.os.Process
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * TEMPORARY diagnostic logger for the "overlay disappears after a while"
 * investigation. Writes to Logcat and to a plain text file in app-private
 * storage, so it can be read from inside the app with no PC/adb needed.
 * This is a debug tool, not app persistence - safe to remove once the
 * root cause is confirmed.
 */
object OverlayDiagnostics {

    private const val TAG = "ApexOverlayDiag"
    private const val LOG_FILE_NAME = "overlay_debug.log"
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun log(context: Context, event: String) {
        val line = "${timeFormat.format(System.currentTimeMillis())} pid=${Process.myPid()} $event"
        Log.i(TAG, line)
        runCatching {
            File(context.filesDir, LOG_FILE_NAME).appendText(line + "\n")
        }
    }

    fun logError(context: Context, event: String, throwable: Throwable) {
        val line = "${timeFormat.format(System.currentTimeMillis())} pid=${Process.myPid()} " +
            "$event EXCEPTION: ${Log.getStackTraceString(throwable)}"
        Log.e(TAG, line)
        runCatching {
            File(context.filesDir, LOG_FILE_NAME).appendText(line + "\n")
        }
    }

    fun readLog(context: Context): String {
        val file = File(context.filesDir, LOG_FILE_NAME)
        if (!file.exists()) return "(no log entries yet)"
        return runCatching { file.readText() }.getOrDefault("(could not read log file)")
    }

    fun clearLog(context: Context) {
        runCatching { File(context.filesDir, LOG_FILE_NAME).delete() }
    }
}
