package com.catscoffeeandkitchen.fitnessjournal.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.catscoffeeandkitchen.fitnessjournal.services.TimerServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Timer
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FJModule {

    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("FitnessJournalPreferences", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideTimerServiceConnection(): TimerServiceConnection {
        return TimerServiceConnection()
    }
}