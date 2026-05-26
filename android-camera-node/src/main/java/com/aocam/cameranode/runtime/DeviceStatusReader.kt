package com.aocam.cameranode.runtime

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.net.Inet4Address
import java.net.NetworkInterface

data class DeviceSnapshot(
    val batteryPercent: Int?,
    val charging: Boolean?,
    val batteryTemperatureCelsius: Float?,
    val storageFreeBytes: Long?,
    val storageTotalBytes: Long?,
    val lanIp: String,
    val appVersion: String,
)

object DeviceStatusReader {
    fun read(context: Context): DeviceSnapshot {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val rawTemperature = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1

        val batteryPercent = if (level >= 0 && scale > 0) {
            ((level.toFloat() / scale.toFloat()) * 100).toInt()
        } else {
            null
        }

        val charging = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING,
            BatteryManager.BATTERY_STATUS_FULL -> true
            BatteryManager.BATTERY_STATUS_DISCHARGING,
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false
            else -> null
        }

        val temperature = if (rawTemperature >= 0) rawTemperature / 10.0f else null
        val filesDir = context.filesDir

        return DeviceSnapshot(
            batteryPercent = batteryPercent,
            charging = charging,
            batteryTemperatureCelsius = temperature,
            storageFreeBytes = filesDir.freeSpace,
            storageTotalBytes = filesDir.totalSpace,
            lanIp = readLanIp(),
            appVersion = readAppVersion(context),
        )
    }

    private fun readLanIp(): String {
        return runCatching {
            NetworkInterface.getNetworkInterfaces().asSequence()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.asSequence() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { !it.isLoopbackAddress }
                ?.hostAddress
                ?: "Unavailable"
        }.getOrDefault("Unavailable")
    }

    @Suppress("DEPRECATION")
    private fun readAppVersion(context: Context): String {
        return runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
        }.getOrDefault("Unknown")
    }
}
