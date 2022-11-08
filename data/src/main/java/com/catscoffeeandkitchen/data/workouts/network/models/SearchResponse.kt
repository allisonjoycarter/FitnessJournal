package com.catscoffeeandkitchen.data.workouts.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResponse(
    val suggestions: List<WgerSearchSuggestion>
)
