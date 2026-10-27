package com.explay.audio.audio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.explay.audio.MainActivity
import com.explay.audio.audio.engine.AudioEngineManager

class AudioEngineService : Service() {

    private lateinit var audioEngineManager: AudioEngineManager

    override fun onCreate() {
        super.onCreate()
        audioEngineManager = AudioEngineManager.getInstance(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_STOP_SERVICE -> {
                audioEngineManager.stopEngine()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_ENGINE -> {
                val newState = !audioEngineManager.isProcessingActive.value
                audioEngineManager.setEngineEnabled(newState)
                updateNotification(newState)
            }
            ACTION_START_ENGINE -> {
                val sessionId = intent.getIntExtra(EXTRA_SESSION_ID, 0)
                audioEngineManager.startEngine(sessionId)
                val notification = buildNotification(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    } else {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
                    }
                    startForeground(NOTIFICATION_ID, notification, serviceType)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
            else -> {
                val notification = buildNotification(audioEngineManager.isProcessingActive.value)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    } else {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
                    }
                    startForeground(NOTIFICATION_ID, notification, serviceType)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ExPlay Audio Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Layanan pemrosesan audio background ExPlay"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(isActive: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = Intent(this, AudioEngineService::class.java).apply {
            action = ACTION_TOGGLE_ENGINE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, AudioEngineService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val statusText = if (isActive) "Mesin 16-Band & Limiter Aktif" else "Equalizer Siaga / Dijeda"
        val toggleText = if (isActive) "Jeda" else "Aktifkan"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ExPlay Audio Engine")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_play, toggleText, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Tutup", stopPendingIntent)
            .build()
    }

    private fun updateNotification(isActive: Boolean) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(isActive))
    }

    companion object {
        const val CHANNEL_ID = "explay_audio_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_ENGINE = "com.explay.audio.action.START"
        const val ACTION_STOP_SERVICE = "com.explay.audio.action.STOP"
        const val ACTION_TOGGLE_ENGINE = "com.explay.audio.action.TOGGLE"
        const val EXTRA_SESSION_ID = "extra_session_id"

        fun start(context: Context, sessionId: Int = 0) {
            val intent = Intent(context, AudioEngineService::class.java).apply {
                action = ACTION_START_ENGINE
                putExtra(EXTRA_SESSION_ID, sessionId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AudioEngineService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
