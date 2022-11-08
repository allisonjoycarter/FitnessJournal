package com.catscoffeeandkitchen.data.workouts.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WgerSearchSuggestion(
    val value: String,
    val data: WgerSearchData
)
