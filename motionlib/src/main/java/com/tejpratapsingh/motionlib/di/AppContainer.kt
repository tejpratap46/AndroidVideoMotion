package com.tejpratapsingh.motionlib.di

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.logging.*

object AppContainer {

    lateinit var authority: String

    val httpClient by lazy {
        HttpClient(CIO) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.HEADERS
            }
        }
    }
}