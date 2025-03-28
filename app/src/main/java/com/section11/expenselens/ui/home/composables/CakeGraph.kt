package com.section11.expenselens.ui.home.composables

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
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
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview
import com.section11.expenselens.ui.utils.formatToTwoDecimal

private const val SPIN_ANIMATION_DURATION = 1000
private const val SPIN_ANIMATION_FULL_SPIN_DEGREES = 360
private const val SLICE_ENLARGE_ON_TAP_AMOUNT = 8f
private const val SLICE_SPACING = 5f
private const val SIXTY_PERCENT = 0.6f
private const val HOLE_RADIUS = 40f

@Composable
fun CakeGraph(
    graphUiModel: CakeGraphUiModel,
    modifier: Modifier = Modifier
) {
    val transparent = Color.Transparent.toArgb()
    var titleLabel by remember { mutableStateOf(graphUiModel.titleLabel) }
    var valueLabel by remember { mutableStateOf(graphUiModel.valueLabel) }
    val colorList = ArrayList<Int>()
    for (c in ColorTemplate.PASTEL_COLORS) colorList.add(c)

    var pieChartUnselectCallback: () -> Unit = {}
    val valueChangeListener = object : OnChartValueSelectedListener {
        override fun onValueSelected(entry: Entry?, highlight: Highlight?) {
            titleLabel = entry?.data.toString()
            valueLabel = "\$${entry?.y?.formatToTwoDecimal()}"

        }
        override fun onNothingSelected() {
            titleLabel = graphUiModel.titleLabel
            valueLabel = graphUiModel.valueLabel
        }
    }
    val dimens = LocalDimens.current

    Column(
        modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    titleLabel = graphUiModel.titleLabel
                    valueLabel = graphUiModel.valueLabel
                    pieChartUnselectCallback()
                })
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AndroidView(
            modifier = Modifier
                .padding(horizontal = dimens.m2)
                .fillMaxWidth()
                .weight(SIXTY_PERCENT),
            factory = { context ->
                getPieChart(context, transparent, valueChangeListener)
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

                pieChart.data = pieData
                pieChart.invalidate()
                pieChartUnselectCallback =  { pieChart.highlightValues(emptyArray()) }
            }
        )
        Spacer(Modifier.height(dimens.m2))
        Text(titleLabel, style = MaterialTheme.typography.titleLarge)
        Text(valueLabel)
    }
}

private fun getPieChart(
    context: Context,
    transparentColor: Int,
    valueChangeListener: OnChartValueSelectedListener
): PieChart {
    return PieChart(context).apply {
        description.isEnabled = false
        isDrawHoleEnabled = true
        transparentCircleRadius = 0f
        holeRadius = HOLE_RADIUS
        legend.isEnabled = false
        renderer = PieChartRenderer(this, this.animator, this.viewPortHandler)
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        setUsePercentValues(true)
        setHoleColor(transparentColor)
        setTransparentCircleColor(transparentColor)
        setTransparentCircleAlpha(0)
        setDrawRoundedSlices(false)
        setDrawEntryLabels(false)
        spin(
            SPIN_ANIMATION_DURATION,
            rotationAngle,
            rotationAngle + SPIN_ANIMATION_FULL_SPIN_DEGREES,
            Easing.EaseInOutCubic
        )
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
