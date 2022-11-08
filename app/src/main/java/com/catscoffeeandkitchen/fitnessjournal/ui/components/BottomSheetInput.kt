package com.catscoffeeandkitchen.fitnessjournal.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetInputModal(
    content: @Composable() () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )
    val scope = rememberCoroutineScope()
    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            Column {
                Row {
                    Button(onClick = {}) {
                        Text("1")
                    }
                    Button(onClick = {}) {
                        Text("2")
                    }
                    Button(onClick = {}) {
                        Text("2")
                    }
                }

                Row {
                    Button(onClick = {}) {
                        Text("3")
                    }
                    Button(onClick = {}) {
                        Text("4")
                    }
                    Button(onClick = {}) {
                        Text("5")
                    }
                }

                Row {
                    Button(onClick = {}) {
                        Text("6")
                    }
                    Button(onClick = {}) {
                        Text("7")
                    }
                    Button(onClick = {}) {
                        Text("8")
                    }
                }
            }
        }
    ) {
        content()
    }
}