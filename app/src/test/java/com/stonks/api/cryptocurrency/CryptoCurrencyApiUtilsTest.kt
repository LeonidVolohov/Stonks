package com.stonks.api.cryptocurrency

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
class CryptoCurrencyApiUtilsTest {

    val server = MockWebServer()
    private lateinit var requestBaseUrl: String
    private lateinit var api: CryptoCurrencyApiUtils

    @Before
    fun setUp() {
        server.start(8080)
        requestBaseUrl = server.url("/").toString()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = MockResponse()
                when (request.requestUrl?.queryParameter("function")) {
                    "CURRENCY_EXCHANGE_RATE" -> response.setBody(currencyExchangeRateResponse)
                    "DIGITAL_CURRENCY_DAILY" -> response.setBody(dailyResponse)
                    else -> TODO("Incorrect request URL")
                }
                return response
            }
        }
        api = CryptoCurrencyApiUtils("BTC", requestBaseUrl)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getPricesFor1Week_test() {
        val result = api.getPricesFor1Week()
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getMonthDynamics_test() {
        val result = api.getMonthDynamics()
        Assert.assertEquals(result, 0.0, 0.00001)
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
    fun convertToCurrency_test() {
        val testDT = ZonedDateTime.of(
            2021, 3, 1, 0, 0, 0, 0, ZoneId.systemDefault()
        )
        val testRates = CryptoCurrencyDataModel.RatesProcessed(mapOf(testDT to 500.0).toSortedMap())
        api.convertToCurrency("EUR", testRates)
        Assert.assertEquals(testRates.rates[testRates.rates.firstKey()] ?: 0.0, 415.0, 0.0001)
    }

    @Test
    fun getApiUrl_test() {
    }

    private val currencyExchangeRateResponse = "{\n" +
            "    \"Realtime Currency Exchange Rate\": {\n" +
            "        \"1. From_Currency Code\": \"BTC\",\n" +
            "        \"2. From_Currency Name\": \"Bitcoin\",\n" +
            "        \"3. To_Currency Code\": \"CNY\",\n" +
            "        \"4. To_Currency Name\": \"Chinese Yuan\",\n" +
            "        \"5. Exchange Rate\": \"380019.86616000\",\n" +
            "        \"6. Last Refreshed\": \"2021-04-07 00:28:19\",\n" +
            "        \"7. Time Zone\": \"UTC\",\n" +
            "        \"8. Bid Price\": \"380018.75487000\",\n" +
            "        \"9. Ask Price\": \"380018.82024000\"\n" +
            "    }\n" +
            "}"
    private val dailyResponse = "{\n" +
            "    \"Meta Data\": {\n" +
            "        \"1. Information\": \"Daily Prices and Volumes for Digital Currency\",\n" +
            "        \"2. Digital Currency Code\": \"BTC\",\n" +
            "        \"3. Digital Currency Name\": \"Bitcoin\",\n" +
            "        \"4. Market Code\": \"CNY\",\n" +
            "        \"5. Market Name\": \"Chinese Yuan\",\n" +
            "        \"6. Last Refreshed\": \"2021-04-06 00:00:00\",\n" +
            "        \"7. Time Zone\": \"UTC\"\n" +
            "    },\n" +
            "    \"Time Series (Digital Currency Daily)\": {\n" +
            "        \"2021-04-06\": {\n" +
            "            \"1a. open (USD)\": \"388324.38332700\",\n" +
            "            \"1b. open (USD)\": \"59129.99000000\",\n" +
            "            \"2a. high (USD)\": \"390616.43670000\",\n" +
            "            \"2b. high (USD)\": \"59479.00000000\",\n" +
            "            \"3a. low (USD)\": \"387756.18053100\",\n" +
            "            \"3b. low (USD)\": \"59043.47000000\",\n" +
            "            \"4a. close (USD)\": \"389010.92886900\",\n" +
            "            \"4b. close (USD)\": \"59234.53000000\",\n" +
            "            \"5. volume\": \"2573.65850200\",\n" +
            "            \"6. market cap (USD)\": \"2573.65850200\"\n" +
            "        }\n" +
            "    }\n" +
            "}"
}