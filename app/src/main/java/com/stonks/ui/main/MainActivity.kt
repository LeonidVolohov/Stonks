package com.stonks.ui.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
    private lateinit var clSetupPrefs: ConstraintLayout
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var btnSavePrefs: Button
    private lateinit var spinnerDefaultCurrency: Spinner
    private lateinit var groupLanguageSelection: RadioGroup
//    private var textHelp: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
        val firstLaunch = sharedPrefs.getBoolean("first_launch", true)
        Log.i("MAIN", "SharedPrefsValue: $firstLaunch")

        rlGuideFirstPage = findViewById(R.id.guide_overlay_first_layout)
        rlGuideSecondPage = findViewById(R.id.guide_overlay_second_layout)
        clSetupPrefs = findViewById(R.id.preferences_setup_layout)
        fragmentContainer = findViewById(R.id.fragment_container)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        btnSavePrefs = findViewById(R.id.save_preferences_btn)
        spinnerDefaultCurrency = findViewById(R.id.default_currency_selector_spinner)
        groupLanguageSelection = findViewById(R.id.language_selection_group)

        if (firstLaunch) {
            var currency = ""
            var language = ""
            Log.i("MAIN", "Wrote false to SharedPrefs")
            Log.i(
                "MAIN",
                "Curr value in SharedPrefs: ${sharedPrefs.getBoolean("first_launch", true)}"
            )

            supportActionBar?.hide()
            bottomNavigation.visibility = View.GONE
            rlGuideFirstPage.visibility = View.GONE
            rlGuideSecondPage.visibility = View.GONE

            btnSavePrefs.setOnClickListener {
                currency = spinnerDefaultCurrency.selectedItem.toString()
                when (groupLanguageSelection.checkedRadioButtonId) {
                    R.id.english_language -> {
                        language = "eng"
                    }
                    R.id.russian_language -> {
                        language = "ru"
                    }
                }
                with(sharedPrefs.edit()) {
                    putBoolean("first_launch", false)
                    putString("currency", currency)
                    putString("language", language)
                    commit()
                }
                clSetupPrefs.visibility = View.GONE
                bottomNavigation.visibility = View.VISIBLE
                rlGuideFirstPage.visibility = View.VISIBLE
            }

            rlGuideFirstPage.setOnClickListener {
                bottomNavigation.selectedItemId = R.id.currency_tab
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
            rlGuideSecondPage.visibility = View.GONE
            clSetupPrefs.visibility = View.GONE
            bottomNavigation.selectedItemId = R.id.currency_tab
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commitNow()
        }

        bottomNavigation.setOnNavigationItemSelectedListener(navListener)
        if (firstLaunch) {
            bottomNavigation.menu.getItem(0).isEnabled = false
            bottomNavigation.menu.getItem(2).isEnabled = false
        }
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
