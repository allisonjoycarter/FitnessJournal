package com.catscoffeeandkitchen.fitnessjournal.workouts

import com.catscoffeeandkitchen.data.workouts.network.ExerciseSearchService
import com.catscoffeeandkitchen.data.workouts.network.models.*

class TestExerciseSearchService: ExerciseSearchService {

    override suspend fun searchExercises(
        search: String,
    ): SearchResponse {
        return SearchResponse(emptyList())
    }

    override suspend fun getExercises(
        limit: Int,
        offset: Int,
        name: String?,
        muscle: WgerMuscle?,
        category: WgerExerciseCategory?,
    ): WgerPage<WgerExerciseItem> {
        return WgerPage(0, null, null, emptyList())
    }

    override suspend fun getExerciseImages(
        id: Int?,
        limit: Int,
        offset: Int
    ): WgerPage<WgerExerciseImage> {
        return WgerPage(0, null, null, emptyList())
    }
}