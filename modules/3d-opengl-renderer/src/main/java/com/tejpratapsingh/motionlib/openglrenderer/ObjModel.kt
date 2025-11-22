package com.tejpratapsingh.motionlib.openglrenderer

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class ObjModel(context: Context, filename: String) {
    val vertexBuffer: FloatBuffer
    val indexBuffer: ShortBuffer
    val indexCount: Int

    init {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        val reader = BufferedReader(InputStreamReader(context.assets.open(filename)))
        val tempVertices = mutableListOf<FloatArray>()

        reader.forEachLine { line ->
            when {
                line.startsWith("v ") -> {
                    val parts = line.split(" ")
                    val x = parts[1].toFloat()
                    val y = parts[2].toFloat()
                    val z = parts[3].toFloat()
                    tempVertices.add(floatArrayOf(x, y, z))
                }

                line.startsWith("f ") -> {
                    val parts = line.split(" ")
                    for (i in 1..3) {
                        val indexStr = parts[i].split("/")[0]
                        val idx = indexStr.toInt()
                        indices.add((idx - 1).toShort())  // OBJ is 1-based
                    }
                }
            }
        }

        tempVertices.forEach {
            vertices.addAll(it.toList())
        }

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices.toFloatArray())
                position(0)
            }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(indices.toShortArray())
                position(0)
            }

        indexCount = indices.size
    }
}