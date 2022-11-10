package com.catscoffeeandkitchen.domain.usecases.data

import com.catscoffeeandkitchen.domain.interfaces.DataRepository
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RestoreDataUseCase @Inject constructor(
    private val repository: DataRepository
) {
    fun run(): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading())
        repository.restoreData { successful ->
            CoroutineScope(Dispatchers.IO).launch {
                this@flow.emit(DataState.Success(successful))
            }
        }
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

