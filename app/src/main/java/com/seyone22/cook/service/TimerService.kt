package com.seyone22.cook.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.seyone22.cook.R
import kotlinx.coroutines.flow.asStateFlow

class TimerService : Service() {
    private var countdownTimer: CountDownTimer? = null

    companion object {
        private val _remainingTime = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)
        val remainingTime: kotlinx.coroutines.flow.StateFlow<Long?> = _remainingTime.asStateFlow()

        const val CHANNEL_ID = "cook_timers"
        const val ACTION_START = "START_TIMER"
        const val ACTION_STOP = "STOP_TIMER"
        const val EXTRA_DURATION_S = "DURATION_SECONDS"

        fun start(context: Context, durationSeconds: Long) {
            _remainingTime.value = durationSeconds // Immediate UI feedback

            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION_S, durationSeconds)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getLongExtra(EXTRA_DURATION_S, 0)
                startTimer(duration)
            }
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    private fun startTimer(durationSeconds: Long) {
        countdownTimer?.cancel()
        createNotificationChannel()

        countdownTimer = object : CountDownTimer(durationSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                _remainingTime.value = seconds
                updateNotification(seconds)
            }

            override fun onFinish() {
                playAlarm()
                _remainingTime.value = null
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }
        }.start()

        startForeground(999, buildNotification(durationSeconds))
    }

    private fun updateNotification(secondsLeft: Long) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(999, buildNotification(secondsLeft))
    }

    private fun buildNotification(secondsLeft: Long): Notification {
        val minutes = secondsLeft / 60
        val seconds = secondsLeft % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)

        val stopIntent = Intent(this, TimerService::class.java).apply { action = ACTION_STOP }
        val pendingStop = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Cooking Timer")
            .setContentText("Time remaining: $timeStr")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, "Stop", pendingStop)
            .build()
    }

    private fun playAlarm() {
        // You can add Ringtones/Vibration here
    }

    private fun stopTimer() {
        countdownTimer?.cancel()
        _remainingTime.value = null
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Timers", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}