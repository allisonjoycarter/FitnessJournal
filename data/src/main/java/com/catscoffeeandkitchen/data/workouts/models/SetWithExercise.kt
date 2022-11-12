package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class SetWithExercise(
    @Embedded var set: ExerciseSet,
    @Relation(
        parentColumn = "exerciseId",
        entity = Exercise::class,
        entityColumn = "eId",
    )
    val exercise: Exercise,
    )

