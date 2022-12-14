package com.catscoffeeandkitchen.fitnessjournal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun FitnessJournalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    columnPaddingHorizontal: Dp = 8.dp,
    columnPaddingVertical: Dp = 0.dp,
    columnItemSpacing: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = if (backgroundColor != null)
            CardDefaults.cardColors(containerColor = backgroundColor)
            else CardDefaults.cardColors()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(columnItemSpacing),
            modifier = Modifier.padding(
                horizontal = columnPaddingHorizontal,
                vertical = columnPaddingVertical
            )
        ) {
            content()
        }
    }
}
