package com.catscoffeeandkitchen.data.util

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.catscoffeeandkitchen.data.workouts.util.toOffsetDateTime
import com.catscoffeeandkitchen.data.workouts.util.toUTCEpochMilli
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun toDate(epochMilli: Long?): OffsetDateTime? {
        return epochMilli?.toOffsetDateTime()
    }

    @TypeConverter
    fun toDateString(date: OffsetDateTime?): Long? {
        return date?.toUTCEpochMilli()
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