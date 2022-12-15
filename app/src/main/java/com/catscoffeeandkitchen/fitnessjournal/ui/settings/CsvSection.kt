package com.catscoffeeandkitchen.fitnessjournal.ui.settings

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.catscoffeeandkitchen.domain.models.csv.CsvFormatError
import com.catscoffeeandkitchen.domain.models.csv.DatabaseError
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.BuildConfig
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import timber.log.Timber
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt


@Composable
fun CsvSection(
    importStatus: DataState<Double>,
    exportStatus: DataState<Int>,
    importFromCsv: (Uri) -> Unit,
    exportToCsv: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(true) }
    var showExportDialog by remember { mutableStateOf(true) }
    var showImportErrorDialog by remember { mutableStateOf(true) }
    var showExportErrorDialog by remember { mutableStateOf(true) }
    val importCount = (importStatus as? DataState.Success)?.data
    val exportData = (exportStatus as? DataState.Success)?.data

    val chooseCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                importFromCsv(uri)
            } catch (ex: Exception) {
                Toast.makeText(context, "There was a problem getting that file.", Toast.LENGTH_SHORT).show()
                Timber.e(ex)
            }
        }
    }
    val chooseSaveLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                exportToCsv(uri)
            } catch (ex: Exception) {
                Toast.makeText(context, "There was a problem getting that location.", Toast.LENGTH_SHORT).show()
                Timber.e(ex)
            }
        }
    }

    val requestReadPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            chooseCsvLauncher.launch("*/*")
        } else {
            Toast.makeText(context, "Cannot import CSV without file access permissions.", Toast.LENGTH_SHORT).show()
        }
    }

    val requestWritePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            chooseSaveLocationLauncher.launch("text/csv")
        } else {
            Toast.makeText(context, "Cannot export as CSV without file write permissions.", Toast.LENGTH_SHORT).show()
        }
    }

    if (importCount != null && importCount > 0.0) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { },
            dismissButton = {},
            title = { Text("Importing Data") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    (importStatus as? DataState.Success)?.data?.roundToInt()?.let { importCount ->
                        Text("Imported $importCount workouts")
                    }
                }
            }
        )
    } else if (showImportDialog && importCount != null && importCount <= 0) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            confirmButton = {
                TextButton(onClick = { showImportDialog = false} ) {
                    Text("OK")
                }
            },
            dismissButton = {},
            title = { Text("Importing Data") },
            text = {
                Text("Import completed!")
            }
        )
    } else if (showImportErrorDialog && importStatus is DataState.Error) {
        val errorDescription = when (importStatus.e) {
            is CsvFormatError -> "The format of the CSV could not be read."
            is DatabaseError -> "Could not merge CSV data with existing data."
            is DateTimeParseException -> "Could not read the dates from CSV file."
            else -> "Could not read CSV."
        }

        AlertDialog(
            onDismissRequest = { showImportErrorDialog = false },
            confirmButton = {
                TextButton(onClick = { showImportErrorDialog = false } ) {
                    Text("OK")
                }
            },
            dismissButton = {},
            title = { Text("Import Error") },
            text = {
                Text(errorDescription)
            }
        )

    } else if (exportData != null && exportData > 0) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { },
            dismissButton = {},
            title = { Text("Exporting Data") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    (exportStatus as? DataState.Success)?.data?.let { exportCount ->
                        Text("Exported $exportCount sets")
                    }
                }
            }
        )
    } else if (showExportErrorDialog && exportStatus is DataState.Error) {
        AlertDialog(
            onDismissRequest = { showExportErrorDialog = false },
            confirmButton = {
                TextButton(onClick = { showExportErrorDialog = false }) {
                    Text("OK")
                }
            },
            dismissButton = {},
            title = { Text("Export Error") },
            text = {
                Text("There was a problem exporting your workouts.")
            }
        )
    } else if (showExportDialog && exportData != null) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false} ) {
                    Text("OK")
                }
            },
            dismissButton = {},
            title = { Text("Exporting Data") },
            text = {
                Text("Export completed!")
            }
        )
    }

        Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        FitnessJournalButton(
            "Import from CSV",
            fullWidth = true,
            onClick = {
                val permission = when {
                    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> "READ_MEDIA_IMAGES"
                    else -> "WRITE_EXTERNAL_STORAGE"
                }

                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        context,
                        "android.permission.$permission"
                    ) -> {
                        chooseCsvLauncher.launch("*/*")
                    }
                    else -> {
                        // You can directly ask for the permission.
                        // The registered ActivityResultCallback gets the result of this request.
                        requestReadPermissionLauncher.launch(
                            "android.permission.$permission"
                        )
                    }
                }
            }
        )

        FitnessJournalButton(
            "Export to CSV",
            fullWidth = true,
            onClick = {
                val permission = when {
                    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> "READ_MEDIA_IMAGES"
                    else -> "WRITE_EXTERNAL_STORAGE"
                }

                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        context,
                        "android.permission.$permission"
                    ) -> {
                        chooseSaveLocationLauncher.launch("lifting_log.csv")
                    }
                    else -> {
                        // You can directly ask for the permission.
                        // The registered ActivityResultCallback gets the result of this request.
                        requestWritePermissionLauncher.launch(
                            "android.permission.$permission"
                        )
                    }
                }
            }
        )


        Text(
            "Importing and exporting CSVs will only affect workouts, NOT plans or exercise groups.",
            style = MaterialTheme.typography.labelLarge
        )
    }
}