package com.stonks

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.stonks.ui.main.MainActivity
import java.util.*

class FirstLaunchActivity : AppCompatActivity() {

    private lateinit var btnSavePrefs: Button
    private lateinit var spinnerDefaultCurrency: Spinner
    private lateinit var groupLanguageSelection: RadioGroup

    private val sharedPrefs: SharedPreferences
        get() = getPreferences(Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        var (firstLaunch, language, currencyID) = retrievePrefs()

        if (firstLaunch) {
            setContentView(R.layout.activity_first_launch)
            btnSavePrefs = findViewById(R.id.save_preferences_btn)
            spinnerDefaultCurrency = findViewById(R.id.default_currency_selector_spinner)
            groupLanguageSelection = findViewById(R.id.language_selection_group)

            spinnerDefaultCurrency.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        currencyID = position
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

            btnSavePrefs.setOnClickListener {
                when (groupLanguageSelection.checkedRadioButtonId) {
                    R.id.english_language -> {
                        language = "en"
                    }
                    R.id.russian_language -> {
                        language = "ru"
                    }
                }
                with(sharedPrefs.edit()) {
                    putBoolean("firstLaunch", false)
                    putInt("currencyID", currencyID)
                    putString("language", language)
                    commit()
                }
                callMain(language, firstLaunch, currencyID)
            }
        } else {
            callMain(language, firstLaunch, currencyID)
        }
    }

    private fun callMain(language: String, firstLaunch: Boolean, currencyID: Int) {
        updateLocale(language)
        val intent = Intent(this, MainActivity::class.java)
        // To pass any data to next activity
        intent.putExtra("firstLaunch", firstLaunch)
        intent.putExtra("currencyID", currencyID)
        // start your next activity
        startActivity(intent)
    }

    private fun retrievePrefs(): Triple<Boolean, String, Int> {
        val firstLaunch = sharedPrefs.getBoolean("firstLaunch", true)
        val language = sharedPrefs.getString("language", "en").toString()
        val currencyID = sharedPrefs.getInt("currencyID", 0)
        return Triple(firstLaunch, language, currencyID)

    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
