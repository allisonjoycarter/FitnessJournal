package com.catscoffeeandkitchen.data.workouts.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

fun OffsetDateTime.toUTCEpochMilli(): Long {
    return this.atZoneSameInstant(ZoneId.of("UTC")).toInstant().toEpochMilli()
}

fun Long.toOffsetDateTime(): OffsetDateTime =
    OffsetDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)


fun OffsetDateTime.inUTC(): OffsetDateTime =
    this.atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
