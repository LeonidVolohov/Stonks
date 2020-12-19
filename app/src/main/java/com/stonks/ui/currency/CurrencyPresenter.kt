package com.stonks.ui.currency

import android.util.Log
import com.stonks.api.Constants
import com.stonks.api.currency.CurrencyRequests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory

class CurrencyPresenter {

    private val TAG : String = CurrencyPresenter::class.java.name

    fun getAllCurrencyList() {
        val currencyApi: CurrencyRequests = Retrofit.Builder()
                .baseUrl(Constants.CURRENCY_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CurrencyRequests::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {

                val response = currencyApi.getRates(rate = "RUB").awaitResponse()
                val data = response.body()!!

                Log.i(TAG, "Current date: " + data.date)
                Log.i(TAG, "Base rate: " + data.base)
                Log.i(TAG, data.rates?.EUR.toString())
                Log.i(TAG, data.rates?.RUB.toString())
                Log.i(TAG, data.rates?.USD.toString())


            } catch (exception: Exception) {
                Log.e(TAG, exception.toString())
            }
        }
    }
}