package com.social.flare.features.auth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CitizenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCitizen(citizen: CitizenEntity)

    @Query("SELECT * FROM citizen_table WHERE citizen_id = :id")
    suspend fun getCitizenById(id: String): CitizenEntity?

    @Query("SELECT * FROM citizen_table WHERE citizen_id IN (:ids)")
    suspend fun getCitizensByIds(ids: List<String>): List<CitizenEntity>
    @Query("SELECT * FROM citizen_table WHERE citizen_id = :id LIMIT 1")
    fun observeCitizenById(id: String): Flow<CitizenEntity?>

    @Query("SELECT * FROM citizen_table WHERE username = :username LIMIT 1")
    suspend fun getCitizenByUsername(username: String): CitizenEntity?
    @Query("""
        UPDATE citizen_table 
        SET display_name = :displayName, 
            bio = :bio, 
            avatar_url = :avatarUrl, 
            banner_url = :bannerUrl 
        WHERE citizen_id = :citizenId
    """)
    suspend fun updateCitizenProfile(
        citizenId: String,
        displayName: String,
        bio: String?,
        avatarUrl: String?,
        bannerUrl: String?
    )

    @Query("SELECT * FROM citizen_table ORDER BY username ASC")
    suspend fun getAllCitizens(): List<CitizenEntity>

    @Query("SELECT COUNT(*) FROM citizen_table")
    suspend fun getCitizenCount(): Int

    @Query("UPDATE citizen_table SET status = :status WHERE citizen_id = :citizenId")
    suspend fun updateUserStatus(citizenId: String, status: String)

    @Query("DELETE FROM citizen_table WHERE citizen_id = :citizenId")
    suspend fun deleteCitizen(citizenId: String)

    @Query("SELECT * FROM citizen_table WHERE is_admin = 1 LIMIT 1")
    suspend fun getAdminCitizen(): CitizenEntity?
}