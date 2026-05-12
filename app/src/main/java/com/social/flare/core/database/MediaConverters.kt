package com.social.flare.core.database

import androidx.room.TypeConverter

class MediaConverters {
    @TypeConverter
    fun fromMediaList(urls: List<String>?): String {
        if (urls.isNullOrEmpty()) return ""
        return urls.filter { it.isNotBlank() }.joinToString(separator = ",")
    }

    @TypeConverter
    fun toMediaList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(",").filter { it.isNotBlank() }
    }
}