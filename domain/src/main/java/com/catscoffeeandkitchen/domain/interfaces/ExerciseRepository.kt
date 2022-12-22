package com.catscoffeeandkitchen.domain.interfaces

import androidx.paging.PagingData
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import com.catscoffeeandkitchen.domain.models.Workout
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

interface ExerciseRepository {

    suspend fun getExercises(names: List<String>? = null): List<Exercise>

    suspend fun getExerciseByName(name: String): Exercise?

    fun getPagedExercises(search: String?, muscle: String?, category: String?): Flow<PagingData<Exercise>>

    suspend fun createExercise(exercise: Exercise): Exercise

    suspend fun updateExercise(exercise: Exercise, workout: Workout? = null)

    suspend fun createGroup(
        groupName: String,
        exerciseNames: List<String>,
    ): ExerciseGroup
    suspend fun updateGroup(group: ExerciseGroup)
    suspend fun updateGroupExercises(group: ExerciseGroup, exerciseNames: List<String>): ExerciseGroup
    suspend fun removeGroup(group: ExerciseGroup)

    suspend fun getGroups(): List<ExerciseGroup>
}