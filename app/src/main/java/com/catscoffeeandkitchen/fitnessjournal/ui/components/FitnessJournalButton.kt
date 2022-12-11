package com.catscoffeeandkitchen.fitnessjournal.ui.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FitnessJournalButton(
    text: String,
    onClick: () -> Unit,
    icon: (@Composable()() -> Unit)? = null,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false,
) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier)
            .padding(6.dp)
    ) {
        if (icon != null) {
            icon()
        }
        Text(text, modifier = Modifier.padding(start = if (icon != null) 4.dp else 0.dp))
    }
}

@Preview
@Composable
fun FJButtonPreview() {
    FitnessJournalButton(onClick = {}, text = "a test button", fullWidth = false)
}

@Preview
@Composable
fun FJButtonPreviewFullWidth() {
    FitnessJournalButton(onClick = {}, text = "a test button", fullWidth = true)
}