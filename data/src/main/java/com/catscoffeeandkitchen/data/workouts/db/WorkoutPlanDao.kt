package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.*
import com.catscoffeeandkitchen.data.workouts.models.Workout
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlan
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlanWithGoals
import java.time.OffsetDateTime

@Dao
interface WorkoutPlanDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workout: WorkoutPlan): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(workout: WorkoutPlan): Int

    @Transaction
    suspend fun upsert(workout: WorkoutPlan) {
        val id = insert(workout)
        if (id == -1L) {
            update(workout)
        }
    }

    @Delete
    suspend fun delete(workout: WorkoutPlan)

    @Transaction
    @Query("""
        SELECT *
        FROM WorkoutPlan
        ORDER BY addedAt DESC
    """)
    fun getAll(): List<WorkoutPlan>

    @Transaction
    @Query("""
        SELECT *
        FROM WorkoutPlan
        ORDER BY addedAt DESC
    """)
    fun getAllWithGoals(): List<WorkoutPlanWithGoals>

    @Query("""
        SELECT *
        FROM WorkoutPlan
        WHERE addedAt = :addedAt
        LIMIT 1
    """)
    suspend fun getWorkoutPlanByAddedAt(addedAt: OffsetDateTime): WorkoutPlan

//        LEFT JOIN ExerciseGoal ON ExerciseGoal.workoutPlanId = WorkoutPlan.wpId
    @Transaction
    @Query("""
        SELECT *
        FROM WorkoutPlan
        WHERE WorkoutPlan.addedAt = :addedAt
    """)
    fun getWorkoutPlanWithGoalsByAddedAt(addedAt: OffsetDateTime): WorkoutPlanWithGoals

    @Transaction
    @Query("""
        SELECT *
        FROM WorkoutPlan
        WHERE WorkoutPlan.wpId = :planId
    """)
    fun getWorkoutPlanWithGoalsById(planId: Long): WorkoutPlanWithGoals
}