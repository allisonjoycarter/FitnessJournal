package com.catscoffeeandkitchen.data.workouts.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.FileUtils.copy
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.*
import java.time.OffsetDateTime
import javax.inject.Inject

class DatabaseBackupHelper @Inject constructor(
    @ApplicationContext val context: Context,
    val sharedPreferences: SharedPreferences,
    val database: FitnessJournalDb,
) {

    companion object {
        const val FILE_NAME = "database_backup"
        const val MAX_BACKUP_FILES = 5
    }

    fun backup(file: File?) {
        val databaseName = database.openHelper.databaseName

        database.workoutDao().checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
        val dbFile: File = context.getDatabasePath(databaseName)

        val saveFile = when (file) {
            null -> {
                val saveDir = File(context.filesDir.path, "backup")
                val fileName: String = FILE_NAME + System.currentTimeMillis().toString() + ".fjbackup"

                if (!saveDir.exists()) {
                    saveDir.mkdirs()
                } else {
                    // Directory Exists. Delete a file if count is 5 already
                    if ((saveDir.listFiles { f, _ -> f.name.contains(FILE_NAME) }?.size ?: 0) >= MAX_BACKUP_FILES) {
                        val fileToDelete = saveDir.listFiles()
                            ?.minByOrNull { it.name
                                .replace(FILE_NAME, "")
                                .replace(".fjbackup", "")
                                .toLongOrNull() ?: 0L }
                        fileToDelete?.delete()
                    }
                }

                File(saveDir, fileName)
            }
            else -> file
        }

        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                copy(dbFile.inputStream(), saveFile.outputStream())
            } else {
                dbFile.inputStream().use { input ->
                    saveFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

            Timber.d("*** Wrote backup to save file: ${saveFile.absolutePath}")
            database.workoutDao().checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))

            sharedPreferences.edit()
                .putLong("lastDataBackupAt", OffsetDateTime.now().toUTCEpochMilli())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e)
        }
    }


    fun restore(file: File?) {
        val databaseName = database.openHelper.databaseName

        database.close()

        val fileToWrite = when (file) {
            null -> {
                val saveDir = File(context.filesDir.path, "backup")
                saveDir.listFiles()
                    ?.maxByOrNull { it.name.replace(FILE_NAME, "").toLongOrNull() ?: 0L }
                    ?: throw IllegalStateException("Tried to restore the database but no backup file was found.")
            }
            else -> file
        }

        // Copy back database and replace current database
        val source = fileToWrite.inputStream()
        val dbFile: File = context.getDatabasePath(databaseName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            copy(fileToWrite.inputStream(), dbFile.outputStream())
        } else {
            source.use { input ->
                dbFile.outputStream().use { output ->
                    output.write(input.read())
                }
            }
        }

        Timber.d("*** Restored backup from save file: ${fileToWrite.path}")
    }
}