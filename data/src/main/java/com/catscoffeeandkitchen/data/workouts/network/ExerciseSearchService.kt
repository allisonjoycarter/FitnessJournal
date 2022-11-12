package com.catscoffeeandkitchen.data.workouts.network

import com.catscoffeeandkitchen.data.workouts.network.models.*
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

interface ExerciseSearchService {
    interface Endpoints {
        @GET("/api/v2/exercise")
        suspend fun getExercises(
            @Query("limit") limit: Int = 20,
            @Query("offset") offset: Int = 0,
            @Query("name") name: String? = null,
            @Query("muscles") muscles: Int? = null,
            @Query("category") category: Int? = null,
            @Query("language") language: Int = 2, // 2 english, 1 german
        ): WgerPage<WgerExerciseItem>

        @GET("/api/v2/exerciseimage")
        suspend fun getExerciseImages(
            @Query("limit") limit: Int = 20,
            @Query("offset") offset: Int = 0,
            @Query("exercise_base") id: Int? = null,
        ): WgerPage<WgerExerciseImage>

        @GET("/api/v2/exercise/search")
        suspend fun searchExercises(
            @Query("term") name: String? = null,
            @Query("language") language: Int = 2, // 2 english, 1 german
        ): SearchResponse
    }

    class Impl(retrofit: Retrofit) {
        private val endpoint = retrofit.create(Endpoints::class.java)

        suspend fun searchExercises(
            search: String,
        ): SearchResponse {
            return endpoint.searchExercises(
                search,
            )
        }

        suspend fun getExercises(
            limit: Int,
            offset: Int,
            name: String?,
            muscle: WgerMuscle? = null,
            category: WgerExerciseCategory? = null,
        ): WgerPage<WgerExerciseItem> {
            return endpoint.getExercises(
                limit = limit,
                offset = offset,
                name = name,
                muscles = muscle?.number,
                category = category?.number
            )
        }

        suspend fun getExerciseImages(
            id: Int? = null,
            limit: Int = 20,
            offset: Int = 0
        ): WgerPage<WgerExerciseImage> {
            return endpoint.getExerciseImages(id = id, limit = limit, offset = offset)
        }
    }
}