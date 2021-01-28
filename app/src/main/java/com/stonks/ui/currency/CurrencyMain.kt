package com.stonks.ui.currency

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.stonks.R
import com.stonks.api.currencyApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.Double.parseDouble
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CurrencyMain : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)

        val TAG: String = CurrencyMain::class.java.name

        val baseRateSpinner = findViewById<Spinner>(R.id.base_rate_spinner)
        val targetRateSpinner = findViewById<Spinner>(R.id.target_rate_spinner)
        val ratesArray = resources.getStringArray(R.array.rates)
        val rateNumberEditText = findViewById<EditText>(R.id.rate_number)
        val resultRateTextView = findViewById<TextView>(R.id.rate_result)
        val dateRateTextView = findViewById<TextView>(R.id.rate_data)
        val calculateButton = findViewById<Button>(R.id.calculate_button)
        val currencyChart = findViewById<LineChart>(R.id.currency_chart)
        lateinit var baseRateSpinnerString: String
        lateinit var targetRateSpinnerString: String
        var isNumeric: Boolean

        currencyChart.setTouchEnabled(true)
        currencyChart.setPinchZoom(true)




        val baseRateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ratesArray)
        baseRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        baseRateSpinner.adapter = baseRateAdapter
        baseRateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                baseRateSpinnerString = ratesArray[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        val targetRateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ratesArray)
        targetRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        targetRateSpinner.adapter = targetRateAdapter
        targetRateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                targetRateSpinnerString = ratesArray[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        loadApiDate(dateRateTextView)

        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val previousMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

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

                    // Get value rates for last month
                    val rateListPerMonth: MutableList<String> = arrayListOf()
                    val compositeDisposable = CompositeDisposable()
                    compositeDisposable.add(
                        currencyApi.getRatesPerMonth(
                            startDate = previousMonth,
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
                                            rateListPerMonth.add(
                                                response.rates.get(date)?.get(
                                                    targetRateSpinnerString
                                                ).toString()
                                            )
                                        }
                                    }
                                    Log.i(TAG, rateListPerMonth.toString())
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
        return (firstNumber.toDouble() * secondNumber.toDouble()).toString().take(7)
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
                        textView.text = response.date.toString()
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
