package com.aocam.cameranode.runtime

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object CameraNodeStatusStore {
    var state by mutableStateOf(CameraNodeState())
        private set

    @Synchronized
    fun update(transform: (CameraNodeState) -> CameraNodeState) {
        state = transform(state).copy(updatedAtMillis = System.currentTimeMillis())
    }

    fun refreshFromDevice(
        context: Context,
        cameraPermissionGranted: Boolean,
        notificationPermissionGranted: Boolean,
    ) {
        val device = DeviceStatusReader.read(context)
        update {
            it.copy(
                cameraPermissionGranted = cameraPermissionGranted,
                notificationPermissionGranted = notificationPermissionGranted,
                batteryPercent = device.batteryPercent,
                charging = device.charging,
                batteryTemperatureCelsius = device.batteryTemperatureCelsius,
                storageFreeBytes = device.storageFreeBytes,
                storageTotalBytes = device.storageTotalBytes,
                lanIp = device.lanIp,
                appVersion = device.appVersion,
                logPath = AocamLogger.logPath(context),
            )
        }
    }
}
