package com.social.flare.features.auth.data.repository

import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.auth.domain.repository.AuthRepository
import java.security.MessageDigest
import java.util.UUID

class AuthRepositoryImpl(
    private val citizenDao: CitizenDao,
) : AuthRepository {
    override suspend fun login(username: String, pass: String): Result<String> {
        return try {
            val citizen = citizenDao.getCitizenByUsername(username)
            if (citizen != null && citizen.password == pass.toSHA256()){
                Result.success(citizen.citizen_id)
            }else{
                Result.failure(Exception("Usuario no encontrado"))
            }
        }catch (e: Exception) {
            Result.failure(e);
        }
    }
    override suspend fun register(displayName: String, username: String, email: String, pass: String): Result<String> {
        return try {
            val existingCitizen = citizenDao.getCitizenByUsername(username)
            if (existingCitizen != null) {
                return Result.failure(Exception("El nombre de usuario ya está en uso"))
            }
            val newCitizenId = UUID.randomUUID().toString()
            val newCitizen = CitizenEntity(citizen_id = newCitizenId,
                username = username,
                display_name = displayName,
                password = pass.toSHA256(),
                avatar_url = null,
                bio = "I am new to Flare!"
            )
            citizenDao.insertCitizen(newCitizen)
            Result.success(newCitizenId)
        }catch (e: Exception) {
            Result.failure(e);
        }
    }
    fun String.toSHA256(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override suspend fun logout() {
        TODO("Not yet implemented")
    }
}