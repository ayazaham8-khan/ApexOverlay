package com.novasphere.apexoverlay.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.novasphere.apexoverlay.ui.crosshair.CrosshairCanvas

class CrosshairOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        OverlayDiagnostics.log(this, "SERVICE onCreate() start")
        try {
            ensureNotificationChannel()
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
            OverlayDiagnostics.log(this, "SERVICE startForeground() succeeded")
            addOverlayView()
        } catch (e: Exception) {
            OverlayDiagnostics.logError(this, "SERVICE onCreate() failed", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        OverlayDiagnostics.log(this, "SERVICE onStartCommand action=${intent?.action}")
        if (intent?.action == ACTION_STOP_OVERLAY) {
            OverlayDiagnostics.log(this, "SERVICE stop action received, calling stopSelf()")
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        OverlayDiagnostics.log(this, "SERVICE onDestroy() called - this is a CLEAN stop")
        removeOverlayView()
        OverlayDiagnostics.log(this, "SERVICE onDestroy() complete")
        super.onDestroy()
    }

    private fun addOverlayView() {
        OverlayDiagnostics.log(this, "SERVICE addOverlayView() start")

        if (!OverlayPermission.hasOverlayPermission(this)) {
            OverlayDiagnostics.log(this, "SERVICE addOverlayView() aborted - permission missing")
            stopSelf()
            return
        }

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val owner = OverlayLifecycleOwner().also { it.onCreate() }
        overlayLifecycleOwner = owner

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setContent {
                val config = OverlayConfigHolder.crosshairConfig
                CrosshairCanvas(
                    config = config,
                    modifier = Modifier.size(200.dp)
                )
            }
        }
        overlayView = composeView

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        try {
            wm.addView(composeView, params)
            OverlayDiagnostics.log(this, "SERVICE overlay view added to WindowManager successfully")
        } catch (e: Exception) {
            OverlayDiagnostics.logError(this, "SERVICE wm.addView() failed", e)
            stopSelf()
        }
    }

    private fun removeOverlayView() {
        overlayView?.let { view ->
            runCatching { windowManager?.removeView(view) }
        }
        overlayView = null
        overlayLifecycleOwner?.onDestroy()
        overlayLifecycleOwner = null
        windowManager = null
    }

    private fun ensureNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Overlay Status",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when a crosshair overlay is active"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, CrosshairOverlayService::class.java).apply {
            action = ACTION_STOP_OVERLAY
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ApexOverlay")
            .setContentText("Crosshair overlay is active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, "Stop", stopPendingIntent)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "apexoverlay_overlay_status"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_OVERLAY = "com.novasphere.apexoverlay.action.STOP_OVERLAY"
    }
}
