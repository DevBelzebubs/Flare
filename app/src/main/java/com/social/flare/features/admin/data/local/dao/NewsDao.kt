package com.social.flare.features.admin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.social.flare.features.admin.data.local.entity.NewsItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: NewsItemEntity)

    @Query("SELECT * FROM news_table ORDER BY created_at DESC")
    fun getAllNews(): Flow<List<NewsItemEntity>>

    @Query("SELECT * FROM news_table WHERE is_active = 1 ORDER BY created_at DESC")
    fun getActiveNews(): Flow<List<NewsItemEntity>>

    @Query("SELECT * FROM news_table WHERE news_id = :newsId")
    suspend fun getNewsById(newsId: String): NewsItemEntity?

    @Query("UPDATE news_table SET title = :title, description = :description, image_url = :imageUrl WHERE news_id = :newsId")
    suspend fun updateNews(newsId: String, title: String, description: String, imageUrl: String?)

    @Query("UPDATE news_table SET is_active = :isActive WHERE news_id = :newsId")
    suspend fun toggleNewsActive(newsId: String, isActive: Boolean)

    @Query("DELETE FROM news_table WHERE news_id = :newsId")
    suspend fun deleteNews(newsId: String)

    @Query("SELECT COUNT(*) FROM news_table")
    suspend fun countNews(): Int
}
