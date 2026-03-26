package com.example.appestudio.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Converts an ISO 8601 date string to a relative timestamp like "Hace 2h", "Ayer", etc.
 */
fun String.toRelativeTime(): String {
    return try {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd"
        )
        var date: Date? = null
        for (fmt in formats) {
            runCatching {
                val sdf = SimpleDateFormat(fmt, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                date = sdf.parse(this)
            }
            if (date != null) break
        }
        val parsed = date ?: return this.take(10)
        val now = System.currentTimeMillis()
        val diff = now - parsed.time

        val minutes = diff / 60_000
        val hours   = diff / 3_600_000
        val days    = diff / 86_400_000

        when {
            minutes < 1  -> "Ahora"
            minutes < 60 -> "Hace ${minutes}min"
            hours < 24   -> "Hace ${hours}h"
            days == 1L   -> "Ayer"
            days < 7     -> "Hace ${days} días"
            else         -> SimpleDateFormat("dd MMM", Locale("es")).format(parsed)
        }
    } catch (_: Exception) {
        this.take(10)
    }
}
