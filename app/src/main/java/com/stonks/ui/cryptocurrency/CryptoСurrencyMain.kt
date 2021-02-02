package com.stonks.ui.cryptocurrency

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.stonks.R
import com.stonks.api.cryptoCurrencyApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_EVEN
import java.util.*

class CryptoCurrencyMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crypto)

        val TAG: String = CryptoCurrencyMain::class.java.name

        val apiKey = "W0W3UA4EHIXTZD3F"
        val defaultCurrencyIndex = 17
        val decimalPointPrecision = 5

        val lastUpdateDate = findViewById<TextView>(R.id.crypto_last_date_update)
        val exchangeRate = findViewById<TextView>(R.id.first_crypto_currency_result)
        val bidPrice = findViewById<TextView>(R.id.second_crypto_currency_result)
        val askPrice = findViewById<TextView>(R.id.third_crypto_currency_result)
        val calculateButton = findViewById<Button>(R.id.crypto_calculate_button)
        val cryptoCurrencyNameSpinner = findViewById<Spinner>(R.id.crypto_currency_name_spinner)
        val toCurrencyNameSpinner = findViewById<Spinner>(R.id.to_currency_name_spinner)
        val cryptoCurrenciesArray = resources.getStringArray(R.array.crypto_currencies)
        val currencyNameArray = resources.getStringArray(R.array.rates)
        var cryptoCurrencyNameSpinnerString = ""
        var toCurrencySpinnerString = ""


        val exchangeRateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cryptoCurrenciesArray)
        exchangeRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cryptoCurrencyNameSpinner.adapter = exchangeRateAdapter
        cryptoCurrencyNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                cryptoCurrencyNameSpinnerString = cryptoCurrenciesArray[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        val toCurrencyNameAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyNameArray)
        toCurrencyNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        toCurrencyNameSpinner.adapter = toCurrencyNameAdapter
        toCurrencyNameSpinner.setSelection(defaultCurrencyIndex)
        toCurrencyNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                toCurrencySpinnerString = currencyNameArray[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        calculateButton.setOnClickListener {
            val compositeDisposable = CompositeDisposable()
            compositeDisposable.add(
                cryptoCurrencyApi.getCryptoCurrencyRatePerDay(
                    function = "CURRENCY_EXCHANGE_RATE",
                    cryptoCurrencyName = cryptoCurrencyNameSpinnerString.split(",")[0],
                    toCurrencyName = toCurrencySpinnerString.split(",")[0],
                    apiKey = apiKey
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { response ->
                            lastUpdateDate.text = getString(R.string.last_updated_date, response.cryptoCurrency.lastRefreshedDate)
                            exchangeRate.text = String.format(BigDecimal(response.cryptoCurrency.exchangeRate.toDouble()).setScale(decimalPointPrecision, ROUND_HALF_EVEN).toString())
                            bidPrice.text = String.format(BigDecimal(response.cryptoCurrency.bidPrice.toDouble()).setScale(decimalPointPrecision, ROUND_HALF_EVEN).toString())
                            askPrice.text = String.format(BigDecimal(response.cryptoCurrency.askPrice.toDouble()).setScale(decimalPointPrecision, ROUND_HALF_EVEN).toString())
                        },
                        { failure ->
                            Log.i(TAG, "No data")
                        }
                    )
            )
        }
    }
}