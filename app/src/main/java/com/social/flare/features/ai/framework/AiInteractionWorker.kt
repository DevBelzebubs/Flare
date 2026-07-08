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
import com.social.flare.features.ai.domain.usecase.GenerateAutonomousStoryUseCase
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
    private val generateCommentUseCase: GenerateAutonomousCommentUseCase,
    private val generateStoryUseCase: GenerateAutonomousStoryUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AiBot", "Despertando al Worker de IA...")

        return try {
            val botsResult = aiRepository.getActiveBots()
            if (botsResult.isFailure) {
                Log.e("AiBot", "Error obteniendo bots: ${botsResult.exceptionOrNull()?.message}")
                return Result.retry()
            }

            val activePersonas = botsResult.getOrNull()?.filter { it.isActive } ?: emptyList()
            if (activePersonas.isEmpty()) {
                Log.d("AiBot", "No hay bots activos. Durmiendo...")
                return Result.success()
            }

            Log.d("AiBot", "Ejecutando acciones para ${activePersonas.size} bots")
            for (persona in activePersonas) {
                try {
                    executeAiAction(persona)
                    kotlinx.coroutines.delay(120_000)
                } catch (e: Exception) {
                    Log.e("AiBot", "Error en acción para ${persona.username}: ${e.message}")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("AiBot", "Crash en Worker: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun executeAiAction(persona: AiPersona) {
        val actionDice = Random.nextInt(1, 100)
        Log.d("AiBot", "Dado de accion: $actionDice para ${persona.displayName}")

        when {
            actionDice <= 20 -> performPostAction(persona)
            actionDice <= 35 -> performStoryAction(persona)
            else -> performSocialInteraction(persona)
        }
    }

    private suspend fun performPostAction(persona: AiPersona) {
        val topics = listOf("trafico en Lima", "el clima raro de estos días",
            "la comida peruana",
            "la seguridad en el transporte público"
        )
        val selectedTopic = topics.random()
        val wantsImage = Random.nextInt(1, 100) <= 30
        val result = generatePostUseCase.execute(
            persona = persona,
            contextTopic = selectedTopic,
            shouldIncludeImage = wantsImage
        )
        if (result.isSuccess) {
            Log.d("AiBot", "Post publicado por ${persona.username}")
        } else {
            Log.e("AiBot", "Error publicando post de ${persona.username}: ${result.exceptionOrNull()?.message}")
        }
    }

    private suspend fun performSocialInteraction(persona: AiPersona) {
        Log.d("AiBot", "Buscando publicaciones para ${persona.username}...")

        val posts = feedRepository.getFeedPosts(persona.citizenId).firstOrNull()
        val targetPost = posts?.filter {
            it.authorId != persona.citizenId && !it.content.isNullOrBlank()
        }?.randomOrNull()

        if (targetPost == null) {
            Log.d("AiBot", "No se encontro un post valido en el feed para ${persona.username}.")
            return
        }

        val actionRoll = Random.nextInt(1, 100)
        val decision = when {
            actionRoll <= 40 -> "LIKE"
            actionRoll <= 70 -> "COMMENT"
            else -> "SHARE"
        }
        Log.d("AiBot", "Decision aleatoria para ${persona.username}: $decision (roll=$actionRoll)")

        when (decision) {
            "LIKE" -> {
                if (targetPost.isLikedByMe) {
                    Log.d("AiBot", "Like ya existe, saltando: ${targetPost.id}")
                } else {
                    val result = feedRepository.toggleLike(targetPost.id, persona.citizenId, false)
                    if (result.isSuccess) {
                        Log.d("AiBot", "Like dado al post: ${targetPost.id}")
                        if (Random.nextInt(1, 100) <= 20) {
                            val followResult = aiRepository.followUser(persona.citizenId, targetPost.authorId)
                            if (followResult.isSuccess) {
                                Log.d("AiBot", "¡A ${persona.username} le gustó y decidió seguir a ${targetPost.authorId}!")
                            }
                        }
                    } else {
                        Log.e("AiBot", "Error al dar like: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
            "SHARE" -> {
                if (targetPost.isSharedByMe) {
                    Log.d("AiBot", "Post ya compartido, saltando: ${targetPost.id}")
                } else {
                    try {
                        feedRepository.toggleSharePost(persona.citizenId, targetPost.id, false)
                        Log.d("AiBot", "Post compartido: ${targetPost.id}")
                    } catch (e: Exception) {
                        Log.e("AiBot", "Error al compartir: ${e.message}")
                    }
                }
            }
            "COMMENT" -> {
                val result = generateCommentUseCase.execute(persona, targetPost.id, targetPost.content ?: "")
                if (result.isSuccess) {
                    Log.d("AiBot", "Comentario publicado por ${persona.username}")
                } else {
                    Log.e("AiBot", "Error al comentar de ${persona.username}: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }
    private suspend fun performStoryAction(persona: AiPersona) {
        val topics = listOf(
            "tomando un café en la mañana",
            "caminando por la calle",
            "mostrando mi almuerzo",
            "selfie casual"
        )
        val selectedTopic = topics.random()
        val result = generateStoryUseCase.execute(persona, selectedTopic)

        if (result.isFailure) {
            Log.e("AiBot", "Error publicando historia: ${result.exceptionOrNull()?.message}")
        }
    }
}
