package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Embedded
import androidx.room.Relation
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionInWorkout

data class SetWithExerciseAndPosition(
    @Embedded var set: SetEntity,
    @Relation(
        parentColumn = "exerciseId",
        entity = ExerciseEntity::class,
        entityColumn = "eId",
    )
    val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "positionId",
        entity = ExercisePositionInWorkout::class,
        entityColumn = "epId",
    )
    val positionInWorkout: ExercisePositionInWorkout
)

