package com.stonks.integration

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.runner.AndroidJUnit4
import com.stonks.R
import com.stonks.api.stocks.StocksApiDataUtils
import com.stonks.ui.stocks.StocksFragment
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

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class StocksScenarios {

    val server = MockWebServer()
    private lateinit var requestBaseUrl: String
    private lateinit var api: StocksApiDataUtils

    @Before
    fun setUp() {
        server.start(8080)
        requestBaseUrl = server.url("/").toString()
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
        api = StocksApiDataUtils("noStock", requestBaseUrl)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun userCanGetStockPrice() {
        val args = Bundle()
        args.putString("url", requestBaseUrl)

        val scenario = FragmentScenario.launchInContainer(
            StocksFragment::class.java,
            args,
            R.style.Theme_Stonks
        )
        scenario.onFragment { fragment ->
            fragment.updateStockData(false)
            fragment.toggleGroupPeriod.check(R.id.togglebutton_one_day_selector)
        }
        Assert.assertEquals("USD", testCurrencyName)
        Assert.assertEquals("59234.53000000", testCurrencyPrice)
    }

    @Test
    fun userCanConvertStocks() {
        val args = Bundle()
        args.putString("url", requestBaseUrl)

        val scenario = FragmentScenario.launchInContainer(
            StocksFragment::class.java,
            args,
            R.style.Theme_Stonks
        )
        scenario.onFragment { fragment ->
            fragment.updateStockData(false)
            fragment.toggleGroupPeriod.check(R.id.togglebutton_one_day_selector)
        }
        Assert.assertEquals("USD", testCurrencyName)
        Assert.assertEquals("59234.53000000", testCurrencyPrice)
    }

    @Test
    fun userCanPlotStockPrice() {
        val args = Bundle()
        args.putString("url", requestBaseUrl)

        val scenario = FragmentScenario.launchInContainer(
            StocksFragment::class.java,
            args,
            R.style.Theme_Stonks
        )
        scenario.onFragment { fragment ->
            fragment.updateStockData(false)
            fragment.toggleGroupPeriod.check(R.id.togglebutton_one_day_selector)
        }
        Assert.assertEquals("USD", testCurrencyName)
        Assert.assertEquals("59234.53000000", testCurrencyPrice)
    }

    @Test
    fun userCanPredictStockPrice() {
        val args = Bundle()
        args.putString("url", requestBaseUrl)

        val scenario = FragmentScenario.launchInContainer(
            StocksFragment::class.java,
            args,
            R.style.Theme_Stonks
        )
        scenario.onFragment { fragment ->
            fragment.updateStockData(false)
            fragment.toggleGroupPeriod.check(R.id.togglebutton_one_day_selector)
        }
        Assert.assertEquals("USD", testCurrencyName)
        Assert.assertEquals("59234.53000000", testCurrencyPrice)
    }

    private val testCurrencyName = "USD"
    private val testCurrencyPrice = "59234.53000000"

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