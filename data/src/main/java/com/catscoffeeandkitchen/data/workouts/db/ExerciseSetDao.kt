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
    suspend fun insert(exerciseSet: SetEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(sets: List<SetEntity>): List<Long>

    @Update
    suspend fun update(exerciseSet: SetEntity)

    @Update(entity = SetEntity::class)
    suspend fun updatePartial(exerciseSet: ExerciseSetPartial)

    @Update(entity = SetEntity::class)
    suspend fun updateAllPartial(sets: List<ExerciseSetPartial>)

    @Update
    suspend fun updateAll(exerciseSet: List<SetEntity>)

    @Query(
        """
        DELETE FROM SetEntity
        WHERE sId = :setId
    """
    )
    suspend fun delete(setId: Long)

    @Delete
    suspend fun deleteAll(sets: List<SetEntity>)

    @Query(
        """
        DELETE 
        FROM SetEntity
        WHERE positionId = :positionId
    """
    )
    suspend fun deleteSetsWithPositionId(positionId: Long)

    @Query("""
        SELECT *
        FROM SetEntity
        WHERE sId = :id
    """)
    fun getSet(id: Long): SetEntity

    @Query(
        """
        SELECT *
        FROM SetEntity
        WHERE sId IN (:ids)
    """
    )
    suspend fun getSetsByIds(ids: List<Long>): List<SetEntity>

    @Transaction
    @Query(
        """
        SELECT *
        FROM SetEntity
        WHERE workoutId = :workoutId
    """
    )
    suspend fun getSetsAndExercisesInWorkout(workoutId: Long): List<SetWithExercise>

    @Query(
        """
        SELECT *
        FROM SetEntity
        WHERE workoutId = :workoutId
    """
    )
    suspend fun getSetsInWorkout(workoutId: Long): List<SetEntity>

    @Transaction
    @Query(
        """
        SELECT *
        FROM SetEntity
        INNER JOIN WorkoutEntity ON WorkoutEntity.wId = SetEntity.workoutId
        WHERE WorkoutEntity.completedAt IS NOT NULL
    """
    )
    suspend fun getAllCompletedSets(): List<SetWithExercise>

    @Transaction
    @Query(
        """
        SELECT *
        FROM SetEntity
        WHERE SetEntity.exerciseId = :eId 
            AND SetEntity.completedAt IS NOT NULL
    """
    )
    suspend fun getAllCompletedSetsForExercise(eId: Long): List<SetWithExercise>

    @Query(
        """
        SELECT *
        FROM SetEntity
        WHERE SetEntity.completedAt IS NOT NULL AND SetEntity.exerciseId = :exerciseId
        ORDER BY SetEntity.completedAt DESC
        LIMIT 1
    """
    )
    fun getLastCompletedSet(exerciseId: Long): SetEntity?

    @Query(
        """
        SELECT *
        FROM SetEntity
        WHERE SetEntity.exerciseId = :exerciseId
        ORDER BY SetEntity.sId DESC
        LIMIT 1
    """
    )
    fun getLastSet(exerciseId: Long): SetEntity?

    @Query(
        """
        SELECT *
        FROM SetEntity
        WHERE exerciseId = :exerciseId AND workoutId = :workoutId
        ORDER BY setNumber DESC
        LIMIT 1
    """
    )
    fun getLastSetOfExerciseInWorkout(exerciseId: Long, workoutId: Long): SetEntity?

    @Query(
        """
            SELECT *
            FROM SetEntity AS t1
            WHERE
                exerciseId = :exerciseId AND
                t1.sId != :bestSetId AND
                completedAt >= :epochMillis AND
                (weightInPounds / (1.0278 - 0.0278 * reps)) = (
                SELECT MIN(weightInPounds / (1.0278 - 0.0278 * reps)) 
                FROM SetEntity AS t2 
                WHERE t2.sId <= t1.sId AND t2.exerciseId = t1.exerciseId
            )
            ORDER BY (
                SELECT MIN(weightInPounds / (1.0278 - 0.0278 * reps)) 
                FROM SetEntity AS t2 
                WHERE t2.sId <= t1.sId AND t2.exerciseId = t1.exerciseId
            ) 
            ASC
            LIMIT 1
        """
    )
    suspend fun getWorstSetSince(epochMillis: Long, exerciseId: Long, bestSetId: Long): SetEntity?

    @Query(
        """
            SELECT *
            FROM SetEntity AS t1
            WHERE completedAt >= :epochMillis
            ORDER BY (
                SELECT MAX(
                    (t1.weightInPounds / (1.0278 - 0.0278 * t1.reps)) - 
                    (t2.weightInPounds / (1.0278 - 0.0278 * t2.reps))
                )   
                FROM SetEntity AS t2 
                WHERE t2.sId <= t1.sId AND 
                    t2.exerciseId = t1.exerciseId AND 
                    t2.completedAt < t1.completedAt
            ) 
            DESC
            LIMIT 1
        """
    )
    suspend fun getBestSetSince(epochMillis: Long): SetEntity?
}