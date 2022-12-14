package com.catscoffeeandkitchen.domain.interfaces

import android.net.Uri
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.flow.Flow
import java.io.File

interface DataRepository {
    fun backupData(uri: Uri? = null)
    fun importDataFromCsv(uri: Uri): Flow<DataState<Double>>
    fun writeDataToCsv(saveLocation: Uri): Flow<DataState<Int>>
    fun restoreData(file: File? = null)
}