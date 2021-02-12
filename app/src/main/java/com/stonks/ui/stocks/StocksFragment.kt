package com.stonks.ui.stocks

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import com.stonks.R
import com.stonks.api.stocks.StocksApiDataUtils
import com.stonks.api.stocks.StocksDataModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*

class StocksFragment : Fragment() {

    private var disposable: Disposable? = null

    private lateinit var spinnerStocks: Spinner
    private lateinit var spinnerCurrency: Spinner
    private lateinit var textViewMarket: TextView
    private lateinit var textViewPrice: TextView
    private lateinit var toggleGroupPeriod: MaterialButtonToggleGroup
    private lateinit var switchPrediction: SwitchMaterial

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
        spinnerStocks.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateStockData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        toggleGroupPeriod.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                updateStockData(changedPeriodOnly = true)
            }
        }
        updateStockData()
    }

    private fun updateStockData(changedPeriodOnly: Boolean = false) {
        if (!changedPeriodOnly) {
            textViewMarket.text = "Loading..."
            val stock = spinnerStocks.selectedItem.toString().split(",")[0]
            disposable = StocksApiDataUtils().getMarket(stock)
                .subscribe(
                    { result ->
                        textViewMarket.text = result.market
                    },
                    ::logError
                )
            toggleGroupPeriod.check(R.id.togglebutton_one_day_selector)
        }
        var observable: Observable<StocksDataModel.RatesProcessed>? = null
        when (toggleGroupPeriod.checkedButtonId) {
            R.id.togglebutton_one_day_selector -> {
                println("One Day Period Selected")
                observable =
                    StocksApiDataUtils().getPricesFor1Day(spinnerStocks.selectedItem.toString())
            }
            R.id.togglebutton_one_week_selector -> {
                println("One Week Period Selected")
                observable =
                    StocksApiDataUtils().getPricesFor1Week(spinnerStocks.selectedItem.toString())
            }
            R.id.togglebutton_one_month_selector -> {
                println("One Month Period Selected")
                observable =
                    StocksApiDataUtils().getPricesFor1Month(spinnerStocks.selectedItem.toString())
            }
            R.id.togglebutton_six_months_selector -> {
                println("Six Months Period Selected")
                observable =
                    StocksApiDataUtils().getPricesFor6Months(spinnerStocks.selectedItem.toString())
            }
            R.id.togglebutton_one_year_selector -> {
                println("One Year Period Selected")
                observable =
                    StocksApiDataUtils().getPricesFor1Year(spinnerStocks.selectedItem.toString())
            }
            R.id.togglebutton_five_years_selector -> {
                println("Five Years Period Selected")
                observable =
                    StocksApiDataUtils().getPricesFor5Years(spinnerStocks.selectedItem.toString())
            }
            R.id.togglebutton_custom_period_selector -> {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val datePickerDialog =
                    DatePickerDialog(
                        requireContext(), DatePickerDialog.OnDateSetListener
                        { view, year, monthOfYear, dayOfMonth ->

                            Log.e(
                                "error",
                                "" + dayOfMonth + " - " + (monthOfYear + 1) + " - " + year
                            )
                        }, year, month, day
                    )
                datePickerDialog.setTitle("Chose start period")
                datePickerDialog.datePicker.minDate = calendar.timeInMillis
                datePickerDialog.show()
            }
            else -> TODO("Error")
        }
        disposable = observable?.subscribe(
            { result ->
                if (!changedPeriodOnly) {
                    textViewPrice.text = result.rates[result.rates.lastKey()].toString()
                }
            },
            ::logError
        )
    }

    private fun logError(error: Throwable) {
        Log.e("error", error.message ?: "error occured", error)
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
        switchPrediction = view.findViewById(R.id.switch_prediction)
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }
}
