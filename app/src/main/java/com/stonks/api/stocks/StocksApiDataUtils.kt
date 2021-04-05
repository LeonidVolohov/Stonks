package com.stonks.api.stocks

import com.google.gson.Gson
import com.stonks.api.AsyncGetter
import com.stonks.api.Constants
import com.stonks.api.stocksApi
import com.stonks.api.utils.UrlBuilder
import com.stonks.calculations.CurrencyConverter
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

    private val TAG = this::class.java.name

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

    fun getMarketOkHttp(): StocksDataModel.ResultCompanyInfo {
        val endpoint = "query"
        val params = mapOf(
            "apikey" to Constants.STOCK_API_KEY,
            "symbol" to stock,
            "function" to "OVERVIEW"
        )
        val url = UrlBuilder.build(Constants.STOCK_API_BASE_URL, endpoint, params)
        val jsonResponse = AsyncGetter.execute(url).get()
        return Gson().fromJson(jsonResponse, StocksDataModel.ResultCompanyInfo::class.java)
    }

    fun getLatestRate(): Observable<Double> {
        return getIntradayPrices().map {
            it[it.size - 1].second
        }
    }

    fun getMonthDynamics(): Observable<Double> {
        return getPricesFor1Month().map {
            (it.rates[it.rates.lastKey()]?.let { it1 -> it.rates[it.rates.firstKey()]?.minus(it1) })
                ?: 0.0
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
        return getDailyPrices().map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor1WeekOkHttp(): StocksDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 0, 7)
        val result = getDailyPricesOkHttp()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
        )
    }

    fun getPricesFor1Month(): Observable<StocksDataModel.RatesProcessed> {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 1, 0)
        return getDailyPrices().map {
            StocksDataModel.RatesProcessed(
                filterPeriod(it, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
            )
        }
    }

    fun getPricesFor1MonthOkHttp(): StocksDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 1, 0)
        val result = getDailyPricesOkHttp()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
        )
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

    fun getPricesFor6MonthsOkHttp(): StocksDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(0, 6, 0)
        val result = getDailyPricesOkHttp()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeDaily).toMap().toSortedMap()
        )
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

    fun getPricesFor1YearOkHttp(): StocksDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(1, 0, 0)
        val result = getDailyPricesOkHttp()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeDaily).toMap().toSortedMap()
        )
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

    fun getPricesFor5YearsOkHttp(): StocksDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(5, 0, 0)
        val result = getDailyPricesOkHttp()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeDaily).toMap().toSortedMap()
        )
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

    fun getPricesForCustomPeriodOkHttp(
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime
    ): StocksDataModel.RatesProcessed {
        val result = getDailyPricesOkHttp()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTime).toMap().toSortedMap()
        )
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

    fun getDailyPricesOkHttp(): List<Pair<ZonedDateTime, Double>> {
        if (checkDailyDataValid()) {
            return dailyData!!
        } else {
            val endpoint = "query"
            val params = mapOf(
                "apikey" to Constants.STOCK_API_KEY,
                "symbol" to stock,
                "function" to "TIME_SERIES_DAILY",
                "outputsize" to "full"
            )
            val url = UrlBuilder.build(Constants.STOCK_API_BASE_URL, endpoint, params)
            val jsonResponse = AsyncGetter.execute(url).get()
            val result = Gson().fromJson(jsonResponse, StocksDataModel.ResultDaily::class.java)
            dailyData = result.data.map {
                ZonedDateTime.of(
                    LocalDate.parse(it.key, DateTimeFormatter.ofPattern(dateFormatDaily)),
                    LocalTime.MIDNIGHT,
                    ZoneId.of(result.metaData.timeZone)
                ).withZoneSameInstant(ZoneId.systemDefault()) to it.value.price.toDouble()
            }
            lastUpdatedDailyData = ZonedDateTime.now(ZoneId.systemDefault())
            return dailyData!!
        }
    }

    /**
     * Returns data about intraday prices as an Observable
     */
    private fun getIntradayPrices(): Observable<List<Pair<ZonedDateTime, Double>>> {
        if (checkIntradayDataValid()) {
            return Observable.just(intradayData)
        } else {
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

    fun convertToCurrency(currencyCode: String, rates: StocksDataModel.RatesProcessed) {
        val conversionRate = CurrencyConverter.ratesToUSD[currencyCode] ?: 0.0
        rates.rates.replaceAll { t, u -> u * conversionRate }
    }
}
