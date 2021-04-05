package com.stonks.api.stocks

import androidx.test.runner.AndroidJUnit4
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class StocksApiDataUtilsTest {

    val marketDataLocation =
        "/home/egor/dev/android/Stonks/app/src/test/java/com/stonks/testdata/marketDataStocksTestResponse.json"
    val intradayDataLocation =
        "/home/egor/dev/android/Stonks/app/src/test/java/com/stonks/testdata/intradayStocksTestResponse.json"
    val dailyDataLocation =
        "/home/egor/dev/android/Stonks/app/src/test/java/com/stonks/testdata/dailyStocksTestResponse.json"

    val server = MockWebServer()
    private lateinit var requestUrl: String
    private lateinit var api: StocksApiDataUtils

    @Before
    fun setUp() {
        server.start(8080)
        requestUrl = server.url("/").toString()
        api = StocksApiDataUtils("noStock", requestUrl)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getMarket_test() {
//        val testResponse = openFile(marketDataLocation)
        val testResponse = "{\n" +
                "  \"Symbol\": \"IBM\",\n" +
                "  \"Exchange\": \"NYSE\",\n" +
                "  \"Currency\": \"USD\"\n" +
                "}"
        println(testResponse)
        server.enqueue(MockResponse().setBody(testResponse))
        val result = api.getMarket()
        Assert.assertEquals(result.market, "NYSE")
        Assert.assertEquals(result.currency, "USD")
    }

    @Test
    fun getLatestRate_test() {
        val testResponse = "{\n" +
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
        println(testResponse)
        server.enqueue(MockResponse().setBody(testResponse))
        val result = api.getLatestRate()
        println(result)
        Assert.assertEquals(result, 133.2300, 0.00001)
    }

    @Test
    fun getMonthDynamics_test() {
        val testResponse = "{\n" +
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
        println(testResponse)
        server.enqueue(MockResponse().setBody(testResponse))
        val result = api.getMonthDynamics()
        println(result)
        Assert.assertEquals(result, 0.0, 0.00001)
    }

    @Test
    fun getPricesFor1Day() {
        val testResponse = "{\n" +
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
        println(testResponse)
        server.enqueue(MockResponse().setBody(testResponse))
        val result = api.getPricesFor1Day()
        println(result)
        Assert.assertEquals(result.rates.size, 0)
    }

    @Test
    fun getPricesFor1Week() {
        val testResponse = "{\n" +
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
        println(testResponse)
        server.enqueue(MockResponse().setBody(testResponse))
        val result = api.getPricesFor1Week()
        println(result)
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getPricesFor1Month() {
        val testResponse = "{\n" +
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
        println(testResponse)
        server.enqueue(MockResponse().setBody(testResponse))
        val result = api.getPricesFor1Month()
        println(result)
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getPricesFor6Months() {
        val testResponse = "{\n" +
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
        println(testResponse)
        server.enqueue(MockResponse().setBody(testResponse))
        val result = api.getPricesFor6Months()
        println(result)
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getPricesFor1Year() {
        val testResponse = "{\n" +
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
        println(testResponse)
        server.enqueue(MockResponse().setBody(testResponse))
        val result = api.getPricesFor1Year()
        println(result)
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getPricesFor5Years() {
        val testResponse = "{\n" +
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
        println(testResponse)
        server.enqueue(MockResponse().setBody(testResponse))
        val result = api.getPricesFor5Years()
        println(result)
        Assert.assertEquals(result.rates.size, 1)
    }

    @Test
    fun getPricesForCustomPeriod() {
    }

    @Test
    fun getDailyPrices() {
    }

    @Test
    fun convertToCurrency() {
    }

    @Test
    fun getStock() {
    }

    @Test
    fun getApiUrl() {
    }

    private fun openFile(filename: String): String {
        val fileInputStream = javaClass.classLoader!!.getResourceAsStream(filename)
        return fileInputStream?.bufferedReader()?.readText() ?: ""
    }
}