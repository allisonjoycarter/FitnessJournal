package com.catscoffeeandkitchen.fitnessjournal.ui.settings

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileNameDialog(
    onCancel: () -> Unit,
    backupData: (File) -> Unit,
) {
    var fileName by remember { mutableStateOf("Backup") }

    AlertDialog(
        onDismissRequest = { onCancel() },
        text = {
            Column() {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("backup file name")}
                )
                Text(
                    ".fjbackup",
                    style = MaterialTheme.typography.labelSmall,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 6.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val fileToDownloadTo = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "${fileName.trim()}.fjbackup"
                )
                backupData(fileToDownloadTo)
            }) {
                Text("Backup")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onCancel()
            }) {
                Text("Cancel")
            }
        }
    )
}
