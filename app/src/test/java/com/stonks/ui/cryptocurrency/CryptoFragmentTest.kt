package com.stonks.ui.cryptocurrency

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.runner.AndroidJUnit4
import com.stonks.R
import com.stonks.api.cryptocurrency.CryptoCurrencyApiUtils
import com.stonks.api.cryptocurrency.CryptoCurrencyDataModel
import kotlinx.android.synthetic.main.fragment_currency.*
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.ZoneId
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
@Config(manifest = "AndroidManifest.xml")
class CryptoFragmentTest {

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
                    "TIME_SERIES_DAILY" -> response.setBody(dailyResponse)
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
    fun testingTest() {
        val args = Bundle()
        args.putString("url", requestBaseUrl)

        val scenario = FragmentScenario.launchInContainer(
            CryptoFragment::class.java,
            args,
            R.style.Theme_Stonks
        )
        scenario.onFragment { fragment ->
            try {
                fragment.processResult(
                    CryptoCurrencyDataModel.RatesProcessed(
                        mapOf(ZonedDateTime.now(ZoneId.systemDefault()) to 60.0).toSortedMap()
                    )
                )
                fragment.calculate_button.performClick()
            } catch (e: Throwable) {
                fragment.logError(e)
            }
        }
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
            "            \"1a. open (CNY)\": \"388324.38332700\",\n" +
            "            \"1b. open (USD)\": \"59129.99000000\",\n" +
            "            \"2a. high (CNY)\": \"390616.43670000\",\n" +
            "            \"2b. high (USD)\": \"59479.00000000\",\n" +
            "            \"3a. low (CNY)\": \"387756.18053100\",\n" +
            "            \"3b. low (USD)\": \"59043.47000000\",\n" +
            "            \"4a. close (CNY)\": \"389010.92886900\",\n" +
            "            \"4b. close (USD)\": \"59234.53000000\",\n" +
            "            \"5. volume\": \"2573.65850200\",\n" +
            "            \"6. market cap (USD)\": \"2573.65850200\"\n" +
            "        }\n" +
            "    }\n" +
            "}"
}