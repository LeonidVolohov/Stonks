package com.stonks.ui.chart

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.stonks.R
import java.math.BigDecimal

class StockLineChart(lineChart: LineChart) {
    private var localLineChart = lineChart

    init {
        setChartSetting(lineChart = localLineChart)
    }

    private fun setChartSetting(lineChart: LineChart) {
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setDrawGridBackground(true)
        lineChart.setBorderColor(Color.GRAY)
        lineChart.setBorderWidth(5f)
        lineChart.description.text = ""

        lineChart.legend.isEnabled = true
        lineChart.legend.form = Legend.LegendForm.LINE
        lineChart.legend.textSize = 16f
        lineChart.legend.formSize = 12f
    }

    fun setXAxis(lineChart: LineChart, dateList: List<String>?) {
        lineChart.xAxis.position = XAxis.XAxisPosition.TOP
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(dateList)
        lineChart.xAxis.textSize = 10f
        lineChart.xAxis.labelCount = 4
    }

    fun getLineData(entries: List<Double>, baseCurrency: String, targetCurrency: String): LineDataSet {
        val localEntry = ArrayList<Entry>()
        var iter = -1.0f
        for (item in entries) {
            iter += 1.0f
            localEntry.add(Entry(iter, BigDecimal(item).setScale(5, BigDecimal.ROUND_HALF_EVEN).toFloat()))
        }

        val lineChartData = LineDataSet(localEntry, "$baseCurrency to $targetCurrency")
        lineChartData.lineWidth = 4f
        lineChartData.setDrawCircles(false)
        lineChartData.setDrawCircleHole(false)
        lineChartData.valueTextSize = 0f
        lineChartData.color = R.color.purple_500

        return lineChartData
    }

    fun getPredictionLineData(predictionEntries: List<Double>): LineDataSet {
        val localPredictionEntry = ArrayList<Entry>()
        var iter = -1.0f
        for (item in predictionEntries) {
            iter += 1.0f
            localPredictionEntry.add(
                Entry(
                    iter,
                    BigDecimal(item).setScale(5, BigDecimal.ROUND_HALF_EVEN).toFloat()
                )
            )
        }

        val lineChartPredictionData = LineDataSet(localPredictionEntry, "")
        lineChartPredictionData.lineWidth = 2f
        lineChartPredictionData.setDrawCircles(false)
        lineChartPredictionData.setDrawCircleHole(false)
        lineChartPredictionData.valueTextSize = 0f
        lineChartPredictionData.color = R.color.purple_500
        lineChartPredictionData.enableDashedLine(10f, 10f, 0f)

        return lineChartPredictionData
    }

    fun displayChart(lineChart: LineChart, lineChartData: LineDataSet) {
        // lineChart.marker = dateList?.let { it -> CustomMarkerView(this, R.layout.activity_textview_content, it) }
        lineChart.onTouchListener.setLastHighlighted(null)
        lineChart.highlightValues(null)
        lineChart.data = LineData(lineChartData)
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    fun displayPredictionChart(
        lineChart: LineChart,
        lineChartData: LineDataSet,
        lineChartPredictionData: LineDataSet
    ) {
        val chartData = LineData()
        lineChartPredictionData.setDrawFilled(true)
        lineChartPredictionData.fillAlpha = 128
        chartData.addDataSet(lineChartData)
        chartData.addDataSet(lineChartPredictionData)
        // lineChart.marker = dateList?.let { it -> CustomMarkerView(this, R.layout.activity_textview_content, it) }
        lineChart.onTouchListener.setLastHighlighted(null)
        lineChart.highlightValues(null)
        lineChart.data = chartData
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    fun displayPredictionChartTransparent(
        lineChart: LineChart,
        lineChartData: LineDataSet,
        lineChartPredictionDataMin: LineDataSet,
        lineChartPredictionDataMax: LineDataSet
    ) {
        val chartData = LineData()
        lineChartPredictionDataMax.setDrawFilled(true)
        lineChartPredictionDataMax.fillAlpha = 96
        lineChartPredictionDataMin.setDrawFilled(true)
        lineChartPredictionDataMin.fillColor = Color.TRANSPARENT
        chartData.addDataSet(lineChartData)
        chartData.addDataSet(lineChartPredictionDataMax)
        chartData.addDataSet(lineChartPredictionDataMin)
        lineChart.onTouchListener.setLastHighlighted(null)
        lineChart.highlightValues(null)
        lineChart.data = chartData
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    fun clearChart() {
        this.localLineChart.invalidate()
        this.localLineChart.clear()
    }
}
