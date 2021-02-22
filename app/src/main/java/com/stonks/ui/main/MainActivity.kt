package com.stonks.ui.main

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.stonks.R
import com.stonks.ui.Constants
import com.stonks.ui.cryptocurrency.CryptoFragment
import com.stonks.ui.currency.CurrencyFragment
import com.stonks.ui.stocks.StocksFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val bottomNavigationHeight: Int = 0

    private lateinit var rlGuideFirstPage: RelativeLayout

    private lateinit var rlGuideSecondPage: RelativeLayout
    private lateinit var bottomNavigation: BottomNavigationView

    private var defaultCurrencyInd = Constants.DEFAULT_CURRENCY_ID

    // Variable storing currently selected fragment for displaying
    private lateinit var selectedFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        defaultCurrencyInd = intent.getIntExtra("defaultCurrencyInd", Constants.DEFAULT_CURRENCY_ID)
        selectedFragment = CurrencyFragment(bottomNavigationHeight, defaultCurrencyInd)

        val firstLaunch = intent.getBooleanExtra("firstLaunch", true)

        rlGuideSecondPage = findViewById(R.id.guide_overlay_second_layout)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        rlGuideFirstPage = findViewById(R.id.guide_overlay_first_layout)
        rlGuideSecondPage.visibility = View.GONE

        if (firstLaunch) {
            rlGuideFirstPage.setOnClickListener {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commitNow()
                rlGuideSecondPage.visibility = View.VISIBLE
                rlGuideFirstPage.visibility = View.GONE
            }
            rlGuideSecondPage.setOnClickListener {
                rlGuideSecondPage.visibility = View.GONE
                bottomNavigation.menu.getItem(0).isEnabled = true
                bottomNavigation.menu.getItem(2).isEnabled = true

                if (!isOnline(this)) {
                    Toast.makeText(this, "Please, check internet connection!", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            rlGuideFirstPage.visibility = View.GONE

            if (!isOnline(this)) {
                Toast.makeText(this, "Please, check internet connection!", Toast.LENGTH_LONG).show()
            }
        }

        bottomNavigation.selectedItemId = R.id.currency_tab
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commitNow()

        bottomNavigation.setOnNavigationItemSelectedListener(navListener)
        if (firstLaunch) {
            bottomNavigation.menu.getItem(0).isEnabled = false
            bottomNavigation.menu.getItem(2).isEnabled = false
        }
    }

    private val navListener: BottomNavigationView.OnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            selectedFragment = when (item.itemId) {
                R.id.cryptocurrency_tab -> CryptoFragment(defaultCurrencyInd)
                R.id.currency_tab -> CurrencyFragment(bottom_navigation.height, defaultCurrencyInd)
                R.id.stocks_tab -> StocksFragment(defaultCurrencyInd)
                else -> TODO("Rewrite it without else statement")
            }

            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()

            true
        }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
        return false
    }
}
