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
}