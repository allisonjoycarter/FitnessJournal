package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.catscoffeeandkitchen.data.workouts.models.*

@Dao
interface ExerciseSetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exerciseSet: ExerciseSet): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(sets: List<ExerciseSet>): List<Long>

    @Update
    suspend fun update(exerciseSet: ExerciseSet)

    @Update(entity = ExerciseSet::class)
    suspend fun updatePartial(exerciseSet: ExerciseSetPartial)

    @Update
    suspend fun updateAll(exerciseSet: List<ExerciseSet>)

    @Query("""
        DELETE FROM ExerciseSet
        WHERE sId = :setId
    """)
    suspend fun delete(setId: Long)

    @Delete
    suspend fun deleteAll(sets: List<ExerciseSet>)

    @Query("""
        SELECT *
        FROM ExerciseSet
    """)
    suspend fun getAllSets(): List<ExerciseSet>

    @Query("""
        SELECT *
        FROM ExerciseSet
        LEFT JOIN Exercise ON Exercise.eId = ExerciseSet.exerciseId
        WHERE workoutId = :workoutId
    """)
    suspend fun getSetsAndExercisesInWorkout(workoutId: Long): List<SetAndExerciseCombined>

    @Query("""
        SELECT *
        FROM ExerciseSet
        WHERE workoutId = :workoutId
    """)
    suspend fun getSetsInWorkout(workoutId: Long): List<ExerciseSet>

    @Query("""
        SELECT *
        FROM ExerciseSet
        LEFT JOIN Exercise ON Exercise.eId = ExerciseSet.exerciseId
        INNER JOIN Workout ON Workout.wId = ExerciseSet.workoutId
        WHERE Workout.completedAt IS NOT NULL
    """)
    suspend fun getAllCompletedSets(): List<SetAndExerciseCombined>

    @Query("""
        SELECT *
        FROM ExerciseSet
        LEFT JOIN Exercise ON Exercise.eId = ExerciseSet.exerciseId
        LEFT JOIN Workout ON Workout.wId = ExerciseSet.workoutId
        WHERE Exercise.name = :exerciseName AND Workout.completedAt IS NOT NULL
    """)
    suspend fun getAllCompletedSetsForExercise(exerciseName: String): List<SetAndExerciseCombinedWithCompletedAt>
}