package com.stonks.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.stonks.R
import com.stonks.ui.cryptocurrency.CryptoFragment
import com.stonks.ui.currency.CurrencyFragment
import com.stonks.ui.stocks.StocksFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val bottomNavigationHeight: Int = 0

    // Variable storing currently selected fragment for displaying
    private var selectedFragment: Fragment = CurrencyFragment(bottomNavigationHeight)

    private lateinit var rlGuideFirstPage: RelativeLayout
    private lateinit var rlGuideSecondPage: RelativeLayout
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currencyID = intent.getIntExtra("currencyID", 0)
        val firstLaunch = intent.getBooleanExtra("firstLaunch", true)
        Log.i("INTENT_PARAMS_RECEIVED", "FIRST_LAUNCH: $firstLaunch, CURRENCY: $currencyID")

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
            }
        } else {
            rlGuideFirstPage.visibility = View.GONE
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
                R.id.cryptocurrency_tab -> CryptoFragment()
                R.id.currency_tab -> CurrencyFragment(bottom_navigation.height)
                R.id.stocks_tab -> StocksFragment()
                else -> TODO("Rewrite it without else statement")
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()

            true
        }
}
