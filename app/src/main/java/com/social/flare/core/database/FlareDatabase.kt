package com.social.flare.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.social.flare.features.admin.data.local.dao.NewsDao
import com.social.flare.features.admin.data.local.entity.NewsItemEntity
import com.social.flare.features.auth.data.local.dao.CitizenDao
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.feed.data.local.dao.PostDao
import com.social.flare.features.feed.data.local.entity.PostEntity
import com.social.flare.features.feed.data.local.entity.PostLikeEntity
import com.social.flare.features.feed.data.local.dao.StoryDao
import com.social.flare.features.feed.data.local.entity.SavedPostEntity
import com.social.flare.features.feed.data.local.entity.StoryEntity
import com.social.flare.features.feed.data.local.entity.StoryCommentEntity
import com.social.flare.features.feed.data.local.entity.StoryViewEntity
import com.social.flare.features.feed.data.local.entity.PostMediaEntity
import com.social.flare.features.notifications.data.local.dao.NotificationDao
import com.social.flare.features.notifications.data.local.entity.NotificationEntity
import com.social.flare.features.profile.data.local.dao.FollowDao
import com.social.flare.features.profile.data.local.entity.FollowEntity

@Database(
    entities = [
        CitizenEntity::class,
        PostEntity::class,
        PostMediaEntity::class,
        PostLikeEntity::class,
        SavedPostEntity::class,
        StoryEntity::class,
        StoryViewEntity::class,
        StoryCommentEntity::class,
        FollowEntity::class,
        NotificationEntity::class,
        NewsItemEntity::class
    ],
    version = 18,
    exportSchema = false
)
abstract class FlareDatabase : RoomDatabase() {
    abstract fun citizenDao(): CitizenDao
    abstract fun postDao(): PostDao

    abstract fun storyDao(): StoryDao
    abstract fun followDao(): FollowDao
    abstract fun notificationDao(): NotificationDao
    abstract fun newsDao(): NewsDao
}