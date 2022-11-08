package com.catscoffeeandkitchen.data.workouts.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WgerExerciseItem(
    @Json(name = "author_history") val authorHistory: List<String>,
    val category: Int,
    @Json(name = "creation_date") val creationDate: String,
    val description: String,
    val equipment: List<Int>,
    @Json(name = "exercise_base") val exerciseBase: Int,
    val id: Int,
    val language: Int,
    val license: Int,
    @Json(name = "license_author") val licenseAuthor: String,
    val muscles: List<Int>,
    @Json(name = "muscles_secondary") val musclesSecondary: List<Int>,
    val name: String,
    val uuid: String,
    val variations: List<Int>
)