package com.catscoffeeandkitchen.fitnessjournal.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FitnessJournalInputButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier,
        shape = RoundedCornerShape(3.dp),
        onClick = onClick
    ) {
        Text(text, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Preview(showBackground = true)
@Composable
fun InputButtonPreview() {
    Column(
        modifier = Modifier.height(200.dp).padding(5.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FitnessJournalInputButton(text = "1", onClick = { /*TODO*/ })
        FitnessJournalInputButton(text = "+20 reps", onClick = { /*TODO*/ })
        FitnessJournalInputButton(text = "A very long input button", onClick = { /*TODO*/ })
        FitnessJournalInputButton(text = "30", onClick = { /*TODO*/ })
    }
}