package com.catscoffeeandkitchen.fitnessjournal.ui.components.graphs

import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days


@Composable
fun MPExerciseLineGraph(
    entries: List<Pair<OffsetDateTime, Float>>
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            // programmatically create a LineChart
            val chart = LineChart(context)

            val earliest = entries.minOfOrNull { it.first }
            val hours = earliest?.until(entries.maxOf { it.first }, ChronoUnit.HOURS)
            val unitToUse = when {
                hours == null -> ChronoUnit.HOURS
                hours > (730.5 * 3) -> ChronoUnit.MONTHS
                hours > (24 * 3) -> ChronoUnit.DAYS
                else ->  ChronoUnit.HOURS
            }

            val xAxis = chart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textSize = 10f
            xAxis.textColor = Color.White.toArgb()
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(true)
            xAxis.granularity = when (unitToUse) {
                ChronoUnit.HOURS -> 1f
                ChronoUnit.DAYS -> 24f
                ChronoUnit.MONTHS -> 730.5f
                else -> 1f
            }

            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    val date = entries.first().first.plusHours(value.toLong())
                        .toZonedDateTime()
                        .withZoneSameInstant(ZoneId.systemDefault())
                    return DateTimeFormatterBuilder()
                        .appendPattern("MMM d")
                        .appendPattern(if (unitToUse == ChronoUnit.MONTHS) " 'yy" else "")
                        .appendPattern(if (unitToUse == ChronoUnit.HOURS) " ha" else "")
                        .toFormatter()
                        .format(date)
                }
            }

            val leftAxis = chart.axisLeft
            leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
            leftAxis.textColor = Color.White.toArgb()
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = true
            leftAxis.axisMinimum = 0f
            leftAxis.axisMaximum = (entries.maxOfOrNull { it.second } ?: 0f) + 20f
            leftAxis.yOffset = -9f
            leftAxis.textColor = primaryColor

            chart.axisRight.isEnabled = false

            val lineEntries = arrayListOf<Entry>()
            if (hours != null) {
                entries.forEach { entry ->
                    lineEntries.add(Entry(
                        earliest.until(entry.first, unitToUse).toFloat(),
                        entry.second
                    ))
                }
            }

            Timber.d("*** matching entries entered ${lineEntries.size}")

            val dataSet = LineDataSet(lineEntries, "a cool label").apply {
                color = primaryColor
                axisDependency = YAxis.AxisDependency.LEFT;
                lineWidth = 1.5f
                fillColor = primaryColor
                fillAlpha = 60
                valueTextColor = primaryColor
            }

            val lineData = LineData(dataSet)
            chart.data = lineData
            chart.invalidate()
            chart.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            RelativeLayout(context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
                addView(chart, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        },
    )
}