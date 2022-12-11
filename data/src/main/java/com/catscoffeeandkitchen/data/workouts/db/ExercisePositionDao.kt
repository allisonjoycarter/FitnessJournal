package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.*
import com.catscoffeeandkitchen.data.workouts.models.*
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionInWorkout
import com.catscoffeeandkitchen.data.workouts.models.exercise.PositionWithExerciseAndSets

@Dao
interface ExercisePositionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(position: ExercisePositionInWorkout): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(positions: List<ExercisePositionInWorkout>): List<Long>

    @Update
    suspend fun updateAll(positions: List<ExercisePositionInWorkout>)

    @Update
    suspend fun update(position: ExercisePositionInWorkout)

    @Delete
    suspend fun delete(position: ExercisePositionInWorkout)

    @Query("""
        SELECT *
        FROM ExercisePositionInWorkout
        WHERE workoutId = :workoutId
    """)
    suspend fun getPositionsInWorkout(workoutId: Long): List<ExercisePositionInWorkout>


    @Query("""
        SELECT *
        FROM ExercisePositionInWorkout
        WHERE workoutId = :workoutId 
            AND exerciseId = :exerciseId
    """)
    suspend fun getPositionsInWorkoutWithExerciseId(
        workoutId: Long,
        exerciseId: Long
    ): List<ExercisePositionInWorkout>

    @Transaction
    @Query("""
        SELECT *
        FROM ExercisePositionInWorkout
        WHERE workoutId = :workoutId
    """)
    fun getPositionsWithExerciseAndSets(workoutId: Long): List<PositionWithExerciseAndSets>
}