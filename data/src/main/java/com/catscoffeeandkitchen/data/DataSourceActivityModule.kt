package com.catscoffeeandkitchen.data

import com.catscoffeeandkitchen.data.workouts.db.*
import com.catscoffeeandkitchen.data.workouts.repository.DataRepositoryImpl
import com.catscoffeeandkitchen.data.workouts.util.DatabaseBackupHelper
import com.catscoffeeandkitchen.domain.interfaces.DataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
abstract class DataSourceActivityModule {

}
