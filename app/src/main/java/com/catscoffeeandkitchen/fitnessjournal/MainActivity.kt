package com.catscoffeeandkitchen.fitnessjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.catscoffeeandkitchen.domain.usecases.data.BackupDataUseCase
import com.catscoffeeandkitchen.domain.usecases.data.RestoreDataUseCase
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.Navigation
import com.catscoffeeandkitchen.fitnessjournal.ui.theme.FitnessJournalTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setContent {
            FitnessJournalTheme {
                Navigation()
            }
        }
    }
}
