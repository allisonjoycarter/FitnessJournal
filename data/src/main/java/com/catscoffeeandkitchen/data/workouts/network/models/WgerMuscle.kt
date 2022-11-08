package com.catscoffeeandkitchen.data.workouts.network.models

enum class WgerMuscle(val number: Int, val coloquial: List<String>) {
    AnteriorDeltoid(number = 2, coloquial = listOf("Anterior Delts")),
    BicepsBrachii(number = 1, coloquial = listOf("Biceps")),
    BicepsFemoris(number = 11, coloquial = listOf("Hamstrings")),
    Brachialis(number = 13, coloquial = listOf("Brachialis")),
    Gastrocnemius(number = 7, coloquial = listOf("Calves")),
    GluteusMaximus(number = 8, coloquial = listOf("Glutes")),
    LatissimusDorsi(number = 12, coloquial = listOf("Lats")),
    ObliquusExternusAbdominis(number = 14, coloquial = listOf("Obliques")),
    PectoralisMajor(number = 4, coloquial = listOf("Pecs")),
    QuadricepsFemoris(number = 10, coloquial = listOf("Quads")),
    RectusAbdominis(number = 6, coloquial = listOf("Abs")),
    SerratusAnterior(number = 3, coloquial = listOf("Serratus Anterior")),
    Soleus(number = 15, coloquial = listOf("Soleus")),
    Trapezius(number = 9, coloquial = listOf("Traps")),
    TricepsBrachii(number = 5, coloquial = listOf("Triceps"));
}