package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.searchexercises.SearchExercisesViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PlateCalculatorViewModel @Inject constructor(): ViewModel() {

    data class PlateRequest(
        val weight: Double,
        val plates45: Int = 100,
        val plates35: Int = 100,
        val plates25: Int = 100,
        val plates10: Int = 100,
        val plates5: Int = 100,
        val plates2: Int = 100,
    )

    private val _weight = MutableSharedFlow<PlateRequest>()
    private val _weightRequest: Flow<PlateRequest>
    val weightRequest: Flow<PlateRequest>
        get() = _weightRequest

    private var _plates: MutableStateFlow<Map<Double, Int>> = MutableStateFlow(emptyMap())
    val plates: Flow<Map<Double, Int>>
        get() = _plates

    init {
        _weightRequest = _weight.distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )


        viewModelScope.launch {
            weightRequest.collect { request ->
                _plates.emit(calculatePlates(request))
            }
        }
    }

    fun requestWeight(request: PlateRequest) = viewModelScope.launch {
        _weight.emit(request)
    }

    private fun calculatePlates(request: PlateRequest): Map<Double, Int> {
        val oneSidedWeight = (request.weight - 45) / 2

        return calculateAmountOfPlates(request.copy(weight = oneSidedWeight), 45.0, mapOf())
    }

    private fun calculateAmountOfPlates(request: PlateRequest, plateCalculating: Double, currentPlates: Map<Double, Int>): Map<Double, Int> {
        val platesToCalculate = listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5)
        val nextPlate = platesToCalculate.firstOrNull() { it < plateCalculating }?.toDouble()

        var amountOfPlates = request.weight.toInt() / plateCalculating.toInt()
        val maxPlates = when (plateCalculating) {
            45.0 -> request.plates45
            35.0 -> request.plates35
            25.0 -> request.plates25
            10.0 -> request.plates10
            5.0 -> request.plates5
            2.5 -> request.plates2
            else -> 100
        }

        if (amountOfPlates > maxPlates) {
            amountOfPlates = maxPlates
        }

        val updatedMap = currentPlates + mapOf(plateCalculating to amountOfPlates)
        Timber.d("*** weight = ${request.weight}. calculating $plateCalculating lb plate, " +
                "using $amountOfPlates plates, next plate = $nextPlate. map = $updatedMap")
        return when (nextPlate) {
            null -> currentPlates
            else -> calculateAmountOfPlates(
                request.copy(weight = request.weight - (plateCalculating * amountOfPlates)),
                nextPlate,
                updatedMap
            )
        }
    }

}