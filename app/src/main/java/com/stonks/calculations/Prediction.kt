package com.stonks.calculations

import org.nield.kotlinstatistics.SimpleRegression
import org.nield.kotlinstatistics.simpleRegression

class Prediction {
    data class AmountPerDate (val date: Long, val amount: Double)

    fun prediction (date_array: Array<Long>, amount_array: Array<Double>): SimpleRegression {

        val amountDates = mutableListOf<AmountPerDate>()
        for (i: Int in 0..(date_array.size-1)){
            amountDates.add(AmountPerDate(date_array[i], amount_array[i]))
        }

        return amountDates.simpleRegression(
                xSelector = { it.date },
                ySelector = { it.amount }
        )
    }

    /**
     * Использование в следующем формате:
     * (Prediction::datePrediction)(Prediction(),dateTest,amountTest,30)
     * Где
     * dateTest: Array<Long>
     * amountTest: Array<Double>
     * Возвращает значение типа double через <30> дней
     */
    fun datePrediction (date_array: Array<Long>, amount_array: Array<Double>, days: Int): Double {
        return prediction(date_array,amount_array).predict(date_array[date_array.size-1].toDouble()+days)
    }
}
