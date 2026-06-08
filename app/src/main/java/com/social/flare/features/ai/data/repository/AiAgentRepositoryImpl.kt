package com.social.flare.features.ai.data.repository

import com.google.gson.Gson
import com.social.flare.features.ai.data.remote.OpenRouterApi
import com.social.flare.features.ai.data.remote.dto.AiMessage
import com.social.flare.features.ai.data.remote.dto.OpenRouterRequest
import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import com.social.flare.features.feed.domain.model.Post
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
private data class AiPersonaFetchDto(
    val citizen_id: String,
    val system_prompt: String,
    val temperature: Double,
    val citizens: CitizenMinimalDto? = null
)

@Serializable
private data class CitizenMinimalDto(
    val username: String,
    val display_name: String
)

class AiAgentRepositoryImpl @Inject constructor(
    private val openRouterApi: OpenRouterApi,
    private val gson: Gson,
    private val supabase: SupabaseClient
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
            // Hacemos la consulta a ai_personas y traemos las columnas anidadas de citizens
            val response = supabase.postgrest["ai_personas"]
                .select(columns = Columns.raw("citizen_id, system_prompt, temperature, citizens(username, display_name)"))
                .decodeList<AiPersonaFetchDto>()

            val bots = response.map { dto ->
                AiPersona(
                    citizenId = dto.citizen_id,
                    username = dto.citizens?.username ?: "unknown_bot",
                    displayName = dto.citizens?.display_name ?: "Bot",
                    systemPrompt = dto.system_prompt,
                    temperature = dto.temperature
                )
            }
            Result.success(bots)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun likePost(
        citizenId: String,
        postId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["post_likes"].insert(mapOf("post_id" to postId, "citizen_id" to citizenId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e);
        }
    }

    override suspend fun sharePost(
        citizenId: String,
        originalPost: Post
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sharedPostData = mapOf(
                "author_id" to citizenId,
                "content" to "Compartió una publicación: ${originalPost.content?.take(50)}...",
                "shared_post_id" to originalPost.id,
                "created_at" to System.currentTimeMillis()
            )
            supabase.postgrest["posts"].insert(sharedPostData)
            Result.success(Unit)
        } catch (e: Exception){
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
                Result.success(parsed.action.uppercase())
            } else {
                Result.failure(Exception("Error de API OpenRouter: ${response.code()}"))
            }
        } catch (e: Exception) {
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
        }
        return json
    }
    private data class AiPostResponse(val content: String)

    @Serializable
    private data class AiDecisionResponse(val action: String)
}