package com.catscoffeeandkitchen.data.workouts.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionInWorkout
import com.catscoffeeandkitchen.data.workouts.util.DatabaseBackupHelper
import com.catscoffeeandkitchen.data.workouts.util.populateWeight
import com.catscoffeeandkitchen.domain.interfaces.DataRepository
import com.catscoffeeandkitchen.domain.models.ExerciseEquipmentType
import com.catscoffeeandkitchen.domain.util.DataState
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.util.MalformedCSVException
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.catscoffeeandkitchen.data.workouts.models.WorkoutEntity as DbWorkout
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity as DbExercise
import com.catscoffeeandkitchen.data.workouts.models.SetEntity as DbExerciseSet

@ActivityScoped
class DataRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val backupHelper: DatabaseBackupHelper,
    private val database: FitnessJournalDb
): DataRepository {
    private data class CSVRowWorkoutData(
        val workout: DbWorkout,
        val exercise: DbExercise,
        val set: DbExerciseSet
    )


    override fun backupData(file: File?) {
        backupHelper.backup(file)
    }

    override fun restoreData(file: File?) {
        backupHelper.restore(file)
    }

    override fun importDataFromCsv(uri: Uri): Flow<DataState<Double>> = flow {
        var counted = 0.0
        emit(DataState.Success(counted))

        var setNumber = 1
        var workout: DbWorkout? = null
        var exercise: DbExercise? = null
        val sets = arrayListOf<DbExerciseSet>()

        val inputStream = context.contentResolver.openInputStream(uri) ?: return@flow

        coroutineScope {
            try {

                csvReader { delimiter = ';' }.openAsync(inputStream) {
                    readAllWithHeaderAsSequence().asFlow().collect { row: Map<String, String> ->
                        val exerciseName = row.entries.find { rowData -> rowData.key == "Exercise Name" }?.value
                        val workoutName = row.entries.find { rowData -> rowData.key == "Workout Name" }?.value

                        var csvData = CSVRowWorkoutData(
                            workout ?: DbWorkout(wId = 0L),
                            exercise ?: DbExercise(eId = 0L, name = ""),
                            DbExerciseSet(sId = 0L, exerciseId = 0L, workoutId = 0L, positionId = 0L, setNumber = setNumber)
                        )

                        if (csvData.exercise.name != exerciseName) {
                            val workoutId = when {
                                csvData.workout.wId == 0L || workoutName != workout?.name -> {
                                    database.workoutDao().insert(csvData.workout.copy(wId = 0L))
                                }
                                else -> csvData.workout.wId
                            }
                            csvData = csvData.copy(workout = csvData.workout.copy(wId = workoutId))

                            val dbExercise = database.exerciseDao().searchExercisesByName(csvData.exercise.name)
                            val exerciseToUpdate = DbExercise(
                                eId = dbExercise?.eId ?: 0L,
                                name = csvData.exercise.name,
                                musclesWorked = dbExercise?.musclesWorked.orEmpty(),
                                userCreated = false,
                                category = dbExercise?.category.orEmpty(),
                                thumbnailUrl = dbExercise?.thumbnailUrl
                            )

                            if (exerciseToUpdate.name.isNotEmpty()) {
                                var exerciseId = exerciseToUpdate.eId
                                if (exerciseToUpdate.eId == 0L) {
                                    exerciseId = database.exerciseDao().insert(exerciseToUpdate)
                                } else {
                                    database.exerciseDao().update(exerciseToUpdate)
                                }

                                val positions = database.exercisePositionDao().getPositionsInWorkout(workoutId)
                                val positionToInsert = ExercisePositionInWorkout(
                                    epId = 0L,
                                    exerciseId = exerciseId,
                                    workoutId = workoutId,
                                    position = positions.size + 1,
                                )
                                val positionId = database.exercisePositionDao().insert(positionToInsert)

                                val setsToInsert = sets.map { item ->
                                    item.copy(
                                        sId = 0L,
                                        workoutId = workoutId,
                                        exerciseId = exerciseId,
                                        positionId = positionId,
                                        completedAt = workout?.completedAt
                                    )
                                }
                                database.exerciseSetDao().insertAll(setsToInsert)
                            }

                            sets.removeAll(sets.toSet())
                        }

                        if (workoutName != workout?.name) {
                            counted += 1.0
                            this@flow.emit(DataState.Success(counted))

                            setNumber = 1
                        } else {
                            setNumber++
                        }

                        try {
                            val newData = getRowData(row, csvData)

                            workout = newData.workout
                            exercise = newData.exercise
                            sets.add(newData.set)
                        } catch (e: Exception) {
                            Timber.e(e)
                            this@flow.emit(DataState.Error(e))
                            this@coroutineScope.cancel()
                        }
                    }
                }
            } catch (e: MalformedCSVException) {
                Timber.e(e)
                emit(DataState.Error(e))
                this@coroutineScope.cancel()
            } catch (e: SQLiteConstraintException) {
                Timber.e(e)
                emit(DataState.Error(e))
                this@coroutineScope.cancel()
            }
        }
        inputStream.close()
        emit(DataState.Success(-1.0))
    }

    private fun getRowData(
        row: Map<String, String>,
        csvRowWorkoutData: CSVRowWorkoutData,
    ): CSVRowWorkoutData {
        var csvRowData = csvRowWorkoutData
        val weight = row.entries.find { it.key == "Weight" }?.value?.toFloatOrNull() ?: 0f
        row.entries.forEach { item ->
            when (item.key) {
                "Date" -> {
                    val date = LocalDateTime.parse( // format 2021-10-20 07:20:11
                        item.value,
                        DateTimeFormatter
                            .ofPattern("uuuu-MM-dd HH:mm:ss")
                    )
                        .atZone(ZoneOffset.systemDefault())
                        .toOffsetDateTime()
                    val updated = csvRowData.workout.copy(completedAt = date, addedAt = date)
                    csvRowData = csvRowData.copy(workout = updated)
                }
                "Workout Name" -> {
                    val updated = csvRowData.workout.copy(name = item.value)
                    csvRowData = csvRowData.copy(workout = updated)
                }
                "Exercise Name" -> {
                    val equipment = when {
                        item.value.lowercase().contains("barbell") -> ExerciseEquipmentType.Barbell
                        item.value.lowercase().contains("dumbbell") -> ExerciseEquipmentType.Dumbbell
                        item.value.lowercase().contains("machine") -> ExerciseEquipmentType.Machine
                        item.value.lowercase().contains("cable") -> ExerciseEquipmentType.Cable
                        item.value.lowercase().contains("bodyweight") -> ExerciseEquipmentType.Bodyweight
                        else -> ExerciseEquipmentType.Bodyweight
                    }
                    val updated = csvRowData.exercise.copy(
                        name = item.value,
                        equipmentType = equipment
                    )
                    csvRowData = csvRowData.copy(exercise = updated)
                }
                "Set Order" -> {
                    val updated = csvRowData.set.copy(setNumber = item.value.toIntOrNull() ?: 1)
                    csvRowData = csvRowData.copy(set = updated)
                }
                "Weight" -> {
                }
                "Weight Unit" -> {
                    val updated = if (item.value == "lbs") {
                        csvRowData.set.copy(weightInPounds = weight).populateWeight()
                    } else {
                        csvRowData.set.copy(weightInKilograms = weight).populateWeight()
                    }
                    csvRowData = csvRowData.copy(set = updated)
                }
                "Reps" -> {
                    val updated = csvRowData.set.copy(reps = item.value.toIntOrNull() ?: 0)
                    csvRowData = csvRowData.copy(set = updated)
                }
                "RPE" -> {
                    val updated = csvRowData.set.copy(perceivedExertion = item.value.toIntOrNull() ?: 0)
                    csvRowData = csvRowData.copy(set = updated)
                }
                "Distance" -> { }
                "Distance Unit" -> { }
                "Seconds" -> { }
                "Notes" -> { }
                "Workout Notes" -> {
                    val updated = csvRowData.workout.copy(note = item.value.ifEmpty { null })
                    csvRowData = csvRowData.copy(workout = updated)
                }
            }
        }
        return csvRowData
    }
}