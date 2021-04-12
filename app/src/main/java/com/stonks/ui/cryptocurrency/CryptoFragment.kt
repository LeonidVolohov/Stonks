package com.stonks.ui.cryptocurrency

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.stonks.R
import com.stonks.api.ApiConstants
import com.stonks.api.cryptocurrency.CryptoCurrencyApiUtils
import com.stonks.api.cryptocurrency.CryptoCurrencyDataModel
import com.stonks.calculations.Prediction
import com.stonks.ui.UiConstants
import com.stonks.ui.UiConstants.Companion.DEFAULT_DECIMAL_POINT_PRECISION
import com.stonks.ui.UiConstants.Companion.DEFAULT_EDIT_TEXT_NUMBER
import com.stonks.ui.chart.StockLineChart
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_crypto.*
import java.io.IOException
import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_EVEN
import java.math.RoundingMode
import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

class CryptoFragment : Fragment() {
    var defaultCurrencyInd = UiConstants.DEFAULT_CURRENCY_ID

    val disposables = CompositeDisposable()
    lateinit var apiUtils: CryptoCurrencyApiUtils
    lateinit var url: String
    val periodToFarthestReachableMomentInPast = Period.of(5, 0, 0)
    val startCustomDateLimit: ZonedDateTime
        get() = ZonedDateTime.now(ZoneId.systemDefault()) -
                periodToFarthestReachableMomentInPast
    val endCustomDateLimit: ZonedDateTime
        get() = ZonedDateTime.now(ZoneId.systemDefault())
    lateinit var cryptoChart: StockLineChart
    val cryptoCurrencyName: String
        get() = crypto_currency_name_spinner.selectedItem.toString().split(",")[0]
    val currencyName: String
        get() = to_currency_name_spinner.selectedItem.toString().split(",")[0]
    val plotPrediction: Boolean
        get() = crypto_switch.isChecked


    var cryptoCurrencyNameSpinnerString = ""
    var toCurrencySpinnerString = ""
//    var isNumeric: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        url = arguments?.getString("url") ?: ApiConstants.STOCK_API_BASE_URL
        defaultCurrencyInd =
            arguments?.getInt("defaultCurrencyInd") ?: UiConstants.DEFAULT_CURRENCY_ID
        return inflater.inflate(R.layout.fragment_crypto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cryptoChart = StockLineChart(crypto_currency_chart)
        to_currency_name_spinner.setSelection(defaultCurrencyInd)

        val cryptoCurrenciesArray = resources.getStringArray(R.array.crypto_currencies)
        val currencyNameArray = resources.getStringArray(R.array.rates)
        crypto_currency_name_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                cryptoCurrencyNameSpinnerString = cryptoCurrenciesArray[position]
                crypto_rate_number.setText(UiConstants.DEFAULT_EDIT_TEXT_NUMBER.toString())
                try {
                    updateChart(changedCryptoSpinner = true)
                } catch (e: Throwable) {
                    logError(e)
                }
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
                crypto_rate_number.setText(UiConstants.DEFAULT_EDIT_TEXT_NUMBER.toString())
                try {
                    updateChart(changedCryptoSpinner = false)
                } catch (e: Throwable) {
                    logError(e)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        crypto_calculate_button.setOnClickListener(::clickEventHandler)

        period_selection_group.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                try {
                    updateChart(changedCryptoSpinner = false)
                } catch (e: Throwable) {
                    logError(e)
                }
            }
        }
        crypto_switch.setOnCheckedChangeListener { buttonView, isChecked -> updateChart(false) }
    }

    fun clickEventHandler(view: View) {
        val isNumeric = try {
            crypto_rate_number.text.toString().toDouble()
            true
        } catch (exception: NumberFormatException) {
            false
        }

        if (isNumeric) {
            dynamics_month_value.visibility = View.GONE
            try {
                val response = apiUtils.getCryptoCurrencyRatePerDay(
                    cryptoCurrencyNameSpinnerString.split(",")[0],
                    toCurrencySpinnerString.split(",")[0]
                )

                crypto_last_date_update.text = getString(
                    R.string.last_updated_date,
                    response.cryptoCurrency.lastRefreshedDate
                )
                val exchangeRateResult =
                    BigDecimal(
                        (crypto_rate_number.text.toString().toDouble() *
                                response.cryptoCurrency.exchangeRate.toDouble())
                    ).setScale(
                        DEFAULT_DECIMAL_POINT_PRECISION,
                        BigDecimal.ROUND_HALF_EVEN
                    ).toString()
                first_crypto_currency_result.text = exchangeRateResult
                second_crypto_currency_result.text =
                    String.format(
                        BigDecimal(response.cryptoCurrency.bidPrice.toDouble()).setScale(
                            DEFAULT_DECIMAL_POINT_PRECISION,
                            ROUND_HALF_EVEN
                        ).toString()
                    )
                third_crypto_currency_result.text =
                    String.format(
                        BigDecimal(response.cryptoCurrency.askPrice.toDouble()).setScale(
                            DEFAULT_DECIMAL_POINT_PRECISION,
                            ROUND_HALF_EVEN
                        ).toString()
                    )
            } catch (e: Error) {
                Toast.makeText(
                    context,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
            var monthDynamics = 0.0
            try {
                monthDynamics = apiUtils.getMonthDynamics()
            } catch (e: NullPointerException) {
                logError(e)
            }
            if (crypto_rate_number.text.toString() == DEFAULT_EDIT_TEXT_NUMBER.toString()) {
                dynamics_month_value.visibility = View.VISIBLE

                var extraSymbol = "+"
                dynamics_month_value.setTextColor(Color.GREEN)

                if (monthDynamics < 0) {
                    extraSymbol = "-"
                    dynamics_month_value.setTextColor(Color.RED)
                }

                val dynamic = BigDecimal(abs(monthDynamics)).setScale(
                    DEFAULT_DECIMAL_POINT_PRECISION,
                    RoundingMode.HALF_EVEN
                )
                dynamics_month_value.text = "($extraSymbol${dynamic})"
            } else {
                dynamics_month_value.visibility = View.GONE
                val output = BigDecimal(abs(monthDynamics)).setScale(
                    DEFAULT_DECIMAL_POINT_PRECISION,
                    RoundingMode.HALF_EVEN
                ).toString()
                dynamics_month_value.text = output
            }
        } else {
            Toast.makeText(context, getString(R.string.toast_wrong_input), Toast.LENGTH_LONG).show()
            first_crypto_currency_result.text = ""
            second_crypto_currency_result.text = ""
            third_crypto_currency_result.text = ""
        }
    }

    fun updateChart(changedCryptoSpinner: Boolean = true) {
        if (changedCryptoSpinner) {
            apiUtils = CryptoCurrencyApiUtils(cryptoCurrencyName, url)
            period_selection_group.check(R.id.togglebutton_one_week_selector)
            var monthDynamics = 0.0
            try {
                monthDynamics = apiUtils.getMonthDynamics()
            } catch (e: NullPointerException) {
                logError(e)
            }
            var extraSymbol = "+"
            if (monthDynamics < 0) {
                extraSymbol = "-"
            }
            val dynamic = BigDecimal(abs(monthDynamics)).setScale(
                DEFAULT_DECIMAL_POINT_PRECISION,
                RoundingMode.HALF_EVEN
            )
            dynamics_month_value.text = "($extraSymbol${dynamic})"
        }
        var observable: CryptoCurrencyDataModel.RatesProcessed? = null
        when (period_selection_group.checkedButtonId) {
            R.id.togglebutton_one_day_selector -> {
                Toast.makeText(context, getString(R.string.toast_api_not_provide_for_one_day), Toast.LENGTH_LONG).show()
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
                picker.addOnNegativeButtonClickListener {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.toast_calendar_canceled_selection),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                picker.addOnPositiveButtonClickListener {
                    val startInstant = Instant.ofEpochMilli(it.first ?: 0)
                    val endInstant = Instant.ofEpochMilli(it.second ?: 0)
                    startDateTime = ZonedDateTime.ofInstant(startInstant, ZoneId.systemDefault())
                    endDateTime = ZonedDateTime.ofInstant(endInstant, ZoneId.systemDefault())
                    try {
                        val result = apiUtils.getPricesForCustomPeriod(startDateTime, endDateTime)
                        processResult(result)
                    } catch (e: Error) {
                        logError(e)
                    }
                }
                picker.show(activity?.supportFragmentManager!!, picker.toString())
            }
        }
        val result = observable
            ?: CryptoCurrencyDataModel.RatesProcessed(mapOf<ZonedDateTime, Double>().toSortedMap())
        processResult(result)
    }

    fun processResult(result: CryptoCurrencyDataModel.RatesProcessed) {
        val dateListResult = result.rates.keys.sorted().toMutableList()
        val entriesResult =
            result.rates.entries.sortedBy { it.key }.map { it.value }.toMutableList()
        val predictedRatesMax = entriesResult.toMutableList()
        val predictedRatesMin = entriesResult.toMutableList()
        val dateList = dateListResult.map {
            it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
        cryptoChart.setXAxis(crypto_currency_chart, dateList)
        val data = cryptoChart.getLineData(
            entries = entriesResult, baseCurrency = cryptoCurrencyName,
            targetCurrency = currencyName
        )
        if (plotPrediction) {
            val result = apiUtils.getPricesFor1Year()
            val currentDaysInMonth =
                Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            val dateListLong = result.rates.keys.sorted().toMutableList()
                .map { it.toInstant().toEpochMilli() }
            val entriesResult =
                result.rates.entries.sortedBy { it.key }.map { it.value }
                    .toMutableList()
            val predictedRate = Prediction().datePrediction(
                dateListLong.toTypedArray(),
                entriesResult.toTypedArray(),
                currentDaysInMonth
            )
            val predictedRateMax = predictedRate * 1.50
            val predictedRateMin = predictedRate * 0.50
            val lastExistingDate = dateListResult.last()
            for (i in 1..currentDaysInMonth) {
                dateListResult.add(lastExistingDate + Period.ofDays(i))
                predictedRatesMax.add(entriesResult.last() + (predictedRateMax - entriesResult.last()) / currentDaysInMonth * i)
                predictedRatesMin.add(entriesResult.last() + (predictedRateMin - entriesResult.last()) / currentDaysInMonth * i)
            }
            val lineChartPredictionDataMax = cryptoChart.getPredictionLineData(
                predictionEntries = predictedRatesMax.toList()
            )
            val lineChartPredictionDataMin = cryptoChart.getPredictionLineData(
                predictionEntries = predictedRatesMin.toList()
            )
            val dateList = dateListResult.map {
                it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }
            cryptoChart.setXAxis(crypto_currency_chart, dateList)
            cryptoChart.displayPredictionChartTransparent(
                lineChart = crypto_currency_chart,
                lineChartData = data,
                lineChartPredictionDataMax = lineChartPredictionDataMax,
                lineChartPredictionDataMin = lineChartPredictionDataMin
            )
        } else {
            cryptoChart.displayChart(crypto_currency_chart, data)
        }
    }

    fun logError(error: Throwable) {
        if (error is NullPointerException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_api_returned_null),
                Toast.LENGTH_SHORT
            ).show()
        } else if (error is IOException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_empty_response),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                error.message.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}
