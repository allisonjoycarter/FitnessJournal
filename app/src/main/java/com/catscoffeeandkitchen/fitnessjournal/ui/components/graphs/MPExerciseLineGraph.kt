package com.catscoffeeandkitchen.fitnessjournal.ui.components.graphs

import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.catscoffeeandkitchen.fitnessjournal.ui.util.toCleanString
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.stats.StatsData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import timber.log.Timber
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt


@Composable
fun MPExerciseLineGraph(
    entries: List<StatsData>
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val tertiaryColor = MaterialTheme.colorScheme.tertiary.toArgb()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            // programmatically create a LineChart
            val chart = LineChart(context)

            val earliest = entries.minOfOrNull { it.date }
            val hours = earliest?.until(entries.maxOf { it.date }, ChronoUnit.HOURS)
            val unitToUse = when {
                hours == null -> ChronoUnit.HOURS
                hours > (730.5 * 3) -> ChronoUnit.MONTHS
                hours > (24 * 3) -> ChronoUnit.DAYS
                else ->  ChronoUnit.HOURS
            }

            val dateFormatter = DateTimeFormatterBuilder()
                .appendPattern("MMM d")
                .appendPattern(if (unitToUse == ChronoUnit.MONTHS) " yy" else "")
                .appendPattern(if (unitToUse == ChronoUnit.HOURS) " ha" else "")
                .toFormatter()

            val xAxis = chart.xAxis

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textSize = 8f
            xAxis.textColor = Color.White.toArgb()
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(true)
            xAxis.granularity = .5f
            xAxis.setDrawLabels(true)
            xAxis.setCenterAxisLabels(false)

            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    val date = unitToUse.addTo(earliest, value.toLong())
                    return if (date == null) "" else
                        date
                            .atZoneSameInstant(ZoneId.of("UTC"))
                            .withZoneSameInstant(ZoneId.systemDefault())
                            .format(dateFormatter)
                }
            }
            xAxis.labelRotationAngle = 45f

            val leftAxis = chart.axisLeft
            leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
            leftAxis.textColor = Color.White.toArgb()
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = true
            leftAxis.axisMinimum = 0f
            leftAxis.axisMaximum = (entries.maxOfOrNull { it.repMax } ?: 0f) + 20f
            leftAxis.yOffset = -9f
            leftAxis.textColor = primaryColor

            chart.axisRight.isEnabled = false

            val repMaxEntries = arrayListOf<Entry>()
            val highestWeightEntries = arrayListOf<Entry>()
            val repEntries = arrayListOf<Entry>()
            if (hours != null) {
                val groupingFormatter = when (unitToUse) {
                    ChronoUnit.HOURS -> DateTimeFormatterBuilder().appendPattern("d-ha").toFormatter()
                    ChronoUnit.MONTHS -> DateTimeFormatterBuilder().appendPattern("yyyy-MM").toFormatter()
                    else -> DateTimeFormatterBuilder().appendPattern("yyyy-MM-d").toFormatter()
                }
                val groupedEntries = entries.groupBy { it.date.format(groupingFormatter) }
                Timber.d("*** entries = ${entries.size}, grouped into ${groupedEntries.size}, groups = ${groupedEntries.map { it.key }.joinToString(", ")}")
                groupedEntries.forEach { entry ->
                    val set = entry.value.maxByOrNull { it.repMax }!!

                    val earliestInUnit = when (unitToUse) {
                        ChronoUnit.HOURS -> earliest.withMinute(1)
                        ChronoUnit.MONTHS -> earliest.withDayOfMonth(1)
                        else -> earliest.withMonth(1).withDayOfMonth(1)
                    }

                    val setDateInUnit = when (unitToUse) {
                        ChronoUnit.HOURS -> set.date.withMinute(1)
                        ChronoUnit.MONTHS -> set.date.withDayOfMonth(1)
                        else -> set.date.withMonth(1).withDayOfMonth(1)
                    }

                    repMaxEntries.add(
                        Entry(
                            earliestInUnit.until(setDateInUnit, unitToUse).toFloat(),
                            entry.value.maxOfOrNull { it.repMax } ?: 0f
                        )
                    )

                    repEntries.add(
                        Entry(
                            earliestInUnit.until(setDateInUnit, unitToUse).toFloat(),
                            entry.value.maxOfOrNull { it.reps } ?: 0f
                        )
                    )
                }

                xAxis.labelCount = groupedEntries.size
            }

            val take = if (repMaxEntries.size > 2) 3 else repMaxEntries.size
            val trendEntries = arrayListOf(
                Entry(repMaxEntries.first().x, repMaxEntries.subList(0, take).map { it.y }.average().toFloat()),
                Entry(repMaxEntries.last().x, repMaxEntries.takeLast(take).map { it.y }.average().toFloat())
            )

            val repMaxDataSet = LineDataSet(repMaxEntries.sortedWith(compareBy({ it.x }, { it.y }))
                .distinctBy { it.x }, "Estimated 1RM")
                .apply {
                color = primaryColor
                axisDependency = YAxis.AxisDependency.LEFT;
                lineWidth = 1.5f
                fillColor = primaryColor
                fillAlpha = 60
                valueTextColor = primaryColor
                valueTextSize = 12f
//                valueFormatter = object : ValueFormatter() {
//                    override fun getFormattedValue(value: Float): String {
//                        return ((value * 10).roundToInt() / 10.0f).toCleanString()
//                    }
//                }
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return ""
                    }
                }
                setCircleColor(primaryColor)
                circleHoleColor = primaryColor
            }
            Timber.d("*** rep max entries = ${repMaxEntries.joinToString("\n")}")

            val highestWeightDataSet = LineDataSet(highestWeightEntries.sortedWith(compareBy({ it.x }, { it.y }))
                .distinctBy { it.x }, "Highest Weight Lifted").apply {
                color = tertiaryColor
                axisDependency = YAxis.AxisDependency.LEFT;
                lineWidth = 1.5f
                fillColor = tertiaryColor
                fillAlpha = 60
                valueTextColor = tertiaryColor
                valueTextSize = 12f
//                valueFormatter = object : ValueFormatter() {
//                    override fun getFormattedValue(value: Float): String {
//                        return ((value * 10).roundToInt() / 10.0f).toCleanString()
//                    }
//                }
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return ""
                    }
                }
                setCircleColor(tertiaryColor)
                circleHoleColor = tertiaryColor
                highLightColor = Color.White.toArgb()
            }

            val repsDataSet = LineDataSet(repEntries.sortedWith(compareBy({ it.x }, { it.y }))
                .distinctBy { it.x }, "Reps").apply {
                color = primaryColor
                axisDependency = YAxis.AxisDependency.LEFT;
                lineWidth = 1.5f
                fillColor = primaryColor
                fillAlpha = 60
                valueTextColor = primaryColor
                valueTextSize = 12f
//                valueFormatter = object : ValueFormatter() {
//                    override fun getFormattedValue(value: Float): String {
//                        return ((value * 10).roundToInt() / 10.0f).toCleanString()
//                    }
//                }
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return ""
                    }
                }
                setCircleColor(primaryColor)
                circleHoleColor = primaryColor
            }

            val trendDataSet = LineDataSet(trendEntries.distinctBy { it.x }, "Trend").apply {
                color = secondaryColor
                axisDependency = YAxis.AxisDependency.LEFT;
                lineWidth = 1f
                fillColor = secondaryColor
                fillAlpha = 10
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return ""
                    }
                }
                setDrawCircles(false)
                enableDashedLine(15f, 8f, 0f)
                highLightColor = Color.White.toArgb()
            }

            val lineData = when {
                entries.maxOf { it.repMax } < 30 -> LineData(repsDataSet)
                else -> LineData(repMaxDataSet, highestWeightDataSet, trendDataSet)
            }
            chart.data = lineData

            Timber.d("*** best sets = ${entries.joinToString("\n") { "${it.bestSet.reps}x ${it.bestSet.weightInPounds}lbs, completedAt ${it.bestSet.completedAt?.format(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME)}" }}")
            val markerView = SetMarkerView(
                context,
                (screenWidth * .9).roundToInt(),
                unitToUse,
                earliest,
                entries.map { it.bestSet }
            )
            chart.marker = markerView

            chart.legend.apply {
                textColor = Color.White.toArgb()
            }
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