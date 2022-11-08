package com.catscoffeeandkitchen.data.workouts.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WgerPage<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>
)
