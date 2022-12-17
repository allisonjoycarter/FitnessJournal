package com.catscoffeeandkitchen.data

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.catscoffeeandkitchen.data.workouts.db.*
import com.catscoffeeandkitchen.data.workouts.db.migrations.Migration_3_4
import com.catscoffeeandkitchen.data.workouts.network.ExerciseSearchService
import com.catscoffeeandkitchen.data.workouts.repository.*
import com.catscoffeeandkitchen.data.workouts.util.DatabaseBackupHelper
import com.catscoffeeandkitchen.domain.interfaces.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataSourceModule {
    //region Network
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        moshi: Moshi,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://wger.de/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    fun provideExerciseSearchService(retrofit: Retrofit): ExerciseSearchService.Impl {
        return ExerciseSearchService.Impl(retrofit)
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
        return Room.databaseBuilder(
            appContext,
            FitnessJournalDb::class.java,
            "FitnessJournalDb"
        )
            // uncomment for query logging
//            .setQueryCallback({ sqlQuery, bindArgs ->
//                Timber.d("+++\n SQL Query $sqlQuery \n SQL Args $bindArgs \n +++")
//            }, Executors.newSingleThreadExecutor())
            .addMigrations(Migration_3_4)
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
        exerciseSearchService: ExerciseSearchService.Impl,
        ): WorkoutRepository {
        return WorkoutRepositoryImpl(database, exerciseSearchService)
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
    fun provideExerciseGroupRepository(
        database: FitnessJournalDb,
        ): ExerciseRepository {
        return ExerciseRepositoryImpl(database)
    }

    @Provides
    fun provideDataRepository(
        @ApplicationContext context: Context,
        backupHelper: DatabaseBackupHelper,
        database: FitnessJournalDb
        ): DataRepository {
        return DataRepositoryImpl(context, backupHelper, database)
    }
    //endregion
}
