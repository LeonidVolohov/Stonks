package com.stonks.ui.currency

import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.charts.LineChart
import com.stonks.api.currency.CurrencyApiDataUtils
import com.stonks.ui.chart.StockLineChart
import io.reactivex.disposables.Disposable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

const val DEFAULT_CURRENCY_INDEX = 17
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
     * Загружает в [textView] стоимость [baseRate] относительно [targetRate] со множителем [rateNumber]
     */
    fun setTargetRatePrice(
            baseRate: String,
            targetRate: String,
            rateNumber: EditText,
            textView: TextView
    ) {
        localDisposable = CurrencyApiDataUtils().getTargetRatePrice(baseRate = baseRate)
                .subscribe(
                        { response ->
                            textView.text = stringMultiplication(
                                    rateNumber.text.toString(),
                                    response.rates?.get(targetRate).toString().take(7)
                            )
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
    @RequiresApi(Build.VERSION_CODES.O)
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