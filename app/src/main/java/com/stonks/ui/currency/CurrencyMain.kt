package com.stonks.ui.currency

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.stonks.R
import com.stonks.api.currencyApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.Double.parseDouble

class CurrencyMain : AppCompatActivity() {

    private val TAG : String = CurrencyMain::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)

        val baseRateSpinner = findViewById<Spinner>(R.id.base_rate_spinner)
        val targetRateSpinner = findViewById<Spinner>(R.id.target_rate_spinner)
        val ratesArray = resources.getStringArray(R.array.rates)
        val resultRateTextView = findViewById<TextView>(R.id.rate_result)
        val dateRateTextView = findViewById<TextView>(R.id.rate_data)
        val calculateButton = findViewById<Button>(R.id.calculate_button)
        val rateNumber = findViewById<EditText>(R.id.rate_number)
        lateinit var baseRateSpinnerString : String
        lateinit var targetRateSpinnerString : String
        var isNumeric: Boolean

        // dateRateTextView.text = getApiDate()
        // TODO: Вынести  в отдельную функцию: getApiDate()
        val compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            currencyApi.getRates(rate = "EUR")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { response ->
                        dateRateTextView.text = response.date.toString()},
                    { failure ->
                        Toast.makeText(this, failure.message, Toast.LENGTH_SHORT).show()}
                )
        )


        val baseRateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ratesArray)
        baseRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        baseRateSpinner.adapter = baseRateAdapter
        baseRateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                targetRateSpinnerString = ratesArray[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        calculateButton.setOnClickListener {
            if(baseRateSpinnerString == targetRateSpinnerString) {
                resultRateTextView.text = "1.0"
            } else {
                try {
                    parseDouble(rateNumber.text.toString())
                    isNumeric = true
                } catch (exception: NumberFormatException) {
                    isNumeric = false
                }

                if(isNumeric) {
                    // TODO: Вынести в отдельную функцию getTargetRatePrice()
                    val compositeDisposable2 = CompositeDisposable()
                    compositeDisposable2.add(
                            currencyApi.getRates(rate = baseRateSpinnerString)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(
                                            { response ->
                                                resultRateTextView.text = stringMultiplication(rateNumber.text.toString(),
                                                        response.rates?.get(targetRateSpinnerString).toString().take(7))},
                                            { failure ->
                                                Toast.makeText(this@CurrencyMain, failure.message, Toast.LENGTH_SHORT).show()}
                                    )
                    )
                } else {
                    Toast.makeText(this, "Wrong input", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun stringMultiplication(firstNumber : String, secondNumber : String) : String {
        return (firstNumber.toDouble() * secondNumber.toDouble()).toString().take(7)
    }

    /**
     * @param[baseRate] Указана по умолчанию, т.к. возвращаемое значение не зависит от валюты
     * @return Возвращает поле data с API
     * */
    private fun getApiDate(baseRate: String = "RUB") : String {
        var currentDate = ""
        val compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            currencyApi.getRates(rate = baseRate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { response ->
                        currentDate = response.date.toString()},
                    { failure ->
                        Toast.makeText(this, failure.message, Toast.LENGTH_SHORT).show()}
                )
        )

        // TODO: Ничего не возвращает. Выше ответ с апи есть, но в ретёрн ничего не приходит
        return currentDate
    }

    /**
     * Возвращает стоимость [targetRate] по отношению к [baseRate]
     */
    private fun getTargetRatePrice(baseRate : String, targetRate: String) : String {
        var outputString = ""
        val compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            currencyApi.getRates(rate = baseRate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { response ->
                        outputString = response.rates?.get(baseRate).toString()},
                    { failure ->
                        Toast.makeText(this, failure.message, Toast.LENGTH_SHORT).show()}
                )
        )

        // TODO: Такая же проблема, как и в getApiDate()
        return outputString
    }
}
