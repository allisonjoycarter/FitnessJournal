package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekRow(
    weekdays: List<DayOfWeek>,
    onWeekdaySelected: (DayOfWeek) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        DayOfWeek.values().forEach { day ->
            FilterChip(
                selected = weekdays.contains(day),
                onClick = {
                    onWeekdaySelected(day)
                },
                label = { Text(
                    when (day) {
                        DayOfWeek.SUNDAY -> "Su"
                        DayOfWeek.THURSDAY -> "Th"
                        else -> day.name.first().toString()
                    }
                ) },
                leadingIcon = { },
            )
        }
    }
}