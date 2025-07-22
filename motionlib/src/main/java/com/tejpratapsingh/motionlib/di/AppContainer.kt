package com.tejpratapsingh.motionlib.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

object AppContainer {

    lateinit var authority: String

    val httpClient by lazy {
        HttpClient(CIO) {}
    }
}