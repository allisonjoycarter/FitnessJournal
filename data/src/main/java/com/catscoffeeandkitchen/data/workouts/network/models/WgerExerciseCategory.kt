package com.catscoffeeandkitchen.data.workouts.network.models

enum class WgerExerciseCategory(val number: Int, val coloquial: List<String>) {
    Abs(number = 10, emptyList()),
    Arms(number = 8, emptyList()),
    Back(number = 12, emptyList()),
    Calves(number = 14, emptyList()),
    Cardio(number = 15, emptyList()),
    Chest(number = 11, emptyList()),
    Legs(number = 9, emptyList()),
    Shoulders(number = 13, emptyList()),
    Unknown(number = 0, emptyList());
}