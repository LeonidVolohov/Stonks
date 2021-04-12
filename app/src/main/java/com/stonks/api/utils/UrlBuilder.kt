package com.stonks.api.utils

import android.net.Uri

/**
 * Help class for constructing request URLs
 */
object UrlBuilder {
    /**
     * Utility function that builds URL based on [baseUrl]
     *
     * @param baseUrl The URL used for construction as String (e.g. http://example.com)
     * @param endpoint (Optional) endpoint as a String.
     * If provided, specified endpoint is appended to [baseUrl]
     * @param params (Optional) HashMap of parameters.
     * If provided, specified parameters are added to resulting URL
     *
     * @return Resulting constructed URL as a String
     */
    fun build(baseUrl: String, endpoint: String? = null, params: Map<String, Any>? = null): String {
        val builder = Uri.parse(baseUrl)
            .buildUpon()
        if (endpoint != null) {
            builder.appendPath(endpoint)
        }
        if (params != null) {
            for ((param, value) in params.entries) {
                builder.appendQueryParameter(param, value.toString())
            }
        }
        return builder.build().toString()
    }
}