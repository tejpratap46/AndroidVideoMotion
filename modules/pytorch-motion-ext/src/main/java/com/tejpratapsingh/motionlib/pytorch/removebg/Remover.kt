package com.tejpratapsingh.motionlib.pytorch.removebg

/**
 * Created by erenalpaslan on 11.09.2023
 */
interface Remover<T> {
    fun clearBackground(image: T): T?

    fun getMaskedImage(
        input: T,
        mask: T,
    ): T
}
