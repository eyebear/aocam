package com.aocam.cameranode

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aocam.cameranode.runtime.AocamLogger
import com.aocam.cameranode.runtime.CameraNodeStatusStore
import com.aocam.cameranode.service.CameraNodeService
import com.aocam.cameranode.ui.CameraPreviewPanel
import com.aocam.cameranode.ui.theme.AocamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AocamLogger.log(this, "app_started", "MainActivity created")
        refreshStatus(this)

        setContent {
            AocamTheme {
                AocamCameraNodeScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus(this)
    }
}

@Composable
private fun AocamCameraNodeScreen() {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(hasCameraPermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(hasNotificationPermission(context)) }
    val status = CameraNodeStatusStore.state

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
        AocamLogger.log(context, "camera_permission_result", "granted=$granted")
        refreshStatus(context)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasNotificationPermission = granted
        AocamLogger.log(context, "notification_permission_result", "granted=$granted")
        refreshStatus(context)
    }

    LaunchedEffect(hasCameraPermission, hasNotificationPermission) {
        refreshStatus(context)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Header(status.serviceState, status.cameraState)

            if (hasCameraPermission) {
                CameraPreviewPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                )
            } else {
                PermissionPanel(
                    title = "Camera permission required",
                    body = "Aocam needs camera access before it can start the preview or camera foreground service.",
                    action = "Grant camera",
                    onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                )
            }

            ControlPanel(
                hasCameraPermission = hasCameraPermission,
                hasNotificationPermission = hasNotificationPermission,
                onRequestCamera = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onRequestNotifications = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onStartService = {
                    AocamLogger.log(context, "service_start_requested", "User requested foreground service start")
                    CameraNodeStatusStore.update { it.copy(serviceState = "Starting", lastError = null) }
                    CameraNodeService.start(context)
                },
                onStopService = {
                    AocamLogger.log(context, "service_stop_requested", "User requested foreground service stop")
                    CameraNodeService.stop(context)
                    CameraNodeStatusStore.update { it.copy(serviceState = "Stopped") }
                },
                onRefresh = {
                    refreshStatus(context)
                    AocamLogger.log(context, "status_refreshed", "User refreshed status")
                },
            )

            StatusPanel(status = status)
        }
    }
}

@Composable
private fun Header(serviceState: String, cameraState: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Aocam",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Service: $serviceState   Camera: $cameraState",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PermissionPanel(
    title: String,
    body: String,
    action: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onClick) {
                Text(action)
            }
        }
    }
}

@Composable
private fun ControlPanel(
    hasCameraPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestCamera: () -> Unit,
    onRequestNotifications: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onRefresh: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Camera controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onStartService,
                    enabled = hasCameraPermission,
                ) {
                    Text("Start service")
                }
                OutlinedButton(onClick = onStopService) {
                    Text("Stop")
                }
                OutlinedButton(onClick = onRefresh) {
                    Text("Refresh")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!hasCameraPermission) {
                    OutlinedButton(onClick = onRequestCamera) {
                        Text("Camera permission")
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    OutlinedButton(onClick = onRequestNotifications) {
                        Text("Notification permission")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPanel(status: com.aocam.cameranode.runtime.CameraNodeState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Node status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            StatusRow("Service", status.serviceState)
            StatusRow("Camera", status.cameraState)
            StatusRow("Recording", status.recordingState)
            StatusRow("Motion", status.motionState)
            StatusRow("Camera permission", status.cameraPermissionGranted.yesNo())
            StatusRow("Notification permission", status.notificationPermissionGranted.yesNo())
            StatusRow("Battery", status.batteryLabel())
            StatusRow("Temperature", status.temperatureLabel())
            StatusRow("Storage", status.storageLabel())
            StatusRow("LAN IP", status.lanIp)
            StatusRow("App version", status.appVersion)
            StatusRow("Last log", status.lastLogEvent ?: "None")
            StatusRow("Log file", status.logPath ?: "Unavailable")
            StatusRow("Last error", status.lastError ?: "None")
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.width(152.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun refreshStatus(context: Context) {
    CameraNodeStatusStore.refreshFromDevice(
        context = context,
        cameraPermissionGranted = hasCameraPermission(context),
        notificationPermissionGranted = hasNotificationPermission(context),
    )
}

private fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}

private fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        true
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
}

private fun Boolean.yesNo(): String = if (this) "Granted" else "Missing"
