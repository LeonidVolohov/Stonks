package com.stonks.ui.currency

import android.content.Context
import android.graphics.Canvas
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.stonks.R

class CustomMarkerView(context: Context?, layoutResource: Int) : MarkerView(context, layoutResource) {
    private var textViewContent : TextView = findViewById(R.id.textViewMarkerContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            textViewContent.text = e.y.toString()
        }

        super.refreshContent(e, highlight)
    }

    // TODO: override this method for correct displaying marker
    override fun draw(canvas: Canvas?, posX: Float, posY: Float) {
        super.draw(canvas, posX, posY)
    }
}