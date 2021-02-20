package com.stonks.ui.currency

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.stonks.R
import com.stonks.ui.chart.StockLineChart
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_currency.*
import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CurrencyFragment(bottomNavigationHeight: Int, private val defaultCurrencyInd: Int) : Fragment() {
    private val TAG = CurrencyFragment::class.java.name
    private var disposable: Disposable? = null
    private lateinit var baseRateSpinnerString: String
    private lateinit var targetRateSpinnerString: String
    private val localBottomNavigationHeight: Int = bottomNavigationHeight
    private var isNumeric: Boolean = false

    private val startCustomDateLimit: ZonedDateTime
        get() = ZonedDateTime.now(ZoneId.systemDefault()) - Period.of(5, 0, 0)
    private val endCustomDateLimit: ZonedDateTime
        get() = ZonedDateTime.now(ZoneId.systemDefault())

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_currency, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ratesNameArray = resources.getStringArray(R.array.rates)
        val chartPrimaryRatesArray = resources.getStringArray(R.array.primary_rates)

        val currencyFragmentUtils = CurrencyFragmentUtils(disposable = disposable)
        val currencyLineChart = StockLineChart(currency_chart)

        initPrimaryRatesName(chartPrimaryRatesArray)

        // TODO: Change to getString() - not working
        currencyFragmentUtils.setLastUpdatedDate(last_date_update, "Data for: ", requireContext())

        base_rate_spinner.setSelection(defaultCurrencyInd)
        base_rate_spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                baseRateSpinnerString = ratesNameArray[position].split(",")[0]
                rate_number.setText("1.0")
                currencyLineChart.clearChart()
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
                currencyLineChart.clearChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        currency_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                currencyFragmentUtils.plotRatesPerPeriod(
                    startDate = "1M",
                    targetRate = targetRateSpinnerString,
                    baseRate = baseRateSpinnerString,
                    stockLineChart = currencyLineChart,
                    currencyChart = currency_chart,
                    isPrediction = true,
                    context = requireContext()
                )

                displayChart()
                currency_button_group.check(R.id.currency_togglebutton_one_week_selector)
            } else {
                currencyLineChart.clearChart()
            }
        }

        currency_chart.isVisible = false
        currency_button_group.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (currency_button_group.checkedButtonId) {
                    R.id.currency_togglebutton_one_day_selector -> {
                        Toast.makeText(
                            requireContext(),
                            "Api does not provide information for one day",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    R.id.currency_togglebutton_one_week_selector -> {
                        currencyFragmentUtils.plotRatesPerPeriod(
                            startDate = "1W",
                            targetRate = targetRateSpinnerString,
                            baseRate = baseRateSpinnerString,
                            stockLineChart = currencyLineChart,
                            currencyChart = currency_chart,
                            context = requireContext()
                        )

                        displayChart()
                    }
                    R.id.currency_togglebutton_one_month_selector -> {
                        currencyFragmentUtils.plotRatesPerPeriod(
                            startDate = "1M",
                            targetRate = targetRateSpinnerString,
                            baseRate = baseRateSpinnerString,
                            stockLineChart = currencyLineChart,
                            currencyChart = currency_chart,
                            context = requireContext()
                        )

                        displayChart()
                    }
                    R.id.currency_togglebutton_six_months_selector -> {
                        currencyFragmentUtils.plotRatesPerPeriod(
                            startDate = "6M",
                            targetRate = targetRateSpinnerString,
                            baseRate = baseRateSpinnerString,
                            stockLineChart = currencyLineChart,
                            currencyChart = currency_chart,
                            context = requireContext()
                        )

                        displayChart()
                    }
                    R.id.currency_togglebutton_one_year_selector -> {
                        currencyFragmentUtils.plotRatesPerPeriod(
                            startDate = "1Y",
                            targetRate = targetRateSpinnerString,
                            baseRate = baseRateSpinnerString,
                            stockLineChart = currencyLineChart,
                            currencyChart = currency_chart,
                            context = requireContext()
                        )

                        displayChart()
                    }
                    R.id.currency_togglebutton_five_years_selector -> {
                        currencyFragmentUtils.plotRatesPerPeriod(
                            startDate = "5Y",
                            targetRate = targetRateSpinnerString,
                            baseRate = baseRateSpinnerString,
                            stockLineChart = currencyLineChart,
                            currencyChart = currency_chart,
                            context = requireContext()
                        )

                        displayChart()
                    }
                    R.id.currency_togglebutton_custom_period_selector -> {
                        var startDateTime: String
                        var endDateTime: String
                        val now = Calendar.getInstance()
                        val picker = MaterialDatePicker.Builder.dateRangePicker()
                            .setSelection(
                                androidx.core.util.Pair(
                                    now.timeInMillis,
                                    now.timeInMillis
                                )
                            )
                            .setCalendarConstraints(
                                CalendarConstraints.Builder()
                                    .setStart(startCustomDateLimit.toInstant().toEpochMilli())
                                    .setEnd(endCustomDateLimit.toInstant().toEpochMilli())
                                    .build()
                            )
                            .build()
                        picker.addOnPositiveButtonClickListener {
                            val startInstant = Instant.ofEpochMilli(it.first ?: 0)
                            val endInstant = Instant.ofEpochMilli(it.second ?: 0)
                            startDateTime =
                                ZonedDateTime.ofInstant(startInstant, ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            endDateTime =
                                ZonedDateTime.ofInstant(endInstant, ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                            currencyFragmentUtils.plotRatesPerPeriod(
                                startDate = startDateTime,
                                endDate = endDateTime,
                                targetRate = targetRateSpinnerString,
                                baseRate = baseRateSpinnerString,
                                stockLineChart = currencyLineChart,
                                currencyChart = currency_chart,
                                context = requireContext()
                            )

                            displayChart()
                        }
                        picker.addOnNegativeButtonClickListener {
                            Log.i(
                                TAG,
                                "Cancelled selection"
                            )
                        }

                        picker.show(activity?.supportFragmentManager!!, picker.toString())
                    }
                }
            }
        }

        calculate_button.setOnClickListener {
            if (baseRateSpinnerString == targetRateSpinnerString) {
                rate_result.text = currencyFragmentUtils.stringMultiplication(
                        rate_number.text.toString(),
                        "1.0"
                )

                rate_difference.visibility = View.GONE
            } else {
                isNumeric = try {
                    rate_number.text.toString().toDouble()
                    true
                } catch (exception: NumberFormatException) {
                    false
                }

                if (isNumeric) {
                    changeToDefaultValue()
                    // currency_button_group.check(R.id.currency_togglebutton_one_week_selector)
                    rate_difference.visibility = View.GONE

                    currencyFragmentUtils.setTargetRatePrice(
                            baseRate = baseRateSpinnerString,
                            targetRate = targetRateSpinnerString,
                            rateNumberEditText = rate_number,
                            resultRateTextView = rate_result,
                            differenceRateTextView = rate_difference
                    )
                    currencyFragmentUtils.plotRatesPerPeriod(
                        startDate = "1W",
                        targetRate = targetRateSpinnerString,
                        baseRate = baseRateSpinnerString,
                        stockLineChart = currencyLineChart,
                        currencyChart = currency_chart,
                        context = requireContext()
                    )

                    currencyFragmentUtils.setPrimaryRatesPerDay(
                        baseRate = baseRateSpinnerString,
                        symbols = PRIMARY_RATES,
                        firstTextView = first_primary_currency_result,
                        secondTextView = second_primary_currency_result,
                        thirdTextView = third_primary_currency_result,
                        fourthTextView = fourth_primary_currency_result,
                        fifthTextView = fifth_primary_currency_result,
                        rateNumber = rate_number,
                        context = requireContext()
                    )
                } else {
                    Toast.makeText(requireContext(), "Wrong input", Toast.LENGTH_LONG).show()
                    changeToDefaultValue()
                }
            }
        }
    }

    private fun changeToDefaultValue() {
        rate_result.text = ""
        first_primary_currency_result.text = ""
        second_primary_currency_result.text = ""
        third_primary_currency_result.text = ""
        fourth_primary_currency_result.text = ""
        fifth_primary_currency_result.text = ""
    }

    private fun initPrimaryRatesName(chartPrimaryRatesArray: Array<String>) {
        first_currency_name.text = chartPrimaryRatesArray[0]
        second_currency_name.text = chartPrimaryRatesArray[1]
        third_currency_name.text = chartPrimaryRatesArray[2]
        fourth_currency_name.text = chartPrimaryRatesArray[3]
        fifth_currency_name.text = chartPrimaryRatesArray[4]
    }

    private fun displayChart() {
        currency_chart.isVisible = true

        val bottomNavigationHeight = localBottomNavigationHeight
        val statusBarHeight = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))
        val navigationBarHeight = resources.getDimensionPixelSize(resources.getIdentifier("navigation_bar_height", "dimen", "android"))
        val actionBarHeight = TypedValue.complexToDimensionPixelSize(TypedValue().data, resources.displayMetrics)

        val params: ViewGroup.LayoutParams = currency_chart.layoutParams
        val totalHeight = bottomNavigationHeight + statusBarHeight + navigationBarHeight + actionBarHeight
        params.height = Resources.getSystem().displayMetrics.heightPixels - (Resources.getSystem().displayMetrics.heightPixels - totalHeight) / 3
        currency_chart.requestLayout()
        currency_scroll_view.fullScroll(View.FOCUS_DOWN)
    }
}
