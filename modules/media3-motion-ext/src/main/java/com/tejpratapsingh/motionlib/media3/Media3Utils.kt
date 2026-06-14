package com.tejpratapsingh.motionlib.media3

import android.graphics.ColorMatrix
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.RgbMatrix

@OptIn(UnstableApi::class)
object Media3Utils {
    /**
     * Converts a Media3 4x4 RGB matrix to an Android 5x4 ColorMatrix.
     * Media3 matrix is assumed to be a 16-float array representing a 4x4 matrix.
     * Android ColorMatrix is a 20-float array (4 rows, 5 columns).
     */
    fun toColorMatrix(media3Matrix: FloatArray): ColorMatrix {
        if (media3Matrix.size != 16) {
            return ColorMatrix()
        }

        // Media3 matrix (column-major standard for GL):
        // [ m0  m4  m8  m12 ]
        // [ m1  m5  m9  m13 ]
        // [ m2  m6  m10 m14 ]
        // [ m3  m7  m11 m15 ]
        
        // Android ColorMatrix (row-major):
        // [ a b c d e ]  -> R' = aR + bG + cB + dA + e
        // [ f g h i j ]  -> G' = fR + gG + hB + iA + j
        // [ k l m n o ]  -> B' = kR + lG + mB + nA + o
        // [ p q r s t ]  -> A' = pR + qG + rB + sA + t

        val colorMatrixArray = FloatArray(20)
        
        // Row 0 (Red)
        colorMatrixArray[0] = media3Matrix[0]  // m0
        colorMatrixArray[1] = media3Matrix[4]  // m4
        colorMatrixArray[2] = media3Matrix[8]  // m8
        colorMatrixArray[3] = 0f               // d (Alpha contribution to Red)
        colorMatrixArray[4] = media3Matrix[12] * 255f // e (Offset, normalized to 0-255)

        // Row 1 (Green)
        colorMatrixArray[5] = media3Matrix[1]  // m1
        colorMatrixArray[6] = media3Matrix[5]  // m5
        colorMatrixArray[7] = media3Matrix[9]  // m9
        colorMatrixArray[8] = 0f               // i
        colorMatrixArray[9] = media3Matrix[13] * 255f // j

        // Row 2 (Blue)
        colorMatrixArray[10] = media3Matrix[2] // m2
        colorMatrixArray[11] = media3Matrix[6] // m6
        colorMatrixArray[12] = media3Matrix[10]// m10
        colorMatrixArray[13] = 0f              // n
        colorMatrixArray[14] = media3Matrix[14] * 255f // o

        // Row 3 (Alpha) - Identity
        colorMatrixArray[15] = 0f
        colorMatrixArray[16] = 0f
        colorMatrixArray[17] = 0f
        colorMatrixArray[18] = 1f
        colorMatrixArray[19] = 0f

        return ColorMatrix(colorMatrixArray)
    }
}
