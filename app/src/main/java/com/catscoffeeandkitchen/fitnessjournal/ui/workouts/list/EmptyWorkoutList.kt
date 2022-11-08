package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.list

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.catscoffeeandkitchen.fitnessjournal.R
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton

@Composable
fun EmptyWorkoutList(
    modifier: Modifier = Modifier,
    addWorkout: (() -> Unit)? = null,
    addPlan: (() -> Unit)? = null,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.squat))
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            LottieAnimation(
                composition = composition,
                modifier = Modifier.width(400.dp).height(300.dp),
                iterations = LottieConstants.IterateForever,
            )
            Text(
                "No workouts yet.",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        if (addWorkout != null) {
            FitnessJournalButton(
                text = "Add Workout",
                modifier = Modifier.align(Alignment.BottomCenter),
                fullWidth = true,
                onClick = addWorkout
            )
        }

        if (addPlan != null) {
            FitnessJournalButton(
                text = "Add Workout Plan",
                modifier = Modifier.align(Alignment.BottomCenter),
                fullWidth = true,
                onClick = addPlan
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyWorkoutListPreview() {
    EmptyWorkoutList(
        addPlan = {}
    )
}