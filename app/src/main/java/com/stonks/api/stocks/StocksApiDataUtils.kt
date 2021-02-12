package com.stonks.api.stocks

import android.util.Log
import com.stonks.api.Constants
import com.stonks.api.stocksApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.time.*
import java.time.format.DateTimeFormatter

class StocksApiDataUtils {

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
    private var dailyData: StocksDataModel.RatesProcessed? = null

    private var lastUpdatedIntradayData: ZonedDateTime? = null
    private var intradayData: StocksDataModel.RatesProcessed? = null

    fun getMarket(stock: String): Observable<StocksDataModel.ResultCompanyInfo> {
        return stocksApi.getCompanyMarket(symbol = stock, apikey = Constants.STOCK_API_KEY)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                it.apply {
                    Log.e("error", it.toString())
                }
            }
    }

    fun getPricesFor1Day(stock: String): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 0, 1)
        return getIntradayPrices(stock).map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor1Week(stock: String): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 0, 7)
        return getIntradayPrices(stock).map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor1Month(stock: String): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 0, 7)
        return getIntradayPrices(stock).map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor6Months(stock: String): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(0, 6, 0)
        return getDailyPrices(stock).map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeDaily).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor1Year(stock: String): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(1, 0, 0)
        return getDailyPrices(stock).map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeDaily).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor5Years(stock: String): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(5, 0, 0)
        return getDailyPrices(stock).map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeDaily).toMap().toSortedMap()
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
     *
     * @param stock Name of the stock to get data
     */
    private fun getDailyPrices(
        stock: String
    ): Observable<List<Pair<ZonedDateTime, Double>>> {
        return stocksApi.getDailyData(symbol = stock, apikey = Constants.STOCK_API_KEY)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { result ->
                Log.e("error", result.toString())
                result.data.map {
                    ZonedDateTime.of(
                        LocalDate.parse(it.key, DateTimeFormatter.ofPattern(dateFormatDaily)),
                        LocalTime.MIDNIGHT,
                        ZoneId.of(result.metaData.timeZone)
                    ).withZoneSameInstant(ZoneId.systemDefault()) to it.value.price.toDouble()
                }
            }
    }

    /**
     * Returns data about intraday prices as an Observable
     *
     * @param stock Name of the stock to get data for
     */
    private fun getIntradayPrices(
        stock: String,
    ): Observable<List<Pair<ZonedDateTime, Double>>> {
        return stocksApi.getIntradayData(apikey = Constants.STOCK_API_KEY, symbol = stock)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { result ->
                result.apply {
                    Log.e("error", result.toString())
                }
                result.data.map {
                    ZonedDateTime.of(
                        LocalDateTime.parse(
                            it.key,
                            DateTimeFormatter.ofPattern(dateFormatIntraday)
                        ),
                        ZoneId.of(result.metaData.timeZone)
                    ).withZoneSameInstant(ZoneId.systemDefault()) to it.value.price.toDouble()
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
