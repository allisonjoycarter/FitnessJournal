package com.catscoffeeandkitchen.domain.interfaces

import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import java.time.OffsetDateTime

interface ExerciseRepository {
    suspend fun createGroup(
        groupName: String,
        exerciseNames: List<String>,
    ): ExerciseGroup
    suspend fun updateGroup(group: ExerciseGroup)
    suspend fun updateGroupExercises(group: ExerciseGroup, exerciseNames: List<String>): ExerciseGroup
    suspend fun removeGroup(group: ExerciseGroup)

    suspend fun getGroups(): List<ExerciseGroup>
}