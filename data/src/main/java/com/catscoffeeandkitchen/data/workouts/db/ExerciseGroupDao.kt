package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.*
import com.catscoffeeandkitchen.data.workouts.models.*

@Dao
interface ExerciseGroupDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(group: ExerciseGroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(groups: List<ExerciseGroupEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllRefs(groups: List<GroupExerciseXRef>): List<Long>

    @Update
    suspend fun updateAll(groups: List<ExerciseGroupEntity>)

    @Update
    suspend fun update(group: ExerciseGroupEntity)

    @Delete
    suspend fun delete(group: ExerciseGroupEntity)

    @Query(
        """
        SELECT *
        FROM ExerciseGroupEntity
        WHERE gId = :id
    """
    )
    suspend fun getGroupById(id: Long): ExerciseGroupEntity

    @Transaction
    @Query(
        """
        SELECT *
        FROM ExerciseGroupEntity
        WHERE gId = :id
    """
    )
    suspend fun getGroupByIdWithExercises(id: Long): GroupWithExercises

    @Transaction
    @Query(
        """
        SELECT *
        FROM ExerciseGroupEntity
    """
    )
    suspend fun getGroups(): List<GroupWithExercises>

    @Query(
        """
        DELETE
        FROM GroupExerciseXRef
        WHERE GroupExerciseXRef.groupId = :groupId
    """
    )
    suspend fun removeGroupXRefs(groupId: Long)

}