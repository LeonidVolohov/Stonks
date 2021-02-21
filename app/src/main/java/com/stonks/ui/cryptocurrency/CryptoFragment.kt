package com.stonks.ui.cryptocurrency

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.stonks.R
import com.stonks.api.cryptoCurrencyApi
import com.stonks.api.cryptocurrency.CryptoCurrencyApiUtils
import com.stonks.api.cryptocurrency.CryptoCurrencyDataModel
import com.stonks.ui.chart.StockLineChart
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_crypto.*
import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_EVEN
import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CryptoFragment(private val defaultCurrencyInd: Int) : Fragment() {

    private lateinit var apiUtils: CryptoCurrencyApiUtils
    private val disposables = CompositeDisposable()
    private val periodToFarthestReachableMomentInPast = Period.of(5, 0, 0)
    private val startCustomDateLimit: ZonedDateTime
        get() = ZonedDateTime.now(ZoneId.systemDefault()) -
                periodToFarthestReachableMomentInPast
    private val endCustomDateLimit: ZonedDateTime
        get() = ZonedDateTime.now(ZoneId.systemDefault())
    private val TAG = this::class.java.name
    private lateinit var cryptoChart: StockLineChart
    var cryptoCurrencyName = ""
    var currencyName = ""

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crypto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cryptoChart = StockLineChart(crypto_currency_chart)
        to_currency_name_spinner.setSelection(defaultCurrencyInd)

        val apiKey = "W0W3UA4EHIXTZD3F"
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
                updateChart(changedCryptoSpinner = true)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

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
                disposables.add(
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

        period_selection_group.addOnButtonCheckedListener{ group, checkedId, isChecked ->
            if (isChecked) {
                updateChart(changedCryptoSpinner = false)
            }
        }
    }

    private fun updateChart(changedCryptoSpinner: Boolean = true) {
        cryptoCurrencyName = crypto_currency_name_spinner.selectedItem.toString().split(",")[0]
        currencyName = to_currency_name_spinner.selectedItem.toString().split(",")[0]
        if (changedCryptoSpinner) {
            apiUtils = CryptoCurrencyApiUtils(cryptoCurrencyName)
            period_selection_group.check(R.id.togglebutton_one_week_selector)
        }
        var observable: Observable<CryptoCurrencyDataModel.RatesProcessed>? = null
        when (period_selection_group.checkedButtonId) {
            R.id.togglebutton_one_day_selector -> {
                Toast.makeText(context, "API doesn't provide intraday data", Toast.LENGTH_LONG).show()
                period_selection_group.check(R.id.togglebutton_one_week_selector)
                observable = apiUtils.getPricesFor1Week()
            }
            R.id.togglebutton_one_week_selector -> {
                observable = apiUtils.getPricesFor1Week()
            }
            R.id.togglebutton_one_month_selector -> {
                observable = apiUtils.getPricesFor1Month()
            }
            R.id.togglebutton_six_months_selector -> {
                observable = apiUtils.getPricesFor6Months()
            }
            R.id.togglebutton_one_year_selector -> {
                observable = apiUtils.getPricesFor1Year()
            }
            R.id.togglebutton_five_years_selector -> {
                observable = apiUtils.getPricesFor5Years()
            }
            R.id.togglebutton_custom_period_selector -> {
                var startDateTime: ZonedDateTime
                var endDateTime: ZonedDateTime
                val now = Calendar.getInstance()
                val picker = MaterialDatePicker.Builder.dateRangePicker()
                        .setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))
                        .setCalendarConstraints(
                                CalendarConstraints.Builder()
                                        .setStart(startCustomDateLimit.toInstant().toEpochMilli())
                                        .setEnd(endCustomDateLimit.toInstant().toEpochMilli())
                                        .build()
                        )
                        .build()
                picker.addOnNegativeButtonClickListener { Log.i(TAG, "Cancelled selection") }
                picker.addOnPositiveButtonClickListener {
                    val startInstant = Instant.ofEpochMilli(it.first ?: 0)
                    val endInstant = Instant.ofEpochMilli(it.second ?: 0)
                    startDateTime = ZonedDateTime.ofInstant(startInstant, ZoneId.systemDefault())
                    endDateTime = ZonedDateTime.ofInstant(endInstant, ZoneId.systemDefault())
                    disposables.add(
                            apiUtils.getPricesForCustomPeriod(startDateTime, endDateTime)
                                    .subscribe(::processResult, ::logError)
                    )
                }
                picker.show(activity?.supportFragmentManager!!, picker.toString())
            }
            else -> TODO("Error")
        }
        disposables.add(
                observable?.subscribe(::processResult, ::logError) ?: Observable.just(1)
                        .subscribe({}, {})
        )
    }

    private fun processResult(result: CryptoCurrencyDataModel.RatesProcessed) {
        val dateList = result.rates.keys.map {
            it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }.sorted()
        cryptoChart.setXAxis(crypto_currency_chart, dateList)
        val entries = result.rates.values.sorted()
        val data = cryptoChart.getLineData(
                entries = entries, baseCurrency = cryptoCurrencyName,
                targetCurrency = currencyName
        )
        cryptoChart.displayChart(crypto_currency_chart, data)
    }

    private fun logError(error: Throwable) {
        Log.e(TAG, error.message ?: "error occured", error)
    }
}
