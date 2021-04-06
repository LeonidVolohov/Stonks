package com.stonks.api.cryptocurrency

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

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
                    "OVERVIEW" -> response.setBody(overviewResponse)
                    "TIME_SERIES_DAILY" -> response.setBody(dailyResponse)
                    "TIME_SERIES_INTRADAY" -> response.setBody(intradayResponse)
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
    fun getPricesFor1Week() {
    }

    @Test
    fun getMonthDynamics() {
    }

    @Test
    fun getPricesFor1Month() {
    }

    @Test
    fun getPricesFor6Months() {
    }

    @Test
    fun getPricesFor1Year() {
    }

    @Test
    fun getPricesFor5Years() {
    }

    @Test
    fun getPricesForCustomPeriod() {
    }

    @Test
    fun convertToCurrency() {
    }

    @Test
    fun getApiUrl() {
    }

    private val overviewResponse = ""
    private val intradayResponse = ""
    private val dailyResponse = ""
}