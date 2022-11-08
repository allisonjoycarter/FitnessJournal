package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.catscoffeeandkitchen.data.util.Converters
import com.catscoffeeandkitchen.data.workouts.models.*

@Database(
    entities = [
        Exercise::class,
        ExerciseSet::class,
        Workout::class,
        WorkoutPlan::class,
        ExerciseGoal::class,
        RemoteKeys::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FitnessJournalDb: RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseSetDao(): ExerciseSetDao
    abstract fun exerciseGoalDao(): ExerciseGoalDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}