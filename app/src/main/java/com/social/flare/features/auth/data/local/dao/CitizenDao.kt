package com.social.flare.features.auth.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CitizenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCitizen(citizen: CitizenEntity)

    @Query("SELECT * FROM citizen_table WHERE citizen_id = :id")
    suspend fun getCitizenById(id: String): CitizenEntity?

    @Query("SELECT * FROM citizen_table WHERE username = :username LIMIT 1")
    suspend fun getCitizenByUsername(username: String): CitizenEntity?
}