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

    override suspend fun login(username: String, pass: String): Result<String> {
        return try {
            val citizen = supabase.postgrest["citizens"]
                .select { filter { eq("username", username) } }
                .decodeSingle<CitizenEntity>()

            supabase.auth.signInWith(Email) {
                email = citizen.email
                password = pass
            }

            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Error al iniciar sesión"))

            citizenDao.insertCitizen(citizen.copy(citizen_id = userId))

            Result.success(userId)
        } catch (e: Throwable) {
            Result.failure(e)
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
}
