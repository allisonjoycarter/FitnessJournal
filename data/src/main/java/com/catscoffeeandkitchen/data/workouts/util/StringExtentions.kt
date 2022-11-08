package com.catscoffeeandkitchen.data.workouts.util

fun String?.capitalizeWords(): String? {
    return this
        ?.split(" ")
        ?.joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } }
}
