package com.catscoffeeandkitchen.fitnessjournal.ui.settings

import android.content.pm.PackageManager
import android.net.Uri
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
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import timber.log.Timber
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt


@Composable
fun ImportFromCsvSection(
    importStatus: DataState<Double>,
    importFromCsv: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(true) }
    var showImportErrorDialog by remember { mutableStateOf(true) }
    val importCount = (importStatus as? DataState.Success)?.data

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

    val requestReadPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            chooseCsvLauncher.launch("*/*")
        } else {
            Toast.makeText(context, "Cannot import CSV without file access permissions.", Toast.LENGTH_SHORT).show()
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
                when (importStatus.e) {
                    is DateTimeParseException -> Text("Could not read the dates from CSV file.")
                    else -> Text("Could not read CSV.")
                }
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
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        context,
                        "android.permission.READ_EXTERNAL_STORAGE"
                    ) -> {
                        chooseCsvLauncher.launch("*/*")
                    }
                    else -> {
                        // You can directly ask for the permission.
                        // The registered ActivityResultCallback gets the result of this request.
                        requestReadPermissionLauncher.launch(
                            "android.permission.READ_EXTERNAL_STORAGE"
                        )
                    }
                }
            }
        )
    }
}