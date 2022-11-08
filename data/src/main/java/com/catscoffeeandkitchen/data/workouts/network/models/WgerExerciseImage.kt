package com.catscoffeeandkitchen.data.workouts.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WgerExerciseImage(
    val id: Int,
    val uuid: String,
    @Json(name = "exercise_base") val exerciseBase: Int,
    val image: String,
    @Json(name = "is_main") val isMain: Boolean,
    val style: Int,
    val license: Int,
    @Json(name = "license_author") val licenseAuthor: String,
    @Json(name = "author_history") val authorHistory: List<Any>
)
