package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.searchexercises

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchExercisesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SearchExercisesViewModel = hiltViewModel(),
    ) {
    val searchState = viewModel.searchRequest.collectAsState(initial = SearchExercisesViewModel.ExerciseSearch())
    val pagingItems = viewModel.pagedExerciseFlow.collectAsLazyPagingItems()

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(coroutineScope) {
        if (searchState.value == SearchExercisesViewModel.ExerciseSearch()) {
            viewModel.searchExercises(SearchExercisesViewModel.ExerciseSearch())
        }
    }

    LazyColumn(modifier = modifier) {
        stickyHeader {
            SearchExerciseHeader(searchState.value.name, searchState.value.muscle,
                onSearch = { text ->
                    viewModel.searchExercises(searchState.value.copy(name = text))
                    pagingItems.refresh()
                },
                filterCategory = { category ->
                    viewModel.searchExercises(searchState.value.copy(category = category))
                    pagingItems.refresh()
                }
            )
        }

        items(pagingItems) {exercise ->
            if (exercise != null) {
                ExerciseItem(exercise, onTap = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "exerciseToAdd",
                        "${exercise.name}|${exercise.musclesWorked.joinToString(";")}"
                    )
                    navController.popBackStack()
                })
            }
        }

        item {
            when (val loadState = pagingItems.loadState.mediator?.refresh) {
                is LoadState.Loading -> CircularProgressIndicator()
                is LoadState.Error -> {
                    Divider()
                    Text(loadState.error.message.toString())
                }
                else -> { }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ExerciseItem(
    exercise: Exercise,
    onTap: () -> Unit = {},
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    FitnessJournalCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onTap() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (exercise.thumbnailUrl?.isNotEmpty() == true) {
                GlideImage(
                    model = exercise.thumbnailUrl!!,
                    contentDescription = "Exercise Image",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(6.dp),
                    colorFilter = if (darkTheme)
                        ColorFilter.colorMatrix(
                            ColorMatrix(floatArrayOf(
                                -1f, 0f, 0f, 0f, 255f,
                                0f, -1f, 0f, 0f, 255f,
                                0f, 0f, -1f, 0f, 255f,
                                0f, 0f, 0f, 1f, 0f
                            ))
                        ) else null
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    exercise.name,
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
    //            Text(
    //                "Performed ${exercise} sets",
    //                modifier = Modifier.padding(2.dp),
    //                style = MaterialTheme.typography.labelSmall
    //            )
                exercise.category?.let { Text(it, style = MaterialTheme.typography.labelLarge) }

                if (exercise.musclesWorked.isNotEmpty()) {
                    Text(exercise.musclesWorked.joinToString(", ") { it })
                }
        }
        }
    }
}

@Preview
@Composable
fun ExerciseItemPreview() {
    ExerciseItem(
        Exercise(
            name = "Bicep Curl",
            musclesWorked = listOf("Biceps", "Triceps"),
        )
    )
}