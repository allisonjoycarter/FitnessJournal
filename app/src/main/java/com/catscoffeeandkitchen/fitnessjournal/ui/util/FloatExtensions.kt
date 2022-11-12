package com.catscoffeeandkitchen.fitnessjournal.ui.util

fun Float.toCleanString(): String {
    return this.toString().replace(".0", "")
}