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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.novasphere.apexoverlay.ui.crosshair.CrosshairCanvas
import com.novasphere.apexoverlay.ui.crosshair.QuickControlButton
import com.novasphere.apexoverlay.ui.crosshair.QuickControlsPanel

class CrosshairOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var crosshairView: ComposeView? = null
    private var buttonView: ComposeView? = null
    private var panelView: ComposeView? = null
    private var sharedLifecycleOwner: OverlayLifecycleOwner? = null
    private var buttonParams: WindowManager.LayoutParams? = null

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
            setupOverlayViews()
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
        removeAllOverlayViews()
        OverlayDiagnostics.log(this, "SERVICE onDestroy() complete")
        super.onDestroy()
    }

    private fun setupOverlayViews() {
        if (!OverlayPermission.hasOverlayPermission(this)) {
            OverlayDiagnostics.log(this, "SERVICE setupOverlayViews() aborted - permission missing")
            stopSelf()
            return
        }

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val owner = OverlayLifecycleOwner().also { it.onCreate() }
        sharedLifecycleOwner = owner

        addCrosshairView(wm, owner)
        addQuickControlsButton(wm, owner)
    }

    private fun addCrosshairView(wm: WindowManager, owner: OverlayLifecycleOwner) {
        OverlayDiagnostics.log(this, "SERVICE addOverlayView() start")

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setContent {
                val config by OverlayConfigHolder.configFlow.collectAsState()
                CrosshairCanvas(
                    config = config,
                    modifier = Modifier.size(200.dp)
                )
            }
        }
        crosshairView = view

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
            wm.addView(view, params)
            OverlayDiagnostics.log(this, "SERVICE overlay view added to WindowManager successfully")
        } catch (e: Exception) {
            OverlayDiagnostics.logError(this, "SERVICE wm.addView() failed", e)
            stopSelf()
        }
    }

    private fun addQuickControlsButton(wm: WindowManager, owner: OverlayLifecycleOwner) {
        val density = resources.displayMetrics.density
        val startX = (16 * density).toInt()
        val startY = (resources.displayMetrics.heightPixels * 0.35f).toInt()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = startX
            y = startY
        }
        buttonParams = params

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setContent {
                QuickControlButton(
                    onDrag = { dx, dy -> moveQuickControlsButton(dx, dy) },
                    onTap = { toggleQuickControlsPanel() }
                )
            }
        }
        buttonView = view

        try {
            wm.addView(view, params)
            OverlayDiagnostics.log(this, "SERVICE Quick Controls button created")
        } catch (e: Exception) {
            OverlayDiagnostics.logError(this, "SERVICE Quick Controls button addView failed", e)
            buttonView = null
        }
    }

    private fun moveQuickControlsButton(dxPx: Float, dyPx: Float) {
        val wm = windowManager ?: return
        val view = buttonView ?: return
        val params = buttonParams ?: return
        params.x = (params.x + dxPx.toInt()).coerceAtLeast(0)
        params.y = (params.y + dyPx.toInt()).coerceAtLeast(0)
        try {
            wm.updateViewLayout(view, params)
        } catch (e: Exception) {
            OverlayDiagnostics.logError(this, "SERVICE Quick Controls button updateViewLayout failed", e)
        }
    }

    private fun toggleQuickControlsPanel() {
        if (QuickControlsState.isPanelOpen) {
            closeQuickControlsPanel()
        } else {
            openQuickControlsPanel()
        }
    }

    private fun openQuickControlsPanel() {
        val wm = windowManager ?: return
        val owner = sharedLifecycleOwner ?: return
        if (panelView != null) return

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setContent {
                val config by OverlayConfigHolder.configFlow.collectAsState()
                QuickControlsPanel(
                    config = config,
                    onConfigChange = { OverlayConfigHolder.crosshairConfig = it },
                    onClose = { closeQuickControlsPanel() }
                )
            }
        }
        panelView = view

        try {
            wm.addView(view, computePanelParams())
            QuickControlsState.isPanelOpen = true
            OverlayDiagnostics.log(this, "UI Quick Controls opened")
        } catch (e: Exception) {
            OverlayDiagnostics.logError(this, "SERVICE Quick Controls panel addView failed", e)
            panelView = null
        }
    }

    private fun closeQuickControlsPanel() {
        val view = panelView ?: return
        runCatching { windowManager?.removeView(view) }
        panelView = null
        QuickControlsState.isPanelOpen = false
        OverlayDiagnostics.log(this, "UI Quick Controls closed")
    }

    private fun computePanelParams(): WindowManager.LayoutParams {
        val density = resources.displayMetrics.density
        val screenHeight = resources.displayMetrics.heightPixels
        val buttonSizePx = (52 * density).toInt()
        val marginPx = (8 * density).toInt()
        val estimatedPanelHeightPx = (340 * density).toInt()

        val anchorX = buttonParams?.x ?: 0
        val anchorY = buttonParams?.y ?: 0

        val panelY = if (anchorY > screenHeight / 2) {
            (anchorY - estimatedPanelHeightPx - marginPx).coerceAtLeast(0)
        } else {
            anchorY + buttonSizePx + marginPx
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = anchorX.coerceAtLeast(0)
            y = panelY
        }
    }

    private fun removeAllOverlayViews() {
        crosshairView?.let { view -> runCatching { windowManager?.removeView(view) } }
        crosshairView = null

        buttonView?.let { view -> runCatching { windowManager?.removeView(view) } }
        buttonView = null
        OverlayDiagnostics.log(this, "SERVICE Quick Controls removed")

        panelView?.let { view -> runCatching { windowManager?.removeView(view) } }
        panelView = null
        QuickControlsState.isPanelOpen = false

        sharedLifecycleOwner?.onDestroy()
        sharedLifecycleOwner = null
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
