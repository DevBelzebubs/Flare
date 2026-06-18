package com.social.flare.core.sync.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.social.flare.core.media.CloudinaryService
import com.social.flare.core.sync.data.local.dao.SyncDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncDao: SyncDao,
    private val supabase: SupabaseClient,
    private val cloudinaryService: CloudinaryService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("SyncWorker", "Iniciando sincronización en segundo plano...")
            val pendingTasks = syncDao.getPendingTasks()

            if (pendingTasks.isEmpty()) {
                Log.d("SyncWorker", "No hay tareas pendientes.")
                return@withContext Result.success()
            }

            for (task in pendingTasks) {
                try {
                    when (task.operation_type) {
                        "CREATE_NEWS" -> processCreateNewsTask(task)
                    }
                    syncDao.deleteSyncTask(task.id)
                    Log.d("SyncWorker", "Tarea ${task.id} sincronizada con éxito")

                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error sincronizando tarea ${task.id}: ${e.message}")
                    syncDao.updateTaskStatus(task.id, "FAILED")
                    return@withContext Result.retry()
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun processCreateNewsTask(task: com.social.flare.core.sync.data.local.entity.SyncQueueEntity) {
        val json = Json { ignoreUnknownKeys = true }.parseToJsonElement(task.payload_json).jsonObject

        val newsId = json["news_id"]?.jsonPrimitive?.content ?: return
        val title = json["title"]?.jsonPrimitive?.content ?: return
        val description = json["description"]?.jsonPrimitive?.content ?: return
        val createdAt = json["created_at"]?.jsonPrimitive?.content?.toLong() ?: System.currentTimeMillis()

        val imageUrl = if (!task.media_uri.isNullOrBlank()) {
            val uri = Uri.parse(task.media_uri)
            cloudinaryService.uploadImage(uri)
        } else {
            ""
        }
        val newsData = mapOf(
            "news_id" to newsId,
            "title" to title,
            "description" to description,
            "image_url" to imageUrl,
            "created_at" to createdAt,
            "is_active" to true
        )
        supabase.postgrest["news"].insert(newsData)
    }
}