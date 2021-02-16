package com.stonks.api.currency

import com.stonks.api.currencyApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CurrencyApiDataUtils {

    fun getLastUpdatedDate(): Observable<CurrencyDataModel.LastUpdatedDate> {
        return currencyApi.getLastUpdatedDate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getTargetRatePrice(baseRate: String): Observable<CurrencyDataModel.CurrencyPerDay> {
        return currencyApi.getRatesPerDay(baseRate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getRatesPerPeriod(startDate: String, endDate: String, baseRate: String, targetRate: String)
            : Observable<CurrencyDataModel.CurrencyPerPeriod> {
        return currencyApi.getRatesPerPeriod(startDate, endDate, baseRate, targetRate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getPrimaryRatesPerDay(baseRate: String, symbols: String): Observable<CurrencyDataModel.CurrencyPrimaryPerDay> {
        return currencyApi.getPrimaryRatesPerDay(baseRate, symbols)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}
