package com.catscoffeeandkitchen.domain.usecases.data

import android.net.Uri
import com.catscoffeeandkitchen.domain.interfaces.DataRepository
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class ExportToCsvDataUseCase @Inject constructor(
    private val repository: DataRepository
) {
    fun run(saveLocation: Uri): Flow<DataState<Int>> = repository.writeDataToCsv(saveLocation)
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

