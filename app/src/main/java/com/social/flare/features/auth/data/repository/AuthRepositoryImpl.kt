package com.social.flare.features.auth.data.repository

import com.social.flare.BuildConfig
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthRepositoryImpl(
    private val citizenDao: CitizenDao,
    private val supabase: SupabaseClient
) : AuthRepository {

    private val httpClient = HttpClient(Android)
    private val json = Json { ignoreUnknownKeys = true }

    private val baseUrl: String by lazy {
        BuildConfig.SUPABASE_URL.substringBefore("/rest/v1")
    }

    override suspend fun login(username: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val isEmail = username.contains("@")

            val finalEmailToLogin = if (isEmail) {
                username
            } else {
                val citizenResult = supabase.postgrest["citizens"]
                    .select {
                        filter { eq("username", username) }
                    }

                val citizenList = citizenResult.decodeList<CitizenEntity>()
                if (citizenList.isEmpty()) {
                    throw Exception("Usuario no encontrado")
                }
                citizenList.first().email
            }

            supabase.auth.signInWith(Email) {
                this.email = finalEmailToLogin
                this.password = pass
            }

            val session = supabase.auth.currentSessionOrNull()
                ?: throw Exception("Error al iniciar sesión: No se generó sesión")
            val user = session.user ?: throw Exception("Error al obtener datos del usuario")

            val citizen = citizenDao.getCitizenById(user.id)
            if (citizen == null) {
                val supabaseCitizen = supabase.postgrest["citizens"]
                    .select { filter { eq("citizen_id", user.id) } }
                    .decodeSingleOrNull<CitizenEntity>()

                if (supabaseCitizen != null) {
                    citizenDao.insertCitizen(supabaseCitizen)
                } else {
                    throw Exception("Perfil de ciudadano no encontrado")
                }
            }

            Result.success(user.id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(e.message ?: "Error desconocido en login"))
        }
    }

    override suspend fun register(
        displayName: String,
        username: String,
        email: String,
        pass: String
    ): Result<String> {
        return try {
            val requestBody = """
                {"email":"$email","password":"$pass","email_confirm":true,"user_metadata":{"username":"$username","display_name":"$displayName"}}
            """.trimIndent()

            val response = httpClient.post("$baseUrl/auth/v1/admin/users") {
                header("apikey", BuildConfig.SUPABASE_SERVICE_ROLE_KEY)
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_SERVICE_ROLE_KEY}")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val body = response.bodyAsText()
            val parsed = json.parseToJsonElement(body).jsonObject
            val userId = parsed["id"]?.jsonPrimitive?.content
                ?: return Result.failure(Exception("Error al registrar usuario: $body"))

            citizenDao.insertCitizen(
                CitizenEntity(
                    citizen_id = userId,
                    email = email,
                    username = username,
                    display_name = displayName,
                    password = "",
                    bio = "I am new to Flare!",
                    is_admin = false,
                    status = "active"
                )
            )

            Result.success(userId)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            supabase.auth.signOut()
        } catch (e: Throwable) { e.printStackTrace() }
    }

    override suspend fun changePassword(newPassword: String): Result<Unit> {
        return try {
            supabase.auth.awaitInitialization()
            if (supabase.auth.currentSessionOrNull() == null) {
                return Result.failure(Exception("No active session found. Please log in again."))
            }

            supabase.auth.updateUser {
                password = newPassword
            }

            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}