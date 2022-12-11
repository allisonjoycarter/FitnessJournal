package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.searchexercises

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.usecases.CreateExercisesUseCase
import com.catscoffeeandkitchen.domain.usecases.GetPagedExercisesUseCase
import com.catscoffeeandkitchen.domain.usecases.UpdateExerciseUseCase
import com.catscoffeeandkitchen.domain.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchExercisesViewModel @Inject constructor(
    private val getPagedExercisesUseCase: GetPagedExercisesUseCase,
    private val createExerciseUseCase: CreateExercisesUseCase,
    private val updateExerciseUseCase: UpdateExerciseUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _search = MutableSharedFlow<ExerciseSearch>()
    private val _searchRequest: Flow<ExerciseSearch>
    val searchRequest: Flow<ExerciseSearch>
        get() = _searchRequest

    private var _pagedExerciseFlow: Flow<PagingData<Exercise>>
    val pagedExerciseFlow: Flow<PagingData<Exercise>>
        get() = _pagedExerciseFlow

    private var _creatingExercise: MutableState<DataState<Exercise>> = mutableStateOf(DataState.NotSent())
    val creatingExercise: State<DataState<Exercise>> = _creatingExercise

    init {
        _searchRequest = _search.distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )

        _pagedExerciseFlow = searchRequest.flatMapLatest { request ->
            getPagedExercisesUseCase.run(request.name, request.muscle, request.category)
        }
            .cachedIn(viewModelScope)
    }

    fun searchExercises(search: ExerciseSearch) = viewModelScope.launch {
        _search.emit(search)
    }

    fun createExercise(exercise: Exercise, onSuccess: () -> Unit) = viewModelScope.launch {
        createExerciseUseCase.run(exercise).collect { state ->
            _creatingExercise.value = state
            if (state is DataState.Success) {
                onSuccess()
            }
        }
    }

    fun updateExercise(exercise: Exercise, refreshItems: () -> Unit) = viewModelScope.launch {
        updateExerciseUseCase.run(exercise).collect { state ->
            if (state is DataState.Success) {
                refreshItems()
            }
        }
    }
}