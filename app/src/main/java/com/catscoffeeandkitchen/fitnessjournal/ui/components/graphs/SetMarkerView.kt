package com.catscoffeeandkitchen.fitnessjournal.ui.components.graphs

import android.content.Context
import android.graphics.Canvas
import android.widget.TextView
import androidx.core.view.isVisible
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.fitnessjournal.R
import com.catscoffeeandkitchen.fitnessjournal.ui.util.toCleanString
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import timber.log.Timber
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class SetMarkerView(
    context: Context,
    val chartWidth: Int,
    val dateUnit: ChronoUnit,
    val earliestTime: OffsetDateTime?,
    val bestSets: List<ExerciseSet>
    ): MarkerView(context, R.layout.chart_marker) {
    private var markerOffset: MPPointF? = null

    private val dateTextView: TextView = findViewById(R.id.date_label)
    private val setTextView: TextView = findViewById(R.id.set_label)
    private val weightTextView: TextView = findViewById(R.id.weight_label)

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {

        val expected = super.getOffsetForDrawingAtPoint(posX, posY)
        if (posX + width > chartWidth) {
            val difference = posX - chartWidth
            return MPPointF(expected.x - difference - width, expected.y)
        }

        return expected
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val formatter = when (dateUnit) {
            ChronoUnit.HOURS -> DateTimeFormatterBuilder().appendPattern("d-ha").toFormatter()
            ChronoUnit.MONTHS -> DateTimeFormatterBuilder().appendPattern("MMM dd yy").toFormatter()
            else -> DateTimeFormatterBuilder().appendPattern("yyyy-MM-d").toFormatter()
        }

        val date = dateUnit.addTo(earliestTime, e?.x?.toLong() ?: 0)
        dateTextView.text = if (date == null) "" else
            date
                .toZonedDateTime()
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(formatter)

        val earliestInUnit = when (dateUnit) {
            ChronoUnit.HOURS -> earliestTime?.withMinute(1)
            ChronoUnit.MONTHS -> earliestTime?.withDayOfMonth(1)
            else -> earliestTime?.withMonth(1)?.withDayOfMonth(1)
        }

        val set = bestSets.firstOrNull { item ->
            val setDateInUnit = when (dateUnit) {
                ChronoUnit.HOURS -> item.completedAt?.withMinute(1)
                ChronoUnit.MONTHS -> item.completedAt?.withDayOfMonth(1)
                else -> item.completedAt?.withMonth(1)?.withDayOfMonth(1)
            }
            earliestInUnit?.until(setDateInUnit, dateUnit)?.toFloat() == e?.x
        }

        setTextView.isVisible = set != null
        set?.let { setTextView.text = "${set.reps} reps at ${set.weightInPounds.toCleanString()}lbs" }

        weightTextView.text = "1RM: ${e?.y?.roundToInt() ?: "-" }lbs"

        super.refreshContent(e, highlight);
    }
}