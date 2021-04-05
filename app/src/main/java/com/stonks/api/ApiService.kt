package com.stonks.api

import com.stonks.api.Constants.Companion.CRYPTOCURRENCY_API_BASE_URL
import com.stonks.api.Constants.Companion.CURRENCY_API_BASE_URL
import com.stonks.api.cryptocurrency.CryptoCurrencyRequests
import com.stonks.api.currency.CurrencyRequests
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

val interceptor: HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply { this.level = HttpLoggingInterceptor.Level.BODY }

val client: OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(interceptor)
    .build()

val currencyApi: CurrencyRequests = Retrofit.Builder()
    .baseUrl(CURRENCY_API_BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .client(client)
    .build()
    .create(CurrencyRequests::class.java)

val cryptoCurrencyApi: CryptoCurrencyRequests = Retrofit.Builder()
    .baseUrl(CRYPTOCURRENCY_API_BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .client(client)
    .build()
    .create(CryptoCurrencyRequests::class.java)
