package com.catscoffeeandkitchen.data.workouts.util

import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.data.workouts.models.SetEntity as DbExerciseSet

fun ExerciseSet.populateWeight(): ExerciseSet {
    return if (this.weightInPounds == 0f && this.weightInKilograms > 0) {
        this.copy(weightInPounds = (this.weightInKilograms * 2.204623).toFloat())
    } else if (this.weightInKilograms == 0f && this.weightInPounds > 0) {
        this.copy(weightInKilograms = (this.weightInPounds * 0.4535924).toFloat())
    } else {
        this
    }
}

fun DbExerciseSet.populateWeight(): DbExerciseSet {
    return if (this.weightInPounds == 0f && this.weightInKilograms > 0) {
        this.copy(weightInPounds = (this.weightInKilograms * 2.204623).toFloat())
    } else if (this.weightInKilograms == 0f && this.weightInPounds > 0) {
        this.copy(weightInKilograms = (this.weightInPounds * 0.4535924).toFloat())
    } else {
        this
    }
}
