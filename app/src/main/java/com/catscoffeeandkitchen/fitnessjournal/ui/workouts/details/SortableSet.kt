package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet

data class SortableSet(
    val setNumberInWorkout: Int,
    val exercise:Exercise
) {
    companion object {
        fun sortExercises(sets: List<ExerciseSet>, expectedSets: List<ExpectedSet>): List<Exercise> {
            return (expectedSets
                .filter { expectedSet -> sets.none { set -> expectedSet.exercise.name == set.exercise.name } }
                .map { SortableSet(it.setNumberInWorkout, it.exercise) } +
                    sets
                        .map { SortableSet(it.setNumberInWorkout, it.exercise) }
            ).sortedBy { it.setNumberInWorkout }.map { it.exercise }
        }
    }
}