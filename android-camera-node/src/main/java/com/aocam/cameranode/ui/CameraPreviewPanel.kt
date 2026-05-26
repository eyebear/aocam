package com.aocam.cameranode.ui

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aocam.cameranode.runtime.AocamLogger
import com.aocam.cameranode.runtime.CameraNodeStatusStore
import java.util.concurrent.ExecutionException

@Composable
fun CameraPreviewPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        var disposed = false

        val listener = Runnable {
            if (disposed) return@Runnable

            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                )

                CameraNodeStatusStore.update {
                    it.copy(cameraState = "Preview active", lastError = null)
                }
                AocamLogger.log(context, "camera_preview_started", "CameraX preview bound to lifecycle")
            } catch (exception: ExecutionException) {
                reportPreviewFailure(context, exception.cause ?: exception)
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
                reportPreviewFailure(context, exception)
            } catch (exception: RuntimeException) {
                reportPreviewFailure(context, exception)
            }
        }

        cameraProviderFuture.addListener(listener, mainExecutor)

        onDispose {
            disposed = true
            if (cameraProviderFuture.isDone && !cameraProviderFuture.isCancelled) {
                runCatching { cameraProviderFuture.get().unbindAll() }
            }
            CameraNodeStatusStore.update { it.copy(cameraState = "Inactive") }
            AocamLogger.log(context, "camera_preview_stopped", "CameraX preview unbound")
        }
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView },
        )
    }
}

private fun reportPreviewFailure(context: android.content.Context, error: Throwable) {
    val message = error.message ?: error::class.java.simpleName
    CameraNodeStatusStore.update {
        it.copy(cameraState = "Error", lastError = message)
    }
    AocamLogger.log(context, "camera_preview_failed", message)
}
