package com.catscoffeeandkitchen.data.workouts.db

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.catscoffeeandkitchen.data.workouts.models.WorkoutEntity
import com.catscoffeeandkitchen.data.workouts.models.WorkoutWithPlanAndGoals
import com.catscoffeeandkitchen.data.workouts.models.WorkoutWithSetsAndExercises
import java.time.OffsetDateTime


@Dao
interface WorkoutDao {
    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workout: WorkoutEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(workout: WorkoutEntity): Int

    @Transaction
    suspend fun upsert(workout: WorkoutEntity) {
        val id = insert(workout)
        if (id == -1L) {
            update(workout)
        }
    }

    @Delete
    suspend fun delete(workout: WorkoutEntity)

    @Transaction
    @Query(
        """
        SELECT *
        FROM WorkoutEntity
        ORDER BY addedAt DESC, completedAt DESC
    """
    )
    fun getAll(): List<WorkoutEntity>


    @Transaction
    @Query(
        """
        SELECT *
        FROM WorkoutEntity
        ORDER BY addedAt DESC, completedAt DESC
    """
    )
    fun getAllPaged(): PagingSource<Int, WorkoutWithSetsAndExercises>


    @Query(
        """
        SELECT *
        FROM WorkoutEntity
        WHERE completedAt IS NOT NULL
        ORDER BY addedAt DESC, completedAt DESC
    """
    )
    suspend fun getAllCompletedWorkouts(): List<WorkoutEntity>

    @Query(
        """
        SELECT *
        FROM WorkoutEntity
        WHERE completedAt IS NOT NULL
        ORDER BY completedAt DESC
        LIMIT 1
    """
    )
    suspend fun getLastWorkout(): WorkoutEntity

    @Query(
        """
        SELECT *
        FROM WorkoutEntity
        WHERE addedAt = :addedAt
        LIMIT 1
    """
    )
    suspend fun getWorkoutByAddedAt(addedAt: OffsetDateTime): WorkoutEntity

    @Transaction
    @Query(
        """
        SELECT *
        FROM WorkoutEntity
    """
    )
    suspend fun getWorkoutsWithPlans(): List<WorkoutWithPlanAndGoals>

}