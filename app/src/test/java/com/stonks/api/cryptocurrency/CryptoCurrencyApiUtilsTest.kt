package com.stonks.api.cryptocurrency

import androidx.test.runner.AndroidJUnit4
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

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
    fun getPricesFor1Week_test() {
    }

    @Test
    fun getMonthDynamics_test() {
    }

    @Test
    fun getPricesFor1Month_test() {
    }

    @Test
    fun getPricesFor6Months_test() {
    }

    @Test
    fun getPricesFor1Year_test() {
    }

    @Test
    fun getPricesFor5Years_test() {
    }

    @Test
    fun getPricesForCustomPeriod_test() {
    }

    @Test
    fun convertToCurrency_test() {
    }

    @Test
    fun getApiUrl_test() {
    }

    private val overviewResponse = ""
    private val intradayResponse = ""
    private val dailyResponse = ""
}