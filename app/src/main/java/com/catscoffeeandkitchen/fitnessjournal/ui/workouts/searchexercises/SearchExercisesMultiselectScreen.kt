package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.searchexercises

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.util.capitalizeWords
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.searchexercises.create.CreateOrChangeExerciseDialog
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchExercisesMultiSelectScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    muscle: String? = null,
    category: String? = null,
    viewModel: SelectExercisesViewModel = hiltViewModel(),
    ) {
    val searchState = viewModel.searchRequest.collectAsState(
        initial = ExerciseSearch(
            muscle = muscle, category = category
        ))
    val pagingItems = viewModel.pagedExerciseFlow.collectAsLazyPagingItems()
    val selectedItems = viewModel.selectedExercises.collectAsState(emptyList())
    var showCreateExerciseDialog by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf(null as Exercise?) }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(coroutineScope) {
        Timber.d("*** launchedEffect muscle = $muscle, category = $category")
        if (pagingItems.itemSnapshotList.isEmpty()) {
            viewModel.searchExercises(searchState.value)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.previousBackStackEntry?.savedStateHandle?.set<String>(
                    "selectedExercises",
                    selectedItems.value.joinToString("|") { it.name }
                )
                navController.popBackStack()
            }) {
                Icon(Icons.Default.Check, contentDescription = "finished selecting")
            }
        }
    ) { padding ->
        if (showCreateExerciseDialog) {
            CreateOrChangeExerciseDialog(
                isCreating = editingExercise == null,
                currentExercise = editingExercise ?: Exercise(
                    name = (searchState.value).name.capitalizeWords().orEmpty(),
                    musclesWorked = emptyList()
                ),
                onDismiss = { showCreateExerciseDialog = false },
                onConfirm = { exercise ->
                    if (editingExercise == null) {
                        viewModel.createExercise(exercise) {
//                            navController.previousBackStackEntry?.savedStateHandle?.set(
//                                "exerciseToAdd",
//                                exercise.name
//                            )
//                            navController.popBackStack()
                        }
                    } else {
                        viewModel.updateExercise(exercise, refreshItems = { pagingItems.refresh() })
                    }
                    showCreateExerciseDialog = false
                }
            )
        }

        LazyColumn(modifier = modifier.padding(padding)) {
            stickyHeader {
                SearchExerciseHeader(
                    currentSearch = searchState.value.name,
                    currentCategoryFilter = searchState.value.category,
                    currentMuscleFilter = searchState.value.muscle,
                    onSearch = { text ->
                        viewModel.searchExercises(searchState.value.copy(name = text))
                        pagingItems.refresh()
                    },
                    filterMuscle = { muscle ->
                        viewModel.searchExercises(searchState.value.copy(muscle = muscle))
                        pagingItems.refresh()
                    },
                    filterCategory = { category ->
                        viewModel.searchExercises(searchState.value.copy(category = category))
                        pagingItems.refresh()
                    }
                )

                if (selectedItems.value.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(2.dp)
                        ) {
                            Text(
                                "${selectedItems.value.size} exercises selected",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            items(selectedItems.value) { exercise ->
                SelectableExerciseItem(
                    exercise,
                    isSelected = selectedItems.value.contains(exercise),
                    onTap = {
                        viewModel.unselectExercise(exercise)
                    },
                    onLongPress = {
                        editingExercise = exercise
                        showCreateExerciseDialog = true
                    }
                )
            }

            items(pagingItems) { exercise ->
                if (exercise != null && !selectedItems.value.contains(exercise)) {
                    SelectableExerciseItem(
                        exercise,
                        isSelected = selectedItems.value.contains(exercise),
                        onTap = {
                            if (selectedItems.value.contains(exercise)) {
                                viewModel.unselectExercise(exercise)
                            } else {
                                viewModel.selectExercise(exercise)
                            }
                        },
                        onLongPress = {
                            editingExercise = exercise
                            showCreateExerciseDialog = true
                        }
                    )
                }
            }

            item {
                when (val loadState = pagingItems.loadState.mediator?.refresh) {
                    is LoadState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is LoadState.Error -> {
                        Divider()
                        Text(loadState.error.message.toString())
                    }
                    else -> {}
                }
            }

            if (pagingItems.loadState.mediator?.refresh is LoadState.NotLoading) {
                item {
                    FitnessJournalButton(
                        text = "Create Exercise",
                        fullWidth = true,
                        onClick = {
                            editingExercise = null
                            showCreateExerciseDialog = true
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun SelectableExerciseItem(
    exercise: Exercise,
    isSelected: Boolean,
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    FitnessJournalCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { onTap() },
                onLongClick = { onLongPress() }
            ),
        backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else null,
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
fun SelectableExerciseItemPreview() {
    SelectableExerciseItem(
        isSelected = true,
        exercise = Exercise(
            name = "Bicep Curl",
            musclesWorked = listOf("Biceps", "Triceps"),
        )
    )
}