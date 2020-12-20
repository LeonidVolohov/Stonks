package com.stonks.ui.currency

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
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

        getAllCurrencyList()

        val baseRateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ratesArray)
        baseRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        baseRateSpinner.adapter = baseRateAdapter
        baseRateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Toast.makeText(this@CurrencyMain, ratesArray[position], Toast.LENGTH_LONG).show()
                Log.i(TAG, "Base rate: " + ratesArray[position])
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
                Toast.makeText(this@CurrencyMain, ratesArray[position], Toast.LENGTH_LONG).show()
                Log.i(TAG, "Target rate: " + ratesArray[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun getTargetRate(baseRate : String, targetRate: String) : List<String> {
        val outputList: MutableList<String> = mutableListOf()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = currencyApi.getRates(rate = baseRate).awaitResponse()
                val data = response.body()!!

                // val answer : String = data.rates

                outputList.add("Тут должно быть поле Rates, равное targetRate")
                outputList.add(data.date.toString())

            } catch (exception: Exception) {
                Log.e(TAG, exception.toString())
            }
        }
        return outputList
    }

    private fun getAllCurrencyList() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = currencyApi.getRates(rate = "RUB").awaitResponse()
                val data = response.body()!!

                Log.i(TAG, "Current date: " + data.date)

                for (key in data.rates?.entries!!) {
                    Log.i(TAG, "Rate RUB to ${key.value.toString()}: ")
                }

                // Log.i(TAG, data.rates?.javaClass?.kotlin?.simpleName.toString())

            } catch (exception: Exception) {
                Log.e(TAG, exception.toString())
            }
        }
    }
}
