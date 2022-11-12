package com.catscoffeeandkitchen.data.workouts.db

import androidx.paging.PagingSource
import androidx.room.*
import com.catscoffeeandkitchen.data.workouts.models.*

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: Exercise): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<Exercise>): List<Long>

    @Update
    suspend fun updateAll(exercises: List<Exercise>)

    @Update
    suspend fun update(exercises: Exercise)

//    @Transaction
//    @Insert
//    suspend fun insertCompletedExercise(exercise: Exercise, sets: List<ExerciseSet>, workout: Workout)

    @Delete
    suspend fun delete(exercise: Exercise)

    @Query("""
        DELETE FROM Exercise
        WHERE NOT Exercise.userCreated
    """)
    suspend fun clearRemoteExercises()

    @Query("""
        SELECT *
        FROM Exercise
    """)
    suspend fun getAllExercises(): List<Exercise>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT *, COUNT(ExerciseSet.sId) AS NumberOfSets
        FROM Exercise
        LEFT JOIN ExerciseSet ON Exercise.eId = ExerciseSet.exerciseId
        WHERE (
                :exerciseName <> ''
                AND Exercise.name LIKE '%' || :exerciseName || '%'
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
        GROUP BY Exercise.name
        ORDER BY NumberOfSets DESC, Exercise.name ASC
    """)
    fun getPagedExercisesByName(exerciseName: String, muscle: String, category: String): PagingSource<Int, ExerciseWithSets>



    @Query("""
        SELECT *
        FROM Exercise
        WHERE Exercise.name LIKE '%' || :exerciseName || '%'
        LIMIT 1
    """)
    fun searchExercisesByName(exerciseName: String): Exercise?

    @Transaction
    @Query("""
        SELECT *
        FROM Exercise
        ORDER BY Exercise.name ASC
    """)
    fun getAllPagedExercises(): PagingSource<Int, ExerciseWithSets>

    @Query("""
        SELECT *
        FROM Exercise
        WHERE eId IN (:ids)
    """)
    suspend fun getExercisesByIds(ids: List<Long>): List<Exercise>

    @Query("""
        SELECT *
        FROM Exercise
        WHERE eId = :eId
    """)
    suspend fun getExerciseById(eId: Long): Exercise

    @Query("""
        SELECT *
        FROM Exercise
        WHERE Exercise.name=:name
        LIMIT 1
    """)
    suspend fun getExerciseByName(name: String): Exercise?

    @Transaction
    @Query("""
        SELECT *
        FROM Exercise
    """)
    fun getExercisesWithSets(): List<ExerciseWithSets>


    @Query("""
        SELECT *
        FROM Exercise
        LEFT JOIN ExerciseSet ON Exercise.eId = ExerciseSet.exerciseId
        WHERE ExerciseSet.workoutId = :workoutId
    """)
    suspend fun getSetsAndExercisesInWorkout(workoutId: Long): List<CombinedSetData>
}