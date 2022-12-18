package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.*
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlanEntity
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlanWithGoals
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlanWithGoalsAndPosition
import java.time.OffsetDateTime

@Dao
interface WorkoutPlanDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workout: WorkoutPlanEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(workout: WorkoutPlanEntity): Int

    @Transaction
    suspend fun upsert(workout: WorkoutPlanEntity) {
        val id = insert(workout)
        if (id == -1L) {
            update(workout)
        }
    }

    @Delete
    suspend fun delete(workout: WorkoutPlanEntity)

    @Transaction
    @Query(
        """
        SELECT *
        FROM WorkoutPlanEntity
        ORDER BY addedAt DESC
    """
    )
    fun getAll(): List<WorkoutPlanEntity>

    @Transaction
    @Query(
        """
        SELECT *
        FROM WorkoutPlanEntity
        ORDER BY addedAt DESC
    """
    )
    fun getAllWithGoals(): List<WorkoutPlanWithGoals>

    @Query(
        """
        SELECT *
        FROM WorkoutPlanEntity
        WHERE addedAt = :addedAt
        LIMIT 1
    """
    )
    suspend fun getWorkoutPlanByAddedAt(addedAt: OffsetDateTime): WorkoutPlanEntity

    @Transaction
    @Query(
        """
        SELECT *
        FROM WorkoutPlanEntity
        WHERE WorkoutPlanEntity.addedAt = :addedAt
    """
    )
    fun getWithGoalsByAddedAt(addedAt: OffsetDateTime): WorkoutPlanWithGoals
    @Transaction
    @Query(
        """
        SELECT *
        FROM WorkoutPlanEntity
        WHERE WorkoutPlanEntity.addedAt = :addedAt
    """
    )
    fun getWithGoalAndPositionByAddedAt(addedAt: OffsetDateTime): WorkoutPlanWithGoalsAndPosition

    @Transaction
    @Query(
        """
        SELECT *
        FROM WorkoutPlanEntity
        WHERE WorkoutPlanEntity.wpId = :planId
    """
    )
    fun getWorkoutPlanWithGoalsById(planId: Long): WorkoutPlanWithGoals
}