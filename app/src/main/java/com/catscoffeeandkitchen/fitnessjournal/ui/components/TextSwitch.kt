package com.catscoffeeandkitchen.fitnessjournal.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.runtime.getValue

@Composable
fun TextSwitch(
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    options: List<String>
) {
    TabRow(
        selectedTabIndex = selectedIndex,
        indicator = { tabPositions ->
            val currentTabWidth by animateDpAsState(
                targetValue = tabPositions[selectedIndex].width,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )
            val indicatorOffset by animateDpAsState(
                targetValue = tabPositions[selectedIndex].left,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .fillMaxHeight()
                    .background(color = MaterialTheme.colorScheme.primary.copy(alpha = .3f))

            ) {
                Box(
                    Modifier
                        .padding(4.dp)
                        .width(currentTabWidth - 8.dp)
                        .offset(x = indicatorOffset)
                        .clip(MaterialTheme.shapes.large)
                        .background(color = MaterialTheme.colorScheme.primary)
                        .fillMaxHeight()
                )
            }
        },
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = .3f)),
        tabs = {
            options.forEachIndexed { index, title ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onSelectIndex(index) },
                    text = { Text(
                        text = title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (selectedIndex == index) MaterialTheme.colorScheme.onPrimary else
                            MaterialTheme.colorScheme.primary
                    ) },
                    modifier = Modifier.zIndex(3f)
                )
            }
        }
    )

}

