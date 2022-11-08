package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.*
import com.catscoffeeandkitchen.data.workouts.models.Workout
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlanWithGoals
import com.catscoffeeandkitchen.data.workouts.models.WorkoutWithPlanAndGoals
import java.time.OffsetDateTime

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workout: Workout): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(workout: Workout): Int

    @Transaction
    suspend fun upsert(workout: Workout) {
        val id = insert(workout)
        if (id == -1L) {
            update(workout)
        }
    }

    @Delete
    suspend fun delete(workout: Workout)

    @Transaction
    @Query("""
        SELECT *
        FROM Workout
    """)
    fun getAll(): List<Workout>

    @Query("""
        SELECT *
        FROM Workout
        WHERE completedAt IS NOT NULL
    """)
    suspend fun getAllCompletedWorkouts(): List<Workout>

    @Query("""
        SELECT *
        FROM Workout
        ORDER BY completedAt DESC
        LIMIT 1
    """)
    suspend fun getLastWorkout(): Workout

    @Query("""
        SELECT *
        FROM Workout
        WHERE addedAt = :addedAt
        LIMIT 1
    """)
    suspend fun getWorkoutByAddedAt(addedAt: OffsetDateTime): Workout

    @Transaction
    @Query("""
        SELECT *
        FROM Workout
    """)
    suspend fun getWorkoutsWithPlans(): List<WorkoutWithPlanAndGoals>

}