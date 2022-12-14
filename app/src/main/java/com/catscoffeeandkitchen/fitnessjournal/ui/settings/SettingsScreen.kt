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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.R
import com.catscoffeeandkitchen.fitnessjournal.TestTags
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.testTag(TestTags.ScrollableComponent),
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

        item {
            BackupAndRestoreSection(
                showAppClosingDialog = restoreStatus.value is DataState.Success,
                lastBackupDate.value,
                modifier = modifier,
                backupData = { uri ->
                    if (uri != null) {
                        viewModel.backupDataToExternalFile(uri)
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
            val uriHandler = LocalUriHandler.current
            val privacyPolicyUrl = stringResource(id = R.string.privacy_policy_url)

            TextButton(
                modifier = Modifier.padding(start = 8.dp),
                onClick = {
                    uriHandler.openUri(privacyPolicyUrl)
                }
            ) {
                Text("View Privacy Policy")
            }
        }
    }
}

//@Preview
//@Composable
//fun SettingsScreenPreview() {
//
//}