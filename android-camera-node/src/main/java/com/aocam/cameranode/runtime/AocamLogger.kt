package com.aocam.cameranode.runtime

import android.content.Context
import java.io.File
import java.time.Instant

object AocamLogger {
    private const val MAX_LOG_BYTES = 512_000L

    @Synchronized
    fun log(context: Context, event: String, message: String? = null) {
        val file = logFile(context)
        file.parentFile?.mkdirs()

        if (file.exists() && file.length() > MAX_LOG_BYTES) {
            val rotated = File(file.parentFile, "camera-node.previous.log")
            if (rotated.exists()) rotated.delete()
            file.renameTo(rotated)
        }

        val sanitizedMessage = message?.replace('\n', ' ') ?: ""
        file.appendText("${Instant.now()} event=$event $sanitizedMessage\n")
        CameraNodeStatusStore.update {
            it.copy(lastLogEvent = event, logPath = file.absolutePath)
        }
    }

    fun logPath(context: Context): String {
        return logFile(context).absolutePath
    }

    private fun logFile(context: Context): File {
        return File(File(context.filesDir, "logs"), "camera-node.log")
    }
}
