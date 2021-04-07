package com.stonks.api.cryptocurrency

import com.google.gson.Gson
import com.stonks.api.ApiConstants
import com.stonks.api.AsyncGetter
import com.stonks.api.utils.UrlBuilder
import com.stonks.calculations.CurrencyConverter
import java.time.*
import java.time.format.DateTimeFormatter

class CryptoCurrencyApiUtils(
    private val cryptoCurrencyName: String,
    val apiUrl: String = ApiConstants.CRYPTOCURRENCY_API_BASE_URL
) {

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

    fun getCryptoCurrencyRatePerDay(
        sourceCrypto: String = cryptoCurrencyName,
        targetCurrency: String
    ): CryptoCurrencyDataModel.CryptoCurrencyPerDay {
        val endpoint = "query"
        val params = mapOf(
            "apikey" to ApiConstants.CRYPTOCURRENCY_API_KEY,
            "from_currency" to sourceCrypto,
            "function" to "CURRENCY_EXCHANGE_RATE",
            "to_currency" to targetCurrency
        )
        val url = UrlBuilder.build(apiUrl, endpoint, params)
        val jsonResponse = AsyncGetter().execute(url).get()
        val result =
            Gson().fromJson(jsonResponse, CryptoCurrencyDataModel.CryptoCurrencyPerDay::class.java)
        return result
    }

    fun getPricesFor1Week(): CryptoCurrencyDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(0, 0, 7)
        val result = getDailyPrices()
        return CryptoCurrencyDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeDaily).toMap().toSortedMap()
        )
    }

    fun getMonthDynamics(): Double {
        val result = getPricesFor1Month()
        return (result.rates[result.rates.lastKey()]
            ?.let { result.rates[result.rates.firstKey()]?.minus(it) }) ?: 0.0
    }

    fun getPricesFor1Month(): CryptoCurrencyDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(0, 1, 0)
        val result = getDailyPrices()
        return CryptoCurrencyDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeDaily).toMap().toSortedMap()
        )
    }

    fun getPricesFor6Months(): CryptoCurrencyDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(0, 6, 0)
        val result = getDailyPrices()
        return CryptoCurrencyDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeDaily).toMap().toSortedMap()
        )
    }

    fun getPricesFor1Year(): CryptoCurrencyDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(1, 0, 0)
        val result = getDailyPrices()
        return CryptoCurrencyDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeDaily).toMap().toSortedMap()
        )
    }

    fun getPricesFor5Years(): CryptoCurrencyDataModel.RatesProcessed {
        val startDateTime: ZonedDateTime =
            endDateTimeDaily - Period.of(5, 0, 0)
        val result = getDailyPrices()
        return CryptoCurrencyDataModel.RatesProcessed(
            filterPeriod(result, startDateTime, endDateTimeDaily).toMap().toSortedMap()
        )
    }

    fun getPricesForCustomPeriod(
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime
    ): CryptoCurrencyDataModel.RatesProcessed {
        val result = getDailyPrices()
        return CryptoCurrencyDataModel.RatesProcessed(
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
    private fun getDailyPrices(): List<Pair<ZonedDateTime, Double>> {
        if (checkDailyDataValid()) {
            return dailyData!!
        } else {
            val endpoint = "query"
            val params = mapOf(
                "apikey" to ApiConstants.CRYPTOCURRENCY_API_KEY,
                "symbol" to cryptoCurrencyName,
                "function" to "DIGITAL_CURRENCY_DAILY",
                "outputsize" to "full"
            )
            val url = UrlBuilder.build(apiUrl, endpoint, params)
            val jsonResponse = AsyncGetter().execute(url).get()
            val result =
                Gson().fromJson(jsonResponse, CryptoCurrencyDataModel.ResultDaily::class.java)
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
