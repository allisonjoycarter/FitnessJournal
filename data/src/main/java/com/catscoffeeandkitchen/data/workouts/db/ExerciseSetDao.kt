package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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
        WHERE sId IN (:ids)
    """)
    suspend fun getSetsByIds(ids: List<Long>): List<ExerciseSet>

    @Transaction
    @Query("""
        SELECT *
        FROM ExerciseSet
        WHERE workoutId = :workoutId
    """)
    suspend fun getSetsAndExercisesInWorkout(workoutId: Long): List<SetWithExercise>

    @Query("""
        SELECT *
        FROM ExerciseSet
        WHERE workoutId = :workoutId
    """)
    suspend fun getSetsInWorkout(workoutId: Long): List<ExerciseSet>

    @Transaction
    @Query("""
        SELECT *
        FROM ExerciseSet
        INNER JOIN Workout ON Workout.wId = ExerciseSet.workoutId
        WHERE Workout.completedAt IS NOT NULL
    """)
    suspend fun getAllCompletedSets(): List<SetWithExercise>

    @Transaction
    @Query("""
        SELECT *
        FROM ExerciseSet
        WHERE ExerciseSet.exerciseId = :eId AND ExerciseSet.completedAt IS NOT NULL
    """)
    suspend fun getAllCompletedSetsForExercise(eId: Long): List<SetWithExercise>

    @Query("""
        SELECT *
        FROM ExerciseSet
        WHERE ExerciseSet.completedAt IS NOT NULL AND ExerciseSet.exerciseId = :exerciseId
        ORDER BY ExerciseSet.completedAt DESC
        LIMIT 1
    """)
    fun getLastCompletedSet(exerciseId: Long): ExerciseSet?

    @Query("""
        SELECT *
        FROM ExerciseSet
        WHERE ExerciseSet.exerciseId = :exerciseId
        ORDER BY ExerciseSet.sId DESC
        LIMIT 1
    """)
    fun getLastSet(exerciseId: Long): ExerciseSet?
}