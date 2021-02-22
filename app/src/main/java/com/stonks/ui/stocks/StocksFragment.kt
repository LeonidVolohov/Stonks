package com.stonks.ui.stocks

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.switchmaterial.SwitchMaterial
import com.stonks.R
import com.stonks.api.stocks.StocksApiDataUtils
import com.stonks.api.stocks.StocksDataModel
import com.stonks.calculations.Prediction
import com.stonks.ui.chart.StockLineChart
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_stocks.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

class StocksFragment(private val defaultCurrencyInd: Int) : Fragment() {
    private var disposables = CompositeDisposable()
    private lateinit var apiUtils: StocksApiDataUtils

    private lateinit var spinnerStocks: Spinner
    private val spinnerStocksValue: String
        get() = spinnerStocks.selectedItem.toString().split(',')[0]
    private lateinit var spinnerCurrency: Spinner
    private val spinnerCurrencyValue: String
        get() = spinnerCurrency.selectedItem.toString().split(',')[0]
    private lateinit var textViewMarket: TextView
    private lateinit var textViewPrice: TextView
    private lateinit var textViewDynamics: TextView
    private lateinit var toggleGroupPeriod: MaterialButtonToggleGroup
    private lateinit var switchPrediction: SwitchMaterial
    private val plotPrediction: Boolean
        get() = switchPrediction.isChecked
    private lateinit var stocksChart: StockLineChart
    private lateinit var stocksChartField: LineChart

    private val periodToFarthestReachableMomentInPast = Period.of(5, 0, 0)
    private val startCustomDateLimit: ZonedDateTime
        get() = ZonedDateTime.now(ZoneId.systemDefault()) -
                periodToFarthestReachableMomentInPast
    private val endCustomDateLimit: ZonedDateTime
        get() = ZonedDateTime.now(ZoneId.systemDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stocks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFields(view)
        spinnerCurrency.setSelection(defaultCurrencyInd)
        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateStockData(changedPeriodOnly = true)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerStocks.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateStockData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        toggleGroupPeriod.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                updateStockData(changedPeriodOnly = true)
            }
        }
        switchPrediction.setOnCheckedChangeListener { buttonView, isChecked -> updateStockData(true) }
    }

    private fun updateStockData(changedPeriodOnly: Boolean = false) {
        val stock = spinnerStocksValue
        if (!changedPeriodOnly) {
            apiUtils = StocksApiDataUtils(stock)
            textViewMarket.text = getString(R.string.loading_value_placeholder)
            disposables.add(
                apiUtils.getMarket().subscribe(
                    { result -> textViewMarket.text = result.market },
                    ::logError
                )
            )
            disposables.add(
                    apiUtils.getMonthDynamics().subscribe(
                            { result ->
                                var extraSymbol = "+"
                                textview_dynamics_value.setTextColor(Color.GREEN)
                                if (result < 0) {
                                    extraSymbol = "-"
                                    textview_dynamics_value.setTextColor(Color.RED)
                                }
                                val dynamic = BigDecimal(abs(result)).setScale(2, RoundingMode.HALF_EVEN)
                                textViewDynamics.text = "($extraSymbol${dynamic})"
                            },
                            ::logError
                    )
            )
            toggleGroupPeriod.check(R.id.togglebutton_one_day_selector)
            disposables.add(
                apiUtils.getLatestRate().subscribe(
                    { result -> textViewPrice.text = result.toString() },
                    ::logError
                )
            )
        }
        var observable: Observable<StocksDataModel.RatesProcessed>? = null
        when (toggleGroupPeriod.checkedButtonId) {
            R.id.togglebutton_one_day_selector -> {
                observable = apiUtils.getPricesFor1Day()
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
                            "Cancelled selection",
                            Toast.LENGTH_SHORT
                    ).show()
                }
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
        }
        disposables.add(
            observable?.subscribe(::processResult, ::logError) ?: Observable.just(1)
                .subscribe({}, {})
        )
    }

    private fun processResult(result: StocksDataModel.RatesProcessed) {
        val dateListResult = result.rates.keys.sorted().toMutableList()
        val entriesResult =
            result.rates.entries.sortedBy { it.key }.map { it.value }.toMutableList()
        val predictedRatesMax = entriesResult.toMutableList()
        val predictedRatesMin = entriesResult.toMutableList()
        val dateList = dateListResult.map {
            it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
        apiUtils.convertToCurrency(spinnerCurrencyValue, result)
        stocksChart.setXAxis(view!!.findViewById(R.id.stocks_chart), dateList)
        val data = stocksChart.getLineData(
            entries = entriesResult, baseCurrency = spinnerStocksValue,
            targetCurrency = spinnerCurrencyValue
        )
        if (plotPrediction) {
            disposables.add(
                apiUtils.getPricesFor5Years().subscribe(
                    { result ->
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
                        val lineChartPredictionDataMax = stocksChart.getPredictionLineData(
                            predictionEntries = predictedRatesMax.toList()
                        )
                        val lineChartPredictionDataMin = stocksChart.getPredictionLineData(
                            predictionEntries = predictedRatesMin.toList()
                        )
                        val dateList = dateListResult.map {
                            it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        }
                        stocksChart.setXAxis(view!!.findViewById(R.id.stocks_chart), dateList)
                        stocksChart.displayPredictionChartTransparent(
                            lineChart = stocksChartField,
                            lineChartData = data,
                            lineChartPredictionDataMax = lineChartPredictionDataMax,
                            lineChartPredictionDataMin = lineChartPredictionDataMin
                        )
                    },
                    ::logError
                )
            )
        } else {
            stocksChart.displayChart(view!!.findViewById(R.id.stocks_chart), data)
        }
    }

    private fun logError(error: Throwable) {
        Toast.makeText(
                requireContext(),
                error.message.toString(),
                Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Links UI elements to class lateinit vars
     *
     * @param view View, in which this fragment is created
     * */
    private fun initFields(view: View) {
        spinnerStocks = view.findViewById(R.id.spinner_stocks)
        spinnerCurrency = view.findViewById(R.id.spinner_currency)
        textViewMarket = view.findViewById(R.id.textview_market_name)
        textViewPrice = view.findViewById(R.id.textview_price_value)
        toggleGroupPeriod = view.findViewById(R.id.period_selection_group)
        stocksChartField = view.findViewById(R.id.stocks_chart)
        stocksChart = StockLineChart(stocksChartField)
        switchPrediction = view.findViewById(R.id.switch_prediction)
        textViewDynamics = view.findViewById(R.id.textview_dynamics_value)
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
