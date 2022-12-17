package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.*
import com.catscoffeeandkitchen.data.workouts.models.*
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionEntity
import com.catscoffeeandkitchen.data.workouts.models.exercise.PositionWithExerciseAndSets

@Dao
interface ExercisePositionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(position: ExercisePositionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(positions: List<ExercisePositionEntity>): List<Long>

    @Update
    suspend fun updateAll(positions: List<ExercisePositionEntity>)

    @Update
    suspend fun update(position: ExercisePositionEntity)

    @Delete
    suspend fun delete(position: ExercisePositionEntity)

    @Query(
        """
        SELECT *
        FROM ExercisePositionEntity
        WHERE workoutId = :workoutId
    """
    )
    suspend fun getPositionsInWorkout(workoutId: Long): List<ExercisePositionEntity>

    @Query("""
        SELECT *
        FROM ExercisePositionEntity
        WHERE planId = :planId
    """)
    suspend fun getPositionsInPlan(planId: Long): List<ExercisePositionEntity>

    @Query(
        """
        SELECT *
        FROM ExercisePositionEntity
        WHERE workoutId = :workoutId 
            AND exerciseId = :exerciseId
    """
    )
    suspend fun getPositionsInWorkoutWithExerciseId(
        workoutId: Long,
        exerciseId: Long
    ): List<ExercisePositionEntity>

    @Transaction
    @Query(
        """
        SELECT *
        FROM ExercisePositionEntity
        WHERE workoutId = :workoutId
    """
    )
    fun getPositionsWithExerciseAndSets(workoutId: Long): List<PositionWithExerciseAndSets>

    @Query("""
        SELECT *
        FROM ExercisePositionEntity
        WHERE epId = :positionId
        LIMIT 1
    """)
    fun getPosition(positionId: Long): ExercisePositionEntity

    @Query("""
        SELECT *
        FROM ExercisePositionEntity
        WHERE planId = :planId AND position = :position
        LIMIT 1
    """)
    fun getPositionInPlanByPosition(planId: Long, position: Int): ExercisePositionEntity
}