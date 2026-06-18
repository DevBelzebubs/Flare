package com.social.flare.core.sync.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.social.flare.core.sync.data.local.entity.SyncQueueEntity

@Dao
interface SyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncTask(task: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue_table WHERE status = 'PENDING' ORDER BY created_at ASC")
    suspend fun getPendingTasks(): List<SyncQueueEntity>

    @Query("UPDATE sync_queue_table SET status = :status WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String)

    @Query("DELETE FROM sync_queue_table WHERE id = :id")
    suspend fun deleteSyncTask(id: String)
}