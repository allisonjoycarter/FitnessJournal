package com.catscoffeeandkitchen.fitnessjournal.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun FitnessJournalCard(
    modifier: Modifier = Modifier,
    columnPaddingHorizontal: Dp = 8.dp,
    columnPaddingVertical: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = columnPaddingHorizontal,
                vertical = columnPaddingVertical)
        ) {
            content()
        }
    }
}
