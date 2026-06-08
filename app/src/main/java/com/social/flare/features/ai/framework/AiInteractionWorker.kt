package com.social.flare.features.ai.framework

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import com.social.flare.features.ai.domain.usecase.GenerateAutonomousCommentUseCase
import com.social.flare.features.ai.domain.usecase.GenerateAutonomousPostUseCase
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.feed.domain.repository.FeedRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

@HiltWorker
class AiInteractionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val aiRepository: AiAgentRepository,
    private val feedRepository: FeedRepository,
    private val generatePostUseCase: GenerateAutonomousPostUseCase,
    private val generateCommentUseCase: GenerateAutonomousCommentUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AiBot", "Despertando al Worker de IA...")

        return try {
            val botsResult = aiRepository.getActiveBots()
            val activePersonas = botsResult.getOrNull() ?: emptyList()

            if (activePersonas.isEmpty()) {
                Log.d("AiBot", "⏸No hay bots activos. Durmiendo...")
                return Result.success()
            }

            val persona = activePersonas.random()
            executeAiAction(persona)

            Result.success()
        } catch (e: Exception) {
            Log.e("AiBot", "Crash en Worker: ${e.message}")
            Result.retry()
        }
    }
    private suspend fun executeAiAction(persona: AiPersona) {
        val actionDice = Random.nextInt(1, 100)
        Log.d("AiBot", "Dado de acción: $actionDice para ${persona.displayName}")

        if (actionDice <= 30) {
            performPostAction(persona)
        } else {
            performSocialInteraction(persona)
        }
    }
    private suspend fun performPostAction(persona: AiPersona) {
        val topics = listOf("tráfico en Lima", "clima", "comida barata", "seguridad")
        val result = generatePostUseCase.execute(persona, topics.random())

        if (result.isSuccess) Log.d("AiBot", "Post publicado.")
        else Log.e("AiBot", "Error publicando: ${result.exceptionOrNull()?.message}")
    }
    private suspend fun performSocialInteraction(persona: AiPersona) {
        Log.d("AiBot", "Buscando publicaciones para interactuar...")

        val posts = feedRepository.getFeedPosts(persona.citizenId).firstOrNull()
        val targetPost = posts?.filter {
            it.authorId != persona.citizenId && !it.content.isNullOrBlank()
        }?.randomOrNull()

        if (targetPost == null) {
            Log.d("AiBot", "No se encontró un post válido en el feed.")
            return
        }

        Log.d("AiBot", "Post encontrado. Consultando a la IA qué hacer...")
        val decisionResult = aiRepository.decideAction(persona, targetPost.content ?: "")

        if (decisionResult.isSuccess) {
            val decision = decisionResult.getOrNull()
            Log.d("AiBot", "Decisión de la IA: $decision")

            when (decision) {
                "LIKE" -> {
                    aiRepository.likePost(persona.citizenId, targetPost.id)
                    Log.d("AiBot", "Like dado al post: ${targetPost.id}")
                }
                "SHARE" -> {
                    aiRepository.sharePost(persona.citizenId, targetPost)
                    Log.d("AiBot", "Post compartido: ${targetPost.id}")
                }
                "COMMENT" -> {
                    val result = generateCommentUseCase.execute(persona, targetPost.id, targetPost.content ?: "")
                    if (result.isSuccess) Log.d("AiBot", "Comentario publicado.")
                    else Log.e("AiBot", "Error al comentar.")
                }
                "NONE" -> Log.d("AiBot", "La IA decidió ignorar el post.")
                else -> Log.w("AiBot", "⚠Decisión desconocida: $decision")
            }
        } else {
            Log.e("AiBot", "Error al decidir acción: ${decisionResult.exceptionOrNull()?.message}")
        }
    }
}