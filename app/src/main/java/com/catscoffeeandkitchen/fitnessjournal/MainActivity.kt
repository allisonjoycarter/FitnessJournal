package com.catscoffeeandkitchen.fitnessjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.Navigation
import com.catscoffeeandkitchen.fitnessjournal.ui.theme.FitnessJournalTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        StrictMode.setThreadPolicy(
//            ThreadPolicy.Builder()
//                .detectAll()
//                .penaltyLog()
//                .build()
//        )

//        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
//        }

        setContent {
            FitnessJournalTheme {
                Navigation()
            }
        }
    }
}
