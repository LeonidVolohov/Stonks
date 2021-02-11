package com.stonks.api.stocks

import com.stonks.api.Constants
import com.stonks.api.stocksApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.time.*
import java.time.format.DateTimeFormatter

class StocksApiDataUtils {
    var market: String? = null

    //  Properties to cache results
    private var lastUpdatedDailyData: ZonedDateTime? = null
    private var dailyData: StocksDataModel.RatesProcessed? = null

    private var lastUpdatedIntradayData: ZonedDateTime? = null
    private var intradayData: StocksDataModel.RatesProcessed? = null

    fun getMarket(stock: String): Observable<StocksDataModel.ResultCompanyInfo> {
        return stocksApi.getCompanyMarket(symbol = stock, apikey = Constants.STOCK_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getPricesFor1Day(stock: String): Observable<StocksDataModel.RatesProcessed> {
        return getIntradayPrices(stock, Period.of(0, 0, 1))
    }

    fun getPricesFor1Week(stock: String): Observable<StocksDataModel.RatesProcessed> {
        return getIntradayPrices(stock, Period.of(0, 0, 7))
    }

    fun getPricesFor1Month(stock: String): Observable<StocksDataModel.RatesProcessed> {
        return getIntradayPrices(stock, Period.of(0, 1, 0))
    }

    fun getPricesFor6Months(stock: String): Observable<StocksDataModel.RatesProcessed> {
        return getDailyPrices(stock, Period.of(0, 6, 0))
    }

    fun getPricesFor1Year(stock: String): Observable<StocksDataModel.RatesProcessed> {
        return getDailyPrices(stock, Period.of(1, 0, 0))
    }

    fun getPricesFor5Years(stock: String): Observable<StocksDataModel.RatesProcessed> {
        return getDailyPrices(stock, Period.of(5, 0, 0))
    }

    /**
     * Returns data about daily prices as an Observable
     *
     * @param stock Name of the stock to get data for
     * @param depthInThePast How long ago to get data for (Period of week, month, etc.)
     */
    private fun getDailyPrices(stock: String, depthInThePast: Period): Observable<StocksDataModel.RatesProcessed> {
        if (checkDailyDataValid()) {
            return Observable.just(dailyData)
        } else {
            val observable = stocksApi.getDailyData(symbol = stock, apikey = Constants.STOCK_API_KEY)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { result ->
                        val endDateTime = ZonedDateTime.of(
                                LocalDate.parse(result.data.lastKey(), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                LocalTime.of(0, 0, 0),
                                ZoneId.of(result.metaData.timeZone)
                        )
                        val startDateTime = endDateTime - depthInThePast
                        println(startDateTime)
                        println(endDateTime)
                        StocksDataModel.RatesProcessed(
                                result.data.map {
                                    ZonedDateTime.of(
                                            LocalDate.parse(it.key, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                            LocalTime.of(0, 0, 0),
                                            ZoneId.of(result.metaData.timeZone)
                                    ) to it.value.price.toDouble()
                                }.apply {
                                    val tmp = StocksDataModel.RatesProcessed(this.toMap().toSortedMap())
                                    dailyData = tmp
                                    lastUpdatedDailyData = tmp.rates.lastKey()
                                }.filter {
                                    it.first >= startDateTime && it.first <= endDateTime
                                }.toMap().toSortedMap()
                        )
                    }
            return observable
        }
    }

    /**
     * Returns data about intraday prices as an Observable
     *
     * @param stock Name of the stock to get data for
     * @param depthInThePast How long ago to get data for (Period of week or day)
     */
    private fun getIntradayPrices(stock: String, depthInThePast: Period): Observable<StocksDataModel.RatesProcessed> {
        if (checkIntradayDataValid()) {
            return Observable.just(intradayData)
        } else {
            val observable = stocksApi.getIntradayData(symbol = stock, apikey = Constants.STOCK_API_KEY)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { result ->
                        val endDateTime = ZonedDateTime.of(
                                LocalDateTime.parse(result.data.lastKey(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                ZoneId.of(result.metaData.timeZone)
                        )
                        val startDateTime = endDateTime - depthInThePast
                        println(startDateTime)
                        println(endDateTime)
                        println(result.data.lastKey())
                        StocksDataModel.RatesProcessed(
                                result.data.map {
                                    ZonedDateTime.of(
                                            LocalDateTime.parse(it.key, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                            ZoneId.of(result.metaData.timeZone)
                                    ) to it.value.price.toDouble()
                                }.apply {
                                    val tmp = StocksDataModel.RatesProcessed(this.toMap().toSortedMap())
                                    intradayData = tmp
                                    lastUpdatedIntradayData = tmp.rates.lastKey()
                                }.filter {
                                    it.first >= startDateTime && it.first <= endDateTime
                                }.toMap().toSortedMap()
                        )
                    }
            return observable
        }
    }

    /**
     * Checks whether we can return cached intraday data or a new call ro API is needed
     *
     * @return Boolean showing whether intraday data is still up-to-date'ish
     */
    private fun checkIntradayDataValid(): Boolean = lastUpdatedIntradayData
            ?.withZoneSameInstant(ZoneId.systemDefault())
            ?.isAfter(ZonedDateTime.now(ZoneId.systemDefault()) - Duration.ofMinutes(15))
            ?: false

    /**
     * Checks whether we can return cached daily data or a new call ro API is needed
     *
     * @return Boolean showing whether daily data is still up-to-date'ish
     */
    private fun checkDailyDataValid(): Boolean = lastUpdatedDailyData
            ?.withZoneSameInstant(ZoneId.systemDefault())
            ?.isAfter(ZonedDateTime.now(ZoneId.systemDefault()) - Period.ofDays(1))
            ?: false
}
