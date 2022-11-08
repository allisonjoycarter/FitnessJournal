package com.catscoffeeandkitchen.data.util

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun toDate(epochMilli: Long?): OffsetDateTime? {
        return if (epochMilli == null) {
            null
        } else {
            OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC)
        }
    }

    @TypeConverter
    fun toDateString(date: OffsetDateTime?): Long? {
        return date?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toStringList(unbrokenString: String?): List<String>? {
        return unbrokenString?.split("|")?.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun listToString(items: List<String>?): String? {
        return items?.joinToString("|")
    }
}