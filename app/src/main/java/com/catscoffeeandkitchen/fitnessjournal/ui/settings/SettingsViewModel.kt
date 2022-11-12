package com.catscoffeeandkitchen.fitnessjournal.ui.settings

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catscoffeeandkitchen.domain.usecases.data.BackupDataUseCase
import com.catscoffeeandkitchen.domain.usecases.data.ImportFromCsvDataUseCase
import com.catscoffeeandkitchen.domain.usecases.data.RestoreDataUseCase
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.util.SharedPrefsConstants.WeightUnitKey
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupDataUseCase: BackupDataUseCase,
    private val restoreDataUseCase: RestoreDataUseCase,
    private val importFromCsvDataUseCase: ImportFromCsvDataUseCase,
    private val sharedPrefs: SharedPreferences,
): ViewModel() {

    private var _weightUnits: MutableStateFlow<WeightUnit> = MutableStateFlow(WeightUnit.Pounds)
    val weightUnits: Flow<WeightUnit> = _weightUnits

    private var _backupStatus: MutableStateFlow<DataState<Boolean>> = MutableStateFlow(DataState.NotSent())
    val backupStatus: Flow<DataState<Boolean>> = _backupStatus

    private var _restoreStatus: MutableStateFlow<DataState<Boolean>> = MutableStateFlow(DataState.NotSent())
    val restoreStatus: Flow<DataState<Boolean>> = _restoreStatus

    private var _importStatus: MutableStateFlow<DataState<Double>> = MutableStateFlow(DataState.NotSent())
    val importStatus: Flow<DataState<Double>> = _importStatus

    private var _lastBackup: MutableStateFlow<OffsetDateTime> = MutableStateFlow(OffsetDateTime.now())
    val lastBackup: Flow<OffsetDateTime> = _lastBackup

    init {
        val backupAt = sharedPrefs.getLong("lastDataBackupAt", OffsetDateTime.now().toInstant().toEpochMilli())
        _lastBackup.value = OffsetDateTime.ofInstant(Instant.ofEpochMilli(backupAt), ZoneOffset.systemDefault())

        val savedWeightUnit = sharedPrefs.getInt(WeightUnitKey, 0)
        _weightUnits.value = WeightUnit.values().firstOrNull { it.ordinal == savedWeightUnit } ?: WeightUnit.Pounds
    }

    fun backupData() = viewModelScope.launch {
        backupDataUseCase.run().collect { state ->
            _backupStatus.value = state
        }
    }

    fun restoreData() = viewModelScope.launch {
        restoreDataUseCase.run().collect { state ->
            _restoreStatus.value = state
        }
    }

    fun backupDataToExternalFile(file: File) = viewModelScope.launch {
        backupDataUseCase.run(file).collect { state ->
            _backupStatus.value = state
        }
    }

    fun restoreDataFromFile(file: File) = viewModelScope.launch {
        restoreDataUseCase.run(file).collect { state ->
            _restoreStatus.value = state
        }
    }

    fun importFromCSV(uri: Uri) = viewModelScope.launch {
        importFromCsvDataUseCase.run(uri).collect { imported ->
            Timber.d("*** import state $imported")
            _importStatus.value = imported
        }
    }

    fun setWeightUnits(weight: WeightUnit) = viewModelScope.launch {
        _weightUnits.value = weight
        sharedPrefs.edit().putInt(WeightUnitKey, weight.ordinal).apply()
    }
}