package com.stonks.api.stocks

import androidx.test.runner.AndroidJUnit4
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.ZoneId
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class StocksApiDataUtilsTest {

    val server = MockWebServer()
    private lateinit var requestUrl: String
    private lateinit var api: StocksApiDataUtils

    @Before
    fun setUp() {
        server.start(8080)
        requestUrl = server.url("/").toString()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = MockResponse()
                when (request.requestUrl?.queryParameter("function")) {
                    "OVERVIEW" -> response.setBody(overviewResponse)
                    "TIME_SERIES_DAILY" -> response.setBody(dailyResponse)
                    "TIME_SERIES_INTRADAY" -> response.setBody(intradayResponse)
                    else -> TODO("Incorrect request URL")
                }
                return response
            }
        }
        api = StocksApiDataUtils("noStock", requestUrl)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getMarket_test() {
        val result = api.getMarket()
        Assert.assertEquals(result.market, "NYSE")
        Assert.assertEquals(result.currency, "USD")
    }

    @Test
    fun getLatestRate_test() {
        val result = api.getLatestRate()
        Assert.assertEquals(result, 133.2300, 0.00001)
    }

    @Test
    fun getMonthDynamics_test() {
        val result = api.getMonthDynamics()
        Assert.assertEquals(result, 0.0, 0.00001)
    }

    @Test
    fun getPricesFor1Day_test() {
        val result = api.getPricesFor1Day()
        Assert.assertEquals(result.rates.size, 0)
    }

    @Test
    fun getPricesFor1Week_test() {
        val result = api.getPricesFor1Week()
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getPricesFor1Month_test() {
        val result = api.getPricesFor1Month()
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getPricesFor6Months_test() {
        val result = api.getPricesFor6Months()
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getPricesFor1Year_test() {
        val result = api.getPricesFor1Year()
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getPricesFor5Years_test() {
        val result = api.getPricesFor5Years()
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getPricesForCustomPeriod_test() {
        val testPeriodStart = ZonedDateTime.of(
            2021, 3, 1, 0, 0, 0, 0, ZoneId.systemDefault()
        )
        val testPeriodEnd = ZonedDateTime.of(
            2021, 5, 1, 0, 0, 0, 0, ZoneId.systemDefault()
        )
        val result = api.getPricesForCustomPeriod(testPeriodStart, testPeriodEnd)
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getDailyPrices_test() {
        var dummy = api.getDailyPrices()
        dummy = api.getDailyPrices()
    }

    @Test
    fun getIntradayPrices_test() {
        var dummy = api.getIntradayPrices()
        dummy = api.getIntradayPrices()
    }

    @Test
    fun convertToCurrency_test() {
        val testDT = ZonedDateTime.of(
            2021, 3, 1, 0, 0, 0, 0, ZoneId.systemDefault()
        )
        val testRates = StocksDataModel.RatesProcessed(mapOf(testDT to 500.0).toSortedMap())
        api.convertToCurrency("EUR", testRates)
        Assert.assertEquals(testRates.rates[testRates.rates.firstKey()] ?: 0.0, 415.0, 0.0001)
    }

    @Test
    fun getStock_test() {
        Assert.assertEquals(api.stock, "noStock")
    }

    @Test
    fun getApiUrl_test() {
        Assert.assertEquals(api.apiUrl, "http://localhost:8080/")
    }

    @Test
    fun constructDataModels_test() {
        val dummy1 = StocksDataModel.MetaDataIntraday("UTC")
        val dummy2 = StocksDataModel.MetaDataDaily("UTC")
        val dummy3 = StocksDataModel.ResultCompanyInfo("NASDAQ", "USD")
        val dummy4 = StocksDataModel.RateData("500.0")
        val dummy5 =
            StocksDataModel.ResultIntraday(dummy1, mapOf("2021-01-01" to dummy4).toSortedMap())
        val dummy6 =
            StocksDataModel.ResultDaily(dummy2, mapOf("2021-01-01" to dummy4).toSortedMap())
    }

    private val overviewResponse = "{\n" +
            "  \"Symbol\": \"IBM\",\n" +
            "  \"Exchange\": \"NYSE\",\n" +
            "  \"Currency\": \"USD\"\n" +
            "}"
    private val dailyResponse = "{\n" +
            "    \"Meta Data\": {\n" +
            "        \"1. Information\": \"Daily Prices (open, high, low, close) and Volumes\",\n" +
            "        \"2. Symbol\": \"IBM\",\n" +
            "        \"3. Last Refreshed\": \"2021-04-01\",\n" +
            "        \"4. Output Size\": \"Full size\",\n" +
            "        \"5. Time Zone\": \"US/Eastern\"\n" +
            "    },\n" +
            "    \"Time Series (Daily)\": {\n" +
            "        \"2021-04-01\": {\n" +
            "            \"1. open\": \"133.7600\",\n" +
            "            \"2. high\": \"133.9300\",\n" +
            "            \"3. low\": \"132.2700\",\n" +
            "            \"4. close\": \"133.2300\",\n" +
            "            \"5. volume\": \"4074161\"\n" +
            "        }\n" +
            "  }\n" +
            "}"
    private val intradayResponse = "{\n" +
            "  \"Meta Data\": {\n" +
            "    \"1. Information\": \"Intraday (5min) open, high, low, close prices and volume\",\n" +
            "    \"2. Symbol\": \"IBM\",\n" +
            "    \"3. Last Refreshed\": \"2021-04-01 18:05:00\",\n" +
            "    \"4. Interval\": \"5min\",\n" +
            "    \"5. Output Size\": \"Full size\",\n" +
            "    \"6. Time Zone\": \"US/Eastern\"\n" +
            "  },\n" +
            "  \"Time Series (15min)\": {\n" +
            "    \"2021-04-01 18:05:00\": {\n" +
            "      \"1. open\": \"133.2800\",\n" +
            "      \"2. high\": \"133.2800\",\n" +
            "      \"3. low\": \"133.2300\",\n" +
            "      \"4. close\": \"133.2300\",\n" +
            "      \"5. volume\": \"905\"\n" +
            "    }\n" +
            "  }\n" +
            "}"
}