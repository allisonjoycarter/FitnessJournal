package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ExerciseWithSets(
    @Embedded var exercise: Exercise,
    @Relation(
        parentColumn = "eId",
        entity = ExerciseSet::class,
        entityColumn = "exerciseId",
    )
    val exerciseSets: List<ExerciseSet> = emptyList(),
    )

