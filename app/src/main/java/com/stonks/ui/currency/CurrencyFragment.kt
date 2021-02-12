package com.stonks.ui.currency

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.stonks.R
import com.stonks.api.currencyApi
import com.stonks.ui.chart.StockLineChart
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_currency.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CurrencyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_currency, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val defaultCurrencyIndex = 17

        val ratesNameArray = resources.getStringArray(R.array.rates)
        val chartLengthArray = resources.getStringArray(R.array.chart_length)
        val chartPrimaryRatesArray = resources.getStringArray(R.array.primary_rates)
        var baseRateSpinnerString = ""
        var targetRateSpinnerString = ""
        var chartLengthSpinnerString = ""
        var isNumeric: Boolean
        val currencyLineChart = StockLineChart(currency_chart)

        first_currency_name.text = chartPrimaryRatesArray[0]
        second_currency_name.text = chartPrimaryRatesArray[1]
        third_currency_name.text = chartPrimaryRatesArray[2]
        fourth_currency_name.text = chartPrimaryRatesArray[3]
        fifth_currency_name.text = chartPrimaryRatesArray[4]

        loadApiDate(last_date_update)

        base_rate_spinner?.setSelection(defaultCurrencyIndex)
        base_rate_spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                baseRateSpinnerString = ratesNameArray[position].split(",")[0]
                rate_number.setText("1.0")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        target_rate_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                targetRateSpinnerString = ratesNameArray[position].split(",")[0]
                rate_number.setText("1.0")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        chart_length_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                chartLengthSpinnerString = chartLengthArray[position]
                rate_number.setText("1.0")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        calculate_button.setOnClickListener {
            if (baseRateSpinnerString == targetRateSpinnerString) {
                rate_result.text = stringMultiplication(
                    rate_number.text.toString(),
                    "1.0"
                )
            } else {
                isNumeric = try {
                    rate_number.text.toString().toDouble()
                    true
                } catch (exception: NumberFormatException) {
                    false
                }

                if (isNumeric) {
                    loadTargetRatePrice(
                        baseRateSpinnerString,
                        targetRateSpinnerString,
                        rate_number,
                        rate_result
                    )

                    var startPointDate = ""
                    when (chartLengthSpinnerString) {
                        chartLengthArray[0] -> startPointDate =
                            LocalDate.now().minusWeeks(1).format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            )
                        chartLengthArray[1] -> startPointDate =
                            LocalDate.now().minusMonths(1).format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            )
                        chartLengthArray[2] -> startPointDate =
                            LocalDate.now().minusMonths(6).format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            )
                        chartLengthArray[3] -> startPointDate =
                            LocalDate.now().minusYears(1).format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            )
                        chartLengthArray[4] -> startPointDate =
                            LocalDate.now().minusYears(5).format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            )
                    }

                    val rateListPerPeriod: MutableList<String> = arrayListOf()
                    val compositeDisposable = CompositeDisposable()
                    compositeDisposable.add(
                        currencyApi.getRatesPerPeriod(
                            startDate = startPointDate,
                            endDate = currentMonth,
                            targetRate = targetRateSpinnerString,
                            baseRate = baseRateSpinnerString
                        )
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                { response ->
                                    val dateList: List<String>? =
                                        response.rates?.keys?.sortedBy {
                                            LocalDate.parse(
                                                it, DateTimeFormatter.ofPattern(
                                                    "yyyy-MM-dd"
                                                )
                                            )
                                        }
                                    if (dateList != null) {
                                        for (date in dateList) {
                                            rateListPerPeriod.add(
                                                response.rates[date]?.get(
                                                    targetRateSpinnerString
                                                ).toString()
                                            )
                                        }
                                    }

                                    val entries = rateListPerPeriod.toList()
                                    currencyLineChart.setXAxis(
                                        lineChart = currency_chart,
                                        dateList = dateList
                                    )
                                    val lineChartData = currencyLineChart.getLineData(
                                        entries = entries,
                                        baseCurrency = baseRateSpinnerString,
                                        targetCurrency = targetRateSpinnerString
                                    )
                                    currencyLineChart.displayChart(
                                        lineChart = currency_chart,
                                        lineChartData = lineChartData
                                    )
                                },
                                { failure ->
                                    Toast.makeText(
                                        context,
                                        "No information from server: ${failure.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                    )

                    first_primary_currency_result.text = ""
                    second_primary_currency_result.text = ""
                    third_primary_currency_result.text = ""
                    fourth_primary_currency_result.text = ""
                    fifth_primary_currency_result.text = ""
                    val compositeDisposablePrimaryCurrencies = CompositeDisposable()
                    compositeDisposablePrimaryCurrencies.add(
                        currencyApi.getPrimaryRatesPerDay(
                            baseRate = baseRateSpinnerString,
                            primaryCurrencies = "USD,EUR,GBP,JPY,CHF"
                        )
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                { response ->
                                    first_primary_currency_result.text = stringMultiplication(
                                        rate_number.text.toString(),
                                        response.rates?.get("USD").toString()
                                    )

                                    second_primary_currency_result.text = stringMultiplication(
                                        rate_number.text.toString(),
                                        response.rates?.get("EUR").toString()
                                    )

                                    third_primary_currency_result.text = stringMultiplication(
                                        rate_number.text.toString(),
                                        response.rates?.get("GBP").toString()
                                    )

                                    fourth_primary_currency_result.text = stringMultiplication(
                                        rate_number.text.toString(),
                                        response.rates?.get("JPY").toString()
                                    )

                                    fifth_primary_currency_result.text = stringMultiplication(
                                        rate_number.text.toString(),
                                        response.rates?.get("CHF").toString()
                                    )
                                },
                                { failure ->
                                    Toast.makeText(
                                        context,
                                        "No information from server: ${failure.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            )
                    )
                } else {
                    Toast.makeText(context, "Wrong input", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Умножает две строки [firstNumber]  и [secondNumber] и возвращает только первые 7 символов
     */
    private fun stringMultiplication(firstNumber: String, secondNumber: String): String {
        //return (firstNumber.toDouble() * secondNumber.toDouble()).toString().take(7)
        return BigDecimal((firstNumber.toDouble() * secondNumber.toDouble())).setScale(
            3,
            BigDecimal.ROUND_HALF_EVEN
        ).toString()

    }

    /**
     * Загружает в [textView] дату, полученную с API
     * */
    private fun loadApiDate(textView: TextView) {
        val compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            currencyApi.getRatesPerDay(rate = "EUR")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { response ->
                        textView.text =
                            getString(R.string.last_updated_date, response.date.toString())
                    },
                    { failure ->
                        Toast.makeText(context, failure.message, Toast.LENGTH_SHORT).show()
                    }
                )
        )
    }

    /**
     * Загружает в [textView] стоимость [baseRate] относительно [targetRate] со множителем [rateNumber]
     */
    private fun loadTargetRatePrice(
        baseRate: String,
        targetRate: String,
        rateNumber: EditText,
        textView: TextView
    ) {
        val compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            currencyApi.getRatesPerDay(rate = baseRate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { response ->
                        textView.text = stringMultiplication(
                            rateNumber.text.toString(),
                            response.rates?.get(targetRate).toString().take(7)
                        )
                    },
                    { failure ->
                        Toast.makeText(
                            context,
                            failure.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
        )
    }
}