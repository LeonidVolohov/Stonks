package com.stonks.api.utils

import android.net.Uri

object UrlBuilder {
    fun build(baseUrl: String, params: HashMap<String, Any>): String {
        val builder = Uri.parse(baseUrl)
            .buildUpon()
        for ((param, value) in params.entries) {
            builder.appendQueryParameter(param, value.toString())
        }
        return builder.build().toString()
    }
}