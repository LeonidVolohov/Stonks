package com.stonks.api.cryptocurrency

import com.stonks.api.ApiConstants
import com.stonks.api.cryptoCurrencyApi
import com.stonks.calculations.CurrencyConverter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.time.*
import java.time.format.DateTimeFormatter

class CryptoCurrencyApiUtils(private val cryptoCurrencyName: String) {

    private companion object {
        const val dateFormatDaily = "yyyy-MM-dd"
        val endDateTimeDaily: ZonedDateTime
            get() = ZonedDateTime.of(
                LocalDate.now(),
                LocalTime.MIDNIGHT,
                ZoneId.systemDefault()
            )
    }

    //  Properties to cache results
    private var lastUpdatedDailyData: ZonedDateTime? = null
    private var dailyData: List<Pair<ZonedDateTime, Double>>? = null

    fun getPricesFor1Week(): Observable<CryptoCurrencyDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(0, 0, 7)
        return getDailyPrices().map {
            CryptoCurrencyDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeDaily).toMap().toSortedMap()
            )
        }
    }

    fun getMonthDynamics(): Observable<Double> {
        return getPricesFor1Month().map {
            (it.rates[it.rates.lastKey()]?.let { it1 -> it.rates[it.rates.firstKey()]?.minus(it1) })
                ?: 0.0
        }
    }

    fun getPricesFor1Month(): Observable<CryptoCurrencyDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(0, 1, 0)
        return getDailyPrices().map {
            CryptoCurrencyDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeDaily).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor6Months(): Observable<CryptoCurrencyDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(0, 6, 0)
        return getDailyPrices().map {
            CryptoCurrencyDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeDaily).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor1Year(): Observable<CryptoCurrencyDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(1, 0, 0)
        return getDailyPrices().map {
            CryptoCurrencyDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeDaily).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor5Years(): Observable<CryptoCurrencyDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(5, 0, 0)
        return getDailyPrices().map {
            CryptoCurrencyDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeDaily).toMap().toSortedMap()
            )
        }
    }

    fun getPricesForCustomPeriod(
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime
    ): Observable<CryptoCurrencyDataModel.RatesProcessed> {
        return getDailyPrices().map {
            CryptoCurrencyDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTime).toMap().toSortedMap()
            )
        }
    }

    private fun filterPeriod(
        fullData: List<Pair<ZonedDateTime, Double>>,
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime
    ): List<Pair<ZonedDateTime, Double>> = fullData.filter {
        it.first >= startDateTime && it.first <= endDateTime
    }

    /**
     * Returns data about daily prices as an Observable
     */
    private fun getDailyPrices(): Observable<List<Pair<ZonedDateTime, Double>>> {
        if (checkDailyDataValid()) {
            return Observable.just(dailyData)
        } else {
            return cryptoCurrencyApi
                .getDailyStats(
                    cryptoCurrencyName = cryptoCurrencyName,
                    apikey = ApiConstants.CRYPTOCURRENCY_API_KEY
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { result ->
                    dailyData = result.data.map {
                        ZonedDateTime.of(
                            LocalDate.parse(it.key, DateTimeFormatter.ofPattern(dateFormatDaily)),
                            LocalTime.MIDNIGHT,
                            ZoneId.of(result.metaData.timeZone)
                        ).withZoneSameInstant(ZoneId.systemDefault()) to it.value.price.toDouble()
                    }
                    lastUpdatedDailyData = ZonedDateTime.now(ZoneId.systemDefault())
                    dailyData
                }
        }
    }

    /**
     * Checks whether we can return cached daily data or a new call ro API is needed
     *
     * @return Boolean showing whether daily data is still up-to-date'ish
     */
    private fun checkDailyDataValid(): Boolean = lastUpdatedDailyData
        ?.isAfter(ZonedDateTime.now(ZoneId.systemDefault()) - Period.ofDays(1))
        ?: false

    fun convertToCurrency(currencyCode: String, rates: CryptoCurrencyDataModel.RatesProcessed) {
        val conversionRate = CurrencyConverter.ratesToUSD[currencyCode] ?: 0.0
        rates.rates.replaceAll { t, u -> u * conversionRate }
    }
}
