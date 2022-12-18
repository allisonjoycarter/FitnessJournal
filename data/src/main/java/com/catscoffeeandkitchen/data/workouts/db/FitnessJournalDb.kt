package com.catscoffeeandkitchen.data.workouts.db

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import com.catscoffeeandkitchen.data.util.Converters
import com.catscoffeeandkitchen.data.workouts.models.*
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionEntity

@Database(
    entities = [
        ExerciseEntity::class,
        ExerciseGroupEntity::class,
        GroupExerciseXRef::class,
        SetEntity::class,
        ExercisePositionEntity::class,
        WorkoutEntity::class,
        WorkoutPlanEntity::class,
        ExerciseGoal::class,
        RemoteKeys::class,
    ],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = FitnessJournalDb.Migration2To3::class),
        AutoMigration(from = 4, to = 5)
    ]
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

    @RenameTable(
        fromTableName = "ExercisePositionInWorkout",
        toTableName = "ExercisePositionEntity"
    )
    class Migration2To3: AutoMigrationSpec
}
