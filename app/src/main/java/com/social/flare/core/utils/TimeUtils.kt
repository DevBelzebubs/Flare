package com.social.flare.core.utils

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
object TimeUtils {
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Ahora"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            else -> {
                val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    fun getTimeAgo(timeInMillis: Long): String {
        val now = System.currentTimeMillis()
        return DateUtils.getRelativeTimeSpanString(
            timeInMillis,
            now,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }
}