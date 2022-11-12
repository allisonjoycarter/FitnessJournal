package com.catscoffeeandkitchen.data.workouts.db

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.catscoffeeandkitchen.data.workouts.models.ExerciseWithSets
import com.catscoffeeandkitchen.data.workouts.models.Workout
import com.catscoffeeandkitchen.data.workouts.models.WorkoutWithPlanAndGoals
import java.time.OffsetDateTime


@Dao
interface WorkoutDao {
    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int

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
        ORDER BY addedAt DESC, completedAt DESC
    """)
    fun getAll(): List<Workout>


    @Transaction
    @Query("""
        SELECT *
        FROM Workout
        ORDER BY addedAt DESC, completedAt DESC
    """)
    fun getAllPaged(): PagingSource<Int, Workout>


    @Query("""
        SELECT *
        FROM Workout
        WHERE completedAt IS NOT NULL
        ORDER BY addedAt DESC, completedAt DESC
    """)
    suspend fun getAllCompletedWorkouts(): List<Workout>

    @Query("""
        SELECT *
        FROM Workout
        WHERE completedAt IS NOT NULL
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