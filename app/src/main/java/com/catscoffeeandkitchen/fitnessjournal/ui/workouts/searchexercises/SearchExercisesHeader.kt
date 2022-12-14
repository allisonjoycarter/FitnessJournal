package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.searchexercises

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.android.material.chip.ChipGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchExerciseHeader(
    currentSearch: String?,
    currentMuscleFilter: String?,
    currentCategoryFilter: String?,
    onSearch: (search: String?) -> Unit = {},
    filterMuscle: (muscle: String?) -> Unit = {},
    filterCategory: (category: String?) -> Unit = {}
) {
    var search by remember { mutableStateOf(TextFieldValue(currentSearch.orEmpty())) }
    var muscleFilter by remember { mutableStateOf(currentMuscleFilter) }
    var categoryFilter by remember { mutableStateOf(currentCategoryFilter) }
    val categoryOptions = listOf(
        "Chest",
        "Abs",
        "Legs",
        "Shoulders",
        "Arms",
        "Back"
    )

    val muscleOptions: List<String> =
        (if (!currentMuscleFilter.isNullOrEmpty())
            listOf(currentMuscleFilter)
        else emptyList()) + listOf(
            "Pecs",
            "Quads",
            "Triceps",
            "Abs",
            "Glutes",
            "Lats",
        )

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                value = search,
                onValueChange = { value ->
                    search = value
                },
                singleLine = true,
                keyboardActions = KeyboardActions(onDone = { onSearch(search.text.trim()) })
            )
            IconButton(
                onClick = {
                    onSearch(search.text.trim().ifEmpty { null })
                }
            ) {
                Icon(Icons.Default.Search, "search")
            }
        }

        Text(
            "Muscles",
            modifier = Modifier.padding(start = 4.dp, top = 10.dp),
            style = MaterialTheme.typography.labelSmall
        )
        LazyRow(
            modifier = Modifier
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(muscleOptions) { option ->
                FilterChip(
                    selected = muscleFilter == option,
                    onClick = {
                        muscleFilter = if (muscleFilter == option) {
                            null
                        } else {
                            option
                        }
                        filterMuscle(muscleFilter)
                    },
                    label = { Text(option) }
                )
            }
        }

        Divider()

        Text(
            "Category",
            modifier = Modifier.padding(start = 4.dp, top = 10.dp),
            style = MaterialTheme.typography.labelSmall
        )
        LazyRow(
            modifier = Modifier
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(categoryOptions) { option ->
                FilterChip(
                    selected = categoryFilter == option,
                    onClick = {
                        categoryFilter = if (categoryFilter == option) {
                            null
                        } else {
                            option
                        }
                        filterCategory(categoryFilter)
                    },
                    label = { Text(option) }
                )
            }
        }
    }
}


@Preview
@Composable
fun SearchExerciseHeaderPreview() {
    SearchExerciseHeader(
        "Bicep Curl",
        "Chest",
        "Pecs"
    )
}