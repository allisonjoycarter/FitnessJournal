package com.catscoffeeandkitchen.fitnessjournal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.fitnessjournal.R
import com.catscoffeeandkitchen.fitnessjournal.TestTags

@Composable
fun AddExerciseOrGroupButtons(
    addExercise: () -> Unit,
    addGroup: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        FitnessJournalButton(
            "Add Exercise",
            onClick = {
                addExercise()
            },
            icon = {
                Icon(painterResource(id = R.drawable.fitness_center), "group")
            },
            modifier = Modifier.weight(1f).testTag(TestTags.AddExerciseButton)
        )

        FitnessJournalButton(
            "Add Group",
            icon = {
                Icon(painterResource(id = R.drawable.dataset), "group")
            },
            onClick = {
                addGroup()
            },
            modifier = Modifier.weight(1f).testTag(TestTags.AddGroupButton)
        )
    }
}