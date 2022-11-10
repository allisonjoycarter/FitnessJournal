package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout

import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExerciseSetType

enum class ExerciseSetField {
    Reps,
    WeightInPounds,
    RepsInReserve,
    PerceivedExertion,
    Type,
    Complete;

    fun getSetValue(set: ExerciseSet): Int {
        return when (this) {
            Reps -> set.reps
            WeightInPounds -> set.weightInPounds
            RepsInReserve -> set.repsInReserve
            PerceivedExertion -> set.perceivedExertion
            Type -> set.type.ordinal
            Complete -> if (set.isComplete) 1 else 0
        }
    }

    fun copySetWithNewValue(
        set: ExerciseSet,
        value: Int,
    ): ExerciseSet {
        return when (this) {
            Reps -> {
                set.copy(reps = value)
            }
            WeightInPounds -> {
                set.copy(weightInPounds = value)
            }
            RepsInReserve -> {
                set.copy(repsInReserve = value)
            }
            PerceivedExertion -> {
                set.copy(perceivedExertion = value)
            }
            Complete -> {
                set.copy(isComplete = value == 1)
            }
            Type -> set.copy(type = ExerciseSetType.values().find { it.ordinal == value}
                ?: ExerciseSetType.Working)
        }
    }
}
