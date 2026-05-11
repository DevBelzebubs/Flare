package com.social.flare.core.database

import androidx.room.TypeConverter

class MediaConverters {
    @TypeConverter
    fun fromMediaList(urls: List<String>?): String {
        return urls?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toMediaList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(",")
    }
}