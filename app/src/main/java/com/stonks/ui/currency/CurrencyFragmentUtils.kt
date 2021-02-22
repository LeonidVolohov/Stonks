package com.stonks.ui.currency

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.stonks.api.currency.CurrencyApiDataUtils
import com.stonks.ui.chart.StockLineChart
import io.reactivex.disposables.Disposable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

const val PRIMARY_RATES = "USD,EUR,GBP,JPY,CHF"

class CurrencyFragmentUtils(disposable: Disposable?) {
    private val TAG = CurrencyFragmentUtils::class.java.name
    private var localDisposable = disposable

    /**
     * Умножает две строки [firstNumber]  и [secondNumber] и возвращает только первые 7 символов
     */
    fun stringMultiplication(
            firstNumber: String,
            secondNumber: String
    ): String {
        return BigDecimal((firstNumber.toDouble() * secondNumber.toDouble())).setScale(
                3,
                BigDecimal.ROUND_HALF_EVEN
        ).toString()

    }

    /**
     * Возвращает  количество знаков после запятой равное [scale]
     */
    private fun doubleScale(number: Double, scale: Int): String {
        return BigDecimal(number).setScale(scale, BigDecimal.ROUND_HALF_EVEN).toString()
    }

    /**
     * Загружает в [textView] дату, полученную с API
     * */
    fun setLastUpdatedDate(
            textView: TextView,
            inputText: String
    ) {
        localDisposable = CurrencyApiDataUtils().getLastUpdatedDate()
                .subscribe(
                        { response ->
                            // TODO: Resources getString doesn`t see string from R.string
                            // TODO: Remove inputText param if fixed
                            //textView.text = Resources.getSystem().getString(R.string.last_updated_date, response.date.toString())
                            textView.text = inputText + response.date.toString()
                        },
                        { failure ->
                            // TODO: Add Toast message
                            Log.e(TAG, failure.message.toString())
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
            differenceRateTextView: TextView
    ) {
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

                                resultRateTextView.text = doubleScale(rateList[rateList.size - 1], 3)

                                val differenceAboveZero = (rateList[rateList.size - 1] - rateList[0])
                                if (differenceAboveZero > 0) {
                                    differenceRateTextView.text = "(" + "+" + doubleScale(differenceAboveZero, 3) + ")"
                                    differenceRateTextView.setTextColor(Color.GREEN)
                                } else {
                                    differenceRateTextView.text = "(" + doubleScale(differenceAboveZero, 3) + ")"
                                    differenceRateTextView.setTextColor(Color.RED)
                                }

                            } else {
                                resultRateTextView.text = stringMultiplication(
                                        rateNumberEditText.text.toString(),
                                        doubleScale(rateList[rateList.size - 1], 3)
                                )
                            }
                        },
                        { failure ->
                            // TODO: Add toast message
                            Log.e(TAG, failure.message.toString())
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
            rateNumber: EditText
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
                            // TODO: Add toast
                            Log.e(TAG, failure.message.toString())
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
            currencyChart: LineChart
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
                // TODO: Change variables to values from calendar
                startLocalDate = startDate
                endLocalDate = endDate
            }
        }

        localDisposable = CurrencyApiDataUtils().getRatesPerPeriod(
                startDate = startLocalDate,
                endDate = endLocalDate,
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

                            val entries = rateList.toList()
                            stockLineChart.setXAxis(
                                    lineChart = currencyChart,
                                    dateList = dateList
                            )
                            val lineChartData = stockLineChart.getLineData(
                                    entries = entries,
                                    baseCurrency = baseRate,
                                    targetCurrency = targetRate
                            )
                            stockLineChart.displayChart(
                                    lineChart = currencyChart,
                                    lineChartData = lineChartData
                            )
                        },
                        { failure ->
                            // TODO: Add toast message
                            Log.e(TAG, failure.message.toString())
                        }
                )
    }
}
