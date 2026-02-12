package com.seyone22.cook.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.seyone22.cook.MainActivity
import com.seyone22.cook.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CookingSessionService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): CookingSessionService = this@CookingSessionService
    }

    private val binder = LocalBinder()
    private val TAG = "CookingSessionService"

    // --- STATE MANAGEMENT ---
    private val _sessionState = MutableStateFlow(CookingState())
    val sessionState: StateFlow<CookingState> = _sessionState.asStateFlow()

    companion object {
        const val CHANNEL_ID = "cook_live_updates_v2"
        const val NOTIFICATION_ID = 888

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREV = "ACTION_PREV"

        const val EXTRA_RECIPE_NAME = "EXTRA_RECIPE_NAME"
        const val EXTRA_STEPS = "EXTRA_STEPS"

        // Android 16 Preview Constant
        const val EXTRA_REQUEST_PROMOTED_ONGOING = "android.extra.REQUEST_PROMOTED_ONGOING"

        fun start(context: Context, recipeName: String, instructions: List<String>) {
            val intent = Intent(context, CookingSessionService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RECIPE_NAME, recipeName)
                putStringArrayListExtra(EXTRA_STEPS, ArrayList(instructions))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    data class CookingState(
        val recipeName: String = "",
        val steps: List<String> = emptyList(),
        val currentStepIndex: Int = 0,
        val isRunning: Boolean = false
    )

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val name = intent.getStringExtra(EXTRA_RECIPE_NAME) ?: "Cooking"
                val steps = intent.getStringArrayListExtra(EXTRA_STEPS) ?: arrayListOf()
                startSession(name, steps)
            }
            ACTION_NEXT -> nextStep()
            ACTION_PREV -> prevStep()
            ACTION_STOP -> stopSession()
        }
        return START_NOT_STICKY
    }

    private fun startSession(name: String, steps: List<String>) {
        _sessionState.value = CookingState(name, steps, 0, true)
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    private fun stopSession() {
        _sessionState.value = _sessionState.value.copy(isRunning = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun nextStep() {
        val current = _sessionState.value
        if (current.currentStepIndex < current.steps.size - 1) {
            _sessionState.value = current.copy(currentStepIndex = current.currentStepIndex + 1)
            updateNotification()
        }
    }

    private fun prevStep() {
        val current = _sessionState.value
        if (current.currentStepIndex > 0) {
            _sessionState.value = current.copy(currentStepIndex = current.currentStepIndex - 1)
            updateNotification()
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        // 1. Build the appropriate notification for the OS version
        val notification = if (Build.VERSION.SDK_INT >= 36) { // Android 16
            buildLiveUpdateNotification()
        } else {
            buildLegacyNotification()
        }

        // 2. LOGGING: Check if it qualifies for Live Updates (Android 16+)
        if (Build.VERSION.SDK_INT >= 36) {
            val manager = getSystemService(NotificationManager::class.java)

            // Check if the User/System allows promoted notifications for this app
            val canPost = manager.canPostPromotedNotifications()

            // Check if the Notification object itself meets the technical criteria
            val hasCharacteristics = notification.hasPromotableCharacteristics()

            Log.d(TAG, "Live Update Check -> App Allowed: $canPost | Notification Valid: $hasCharacteristics")

            if (!hasCharacteristics) {
                Log.w(TAG, "Notification failed promotable check. Verify Styles, Ongoing flag, and Permissions.")
            }
        }

        return notification
    }

    // --- ANDROID 16: PROGRESS STYLE (LIVE UPDATE) ---
    @RequiresApi(36)
    private fun buildLiveUpdateNotification(): Notification {
        val state = _sessionState.value

        // 1. DATA SAFETY
        val safeStepsCount = if (state.steps.isEmpty()) 1 else state.steps.size
        val safeCurrentIndex = state.currentStepIndex.coerceIn(0, safeStepsCount - 1)
        val currentStepText = state.steps.getOrElse(safeCurrentIndex) { "Ready" }

        // 2. COLORS
        val themeColor = ContextCompat.getColor(this, R.color.purple_500)

        // 3. JOURNEY (Segments)
        val segments = List(safeStepsCount) {
            Notification.ProgressStyle.Segment(1).setColor(themeColor)
        }

        // --- THE FIX IS HERE ---
        // Point position must be > 0. We add 1 to the index.
        val oneBasedIndex = safeCurrentIndex + 1

        val currentPoint = Notification.ProgressStyle.Point(oneBasedIndex)
            .setColor(themeColor)

        // 4. STYLE
        val progressStyle = Notification.ProgressStyle()
            .setProgressTrackerIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_compass))
            .setProgressSegments(segments)
            .setProgressPoints(listOf(currentPoint))
            .setProgress(oneBasedIndex) // <--- UPDATE THIS TOO

        // 5. INTENT
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        // 6. BUILDER
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // <--- CRITICAL CHANGE
            .setContentTitle(state.recipeName.ifEmpty { "Cooking Session" })
            .setContentText(currentStepText)
            .setStyle(progressStyle)
            .setOngoing(true)
            .setColor(themeColor)
            .setContentIntent(contentIntent)

            // CRITICAL VALIDATION FLAGS
            .setColorized(false) // MUST be false for Live Updates
            .setCategory(Notification.CATEGORY_PROGRESS) // Helps the OS classify it

            // PROMOTION REQUEST
            .addExtras(android.os.Bundle().apply {
                putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true)
            })

            // STATUS BAR CHIP
            .setShortCriticalText("Step ${safeCurrentIndex + 1}/$safeStepsCount")

            // ACTIONS
            .addAction(Notification.Action.Builder(null, "Prev", pendingIntent(ACTION_PREV)).build())
            .addAction(Notification.Action.Builder(null, "Next", pendingIntent(ACTION_NEXT)).build())
            .addAction(Notification.Action.Builder(null, "Stop", pendingIntent(ACTION_STOP)).build())
            .build()
    }

    // --- LEGACY FALLBACK ---
    private fun buildLegacyNotification(): Notification {
        val state = _sessionState.value
        val currentStepText = state.steps.getOrElse(state.currentStepIndex) { "Ready" }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(state.recipeName)
            .setContentText("Step ${state.currentStepIndex + 1}: $currentStepText")
            .setStyle(NotificationCompat.BigTextStyle().bigText(currentStepText))
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Prev", pendingIntent(ACTION_PREV))
            .addAction(R.drawable.ic_launcher_foreground, "Next", pendingIntent(ACTION_NEXT))
            .build()
    }

    private fun pendingIntent(action: String): PendingIntent {
        val intent = Intent(this, CookingSessionService::class.java).apply { this.action = action }
        return PendingIntent.getService(this, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Cooking Mode", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live cooking updates"
                setSound(null, null)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}