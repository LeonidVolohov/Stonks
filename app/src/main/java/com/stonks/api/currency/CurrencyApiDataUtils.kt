package com.stonks.api.currency

import com.stonks.api.Constants.Companion.CURRENCY_API_KEY
import com.stonks.api.currencyApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CurrencyApiDataUtils {

    fun getLastUpdatedDate(): Observable<CurrencyDataModel.LastUpdatedDate> {
        return currencyApi.getLastUpdatedDate(apiKey = CURRENCY_API_KEY)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getTargetRatePrice(baseRate: String): Observable<CurrencyDataModel.CurrencyPerDay> {
        return currencyApi.getRatesPerDay(apiKey = CURRENCY_API_KEY, rate = baseRate)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getRatesPerPeriod(startDate: String, endDate: String, baseRate: String, targetRate: String)
            : Observable<CurrencyDataModel.CurrencyPerPeriod> {
        return currencyApi.getRatesPerPeriod(
            apiKey = CURRENCY_API_KEY,
            startDate = startDate,
            endDate = endDate,
            baseRate = baseRate,
            targetRate = targetRate
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getPrimaryRatesPerDay(baseRate: String, symbols: String): Observable<CurrencyDataModel.CurrencyPrimaryPerDay> {
        return currencyApi.getPrimaryRatesPerDay(
            apiKey = CURRENCY_API_KEY,
            baseRate = baseRate,
            primaryCurrencies = symbols
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}
