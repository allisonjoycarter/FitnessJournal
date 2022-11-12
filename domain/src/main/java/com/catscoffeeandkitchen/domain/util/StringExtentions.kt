package com.catscoffeeandkitchen.domain.util

fun String?.capitalizeWords(): String? {
    return this
        ?.split(" ")
        ?.joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } }
}
