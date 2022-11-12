package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.plates

import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit

class PlateCalculatorHelper {
    data class PlateSettings(
        val amounts: Map<Double, Int>,
        val unit: WeightUnit,
    )
    private val poundPlates = listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5)
    private val kgPlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

    fun calculatePlates(weight: Double, settings: PlateSettings): Map<Double, Int> {
        val barWeight = if (settings.unit == WeightUnit.Pounds) 45 else 20
        val plates = if (settings.unit == WeightUnit.Pounds) poundPlates else kgPlates

        val oneSidedWeight = (weight - barWeight) / 2

        return calculateAmountOfPlates(oneSidedWeight, settings, plates, 0, mapOf())
    }

    private fun calculateAmountOfPlates(
        weight: Double,
        settings: PlateSettings,
        plateOptions: List<Double>,
        plateIndex: Int,
        currentPlates: Map<Double, Int>
    ): Map<Double, Int> {
        val plateCalculating = plateOptions[plateIndex]

        var amountOfPlates = weight.toInt() / plateCalculating.toInt()
        val maxPlates = settings.amounts.getOrDefault(plateCalculating, 100)

        if (amountOfPlates > maxPlates) {
            amountOfPlates = maxPlates
        }

        val updatedMap = currentPlates + mapOf(plateCalculating to amountOfPlates)
//        Timber.d("*** weight = ${weight}. calculating $plateCalculating lb plate, " +
//                "using $amountOfPlates plates, next plate = $nextPlate. map = $updatedMap")
        return when (plateIndex + 1 >= plateOptions.size) {
            true -> updatedMap
            else -> calculateAmountOfPlates(
                weight - (plateCalculating * amountOfPlates),
                settings,
                plateOptions,
                plateIndex + 1,
                updatedMap
            )
        }
    }

}