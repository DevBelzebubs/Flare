package com.social.flare.features.ai.data.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.google.gson.Gson
import com.social.flare.features.ai.data.remote.HuggingFaceApi
import com.social.flare.features.ai.data.remote.OpenRouterApi
import com.social.flare.features.ai.data.remote.dto.AiMessage
import com.social.flare.features.ai.data.remote.dto.HuggingFaceRequest
import com.social.flare.features.ai.data.remote.dto.OpenRouterRequest
import com.social.flare.features.ai.data.repository.dto.AiPersonaFetchDto
import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import com.cloudinary.android.callback.UploadCallback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AiAgentRepositoryImpl @Inject constructor(
    private val openRouterApi: OpenRouterApi,
    private val huggingFaceApi: HuggingFaceApi,
    private val gson: Gson,
    private val supabase: SupabaseClient,
    @ApplicationContext private val context: Context
) : AiAgentRepository {
    override suspend fun generatePost(
        persona: AiPersona,
        topicContext: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemMessage = AiMessage(
                role = "system",
                content = "${persona.systemPrompt} Debes escribir una publicación corta para la red social Flare. Responde ÚNICAMENTE con un objeto JSON plano con la estructura: {\"content\": \"tu publicación aquí\"}. No agregues introducciones ni saludos."
            )
            val userMessage = AiMessage(
                role = "user",
                content = "Escribe sobre el siguiente tema o situación actual: $topicContext"
            )

            val request = OpenRouterRequest(
                messages = listOf(systemMessage, userMessage),
                temperature = persona.temperature
            )

            val response = openRouterApi.generateCompletion(request)
            if (response.isSuccessful) {
                val rawText = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
                val cleanJson = extractJson(rawText)

                val parsed = gson.fromJson(cleanJson, AiPostResponse::class.java)
                Result.success(parsed.content)
            } else {
                Result.failure(Exception("Error de API OpenRouter: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateComment(
        persona: AiPersona,
        targetPostContent: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemMessage = AiMessage(
                role = "system",
                content = "${persona.systemPrompt} Vas a comentar una publicación de otro usuario en Flare. Sé natural y breve (máximo 2 líneas). Responde ÚNICAMENTE con un objeto JSON plano con la estructura: {\"content\": \"tu comentario aquí\"}."
            )
            val userMessage = AiMessage(
                role = "user",
                content = "Publicación a la que vas a responder: '$targetPostContent'"
            )

            val request = OpenRouterRequest(
                messages = listOf(systemMessage, userMessage),
                temperature = persona.temperature
            )

            val response = openRouterApi.generateCompletion(request)
            if (response.isSuccessful) {
                val rawText = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
                val cleanJson = extractJson(rawText)
                val parsed = gson.fromJson(cleanJson, AiPostResponse::class.java)
                Result.success(parsed.content)
            } else {
                Result.failure(Exception("Error de API OpenRouter: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveBots(): Result<List<AiPersona>> = withContext(Dispatchers.IO) {
        try {
            val response = supabase.postgrest["ai_personas"]
                .select(columns = Columns.raw("citizen_id, system_prompt, temperature, is_active, citizens(username, display_name)"))
                {
                    filter {
                        eq("is_active", true)
                    }
                }
                .decodeList<AiPersonaFetchDto>()

            val bots = response.map { dto ->
                AiPersona(
                    citizenId = dto.citizen_id,
                    username = dto.citizens?.username ?: "unknown_bot",
                    displayName = dto.citizens?.display_name ?: "Bot",
                    systemPrompt = dto.system_prompt,
                    temperature = dto.temperature,
                    isActive = dto.is_active ?: true
                )
            }
            Result.success(bots)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun decideAction(
        persona: AiPersona,
        targetPostContent: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemMessage = AiMessage(
                role = "system",
                content = "${persona.systemPrompt} Analiza la siguiente publicación. Decide si quieres darle me gusta ('LIKE'), compartirla ('SHARE'), comentarla ('COMMENT') o ignorarla ('NONE'). Responde ÚNICAMENTE con un objeto JSON plano con la estructura: {\"action\": \"TU_DECISION\"}."
            )
            val userMessage = AiMessage(
                role = "user",
                content = "Publicación: '$targetPostContent'"
            )

            // Bajamos un poco la temperatura para que sea más lógico y menos creativo al decidir
            val request = OpenRouterRequest(
                messages = listOf(systemMessage, userMessage),
                temperature = 0.4
            )
            val response = openRouterApi.generateCompletion(request)
            if (response.isSuccessful) {
                val rawText = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
                val cleanJson = extractJson(rawText)
                val parsed = gson.fromJson(cleanJson, AiDecisionResponse::class.java)
                Result.success(parsed.action?.uppercase() ?: "NONE")
            } else {
                Result.failure(Exception("Error de API OpenRouter: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateAndUploadImage(prompt: String): Result<String> =
        withContext(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                val token = "Bearer ${com.social.flare.BuildConfig.HUGGING_FACE_API_KEY}"
                val request = HuggingFaceRequest(inputs = prompt)
                val response =
                    huggingFaceApi.generateImage(authorization = token, request = request)
                if (!response.isSuccessful || response.body() == null) {
                    return@withContext Result.failure(Exception("Error de Hugging Face ${response.code()}"))
                }
                tempFile = saveResponseBodyToFile(response.body()!!)
                    ?: return@withContext Result.failure(Exception("No se pudo guardar la imagen temporal"))
                val cloudinaryUrl = uploadToCloudinary(tempFile)
                Result.success(cloudinaryUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                tempFile?.delete()
                Result.failure(e)
            }
        }

    override suspend fun generateVisualPrompt(
        persona: AiPersona,
        topicContext: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemMessage = AiMessage(
                role = "system",
                content = "You are an expert prompt engineer for Stable Diffusion XL. " +
                        "The user will give you a topic in Spanish. " +
                        "Your job is to create a highly detailed, cinematic, and descriptive image prompt in ENGLISH based on that topic. " +
                        "Do not include introductory text, just the raw English prompt. Example: " +
                        "'A bustling street in Lima, Peru, heavy traffic, cinematic lighting, ultra realistic'."
            )
            val userMessage = AiMessage(
                role = "user",
                content = topicContext
            )
            val request = OpenRouterRequest(
                messages = listOf(systemMessage, userMessage),
                temperature = 0.7
            )
            val response = openRouterApi.generateCompletion(request)
            if (response.isSuccessful) {
                val rawText =
                    response.body()?.choices?.firstOrNull()?.message?.content?.trim() ?: ""
                Result.success(rawText)
            } else {
                Result.failure(Exception("Error OpenRouter al generar prompt visual: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun followUser(
        followerId: String,
        followedId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val followData = mapOf(
                "followerId" to followerId,
                "followedId" to followedId,
                "timestamp" to System.currentTimeMillis()
            )
            supabase.postgrest["follows"].insert(followData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateLocalImageUri(prompt: String): Result<Uri> = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        try {
            val token = "Bearer ${com.social.flare.BuildConfig.HUGGING_FACE_API_KEY}"
            val request = HuggingFaceRequest(inputs = prompt)
            val response = huggingFaceApi.generateImage(authorization = token, request = request)

            if (!response.isSuccessful || response.body() == null) {
                return@withContext Result.failure(Exception("Error HF: ${response.code()}"))
            }

            tempFile = saveResponseBodyToFile(response.body()!!)
                ?: return@withContext Result.failure(Exception("Error guardando foto temporal"))

            Result.success(Uri.fromFile(tempFile))
        } catch (e: Exception) {
            e.printStackTrace()
            tempFile?.delete()
            Result.failure(e)
        }
    }

    //Helpers
    private fun extractJson(rawText: String): String {
        var json = rawText.trim()
        if (json.startsWith("```json")) {
            json = json.substringAfter("```json").substringBeforeLast("```").trim()
        } else if (json.startsWith("```")) {
            json = json.substringAfter("```").substringBeforeLast("```").trim()
        } else {
            val start = json.indexOf('{')
            val end = json.lastIndexOf('}')
            if (start != -1 && end > start) {
                json = json.substring(start, end + 1)
            }
        }
        return json
    }
    private fun saveResponseBodyToFile(body: ResponseBody): File? {
        return try {
            val file = File(context.cacheDir, "ai_gen_${System.currentTimeMillis()}.jpg")
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) break
                    outputStream.write(fileReader, 0, read)
                }
                outputStream.flush()
                file
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private suspend fun uploadToCloudinary(file: File): String = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(file.absolutePath)
            .option("folder", "flare_ai_generations") // Las guarda en esta carpeta en tu Cloudinary
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) {
                        continuation.resume(url)
                    } else {
                        continuation.resumeWithException(Exception("Cloudinary no devolvió una URL válida"))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }
    private data class AiPostResponse(val content: String)

    @Serializable
    private data class AiDecisionResponse(val action: String)
}