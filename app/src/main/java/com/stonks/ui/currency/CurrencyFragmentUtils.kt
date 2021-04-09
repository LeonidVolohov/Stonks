package com.stonks.ui.currency

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.LineChart
import com.stonks.R
import com.stonks.api.currency.CurrencyApiDataUtils
import com.stonks.api.currencyalphavantage.CurrencyAlphavantageUtils
import com.stonks.calculations.Prediction
import com.stonks.ui.UiConstants.Companion.DEFAULT_DECIMAL_POINT_PRECISION
import com.stonks.ui.chart.StockLineChart
import io.reactivex.disposables.Disposable
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*

const val PRIMARY_RATES = "USD,EUR,GBP,JPY,CHF"

class CurrencyFragmentUtils(disposable: Disposable?) {
    private val TAG = this::class.java.name
    private var localDisposable = disposable

    /**
     * Умножает две строки [firstNumber]  и [secondNumber] и возвращает только первые 7 символов
     */
    fun stringMultiplication(
            firstNumber: String,
            secondNumber: String
    ): String {
        return BigDecimal((firstNumber.toDouble() * secondNumber.toDouble())).setScale(
                DEFAULT_DECIMAL_POINT_PRECISION,
                BigDecimal.ROUND_HALF_EVEN
        ).toString()
    }

    /**
     * Возвращает  количество знаков после запятой равное [DEFAULT_DECIMAL_POINT_PRECISION]
     */
    fun doubleScale(number: Double): String {
        return BigDecimal(number).setScale(
            DEFAULT_DECIMAL_POINT_PRECISION,
            BigDecimal.ROUND_HALF_EVEN
        ).toString()
    }

    /**
     * Загружает в [textView] дату, полученную с API
     * */
    fun setLastUpdatedDate(
        textView: TextView,
        context: Context
    ) {
        localDisposable = CurrencyApiDataUtils().getLastUpdatedDate()
                .subscribe(
                        { response ->
                            textView.text = context.getString(R.string.last_updated_date, response.date.toString())
                        },
                        { failure ->
                            Toast.makeText(context, failure.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                )
    }

    /**
     * Загружает в [resultRateTextView] стоимость [baseRate] относительно [targetRate] со множителем [rateNumberEditText]
     */
    fun setTargetRatePrice(
            baseRate: String,
            targetRate: String,
            startDate: String = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            endDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            rateNumberEditText: EditText,
            resultRateTextView: TextView,
            differenceRateTextView: TextView,
            context: Context
    ) {
        // TODO:  if (rateNumberEditText.text.toString() == (1.0).toString()) then second request, else first
        /*localDisposable = CurrencyApiDataUtils().getTargetRatePrice(baseRate = baseRate)
                .subscribe(
                        { response ->
                            textView.text = stringMultiplication(
                                    rateNumber.text.toString(),
                                    response.rates?.get(targetRate).toString().take(7)
                            )
                        },
                        { failure ->
                            Log.e(TAG, failure.message.toString())
                        }
                )*/

        localDisposable = CurrencyApiDataUtils().getRatesPerPeriod(
                startDate = startDate,
                endDate = endDate,
                baseRate = baseRate,
                targetRate = targetRate
        )
                .subscribe(
                        { response ->
                            val rateList: MutableList<Double> = arrayListOf()
                            val dateList: List<String>? = response.rates?.keys?.toList()

                            if (dateList != null) {
                                for (date in dateList) {
                                    response.rates[date]?.get(
                                            targetRate
                                    )?.let { it ->
                                        rateList.add(it)
                                    }
                                }
                            }

                            if (rateNumberEditText.text.toString() == (1.0).toString()) {
                                val param = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 50f)
                                rateNumberEditText.layoutParams = param
                                differenceRateTextView.layoutParams = param

                                param.marginEnd = 8
                                resultRateTextView.layoutParams = param

                                differenceRateTextView.visibility = View.VISIBLE

                                resultRateTextView.text = doubleScale(rateList[rateList.size - 1])

                                val differenceAboveZero = (rateList[rateList.size - 1] - rateList[0])
                                if (differenceAboveZero > 0) {
                                    differenceRateTextView.text = "(" + "+" + doubleScale(differenceAboveZero) + ")"
                                    differenceRateTextView.setTextColor(Color.GREEN)
                                } else {
                                    differenceRateTextView.text = "(" + doubleScale(differenceAboveZero) + ")"
                                    differenceRateTextView.setTextColor(Color.RED)
                                }

                            } else {
                                resultRateTextView.text = stringMultiplication(
                                        rateNumberEditText.text.toString(),
                                        doubleScale(rateList[rateList.size - 1])
                                )
                            }
                        },
                        { failure ->
                            Toast.makeText(context, failure.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                )
    }

    /**
     * Загружает в textView стоимость [baseRate] относительно [symbols] со множителем [rateNumber]
     */
    fun setPrimaryRatesPerDay(
        baseRate: String,
        symbols: String,
        firstTextView: TextView,
        secondTextView: TextView,
        thirdTextView: TextView,
        fourthTextView: TextView,
        fifthTextView: TextView,
        rateNumber: EditText,
        context: Context
    ) {
        localDisposable = CurrencyApiDataUtils().getPrimaryRatesPerDay(
                baseRate = baseRate,
                symbols = symbols
        )
                .subscribe(
                    { response ->
                        firstTextView.text = stringMultiplication(
                            rateNumber.text.toString(),
                            response.rates?.get("USD").toString()
                        )

                        secondTextView.text = stringMultiplication(
                            rateNumber.text.toString(),
                            response.rates?.get("EUR").toString()
                        )

                        thirdTextView.text = stringMultiplication(
                            rateNumber.text.toString(),
                            response.rates?.get("GBP").toString()
                        )

                        fourthTextView.text = stringMultiplication(
                            rateNumber.text.toString(),
                            response.rates?.get("JPY").toString()
                        )

                        fifthTextView.text = stringMultiplication(
                            rateNumber.text.toString(),
                            response.rates?.get("CHF").toString()
                        )
                    },
                    { failure ->
                        Toast.makeText(context, failure.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                )
    }

    /**
     * Выводи график в элемент [currencyChart] объекта класса [stockLineChart].
     * Период графика составляет с [startDate] по [endDate] и рисует стоимость [baseRate] относительно [targetRate]
     */
    fun plotRatesPerPeriod(
        startDate: String,
        endDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        baseRate: String,
        targetRate: String,
        stockLineChart: StockLineChart,
        currencyChart: LineChart,
        isPrediction: Boolean = false,
        context: Context
    ) {
        var startLocalDate = ""
        var endLocalDate = endDate
        when (startDate) {
            "1W" ->
                startLocalDate = LocalDate.now().minusWeeks(1).format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                )
            "1M" ->
                startLocalDate = LocalDate.now().minusMonths(1).format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                )
            "6M" ->
                startLocalDate = LocalDate.now().minusMonths(6).format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                )
            "1Y" ->
                startLocalDate = LocalDate.now().minusYears(1).format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                )
            "5Y" ->
                startLocalDate = LocalDate.now().minusYears(5).format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                )
            else -> {
                startLocalDate = startDate
                endLocalDate = endDate
            }
        }

        localDisposable = CurrencyAlphavantageUtils().getDataForPeriod(
            fromCurrencyName = baseRate,
            toCurrencyName = targetRate
        )
            .subscribe(
                { response ->

                    val rateList: MutableList<Double> = arrayListOf()
                    val dateList: MutableList<String>? =
                        response.data.keys.toMutableList()
                    val processedDateList: MutableList<String>? = mutableListOf()

                    if (dateList != null) {
                        for (date in dateList) {
                            if ((LocalDate.parse(date)
                                    .isEqual(LocalDate.parse(startLocalDate)) || LocalDate.parse(
                                    date
                                ).isAfter(LocalDate.parse(startLocalDate))) &&
                                LocalDate.parse(date)
                                    .isBefore(LocalDate.parse(endLocalDate)) || LocalDate.parse(date)
                                    .isEqual(LocalDate.parse(startLocalDate))
                            ) {
                                response.data[date]?.get("4. close")?.toDouble()
                                    ?.let { rateList.add(it) }
                                processedDateList?.add(date)
                            }
                        }
                    }

                    val entries = rateList.toList()

                    stockLineChart.setXAxis(
                        lineChart = currencyChart,
                        dateList = processedDateList?.toList(),
                    )

                    val lineChartData = stockLineChart.getLineData(
                        entries = entries,
                        baseCurrency = baseRate,
                        targetCurrency = targetRate
                    )

                    if (!isPrediction) {
                        stockLineChart.displayChart(
                            lineChart = currencyChart,
                            lineChartData = lineChartData
                        )
                    } else {
                        val predictionRateListMax: MutableList<Double> =
                            rateList.toMutableList()
                        val predictionRateListMin: MutableList<Double> =
                            rateList.toMutableList()
                        val dateListLong: ArrayList<Long> = arrayListOf()
                        if (processedDateList != null) {
                            for (item in processedDateList) {
                                dateListLong.add(
                                    (SimpleDateFormat("yyyy-MM-dd").parse(item).toInstant()
                                        .toEpochMilli())
                                )
                            }
                        }

                        val currentDaysInMonth =
                            Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
                        val predictionRate = Prediction().datePrediction(
                            dateListLong.toTypedArray(),
                            entries.toTypedArray(),
                            currentDaysInMonth
                        )
                        val predictionRateMax = predictionRate * 1.5
                        val predictionRateMin = predictionRate * 0.5
                        for (i in 1..currentDaysInMonth) {
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val predictionDate: String = (LocalDate
                                .parse(
                                    processedDateList?.last() ?: "2020-12-20",
                                    formatter
                                ) + Period.ofDays(i))
                                .format(formatter)
                            processedDateList?.add(predictionDate.toString())
                            predictionRateListMax.add(rateList.last() + (predictionRateMax - rateList.last()) / currentDaysInMonth * i)
                            predictionRateListMin.add(rateList.last() + (predictionRateMin - rateList.last()) / currentDaysInMonth * i)
                        }

                        val lineChartPredictionDataMax = stockLineChart.getPredictionLineData(
                            predictionEntries = predictionRateListMax.toList()
                        )
                        val lineChartPredictionDataMin = stockLineChart.getPredictionLineData(
                            predictionEntries = predictionRateListMin.toList()
                        )

                        stockLineChart.displayPredictionChartTransparent(
                            lineChart = currencyChart,
                            lineChartData = lineChartData,
                            lineChartPredictionDataMax = lineChartPredictionDataMax,
                            lineChartPredictionDataMin = lineChartPredictionDataMin
                        )
                    }
                },
                { failure ->
                    Toast.makeText(context, failure.message, Toast.LENGTH_SHORT).show()
                }
            )

/*        localDisposable = CurrencyApiDataUtils().getRatesPerPeriod(
                startDate = startLocalDate,
                endDate = endLocalDate,
                baseRate = baseRate,
                targetRate = targetRate
        )
                .subscribe(
                    { response ->
                        val rateList: MutableList<Double> = arrayListOf()
                        val dateList: MutableList<String>? =
                            response.rates?.keys?.toMutableList()

                        if (dateList != null) {
                            for (date in dateList) {
                                response.rates[date]?.get(
                                    targetRate
                                )?.let { it ->
                                    rateList.add(it)
                                }
                            }
                        }

                        val entries = rateList.toList()

                        stockLineChart.setXAxis(
                            lineChart = currencyChart,
                            dateList = dateList?.toList(),
                        )

                        val lineChartData = stockLineChart.getLineData(
                            entries = entries,
                            baseCurrency = baseRate,
                            targetCurrency = targetRate
                        )

                        if (!isPrediction) {
                            stockLineChart.displayChart(
                                lineChart = currencyChart,
                                lineChartData = lineChartData
                            )
                        } else {
                            val predictionRateListMax: MutableList<Double> =
                                rateList.toMutableList()
                            val predictionRateListMin: MutableList<Double> =
                                rateList.toMutableList()
                            val dateListLong: ArrayList<Long> = arrayListOf()
                            if (dateList != null) {
                                for (item in dateList) {
                                    dateListLong.add(
                                        (SimpleDateFormat("yyyy-MM-dd").parse(item).toInstant()
                                            .toEpochMilli())
                                    )
                                }
                            }

//                            val predictionDate = LocalDate.now().plusMonths(1)
//                            dateList?.add(predictionDate.toString())

                            val currentDaysInMonth =
                                Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
                            val predictionRate = Prediction().datePrediction(
                                dateListLong.toTypedArray(),
                                entries.toTypedArray(),
                                currentDaysInMonth
                            )
                            val predictionRateMax = predictionRate * 1.5
                            val predictionRateMin = predictionRate * 0.5
                            for (i in 1..currentDaysInMonth) {
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val predictionDate: String = (LocalDate
                                    .parse(
                                        dateList?.last() ?: "2020-12-20",
                                        formatter
                                    ) + Period.ofDays(i))
                                    .format(formatter)
                                dateList?.add(predictionDate.toString())
                                predictionRateListMax.add(rateList.last() + (predictionRateMax - rateList.last()) / currentDaysInMonth * i)
                                predictionRateListMin.add(rateList.last() + (predictionRateMin - rateList.last()) / currentDaysInMonth * i)
                            }

                            val lineChartPredictionDataMax = stockLineChart.getPredictionLineData(
                                predictionEntries = predictionRateListMax.toList()
                            )
                            val lineChartPredictionDataMin = stockLineChart.getPredictionLineData(
                                predictionEntries = predictionRateListMin.toList()
                            )

                            stockLineChart.displayPredictionChartTransparent(
                                lineChart = currencyChart,
                                lineChartData = lineChartData,
                                lineChartPredictionDataMax = lineChartPredictionDataMax,
                                lineChartPredictionDataMin = lineChartPredictionDataMin
                            )
                        }
                    },
                    { failure ->
                        Toast.makeText(context, failure.message, Toast.LENGTH_SHORT).show()
                    }
                )*/
    }
}
