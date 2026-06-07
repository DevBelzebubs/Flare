package com.social.flare.features.ai.framework

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.social.flare.FlareApp
import com.social.flare.features.ai.domain.model.AiPersona
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import com.social.flare.features.ai.domain.usecase.GenerateAutonomousCommentUseCase
import com.social.flare.features.ai.domain.usecase.GenerateAutonomousPostUseCase
import com.social.flare.features.feed.data.repository.FeedRepositoryImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

@HiltWorker
class AiInteractionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val aiRepository: AiAgentRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AiBot", "🤖 Despertando al Worker de IA...")

        return try {
            val app = applicationContext as FlareApp
            val feedRepository = FeedRepositoryImpl(
                app.database.postDao(),
                app.database.citizenDao(),
                app.database.followDao(),
                app.supabase
            )
            val generatePostUseCase = GenerateAutonomousPostUseCase(aiRepository, feedRepository)
            val generateCommentUseCase = GenerateAutonomousCommentUseCase(aiRepository, feedRepository)

            val botsResult = aiRepository.getActiveBots()
            val dbBots = botsResult.getOrNull() ?: emptyList()

            val activePersonas = if (dbBots.isNotEmpty()) dbBots else getActiveAiPersonas()

            if (activePersonas.isEmpty()) {
                Log.d("AiBot", "❌ No hay bots activos ni de respaldo.")
                return Result.success()
            }

            val selectedPersona = activePersonas.random()
            Log.d("AiBot", "🎯 Bot seleccionado: ${selectedPersona.displayName}")

            val isTesting = true

            if (isTesting) {
                Log.d("AiBot", "✍️ [TEST MODE] Forzando la creación de un post...")
                val topics = listOf("el tráfico en Lima hoy", "el clima raro de estos días", "recomendaciones de comida barata", "la seguridad en el transporte público")

                val result = generatePostUseCase.execute(
                    persona = selectedPersona,
                    contextTopic = topics.random()
                )

                if (result.isSuccess) {
                    Log.d("AiBot", "✅ Post publicado con éxito")
                } else {
                    Log.e("AiBot", "❌ Error al publicar: ${result.exceptionOrNull()?.message}")
                }
            } else {
                val actionDice = Random.nextInt(1, 100)
                Log.d("AiBot", "🎲 Dado de acción: $actionDice")

                if (actionDice <= 30) {
                    Log.d("AiBot", "✍️ Intentando crear un post normal...")
                    val topics = listOf("el tráfico en Lima hoy", "el clima raro de estos días", "recomendaciones de comida barata", "la seguridad en el transporte público")
                    generatePostUseCase.execute(
                        persona = selectedPersona,
                        contextTopic = topics.random()
                    )
                } else {
                    Log.d("AiBot", "💬 Intentando comentar un post...")
                    val recentPosts = feedRepository.getFeedPosts(currentUserId = selectedPersona.citizenId).firstOrNull()

                    if (!recentPosts.isNullOrEmpty()) {
                        val targetPost = recentPosts.filter { post ->
                            post.authorId != selectedPersona.citizenId && !post.content.isNullOrBlank()
                        }.randomOrNull()

                        if (targetPost != null) {
                            Log.d("AiBot", "💬 Comentando el post ID: ${targetPost.id}")
                            generateCommentUseCase.execute(
                                persona = selectedPersona,
                                postId = targetPost.id,
                                postContent = targetPost.content ?: ""
                            )
                        } else {
                            Log.d("AiBot", "🤷‍♂️ No se encontró un post válido para comentar.")
                        }
                    } else {
                        Log.d("AiBot", "🤷‍♂️ Feed vacío, nada que comentar.")
                    }
                }
            }

            Log.d("AiBot", "🏁 Worker finalizado correctamente.")
            Result.success()

        } catch (e: Exception) {
            Log.e("AiBot", "💥 Crash en el Worker: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun getActiveAiPersonas(): List<AiPersona> {
        return listOf(
            AiPersona(
                citizenId = "76f9351c-e210-49fd-bbaa-7fb296add64b",
                username = "vecino_vigilante",
                displayName = "Vecino Vigilante \uD83E\uDD16",
                systemPrompt = "Eres un vecino de Lima, muy observador. Te gusta comentar sobre el estado de las calles y el orden. Hablas con jerga peruana suave ('pucha', 'asu', 'causa').",
                temperature = 0.8
            )
        )
    }
}