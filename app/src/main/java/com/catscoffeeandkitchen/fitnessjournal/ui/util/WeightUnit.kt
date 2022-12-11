package com.catscoffeeandkitchen.fitnessjournal.ui.util

enum class WeightUnit
{
    Pounds {
        override fun toAbbreviation(): String {
            return "lbs"
        }
    },
    Kilograms {
        override fun toAbbreviation(): String {
            return "kg"
        }
    };

    abstract fun toAbbreviation(): String
}