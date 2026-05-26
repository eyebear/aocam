package com.aocam.cameranode.runtime

data class CameraNodeState(
    val serviceState: String = "Stopped",
    val cameraState: String = "Inactive",
    val recordingState: String = "Idle",
    val motionState: String = "Idle",
    val cameraPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val batteryPercent: Int? = null,
    val charging: Boolean? = null,
    val batteryTemperatureCelsius: Float? = null,
    val storageFreeBytes: Long? = null,
    val storageTotalBytes: Long? = null,
    val lanIp: String = "Unavailable",
    val appVersion: String = "Unknown",
    val lastError: String? = null,
    val lastLogEvent: String? = null,
    val logPath: String? = null,
    val updatedAtMillis: Long = System.currentTimeMillis(),
) {
    fun batteryLabel(): String {
        val level = batteryPercent?.let { "$it%" } ?: "Unknown"
        val chargingLabel = when (charging) {
            true -> "charging"
            false -> "not charging"
            null -> "unknown"
        }
        return "$level, $chargingLabel"
    }

    fun temperatureLabel(): String {
        return batteryTemperatureCelsius?.let { "%.1f C".format(it) } ?: "Unknown"
    }

    fun storageLabel(): String {
        val free = storageFreeBytes ?: return "Unknown"
        val total = storageTotalBytes ?: return "Unknown"
        if (total <= 0L) return "Unknown"
        val usedPercent = (((total - free).toDouble() / total.toDouble()) * 100.0).coerceIn(0.0, 100.0)
        return "%.1f%% used, %s free".format(usedPercent, formatBytes(free))
    }
}

private fun formatBytes(value: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = value.toDouble()
    var unitIndex = 0
    while (size >= 1024.0 && unitIndex < units.lastIndex) {
        size /= 1024.0
        unitIndex += 1
    }
    return if (unitIndex == 0) {
        "${size.toLong()} ${units[unitIndex]}"
    } else {
        "%.1f %s".format(size, units[unitIndex])
    }
}
