package com.social.flare.core.media

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.cloudinary.Url
import com.cloudinary.android.MediaManager
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CloudinaryService(private val context: Context) {
    private val cloudinary get() = MediaManager.get().cloudinary
    suspend fun uploadImage(uri: Uri): String = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"

        val tempFile = File(context.cacheDir, "flare_media_${System.currentTimeMillis()}.$extension")

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }

            if (!tempFile.exists() || tempFile.length() == 0L) {
                throw Exception("No se pudo leer el archivo de la galería")
            }

            val params = mapOf("resource_type" to "auto")
            val resultData = MediaManager.get().cloudinary.uploader().upload(tempFile, params)

            val url = (resultData["secure_url"] ?: resultData["url"]) as? String ?: ""
            Log.d("FLARE_DEBUG", "URL generada por Cloudinary: $url")

            if (url.isBlank()) {
                throw Exception("Cloudinary subió el archivo pero no devolvió el enlace")
            }

            return@withContext url

        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Fallo en la subida: ${e.message ?: e.javaClass.simpleName}")
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }
    private fun extractPublicId(mediaUrl: String): String? {
        return try {
            val lastSlashIndex = mediaUrl.lastIndexOf('/')
            val lastDotIndex = mediaUrl.lastIndexOf('.')
            if (lastSlashIndex != -1 && lastDotIndex != -1 && lastDotIndex > lastSlashIndex){
                mediaUrl.substring(lastSlashIndex + 1, lastDotIndex)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun deleteImage(mediaUrl: String): Boolean{
        return withContext(Dispatchers.IO){
            try {
                val publicId = extractPublicId(mediaUrl)
                if (publicId != null){
                    val result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
                    result["result"] == "ok"
                }else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    suspend fun uploadMultipleImages(uris: List<Uri>): List<String> {
        return uris.map { uploadImage(it) }
    }
}