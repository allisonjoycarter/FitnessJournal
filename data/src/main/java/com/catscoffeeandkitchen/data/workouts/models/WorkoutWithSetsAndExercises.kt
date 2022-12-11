package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutWithSetsAndExercises(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        parentColumn = "wId",
        entity = SetEntity::class,
        entityColumn = "workoutId"
    )
    val sets: List<SetWithExerciseAndPosition> = emptyList(),
)
