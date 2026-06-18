package com.social.flare.features.auth.data.repository

import com.social.flare.BuildConfig
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl(
    private val citizenDao: CitizenDao,
    private val supabase: SupabaseClient
) : AuthRepository {

    override suspend fun login(email: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val isEmail = email.indexOf('@') > 0

            val finalEmailToLogin = if (isEmail) {
                email
            } else {
                val citizenResult = supabase.postgrest["citizens"]
                    .select {
                        filter { eq("username", email) }
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
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val registerEmail = email
            supabase.auth.signUpWith(Email) {
                this.email = registerEmail
                password = pass
                data = buildJsonObject {
                    put("username", username)
                    put("display_name", displayName)
                }
            }

            val session = supabase.auth.currentSessionOrNull()
            if (session == null) {
                val citizen = supabase.postgrest["citizens"]
                    .select { filter { eq("email", email) } }
                    .decodeSingleOrNull<CitizenEntity>()
                if (citizen != null) {
                    citizenDao.insertCitizen(citizen)
                    return@withContext Result.success(citizen.citizen_id)
                }
                return@withContext Result.failure(Exception("Cuenta creada. Revisa tu correo para confirmar el registro."))
            }
            val userId = session.user?.id
                ?: return@withContext Result.failure(Exception("Error al registrar: No se generó usuario"))

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

    override suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signOut()
        } catch (e: Throwable) { e.printStackTrace() }
    }

    override suspend fun changePassword(newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.awaitInitialization()
            if (supabase.auth.currentSessionOrNull() == null) {
                return@withContext Result.failure(Exception("No active session found. Please log in again."))
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