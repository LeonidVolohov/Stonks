package com.stonks.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.stonks.R
import com.stonks.ui.currency.CurrencyPresenter

class MainActivity : AppCompatActivity() {

    private val currencyPresenter = CurrencyPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currencyPresenter.getAllCurrencyList()
    }

}
