package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGoal
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGoalWithExercises
import com.catscoffeeandkitchen.data.workouts.models.GoalAndExerciseCombined

@Dao
interface ExerciseGoalDao {

    @Insert
    fun insert(exerciseGoal: ExerciseGoal): Long

    @Insert
    fun insertAll(exerciseGoals: List<ExerciseGoal>)

    @Update
    fun update(exerciseGoal: ExerciseGoal)

    @Update
    fun updateAll(exerciseGoals: List<ExerciseGoal>)

    @Delete
    fun delete(exerciseGoal: ExerciseGoal)

    @Delete
    fun deleteAll(goals: List<ExerciseGoal>)

    @Query(
        """
        SELECT *
        FROM ExerciseGoal
        WHERE workoutPlanId = :workoutId
        AND positionInWorkout = :position
        LIMIT 1
    """
    )
    fun getExerciseGoalByWorkoutAndSetNumber(workoutId: Long, position: Int): ExerciseGoal

    @Query("""
        SELECT *
        FROM ExerciseGoal
        WHERE ExerciseGoal.workoutPlanId = :wId
    """)
    fun getGoalsInWorkout(wId: Long): List<ExerciseGoalWithExercises>
}