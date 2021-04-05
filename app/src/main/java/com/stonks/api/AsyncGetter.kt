package com.stonks.api

import android.os.AsyncTask
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request

object AsyncGetter : AsyncTask<String, Void, String>() {
    override fun doInBackground(vararg params: String?): String {
        val okHttpClient = OkHttpClient()
        if (params.isNotEmpty()) {
            val request = Request.Builder()
                .url(
                    params[0] ?: ""
                )
                .build()
            val response = okHttpClient.newCall(request).execute()
            return response.body?.string() ?: ""
        } else {
            Log.e(TAG, "No server URL passed. Nothing to fetch")
            return ""
        }
    }

    private val TAG = this::class.java.name
}