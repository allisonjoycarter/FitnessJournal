package com.catscoffeeandkitchen.data.workouts.db

import androidx.paging.PagingSource
import androidx.room.*
import com.catscoffeeandkitchen.data.workouts.models.*
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseWithPositionCount
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseWithStats

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>): List<Long>

    @Update
    suspend fun updateAll(exercises: List<ExerciseEntity>)

    @Update
    suspend fun update(exercises: ExerciseEntity)

    @Delete
    suspend fun delete(exercise: ExerciseEntity)

    @Query(
        """
        DELETE FROM ExerciseEntity
        WHERE NOT ExerciseEntity.userCreated
    """
    )
    suspend fun clearRemoteExercises()

    @Query(
        """
        SELECT *, COUNT(ExercisePositionInWorkout.exerciseId) AS amountPerformed
        FROM ExerciseEntity
        LEFT JOIN ExercisePositionInWorkout ON ExercisePositionInWorkout.exerciseId = ExerciseEntity.eId
        GROUP BY ExerciseEntity.eId
        HAVING amountPerformed > 0
        ORDER BY amountPerformed DESC
    """
    )
    suspend fun getAllExercises(): List<ExerciseWithPositionCount>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT *, COUNT(SetEntity.sId) AS NumberOfSets
        FROM ExerciseEntity
        LEFT JOIN SetEntity ON ExerciseEntity.eId = SetEntity.exerciseId
        WHERE (
                :exerciseName <> ''
                AND ExerciseEntity.name LIKE '%' || :exerciseName || '%'
            )
            OR (
                musclesWorked <> '' 
                AND :exerciseName <> ''
                AND musclesWorked LIKE '%' || :exerciseName || '%'
            )
            OR (
                musclesWorked <> ''
                AND :muscle <> ''
                AND musclesWorked LIKE '%' || :muscle || '%'
            )
            OR (
                category <> ''
                AND :category <> ''
                AND category LIKE '%' || :category || '%'
            )
        GROUP BY ExerciseEntity.name
        ORDER BY NumberOfSets DESC, ExerciseEntity.name ASC
    """
    )
    fun getPagedExercisesByName(exerciseName: String, muscle: String, category: String): PagingSource<Int, ExerciseWithSets>


    @Query(
        """
        SELECT *
        FROM ExerciseEntity
        WHERE ExerciseEntity.name LIKE '%' || :exerciseName || '%'
        LIMIT 1
    """
    )
    fun searchExercisesByName(exerciseName: String): ExerciseEntity?

    @Transaction
    @Query(
        """
        SELECT *
        FROM ExerciseEntity
        ORDER BY ExerciseEntity.name ASC
    """
    )
    fun getAllPagedExercises(): PagingSource<Int, ExerciseWithSets>

    @Query(
        """
        SELECT *
        FROM ExerciseEntity
        WHERE eId IN (:ids)
    """
    )
    suspend fun getExercisesByIds(ids: List<Long>): List<ExerciseEntity>

    @Query(
        """
        SELECT *
        FROM ExerciseEntity
        WHERE name IN (:names)
    """
    )
    suspend fun getExercisesByName(names: List<String>): List<ExerciseEntity>

    @Transaction
    @Query(
        """
        SELECT *, 
            COUNT(SetEntity.completedAt) AS amountPerformed,
            MAX(SetEntity.completedAt) as lastCompletedAt,
            MAX(SetEntity.weightInKilograms) as highestWeightInKilograms,
            MAX(SetEntity.weightInPounds) as highestWeightInPounds
        FROM ExerciseEntity
        LEFT JOIN SetEntity ON SetEntity.exerciseId = ExerciseEntity.eId
        WHERE name IN (:names)
        GROUP BY ExerciseEntity.eId
    """
    )
    suspend fun getExercisesWithStatsByName(names: List<String>): List<ExerciseWithStats>

    @Query(
        """
        SELECT *
        FROM ExerciseEntity
        WHERE eId = :eId
    """
    )
    suspend fun getExerciseById(eId: Long): ExerciseEntity

    @Query(
        """
        SELECT *
        FROM ExerciseEntity
        WHERE ExerciseEntity.name=:name
        LIMIT 1
    """
    )
    suspend fun getExerciseByName(name: String): ExerciseEntity?


    @Transaction
    @Query(
        """
        SELECT *
        FROM ExerciseEntity
    """
    )
    fun getExercisesWithSets(): List<ExerciseWithSets>
}