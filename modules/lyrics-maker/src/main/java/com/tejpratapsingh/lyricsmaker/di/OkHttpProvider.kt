package com.tejpratapsingh.lyricsmaker.di

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object OkHttpProvider {
    val httpClient: OkHttpClient by lazy {
        OkHttpClient
            .Builder()
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request =
                    chain
                        .request()
                        .newBuilder()
                        .header("User-Agent", "LyricsMaker/1.0")
                        .header("Accept", "application/json")
                        .build()
                chain.proceed(request)
            }.connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
