package com.social.flare.core.media

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudinaryService(private val context: Context) {

    suspend fun uploadImage(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            if (bytes == null) {
                if (continuation.isActive) continuation.resumeWithException(Exception("No se pudo leer la imagen local"))
                return@suspendCancellableCoroutine
            }
            MediaManager.get().upload(bytes)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String ?: ""
                        if (continuation.isActive) continuation.resume(secureUrl)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        if (continuation.isActive) continuation.resumeWithException(Exception(error.description))
                    }
                })
                .dispatch()
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resumeWithException(e)
        }
    }

    suspend fun uploadMultipleImages(uris: List<Uri>): List<String> {
        return uris.map { uri ->
            uploadImage(uri)
        }
    }
}