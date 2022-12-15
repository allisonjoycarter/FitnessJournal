package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.catscoffeeandkitchen.data.util.Converters
import com.catscoffeeandkitchen.data.workouts.models.*
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionInWorkout

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseGroupEntity::class,
        GroupExerciseXRef::class,
        SetEntity::class,
        ExercisePositionInWorkout::class,
        WorkoutEntity::class,
        WorkoutPlanEntity::class,
        ExerciseGoal::class,
        RemoteKeys::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FitnessJournalDb: RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseSetDao(): ExerciseSetDao
    abstract fun exerciseGoalDao(): ExerciseGoalDao
    abstract fun exerciseGroupDao(): ExerciseGroupDao
    abstract fun exercisePositionDao(): ExercisePositionDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}