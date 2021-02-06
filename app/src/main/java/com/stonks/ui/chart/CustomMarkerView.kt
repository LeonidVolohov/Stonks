package com.stonks.ui.chart

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.stonks.ui.currency.CurrencyMain

class CustomMarkerView(context: Context?, layoutResource: Int, dataList: List<String>) : MarkerView(context, layoutResource) {
    private val TAG: String = CurrencyMain::class.java.name
    private val localDateList: List<String> = dataList
    private var toastMessage : String = ""
    private var toast : Toast = Toast.makeText(context, "", Toast.LENGTH_SHORT)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        try {
            toastMessage = "Date: ${e?.x?.toInt()?.let { localDateList[it] }} \nValue: ${e?.y}"
            toast = Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT)
            toast.show()
        } catch (exception : ArrayIndexOutOfBoundsException) {
            Log.e(TAG, exception.message.toString())
            toast.cancel()
        }

        super.refreshContent(e, highlight)
    }
}
