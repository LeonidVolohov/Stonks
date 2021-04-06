package com.stonks.api.stocks

import com.google.gson.Gson
import com.stonks.api.ApiConstants
import com.stonks.api.AsyncGetter
import com.stonks.api.utils.UrlBuilder
import com.stonks.calculations.CurrencyConverter
import java.time.*
import java.time.format.DateTimeFormatter

class StocksApiDataUtils(val stock: String, val apiUrl: String = ApiConstants.STOCK_API_BASE_URL) {

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

    //  Properties to cache results
    private var lastUpdatedDailyData: ZonedDateTime? = null
    private var dailyData: List<Pair<ZonedDateTime, Double>>? = null

    private var lastUpdatedIntradayData: ZonedDateTime? = null
    private var intradayData: List<Pair<ZonedDateTime, Double>>? = null

    fun getMarket(): StocksDataModel.ResultCompanyInfo {
        val endpoint = "query"
        val params = mapOf(
            "apikey" to ApiConstants.STOCK_API_KEY,
            "symbol" to stock,
            "function" to "OVERVIEW"
        )
        val url = UrlBuilder.build(apiUrl, endpoint, params)
        val jsonResponse = AsyncGetter().execute(url).get()
        return Gson().fromJson(jsonResponse, StocksDataModel.ResultCompanyInfo::class.java)
    }

    fun getLatestRate(): Double {
        val result = getIntradayPrices()
        return result[result.size - 1].second
    }

    fun getMonthDynamics(): Double {
        val result = getPricesFor1Month()
        return (result.rates[result.rates.lastKey()]
            ?.let { result.rates[result.rates.firstKey()]?.minus(it) }) ?: 0.0
    }

    fun getPricesFor1Day(): StocksDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 0, 1)
        val result = getIntradayPrices()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
        )
    }

    fun getPricesFor1Week(): StocksDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 0, 7)
        val result = getDailyPrices()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
        )
    }

    fun getPricesFor1Month(): StocksDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeIntraDay - Period.of(0, 1, 0)
        val result = getDailyPrices()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeIntraDay).toMap().toSortedMap()
        )
    }

    fun getPricesFor6Months(): StocksDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(0, 6, 0)
        val result = getDailyPrices()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeDaily).toMap().toSortedMap()
        )
    }

    fun getPricesFor1Year(): StocksDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(1, 0, 0)
        val result = getDailyPrices()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeDaily).toMap().toSortedMap()
        )
    }

    fun getPricesFor5Years(): StocksDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(5, 0, 0)
        val result = getDailyPrices()
        return StocksDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeDaily).toMap().toSortedMap()
        )
    }

    fun getPricesForCustomPeriod(
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime
    ): StocksDataModel.RatesProcessed {
        val result = getDailyPrices()
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
    fun getDailyPrices(): List<Pair<ZonedDateTime, Double>> {
        if (checkDailyDataValid()) {
            return dailyData!!
        } else {
            val endpoint = "query"
            val params = mapOf(
                "apikey" to ApiConstants.STOCK_API_KEY,
                "symbol" to stock,
                "function" to "TIME_SERIES_DAILY",
                "outputsize" to "full"
            )
            val url = UrlBuilder.build(apiUrl, endpoint, params)
            val jsonResponse = AsyncGetter().execute(url).get()
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
    fun getIntradayPrices(): List<Pair<ZonedDateTime, Double>> {
        if (checkIntradayDataValid()) {
            return intradayData!!
        } else {
            val endpoint = "query"
            val params = mapOf(
                "apikey" to ApiConstants.STOCK_API_KEY,
                "symbol" to stock,
                "function" to "TIME_SERIES_INTRADAY",
                "outputsize" to "full",
                "interval" to "15min"
            )
            val url = UrlBuilder.build(apiUrl, endpoint, params)
            val jsonResponse = AsyncGetter().execute(url).get()
            val result = Gson().fromJson(jsonResponse, StocksDataModel.ResultIntraday::class.java)
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
            return intradayData!!
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
