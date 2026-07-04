package com.tejpratapsingh.motionlib.coil.utils

import kotlinx.coroutines.runBlocking

fun <T> runBlockingSync(block: suspend () -> T): T = runBlocking {
    block()
}
