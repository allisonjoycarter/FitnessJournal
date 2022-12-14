package com.catscoffeeandkitchen.domain.usecases.data

import android.net.Uri
import com.catscoffeeandkitchen.domain.interfaces.DataRepository
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class BackupDataUseCase @Inject constructor(
    private val repository: DataRepository
) {

    fun run(uri: Uri? = null): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading())
        repository.backupData(uri)
        emit(DataState.Success(true))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

