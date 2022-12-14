package com.catscoffeeandkitchen.domain.models

import java.time.OffsetDateTime

data class ExerciseStats(
    val lastCompletedAt: OffsetDateTime? = null,
    val amountCompleted: Int? = null,
    val amountCompletedThisWeek: Int? = null,
    val highestWeightInKilograms: Float? = null,
    val highestWeightInPounds: Float? = null,
)
