package com.tejpratapsingh.motionlib.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

fun Bitmap.compressToBitmap(quality: Int): Bitmap {
// Initialize a new ByteArrayStream
    val stream = ByteArrayOutputStream()

    /*
        **** reference source developer.android.com ***

        public boolean compress (Bitmap.CompressFormat format, int quality, OutputStream stream)
            Write a compressed version of the bitmap to the specified outputstream.
            If this returns true, the bitmap can be reconstructed by passing a
            corresponding inputstream to BitmapFactory.decodeStream().

            Note: not all Formats support all bitmap configs directly, so it is possible
            that the returned bitmap from BitmapFactory could be in a different bitdepth,
            and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque pixels).

            Parameters
            format : The format of the compressed image
            quality : Hint to the compressor, 0-100. 0 meaning compress for small size,
                100 meaning compress for max quality. Some formats,
                like PNG which is lossless, will ignore the quality setting
            stream: The outputstream to write the compressed data.

            Returns
                true if successfully compressed to the specified stream.


        Bitmap.CompressFormat
            Specifies the known formats a bitmap can be compressed into.

                Bitmap.CompressFormat  JPEG
                Bitmap.CompressFormat  PNG
                Bitmap.CompressFormat  WEBP
    */

    // Compress the bitmap with JPEG format and quality 50%
    this.compress(Bitmap.CompressFormat.JPEG, quality, stream)

    val byteArray = stream.toByteArray()

    // Finally, return the compressed bitmap
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}