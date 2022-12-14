package com.catscoffeeandkitchen.fitnessjournal.ui.settings

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import java.time.OffsetDateTime
import kotlin.system.exitProcess


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val weightUnit = viewModel.weightUnits.collectAsState(initial = WeightUnit.Pounds)

    val restoreStatus = viewModel.restoreStatus.collectAsState(DataState.NotSent())
    val importStatus = viewModel.importStatus.collectAsState(DataState.NotSent())
    val exportStatus = viewModel.exportStatus.collectAsState(DataState.NotSent())
    val lastBackupDate = viewModel.lastBackup.collectAsState(initial = OffsetDateTime.now())
    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(12.dp)
            )
        }

        item {
            UnitsSection(
                weightUnit.value.ordinal,
                onUpdate = { unit ->
                    viewModel.setWeightUnits(if (unit == 0) WeightUnit.Pounds else WeightUnit.Kilograms)
                }
            )
        }

        item {
            BackupAndRestoreSection(
                showAppClosingDialog = restoreStatus.value is DataState.Success,
                lastBackupDate.value,
                modifier = modifier,
                backupData = { file ->
                    if (file != null) {
                        viewModel.backupDataToExternalFile(file)
                    } else {
                        viewModel.backupData()
                    }},
                restoreData = { file ->
                    if (file != null) {
                        viewModel.restoreDataFromFile(file)
                    } else {
                        viewModel.restoreData()
                    }
                },
                closeApp = {
                    val packageManager: PackageManager = context.packageManager

                    val intent: Intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
                    val componentName: ComponentName = intent.component!!
                    val restartIntent: Intent = Intent.makeRestartActivityTask(componentName)

                    restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    if (context is Activity) {
                        context.finishAndRemoveTask()
                    }
                    exitProcess(0)
                }
            )
        }

        item {
            CsvSection(
                importStatus = importStatus.value,
                importFromCsv = { uri ->
                    viewModel.importFromCSV(uri)
                },
                exportStatus = exportStatus.value,
                exportToCsv = { uri ->
                    viewModel.exportToCsv(uri)
                }
            )
        }
    }
}

//@Preview
//@Composable
//fun SettingsScreenPreview() {
//
//}