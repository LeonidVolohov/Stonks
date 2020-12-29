package com.stonks.ui.currency

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.stonks.R
import com.stonks.api.currencyApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

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
        lateinit var baseRateSpinnerString : String
        lateinit var targetRateSpinnerString : String

        // TODO: Синхронизировать поток запроса к апи и вывода на экран при помощи RxJava
        dateRateTextView.text = getApiDate()


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

                resultRateTextView.text = getTargetRatePrice(baseRate = baseRateSpinnerString,
                        targetRate = targetRateSpinnerString)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    /**
     * @param[baseRate] Указана по умолчанию, т.к. возвращаемое значение не зависит от валюты
     * @return Возвращает поле data с API
     * */
    private fun getApiDate(baseRate: String = "RUB") : String {
        var currentDate: String = ""
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = currencyApi.getRates(rate = baseRate).awaitResponse()
                val data = response.body()!!

                currentDate = data.date.toString()
            } catch (exception: Exception) {
                Log.e(TAG, exception.toString())
            }
        }
        return currentDate
    }

    /**
     * Возвращает стоимость [targetRate] по отношению к [baseRate]
     */
    private fun getTargetRatePrice(baseRate : String, targetRate: String) : String {
        var outputString: String = ""
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = currencyApi.getRates(rate = baseRate).awaitResponse()
                val data = response.body()!!

                for (key in data.rates?.entries!!) {
                    if (targetRate == key.key.toString()) {
                        outputString = key.value.toString()
                    }
                }
            } catch (exception: Exception) {
                Log.e(TAG, exception.toString())
            }
        }
        return outputString
    }
}
