package com.stonks.ui.currency

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.stonks.R
import com.stonks.api.currencyApi
import com.stonks.ui.chart.StockLineChart
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.Double.parseDouble
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CurrencyMain : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)

        val TAG: String = CurrencyMain::class.java.name
        val defaultCurrencyIndex = 17

        val baseRateSpinner = findViewById<Spinner>(R.id.base_rate_spinner)
        val targetRateSpinner = findViewById<Spinner>(R.id.target_rate_spinner)
        val chartLengthSpinner = findViewById<Spinner>(R.id.chart_length_spinner)
        val ratesNameArray = resources.getStringArray(R.array.rates)
        val chartLengthArray = resources.getStringArray(R.array.chart_length)
        val chartPrimaryRatesArray = resources.getStringArray(R.array.primary_rates)
        val rateNumberEditText = findViewById<EditText>(R.id.rate_number)
        val resultRateTextView = findViewById<TextView>(R.id.rate_result)
        val dateRateTextView = findViewById<TextView>(R.id.last_date_update)
        val calculateButton = findViewById<Button>(R.id.calculate_button)
        val lineChart = findViewById<LineChart>(R.id.currency_chart)
        val firstPrimaryCurrencyText = findViewById<TextView>(R.id.first_currency_name)
        val secondPrimaryCurrencyText = findViewById<TextView>(R.id.second_currency_name)
        val thirdPrimaryCurrencyText = findViewById<TextView>(R.id.third_currency_name)
        val fourthPrimaryCurrencyText = findViewById<TextView>(R.id.fourth_currency_name)
        val fifthPrimaryCurrencyText = findViewById<TextView>(R.id.fifth_currency_name)
        val firstPrimaryCurrencyResult = findViewById<TextView>(R.id.first_primary_currency_result)
        val secondPrimaryCurrencyResult = findViewById<TextView>(R.id.second_primary_currency_result)
        val thirdPrimaryCurrencyResult = findViewById<TextView>(R.id.third_primary_currency_result)
        val fourthPrimaryCurrencyResult = findViewById<TextView>(R.id.fourth_primary_currency_result)
        val fifthPrimaryCurrencyResult = findViewById<TextView>(R.id.fifth_primary_currency_result)
        var baseRateSpinnerString = ""
        var targetRateSpinnerString = ""
        var chartLengthSpinnerString = ""
        var isNumeric: Boolean
        val currencyLineChart = StockLineChart(lineChart)

        firstPrimaryCurrencyText.text = chartPrimaryRatesArray[0]
        secondPrimaryCurrencyText.text = chartPrimaryRatesArray[1]
        thirdPrimaryCurrencyText.text = chartPrimaryRatesArray[2]
        fourthPrimaryCurrencyText.text = chartPrimaryRatesArray[3]
        fifthPrimaryCurrencyText.text = chartPrimaryRatesArray[4]

        loadApiDate(dateRateTextView)

        baseRateSpinner.setSelection(defaultCurrencyIndex)
        baseRateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                baseRateSpinnerString = ratesNameArray[position].split(",")[0]
                rateNumberEditText.setText("1.0")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        targetRateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                targetRateSpinnerString = ratesNameArray[position].split(",")[0]
                rateNumberEditText.setText("1.0")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        chartLengthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                chartLengthSpinnerString = chartLengthArray[position]
                rateNumberEditText.setText("1.0")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        calculateButton.setOnClickListener {
            if (baseRateSpinnerString == targetRateSpinnerString) {
                resultRateTextView.text = stringMultiplication(
                        rateNumberEditText.text.toString(),
                        "1.0"
                )
            } else {
                isNumeric = try {
                    parseDouble(rateNumberEditText.text.toString())
                    true
                } catch (exception: NumberFormatException) {
                    false
                }

                if (isNumeric) {
                    loadTargetRatePrice(
                            baseRateSpinnerString,
                            targetRateSpinnerString,
                            rateNumberEditText,
                            resultRateTextView
                    )

                    var startPointDate = ""
                    when (chartLengthSpinnerString) {
                        chartLengthArray[0] -> startPointDate = LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        chartLengthArray[1] -> startPointDate = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        chartLengthArray[2] -> startPointDate = LocalDate.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        chartLengthArray[3] -> startPointDate = LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        chartLengthArray[4] -> startPointDate = LocalDate.now().minusYears(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
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
                                                        lineChart = lineChart,
                                                        dateList = dateList
                                                )
                                                val lineChartData = currencyLineChart.getLineData(
                                                        entries = entries,
                                                        baseCurrency = baseRateSpinnerString,
                                                        targetCurrency = targetRateSpinnerString
                                                )
                                                currencyLineChart.displayChart(
                                                        lineChart = lineChart,
                                                        lineChartData = lineChartData
                                                )
                                            },
                                            { failure ->
                                                Toast.makeText(
                                                        this@CurrencyMain,
                                                        "No information from server: ${failure.message}",
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    )
                    )

                    firstPrimaryCurrencyResult.text = ""
                    secondPrimaryCurrencyResult.text = ""
                    thirdPrimaryCurrencyResult.text = ""
                    fourthPrimaryCurrencyResult.text = ""
                    fifthPrimaryCurrencyResult.text = ""
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
                                                firstPrimaryCurrencyResult.text = stringMultiplication(
                                                        rateNumberEditText.text.toString(),
                                                        response.rates?.get("USD").toString()
                                                )

                                                secondPrimaryCurrencyResult.text = stringMultiplication(
                                                        rateNumberEditText.text.toString(),
                                                        response.rates?.get("EUR").toString()
                                                )

                                                thirdPrimaryCurrencyResult.text = stringMultiplication(
                                                        rateNumberEditText.text.toString(),
                                                        response.rates?.get("GBP").toString()
                                                )

                                                fourthPrimaryCurrencyResult.text = stringMultiplication(
                                                        rateNumberEditText.text.toString(),
                                                        response.rates?.get("JPY").toString()
                                                )

                                                fifthPrimaryCurrencyResult.text = stringMultiplication(
                                                        rateNumberEditText.text.toString(),
                                                        response.rates?.get("CHF").toString()
                                                )
                                            },
                                            { failure ->
                                                Toast.makeText(
                                                        this@CurrencyMain,
                                                        "No information from server: ${failure.message}",
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                    )
                    )
                } else {
                    Toast.makeText(this, "Wrong input", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    /**
     * Умножает две строки [firstNumber]  и [secondNumber] и возвращает только первые 7 символов
     */
    private fun stringMultiplication(firstNumber: String, secondNumber: String) : String {
        //return (firstNumber.toDouble() * secondNumber.toDouble()).toString().take(7)
        return BigDecimal((firstNumber.toDouble() * secondNumber.toDouble())).setScale(3, BigDecimal.ROUND_HALF_EVEN).toString()

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
                                    textView.text = getString(R.string.last_updated_date, response.date.toString())
                                },
                                { failure ->
                                    Toast.makeText(this, failure.message, Toast.LENGTH_SHORT).show()
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
                            this@CurrencyMain,
                            failure.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
        )
    }
}
