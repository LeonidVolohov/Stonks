package com.stonks.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.stonks.R
import com.stonks.ui.DefaultFragment
import com.stonks.ui.currency.CurrencyFragment
import com.stonks.ui.stocks.StocksFragment
import kotlinx.android.synthetic.main.activity_main.*
import com.stonks.ui.cryptocurrency.CryptoFragment

class MainActivity : AppCompatActivity() {
    private val bottomNavigationHeight: Int = 0

    // Variable storing currently selected fragment for displaying
    private var selectedFragment: Fragment = CurrencyFragment(bottomNavigationHeight)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener(navListener)
        bottomNavigation.selectedItemId = R.id.currency_tab

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, selectedFragment)
            .commit()
    }

    private val navListener : BottomNavigationView.OnNavigationItemSelectedListener =
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
