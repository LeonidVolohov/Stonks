package com.stonks.ui

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
import com.stonks.R
import com.stonks.ui.main.MainActivity
import java.util.*

class OnboardingActivity : AppCompatActivity() {

    private lateinit var btnSavePrefs: Button
    private lateinit var spinnerDefaultCurrency: Spinner
    private lateinit var groupLanguageSelection: RadioGroup

    private val sharedPrefs: SharedPreferences
        get() = getPreferences(Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        var (firstLaunch, language, defaultCurrencyInd) = retrievePrefs()

        if (firstLaunch) {
            setContentView(R.layout.activity_onboarding)
            btnSavePrefs = findViewById(R.id.save_preferences_btn)
            spinnerDefaultCurrency = findViewById(R.id.default_currency_selector_spinner)
            groupLanguageSelection = findViewById(R.id.language_selection_group)

            spinnerDefaultCurrency.setSelection(UiConstants.DEFAULT_CURRENCY_ID)
            spinnerDefaultCurrency.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        defaultCurrencyInd = position
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
                    putInt("defaultCurrencyInd", defaultCurrencyInd)
                    putString("language", language)
                    commit()
                }
                callMain(language, firstLaunch, defaultCurrencyInd)
            }
        } else {
            callMain(language, firstLaunch, defaultCurrencyInd)
        }
    }

    private fun callMain(language: String, firstLaunch: Boolean, defaultCurrencyInd: Int) {
        updateLocale(language)
        val intent = Intent(this, MainActivity::class.java)
        // To pass any data to next activity
        intent.putExtra("firstLaunch", firstLaunch)
        intent.putExtra("defaultCurrencyInd", defaultCurrencyInd)
        // start your next activity
        startActivity(intent)
    }

    private fun retrievePrefs(): Triple<Boolean, String, Int> {
        val firstLaunch = sharedPrefs.getBoolean("firstLaunch", true)
        val language = sharedPrefs.getString("language", "en").toString()
        val defaultCurrencyInd = sharedPrefs.getInt("defaultCurrencyInd", 0)
        return Triple(firstLaunch, language, defaultCurrencyInd)

    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
