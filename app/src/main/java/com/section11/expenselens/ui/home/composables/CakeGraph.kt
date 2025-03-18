package com.section11.expenselens.ui.home.composables

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.ColorTemplate
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.home.model.CakeGraphUiModel
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview
import com.section11.expenselens.ui.utils.formatToTwoDecimal

private const val SPIN_ANIMATION_DURATION = 1000
private const val SPIN_ANIMATION_FULL_SPIN_DEGREES = 360
private const val SLICE_ENLARGE_ON_TAP_AMOUNT = 8f
private const val SLICE_SPACING = 5f

@Composable
fun CakeGraph(
    graphUiModel: CakeGraphUiModel,
    modifier: Modifier = Modifier
) {
    val transparent = Color.Transparent.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    var centerText by remember { mutableStateOf(graphUiModel.chartCenterText) }
    val colorList = ArrayList<Int>()
    for (c in ColorTemplate.PASTEL_COLORS) colorList.add(c)

    val valueChangeListener = object : OnChartValueSelectedListener {
        override fun onValueSelected(entry: Entry?, highlight: Highlight?) {
            centerText = "\$${entry?.y?.formatToTwoDecimal()} - ${entry?.data}"

        }
        override fun onNothingSelected() {
            centerText = graphUiModel.chartCenterText
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            getPieChart(context, surfaceColor, transparent, valueChangeListener)
        },
        update = { pieChart ->
            val entries = graphUiModel.slices.map { slice ->
                PieEntry(slice.value, slice.label).apply {
                    data = label
                }
            }
            val dataSet = PieDataSet(entries, String()).apply {
                sliceSpace = SLICE_SPACING
                selectionShift = SLICE_ENLARGE_ON_TAP_AMOUNT
                colors = colorList
            }

            val pieData = PieData(dataSet).apply {
                setDrawValues(false)
            }

            pieChart.centerText = centerText
            pieChart.data = pieData
            pieChart.invalidate()
        }
    )
}

private fun getPieChart(
    context: Context,
    surfaceColor: Int,
    transparentColor: Int,
    valueChangeListener: OnChartValueSelectedListener
): PieChart {
    return PieChart(context).apply {
        description.isEnabled = false
        isDrawHoleEnabled = true
        legend.isEnabled = true
        legend.textColor = surfaceColor
        legend.isWordWrapEnabled = true
        transparentCircleRadius = 0f
        renderer = PieChartRenderer(this, this.animator, this.viewPortHandler)
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        setUsePercentValues(true)
        setHoleColor(transparentColor)
        setHoleColor(transparentColor)
        setHoleColor(transparentColor)
        setTransparentCircleColor(transparentColor)
        setDrawCenterText(true)
        setCenterTextColor(surfaceColor)
        setTransparentCircleAlpha(0)
        setDrawRoundedSlices(false)
        setDrawEntryLabels(false)
        spin(
            SPIN_ANIMATION_DURATION,
            rotationAngle,
            rotationAngle + SPIN_ANIMATION_FULL_SPIN_DEGREES,
            Easing.EaseInOutCubic
        )
        highlightValues(null)
        setOnChartValueSelectedListener(valueChangeListener)
    }
}

@DarkAndLightPreviews
@Composable
fun CakeGraphPreview() {
    val fakeRepo = FakeRepositoryForPreviews(LocalContext.current)
    val data = fakeRepo.getUserSignedInState(true).householdInfo?.graphInfo

    Preview {
        CakeGraph(graphUiModel = data!!, modifier = Modifier.fillMaxWidth())
    }
}
