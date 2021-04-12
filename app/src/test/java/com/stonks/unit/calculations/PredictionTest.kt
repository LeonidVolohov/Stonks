package com.stonks.unit.calculations

import com.stonks.calculations.Prediction
import org.junit.Assert.assertEquals
import org.junit.Test

class PredictionTest {

    private val module = Prediction()

    @Test
    fun prediction_test() {
    }

    @Test
    fun datePrediction_testAscending() {
        val prediction = module.datePrediction(
            arrayOf(1.toLong(), 2.toLong(), 3.toLong()),
            arrayOf(1.toDouble(), 2.toDouble(), 3.toDouble()),
            1
        )
        assertEquals(prediction, 4.0, 0.000001)
    }

    @Test
    fun datePrediction_testConstant() {
        val prediction = module.datePrediction(
            arrayOf(1.toLong(), 2.toLong(), 3.toLong()),
            arrayOf(1.toDouble(), 1.toDouble(), 1.toDouble()),
            1
        )
        assertEquals(prediction, 1.0, 0.000001)
    }
}