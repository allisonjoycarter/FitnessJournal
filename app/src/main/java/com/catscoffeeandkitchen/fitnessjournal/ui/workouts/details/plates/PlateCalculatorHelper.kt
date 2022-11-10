package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout.plates

import timber.log.Timber

class PlateCalculatorHelper {
    data class PlateSettings(
        val plates45: Int = 100,
        val plates35: Int = 100,
        val plates25: Int = 100,
        val plates10: Int = 100,
        val plates5: Int = 100,
        val plates2: Int = 100,
    )

    fun calculatePlates(weight: Double, settings: PlateSettings): Map<Double, Int> {
        val oneSidedWeight = (weight - 45) / 2

        return calculateAmountOfPlates(oneSidedWeight, settings, 45.0, mapOf())
    }

    private fun calculateAmountOfPlates(
        weight: Double,
        settings: PlateSettings,
        plateCalculating: Double,
        currentPlates: Map<Double, Int>
    ): Map<Double, Int> {
        val platesToCalculate = listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5)
        val nextPlate = platesToCalculate.firstOrNull() { it < plateCalculating }?.toDouble()

        var amountOfPlates = weight.toInt() / plateCalculating.toInt()
        val maxPlates = when (plateCalculating) {
            45.0 -> settings.plates45
            35.0 -> settings.plates35
            25.0 -> settings.plates25
            10.0 -> settings.plates10
            5.0 -> settings.plates5
            2.5 -> settings.plates2
            else -> 100
        }

        if (amountOfPlates > maxPlates) {
            amountOfPlates = maxPlates
        }

        val updatedMap = currentPlates + mapOf(plateCalculating to amountOfPlates)
        Timber.d("*** weight = ${weight}. calculating $plateCalculating lb plate, " +
                "using $amountOfPlates plates, next plate = $nextPlate. map = $updatedMap")
        return when (nextPlate) {
            null -> updatedMap
            else -> calculateAmountOfPlates(
                weight - (plateCalculating * amountOfPlates),
                settings,
                nextPlate,
                updatedMap
            )
        }
    }

}