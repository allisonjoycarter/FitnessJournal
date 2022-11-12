package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.plates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils

@Composable
fun PlateStack(
    plates: Map<Double, Int>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(12.dp)
    ) {
        plates
            .filter { it.value > 0 }
            .entries
            .sortedBy { it.key }
            .forEach { entry ->
                for (i in 0 until entry.value) {
                    PlateGraphic(weight = entry.key)
                }
            }
    }
}

@Composable
fun PlateStackVertical(
    plates: Map<Double, Int>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(12.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.LightGray,
                            Color.DarkGray
                        )
                    )
                )
        ) { }

        Box(
            modifier = Modifier
                .width(10.dp)
                .height(17.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.LightGray,
                            Color.DarkGray
                        )
                    )
                )
        ) { }

        plates
            .filter { it.value > 0 }
            .entries
            .sortedByDescending { it.key }
            .forEach { entry ->
                for (i in 0 until entry.value) {
                    PlateGraphicVertical(weight = entry.key)
                }
            }

        Box(
            modifier = Modifier
                .width(15.dp)
                .height(14.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.LightGray,
                            Color.DarkGray
                        )
                    )
                )
        ) { }
    }
}

@Composable
fun PlateGraphic(
    weight: Double,
) {
    val weightColor = when (weight) {
        45.0 -> Color.Red
        35.0 -> Color.Yellow
        25.0 -> Color.Green
        15.0 -> Color.White
        10.0 -> Color.Black
        5.0 -> Color.Black
        2.5 -> Color.Black
        else -> Color.Black
    }

    val width = when (weight) {
        45.0 -> 120.dp
        35.0 -> 110.dp
        25.0 -> 90.dp
        10.0 -> 70.dp
        5.0 -> 50.dp
        2.5 -> 40.dp
        else -> 40.dp
    }

    val contrast = ColorUtils.calculateContrast(
        MaterialTheme.colorScheme.onSurface.toArgb(),
        weightColor.toArgb())

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .height(20.dp)
            .width(width)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        weightColor.copy(alpha = .8f),
                        weightColor.copy(alpha = .6f)
                    )
                )
            ),
    ) {
        Text(
            weight.toString().replace(".0", ""),
            color = if (contrast > 1.5f) Color.White else Color.Black,
            modifier = Modifier.align(Alignment.BottomCenter))
    }
}


@Composable
fun PlateGraphicVertical(
    weight: Double,
) {
    val weightColor = when (weight) {
        45.0 -> Color(0xffe04447)
        35.0 -> Color(0xffd9be27)
        25.0 -> Color(0xff1fb26c)
        15.0 -> Color(0xFFD3D3D3)
        else -> Color(0xFF2E2E2E)
    }

    val height = when (weight) {
        45.0 -> 120.dp
        35.0 -> 110.dp
        25.0 -> 90.dp
        10.0 -> 70.dp
        5.0 -> 50.dp
        2.5 -> 40.dp
        else -> 40.dp
    }

    val contrast = ColorUtils.calculateContrast(
        MaterialTheme.colorScheme.onSurface.toArgb(),
        weightColor.toArgb())

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .width(22.dp)
            .height(height)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        weightColor.copy(alpha = .8f),
                        weightColor,
                    )
                )
            ),
    ) {
        Text(
            weight.toString().replace(".0", ""),
            color = if (contrast > 1.5f) Color.White else Color.Black,
            overflow = TextOverflow.Visible,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Preview
@Composable
fun PlateStackPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PlateStack(plates = mapOf(
            45.0 to 2,
            35.0 to 1,
            5.0 to 1
        ))
    }
}
