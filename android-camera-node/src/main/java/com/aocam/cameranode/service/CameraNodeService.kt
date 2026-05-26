package com.aocam.cameranode.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.aocam.cameranode.MainActivity
import com.aocam.cameranode.R
import com.aocam.cameranode.runtime.AocamLogger
import com.aocam.cameranode.runtime.CameraNodeStatusStore

class CameraNodeService : Service() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        CameraNodeStatusStore.update { it.copy(serviceState = "Created") }
        AocamLogger.log(this, "service_created", "CameraNodeService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopCameraService()
            return START_NOT_STICKY
        }

        if (!hasCameraPermission()) {
            val message = "Camera permission is required before starting camera foreground service"
            CameraNodeStatusStore.update { it.copy(serviceState = "Permission missing", lastError = message) }
            AocamLogger.log(this, "service_start_denied", message)
            stopSelf()
            return START_NOT_STICKY
        }

        return try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(),
                cameraForegroundServiceType(),
            )
            CameraNodeStatusStore.update { it.copy(serviceState = "Running", lastError = null) }
            AocamLogger.log(this, "service_started", "Foreground camera service promoted")
            START_STICKY
        } catch (exception: RuntimeException) {
            val message = exception.message ?: exception::class.java.simpleName
            CameraNodeStatusStore.update { it.copy(serviceState = "Error", lastError = message) }
            AocamLogger.log(this, "service_start_failed", message)
            stopSelf()
            START_NOT_STICKY
        }
    }

    override fun onDestroy() {
        CameraNodeStatusStore.update { it.copy(serviceState = "Stopped") }
        AocamLogger.log(this, "service_destroyed", "CameraNodeService destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopCameraService() {
        AocamLogger.log(this, "service_stop_received", "Foreground service stop action received")
        CameraNodeStatusStore.update { it.copy(serviceState = "Stopping") }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun cameraForegroundServiceType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
        } else {
            0
        }
    }

    private fun buildNotification(): Notification {
        val activityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_OPEN_APP,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val stopIntent = Intent(this, CameraNodeService::class.java).setAction(ACTION_STOP)
        val stopPendingIntent = PendingIntent.getService(
            this,
            REQUEST_STOP_SERVICE,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_aocam_notification)
            .setContentTitle(getString(R.string.camera_service_notification_title))
            .setContentText(getString(R.string.camera_service_notification_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(activityPendingIntent)
            .addAction(R.drawable.ic_aocam_notification, "Stop", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.camera_service_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Persistent notification for the Aocam camera node foreground service"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "aocam_camera_service"
        private const val NOTIFICATION_ID = 4201
        private const val REQUEST_OPEN_APP = 4301
        private const val REQUEST_STOP_SERVICE = 4302
        private const val ACTION_STOP = "com.aocam.cameranode.action.STOP_CAMERA_SERVICE"

        fun start(context: Context) {
            val intent = Intent(context, CameraNodeService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CameraNodeService::class.java))
        }
    }
}
