package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import com.catscoffeeandkitchen.data.workouts.util.populateWeight
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.math.roundToInt

sealed class ExerciseSetField(val value: Any?) {
    class Reps(value: Int): ExerciseSetField(value)
    class WeightInPounds(value: Float): ExerciseSetField(value)
    class WeightInKilograms(value: Float): ExerciseSetField(value)
    class RepsInReserve(value: Int): ExerciseSetField(value)
    class PerceivedExertion(value: Int): ExerciseSetField(value)
    class Type(value: ExerciseSetType): ExerciseSetField(value)
    class Complete(value: OffsetDateTime?): ExerciseSetField(value)

    fun copySetWithNewValue(
        set: ExerciseSet,
    ): ExerciseSet {
        return when (this) {
            is Reps -> {
                set.copy(reps = value as Int)
            }
            is WeightInPounds -> {
                set.copy(
                    weightInPounds = value as Float,
                    weightInKilograms = (value * 0.4535924f)
                )
            }
            is WeightInKilograms -> {
                set.copy(
                    weightInKilograms = value as Float,
                    weightInPounds = (value * 2.204623f)
                )
            }
            is RepsInReserve -> {
                set.copy(repsInReserve = value as Int)
            }
            is PerceivedExertion -> {
                set.copy(perceivedExertion = value as Int)
            }
            is Complete -> {
                set.copy(completedAt = value as OffsetDateTime?, isComplete = value != null)
            }
            is Type -> set.copy(type = value as ExerciseSetType)
        }
    }
}
