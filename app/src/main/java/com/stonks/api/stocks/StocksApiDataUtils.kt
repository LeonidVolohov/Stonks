package com.stonks.api.stocks

import android.util.Log
import com.stonks.api.Constants
import com.stonks.api.stocksApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.time.*
import java.time.format.DateTimeFormatter

class StocksApiDataUtils(val stock: String) {

    private companion object {
        const val dateFormatDaily = "yyyy-MM-dd"
        const val dateFormatIntraday = "yyyy-MM-dd HH:mm:ss"
        val endDateTimeDaily: ZonedDateTime
            get() = ZonedDateTime.of(
                LocalDate.now(),
                LocalTime.MIDNIGHT,
                ZoneId.systemDefault()
            )

        val endDateTimeIntraDay: ZonedDateTime
            get() = ZonedDateTime.of(
                LocalDate.now(),
                LocalTime.now(),
                ZoneId.systemDefault()
            )
    }

    var market: String? = null

    //  Properties to cache results
    private var lastUpdatedDailyData: ZonedDateTime? = null
    private var dailyData: List<Pair<ZonedDateTime, Double>>? = null

    private var lastUpdatedIntradayData: ZonedDateTime? = null
    private var intradayData: List<Pair<ZonedDateTime, Double>>? = null

    fun getMarket(): Observable<StocksDataModel.ResultCompanyInfo> {
        return stocksApi.getCompanyMarket(symbol = stock, apikey = Constants.STOCK_API_KEY)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getLatestRate(): Observable<Double> {
        return getIntradayPrices().map {
            it[it.size - 1].second
        }
    }

    fun getPricesFor1Day(): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 0, 1)
        return getIntradayPrices().map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor1Week(): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 0, 7)
        return getIntradayPrices().map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor1Month(): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 7, 0)
        return getIntradayPrices().map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor6Months(): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(0, 6, 0)
        return getDailyPrices().map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeDaily).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor1Year(): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(1, 0, 0)
        return getDailyPrices().map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeDaily).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor5Years(): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(5, 0, 0)
        return getDailyPrices().map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeDaily).toMap().toSortedMap()
            )
        }
    }

    fun getPricesForCustomPeriod(
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime
    ): Observable<StocksDataModel.RatesProcessed> {
        return getDailyPrices().map {
            StocksDataModel.RatesProcessed(
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
            Log.i("CachedData", "Using cached data")
            return Observable.just(dailyData)
        } else {
            Log.i("CachedData", "NOT Using cached data")
            return stocksApi.getDailyData(symbol = stock, apikey = Constants.STOCK_API_KEY)
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
     * Returns data about intraday prices as an Observable
     */
    private fun getIntradayPrices(): Observable<List<Pair<ZonedDateTime, Double>>> {
        if (checkIntradayDataValid()) {
            Log.i("CachedData", "Using cached data")
            return Observable.just(intradayData)
        } else {
            Log.i("CachedData", "NOT Using cached data")
            return stocksApi.getIntradayData(apikey = Constants.STOCK_API_KEY, symbol = stock)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { result ->
                    intradayData = result.data.map {
                        ZonedDateTime.of(
                            LocalDateTime.parse(
                                it.key,
                                DateTimeFormatter.ofPattern(dateFormatIntraday)
                            ),
                            ZoneId.of(result.metaData.timeZone)
                        ).withZoneSameInstant(ZoneId.systemDefault()) to it.value.price.toDouble()
                    }
                    lastUpdatedIntradayData = ZonedDateTime.now(ZoneId.systemDefault())
                    intradayData
                }
        }
    }

    /**
     * Checks whether we can return cached intraday data or a new call ro API is needed
     *
     * @return Boolean showing whether intraday data is still up-to-date'ish
     */
    private fun checkIntradayDataValid(): Boolean = lastUpdatedIntradayData
        ?.isAfter(ZonedDateTime.now(ZoneId.systemDefault()) - Duration.ofMinutes(15))
        ?: false

    /**
     * Checks whether we can return cached daily data or a new call ro API is needed
     *
     * @return Boolean showing whether daily data is still up-to-date'ish
     */
    private fun checkDailyDataValid(): Boolean = lastUpdatedDailyData
        ?.isAfter(ZonedDateTime.now(ZoneId.systemDefault()) - Period.ofDays(1))
        ?: false
}
