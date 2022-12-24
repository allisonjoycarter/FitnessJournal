package com.catscoffeeandkitchen.data

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.catscoffeeandkitchen.data.DataSourceModule
import com.catscoffeeandkitchen.data.workouts.db.*
import com.catscoffeeandkitchen.data.workouts.network.ExerciseSearchService
import com.catscoffeeandkitchen.data.workouts.repository.*
import com.catscoffeeandkitchen.data.workouts.util.DatabaseBackupHelper
import com.catscoffeeandkitchen.domain.interfaces.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataSourceModule::class]
)
class TestDataModule {
    @Provides
    fun provideExerciseSearchService(): ExerciseSearchService {
        return TestExerciseSearchService()
    }

    @Provides
    fun provideDatabaseBackupHelper(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences,
        database: FitnessJournalDb,
    ): DatabaseBackupHelper {
        return DatabaseBackupHelper(context, sharedPreferences, database)
    }
    //endregion

    //region Database DAO
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext appContext: Context,
    ): FitnessJournalDb {
        return Room.inMemoryDatabaseBuilder(
            appContext,
            FitnessJournalDb::class.java,
        )
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun provideExerciseDao(database: FitnessJournalDb): ExerciseDao {
        return database.exerciseDao()
    }

    @Provides
    fun provideExerciseGoalDao(database: FitnessJournalDb): ExerciseGoalDao {
        return database.exerciseGoalDao()
    }

    @Provides
    fun provideExerciseGroupDao(database: FitnessJournalDb): ExerciseGroupDao {
        return database.exerciseGroupDao()
    }

    @Provides
    fun provideExercisePositionDao(database: FitnessJournalDb): ExercisePositionDao {
        return database.exercisePositionDao()
    }

    @Provides
    fun provideExerciseSetDao(database: FitnessJournalDb): ExerciseSetDao {
        return database.exerciseSetDao()
    }

    @Provides
    fun provideWorkoutDao(database: FitnessJournalDb): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    fun provideWorkoutPlanDao(database: FitnessJournalDb): WorkoutPlanDao {
        return database.workoutPlanDao()
    }

    @Provides
    fun provideRemoteKeysDao(database: FitnessJournalDb): RemoteKeysDao {
        return database.remoteKeysDao()
    }

    //endregion

    //region Repositories
    @Provides
    fun provideWorkoutRepository(
        database: FitnessJournalDb,
    ): WorkoutRepository {
        return WorkoutRepositoryImpl(database)
    }

    @Provides
    fun provideWorkoutPlanRepository(
        database: FitnessJournalDb,
    ): WorkoutPlanRepository {
        return WorkoutPlanRepositoryImpl(database)
    }

    @Provides
    fun provideExerciseSetRepository(
        database: FitnessJournalDb,
    ): ExerciseSetRepository {
        return ExerciseSetRepositoryImpl(database)
    }

    @Provides
    fun provideExerciseRepository(
        database: FitnessJournalDb,
        exerciseSearchService: ExerciseSearchService,
    ): ExerciseRepository {
        return ExerciseRepositoryImpl(database, exerciseSearchService)
    }

    @Provides
    fun provideDataRepository(
        @ApplicationContext context: Context,
        backupHelper: DatabaseBackupHelper,
        database: FitnessJournalDb
    ): DataRepository {
        return DataRepositoryImpl(context, backupHelper, database)
    }

    @Provides
    fun provideHomRepository(
        database: FitnessJournalDb
    ): HomeRepository {
        return HomeRepositoryImpl(database)
    }
    //endregion
}