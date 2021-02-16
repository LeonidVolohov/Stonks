package com.stonks.ui.cryptocurrency

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import com.stonks.R
import com.stonks.api.cryptoCurrencyApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_crypto.*
import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_EVEN

class CryptoFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crypto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiKey = "W0W3UA4EHIXTZD3F"
        val defaultCurrencyIndex = 17
        val decimalPointPrecision = 5

        val cryptoCurrenciesArray = resources.getStringArray(R.array.crypto_currencies)
        val currencyNameArray = resources.getStringArray(R.array.rates)
        var cryptoCurrencyNameSpinnerString = ""
        var toCurrencySpinnerString = ""
        var isNumeric: Boolean

        crypto_currency_name_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                cryptoCurrencyNameSpinnerString = cryptoCurrenciesArray[position]
                crypto_rate_number.setText("1.0")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        to_currency_name_spinner.setSelection(defaultCurrencyIndex)
        to_currency_name_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                toCurrencySpinnerString = currencyNameArray[position]
                crypto_rate_number.setText("1.0")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        crypto_calculate_button.setOnClickListener {
            isNumeric = try {
                crypto_rate_number.text.toString().toDouble()
                true
            } catch (exception: NumberFormatException) {
                false
            }

            if (isNumeric) {
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
                                            crypto_last_date_update.text = getString(R.string.last_updated_date, response.cryptoCurrency.lastRefreshedDate)
                                            val exchangeRateResult = BigDecimal((crypto_rate_number.text.toString().toDouble() * response.cryptoCurrency.exchangeRate.toDouble())).setScale(decimalPointPrecision, BigDecimal.ROUND_HALF_EVEN).toString()
                                            first_crypto_currency_result.text = exchangeRateResult
                                            second_crypto_currency_result.text = String.format(BigDecimal(response.cryptoCurrency.bidPrice.toDouble()).setScale(decimalPointPrecision, ROUND_HALF_EVEN).toString())
                                            third_crypto_currency_result.text = String.format(BigDecimal(response.cryptoCurrency.askPrice.toDouble()).setScale(decimalPointPrecision, ROUND_HALF_EVEN).toString())
                                        },
                                        { failure ->
                                            Toast.makeText(context, "No data", Toast.LENGTH_LONG).show()
                                        }
                                )
                )
            } else {
                Toast.makeText(context, "Wrong input", Toast.LENGTH_LONG).show()
                first_crypto_currency_result.text = ""
                second_crypto_currency_result.text = ""
                third_crypto_currency_result.text = ""
            }
        }
    }
}