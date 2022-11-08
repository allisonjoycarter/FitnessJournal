package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.searchexercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.usecases.GetPagedExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchExercisesViewModel @Inject constructor(
    private val getPagedExercisesUseCase: GetPagedExercisesUseCase
) : ViewModel() {

    data class ExerciseSearch(
        val name: String? = null,
        val muscle: String? = null,
        val category: String? = null,
    )

    private val _search = MutableSharedFlow<ExerciseSearch>()
    private val _searchRequest: Flow<ExerciseSearch>
    val searchRequest: Flow<ExerciseSearch>
        get() = _searchRequest

    private var _pagedExerciseFlow: Flow<PagingData<Exercise>>
    val pagedExerciseFlow: Flow<PagingData<Exercise>>
        get() = _pagedExerciseFlow

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
        Timber.d("*** searching exercises for $search")
        _search.emit(search)
    }
}