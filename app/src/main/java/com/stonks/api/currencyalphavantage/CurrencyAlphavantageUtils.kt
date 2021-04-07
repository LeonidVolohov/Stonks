package com.stonks.api.currencyalphavantage

import com.stonks.api.ApiConstants.Companion.CURRENCY_ALPHAVANTAGE_ANOTHER_ANOTHER_API_KEY
import com.stonks.api.currencyAlphavantageApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CurrencyAlphavantageUtils {

    fun getDataForPeriod(
        fromCurrencyName: String,
        toCurrencyName: String
    ): Observable<CurrencyAplhavantageDataModel.CurrencyData> {
        return currencyAlphavantageApi.getDataForPeriod(
            function = "FX_MONTHLY",
            fromCurrencyName = fromCurrencyName,
            toCurrencyName = toCurrencyName,
            apiKey = CURRENCY_ALPHAVANTAGE_ANOTHER_ANOTHER_API_KEY
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }
}
