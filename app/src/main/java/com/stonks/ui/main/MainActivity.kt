package com.stonks.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.stonks.R
import com.stonks.ui.DefaultFragment
import com.stonks.ui.cryptocurrency.CryptoFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener(navListener)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, selectedFragment)
            .commit()
    }

    private val navListener : BottomNavigationView.OnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            selectedFragment = when (item.itemId) {
                R.id.cryptocurrency_tab -> CryptoFragment()
                R.id.currency_tab -> DefaultFragment()          // TODO: Replace with actual fragment
                R.id.stocks_tab -> DefaultFragment()            // TODO: Replace with actual fragment
                else -> TODO("Not implemented yet")
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()

            true
        }

    // Variable storing currently selected fragment for displaying
    private var selectedFragment : Fragment = DefaultFragment()  // TODO: place fragment corresponding to most left tab (now crypto)
}
