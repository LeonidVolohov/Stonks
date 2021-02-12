package com.stonks.api

import com.stonks.api.Constants.Companion.CRYPTOCURRENCY_API_BASE_URL
import com.stonks.api.Constants.Companion.CURRENCY_API_BASE_URL
import com.stonks.api.Constants.Companion.STOCK_API_BASE_URL
import com.stonks.api.cryptocurrency.CryptoCurrencyRequests
import com.stonks.api.currency.CurrencyRequests
import com.stonks.api.stocks.StocksRequests
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

val currencyApi: CurrencyRequests = Retrofit.Builder()
        .baseUrl(CURRENCY_API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(CurrencyRequests::class.java)

val cryptoCurrencyApi: CryptoCurrencyRequests = Retrofit.Builder()
        .baseUrl(CRYPTOCURRENCY_API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(CryptoCurrencyRequests::class.java)

val stocksApi = Retrofit.Builder()
        .baseUrl(STOCK_API_BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StocksRequests::class.java)
