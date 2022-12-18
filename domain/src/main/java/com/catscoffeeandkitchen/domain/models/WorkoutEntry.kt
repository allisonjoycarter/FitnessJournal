package com.catscoffeeandkitchen.domain.models

data class WorkoutEntry(
    val position: Int,
    val exercise: Exercise? = null,
    val expectedSet: ExpectedSet? = null,
    val sets: List<ExerciseSet> = emptyList(),
) {
    val name: String
        get() = exercise?.name
            ?: expectedSet?.exercise?.name
            ?: expectedSet?.exerciseGroup?.name
            ?: ""

    override fun toString(): String {
        return "Workout Entry(\n position = $position " +
                "\n exercise = $exercise " +
                "\n expected set = $expectedSet " +
                "\n amount of sets = ${sets.size}" +
                "\n)"

    }
}
