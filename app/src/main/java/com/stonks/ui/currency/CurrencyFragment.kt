package com.stonks.ui.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.stonks.R
import com.stonks.ui.chart.StockLineChart
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_currency.*

class CurrencyFragment : Fragment() {
    private val TAG = CurrencyFragment::class.java.name
    private var disposable: Disposable? = null
    private lateinit var baseRateSpinnerString: String
    private lateinit var targetRateSpinnerString: String
    private var isNumeric: Boolean = false

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

        currencyFragmentUtils.setLastUpdatedDate(last_date_update, "Data for: ")

        base_rate_spinner?.setSelection(DEFAULT_CURRENCY_INDEX)
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

        currency_button_group.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (currency_button_group.checkedButtonId) {
                    R.id.currency_togglebutton_one_week_selector -> {
                        currencyFragmentUtils.plotRatesPerPeriod(
                                startDate = "1W",
                                targetRate = targetRateSpinnerString,
                                baseRate = baseRateSpinnerString,
                                stockLineChart = currencyLineChart,
                                currencyChart = currency_chart
                        )
                    }
                    R.id.currency_togglebutton_one_month_selector -> {
                        currencyFragmentUtils.plotRatesPerPeriod(
                                startDate = "1M",
                                targetRate = targetRateSpinnerString,
                                baseRate = baseRateSpinnerString,
                                stockLineChart = currencyLineChart,
                                currencyChart = currency_chart
                        )
                    }
                    R.id.currency_togglebutton_six_months_selector -> {
                        currencyFragmentUtils.plotRatesPerPeriod(
                                startDate = "6M",
                                targetRate = targetRateSpinnerString,
                                baseRate = baseRateSpinnerString,
                                stockLineChart = currencyLineChart,
                                currencyChart = currency_chart
                        )
                    }
                    R.id.currency_togglebutton_one_year_selector -> {
                        currencyFragmentUtils.plotRatesPerPeriod(
                                startDate = "1Y",
                                targetRate = targetRateSpinnerString,
                                baseRate = baseRateSpinnerString,
                                stockLineChart = currencyLineChart,
                                currencyChart = currency_chart
                        )
                    }
                    R.id.currency_togglebutton_five_years_selector -> {
                        currencyFragmentUtils.plotRatesPerPeriod(
                                startDate = "5Y",
                                targetRate = targetRateSpinnerString,
                                baseRate = baseRateSpinnerString,
                                stockLineChart = currencyLineChart,
                                currencyChart = currency_chart
                        )
                    }
                    R.id.currency_togglebutton_custom_period_selector -> {
                        // TODO: Add calendar
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
            } else {
                isNumeric = try {
                    rate_number.text.toString().toDouble()
                    true
                } catch (exception: NumberFormatException) {
                    false
                }

                if (isNumeric) {
                    changeToDefaultValue()
                    currency_button_group.check(R.id.currency_togglebutton_one_week_selector)

                    currencyFragmentUtils.setTargetRatePrice(
                            baseRate = baseRateSpinnerString,
                            targetRate = targetRateSpinnerString,
                            rateNumber = rate_number,
                            textView = rate_result
                    )
                    currencyFragmentUtils.plotRatesPerPeriod(
                            startDate = "1W",
                            targetRate = targetRateSpinnerString,
                            baseRate = baseRateSpinnerString,
                            stockLineChart = currencyLineChart,
                            currencyChart = currency_chart
                    )

                    currencyFragmentUtils.setPrimaryRatesPerDay(
                            baseRate = baseRateSpinnerString,
                            symbols = PRIMARY_RATES,
                            firstTextView = first_primary_currency_result,
                            secondTextView = second_primary_currency_result,
                            thirdTextView = third_primary_currency_result,
                            fourthTextView = fourth_primary_currency_result,
                            fifthTextView = fifth_primary_currency_result,
                            rateNumber = rate_number
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
}