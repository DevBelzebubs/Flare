package com.social.flare.features.ai.framework

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import com.social.flare.features.ai.domain.usecase.GenerateAutonomousCommentUseCase
import com.social.flare.features.ai.domain.usecase.GenerateAutonomousPostUseCase
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
        Log.d("AiBot", "🤖 Despertando al Worker de IA...")

        return try {
            val botsResult = aiRepository.getActiveBots()
            val activePersonas = botsResult.getOrNull() ?: emptyList()

            if (activePersonas.isEmpty()) {
                Log.d("AiBot", "⏸️ No hay bots activos en la base de datos. El worker se vuelve a dormir.")
                return Result.success()
            }

            val selectedPersona = activePersonas.random()
            Log.d("AiBot", "Bot seleccionado desde BD: ${selectedPersona.displayName}")

            val actionDice = Random.nextInt(1, 100)
            Log.d("AiBot", "Dado de acción: $actionDice")

            if (actionDice <= 30) {
                Log.d("AiBot", "✍Intentando crear un post normal...")
                val topics = listOf(
                    "el tráfico en Lima hoy",
                    "el clima raro de estos días",
                    "recomendaciones de comida barata",
                    "la seguridad en el transporte público"
                )

                val result = generatePostUseCase.execute(
                    persona = selectedPersona,
                    contextTopic = topics.random()
                )

                if (result.isSuccess) {
                    Log.d("AiBot", "Post publicado con éxito")
                } else {
                    Log.e("AiBot", "Error al publicar: ${result.exceptionOrNull()?.message}")
                }
            } else {
                Log.d("AiBot", "Intentando comentar un post...")
                val recentPosts = feedRepository.getFeedPosts(currentUserId = selectedPersona.citizenId).firstOrNull()

                if (!recentPosts.isNullOrEmpty()) {
                    val targetPost = recentPosts.filter { post ->
                        post.authorId != selectedPersona.citizenId && !post.content.isNullOrBlank()
                    }.randomOrNull()

                    if (targetPost != null) {
                        Log.d("AiBot", "Comentando el post ID: ${targetPost.id}")
                        val result = generateCommentUseCase.execute(
                            persona = selectedPersona,
                            postId = targetPost.id,
                            postContent = targetPost.content ?: ""
                        )

                        if (result.isSuccess) {
                            Log.d("AiBot", "Comentario publicado con éxito")
                        } else {
                            Log.e("AiBot", "Error al comentar: ${result.exceptionOrNull()?.message}")
                        }
                    } else {
                        Log.d("AiBot", "No se encontró un post válido para comentar.")
                    }
                } else {
                    Log.d("AiBot", "Feed vacío, nada que comentar.")
                }
            }

            Log.d("AiBot", "Worker finalizado correctamente.")
            Result.success()

        } catch (e: Exception) {
            Log.e("AiBot", "Crash en el Worker: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }
}