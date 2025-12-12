package com.tejpratapsingh.motionlib.openglrenderer

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.Matrix
import androidx.core.graphics.createBitmap
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import kotlin.math.sqrt

class Object3DToBitmapRenderer(
    private val context: Context,
    private val assetFileName: String,
    private val width: Int,
    private val height: Int,
    private val objectColor: FloatArray = floatArrayOf(0.8f, 0.8f, 0.8f, 1.0f),
) {
    private var egl: EGL10? = null
    private var eglDisplay: EGLDisplay? = null
    private var eglContext: EGLContext? = null
    private var eglSurface: EGLSurface? = null

    private var shaderProgram: Int = 0
    private var positionHandle: Int = 0
    private var normalHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var lightPosHandle: Int = 0
    private var colorHandle: Int = 0

    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private var mesh: Mesh? = null
    private var isInitialized = false

    data class Mesh(
        val vertices: FloatBuffer,
        val normals: FloatBuffer,
        val indices: ShortBuffer,
        val indexCount: Int,
    )

    private val vertexShaderCode =
        """
        attribute vec4 vPosition;
        attribute vec3 vNormal;
        uniform mat4 uMVPMatrix;
        uniform vec3 uLightPos;
        varying float vLightIntensity;
        
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            
            // Simple lighting calculation
            vec3 modelViewVertex = vec3(uMVPMatrix * vPosition);
            vec3 modelViewNormal = vec3(uMVPMatrix * vec4(vNormal, 0.0));
            float distance = length(uLightPos - modelViewVertex);
            vec3 lightVector = normalize(uLightPos - modelViewVertex);
            float diffuse = max(dot(modelViewNormal, lightVector), 0.1);
            diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));
            vLightIntensity = diffuse;
        }
        """.trimIndent()

    private val fragmentShaderCode =
        """
        precision mediump float;
        uniform vec4 uColor;
        varying float vLightIntensity;
        
        void main() {
            gl_FragColor = vec4(uColor.rgb * vLightIntensity, uColor.a);
        }
        """.trimIndent()

    /**
     * Initialize the renderer - call this once before generating bitmaps
     * @return true if initialization successful, false otherwise
     */
    fun initialize(): Boolean =
        try {
            setupOffscreenRendering(width, height)
            mesh = loadObjFromAssets(assetFileName)
            setupShaders()
            setupMatrices(width, height)
            isInitialized = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
            false
        }

    /**
     * Set the rotation angles for the 3D model
     * @param rotationX Rotation around X-axis in degrees
     * @param rotationY Rotation around Y-axis in degrees
     * @param rotationZ Rotation around Z-axis in degrees
     */
    fun setRotation(
        rotationX: Float = 0f,
        rotationY: Float = 0f,
        rotationZ: Float = 0f,
    ) {
        if (!isInitialized) {
            throw IllegalStateException("Renderer not initialized. Call initialize() first.")
        }

        // Set up model matrix with rotations
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, rotationX, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, rotationY, 0f, 1f, 0f)
        Matrix.rotateM(modelMatrix, 0, rotationZ, 0f, 0f, 1f)
    }

    /**
     * Generate bitmap with current rotation settings
     * @return Bitmap representation of the 3D object with current rotation
     */
    fun generateBitmap(): Bitmap? {
        if (!isInitialized || mesh == null) {
            throw IllegalStateException("Renderer not initialized. Call initialize() first.")
        }

        return try {
            renderToBitmap(mesh!!, width, height, objectColor)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convenience method to set rotation and generate bitmap in one call
     * @param rotationX Rotation around X-axis in degrees
     * @param rotationY Rotation around Y-axis in degrees
     * @param rotationZ Rotation around Z-axis in degrees
     * @return Bitmap representation of the rotated 3D object
     */
    fun generateBitmapWithRotation(
        rotationX: Float = 0f,
        rotationY: Float = 0f,
        rotationZ: Float = 0f,
    ): Bitmap? {
        setRotation(rotationX, rotationY, rotationZ)
        return generateBitmap()
    }

    /**
     * Clean up resources - call this when done with the renderer
     */
    fun dispose() {
        cleanup()
        isInitialized = false
        mesh = null
    }

    private fun setupOffscreenRendering(
        width: Int,
        height: Int,
    ) {
        egl = EGL10.EGL_NO_CONTEXT as EGL10
        eglDisplay = egl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

        val version = IntArray(2)
        egl!!.eglInitialize(eglDisplay, version)

        val configAttribs =
            intArrayOf(
                EGL10.EGL_RENDERABLE_TYPE,
                4, // EGL_OPENGL_ES2_BIT
                EGL10.EGL_RED_SIZE,
                8,
                EGL10.EGL_GREEN_SIZE,
                8,
                EGL10.EGL_BLUE_SIZE,
                8,
                EGL10.EGL_ALPHA_SIZE,
                8,
                EGL10.EGL_DEPTH_SIZE,
                16,
                EGL10.EGL_NONE,
            )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        egl!!.eglChooseConfig(eglDisplay, configAttribs, configs, 1, numConfigs)

        val contextAttribs =
            intArrayOf(
                0x3098,
                2, // EGL_CONTEXT_CLIENT_VERSION
                EGL10.EGL_NONE,
            )

        eglContext =
            egl!!.eglCreateContext(
                eglDisplay,
                configs[0],
                EGL10.EGL_NO_CONTEXT,
                contextAttribs,
            )

        val surfaceAttribs =
            intArrayOf(
                EGL10.EGL_WIDTH,
                width,
                EGL10.EGL_HEIGHT,
                height,
                EGL10.EGL_NONE,
            )

        eglSurface = egl!!.eglCreatePbufferSurface(eglDisplay, configs[0], surfaceAttribs)
        egl!!.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

        GLES20.glViewport(0, 0, width, height)
    }

    private fun loadObjFromAssets(fileName: String): Mesh {
        val vertices = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
        val tempVertices = mutableListOf<FloatArray>()
        val tempNormals = mutableListOf<FloatArray>()
        val indices = mutableListOf<Short>()

        context.assets.open(fileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.lineSequence().forEach { line ->
                    val parts = line.trim().split("\\s+".toRegex())
                    when (parts[0]) {
                        "v" -> {
                            // Vertex position
                            tempVertices.add(
                                floatArrayOf(
                                    parts[1].toFloat(),
                                    parts[2].toFloat(),
                                    parts[3].toFloat(),
                                ),
                            )
                        }

                        "vn" -> {
                            // Vertex normal
                            tempNormals.add(
                                floatArrayOf(
                                    parts[1].toFloat(),
                                    parts[2].toFloat(),
                                    parts[3].toFloat(),
                                ),
                            )
                        }

                        "f" -> {
                            // Face (assuming triangulated faces)
                            for (i in 1..3) {
                                val vertexData = parts[i].split("/")
                                val vertexIndex = vertexData[0].toInt() - 1
                                val normalIndex =
                                    if (vertexData.size > 2 && vertexData[2].isNotEmpty()) {
                                        vertexData[2].toInt() - 1
                                    } else {
                                        vertexIndex
                                    }

                                // Add vertex
                                vertices.addAll(tempVertices[vertexIndex].toList())

                                // Add normal (use vertex normal if available, otherwise calculate)
                                if (normalIndex < tempNormals.size) {
                                    normals.addAll(tempNormals[normalIndex].toList())
                                } else {
                                    normals.addAll(listOf(0f, 0f, 1f)) // Default normal
                                }

                                indices.add((vertices.size / 3 - 1).toShort())
                            }
                        }
                    }
                }
            }
        }

        // If no normals were provided, calculate them
        if (tempNormals.isEmpty()) {
            calculateNormals(vertices.toFloatArray(), indices.toShortArray())
        }

        return Mesh(
            vertices = createFloatBuffer(vertices.toFloatArray()),
            normals = createFloatBuffer(normals.toFloatArray()),
            indices = createShortBuffer(indices.toShortArray()),
            indexCount = indices.size,
        )
    }

    private fun calculateNormals(
        vertices: FloatArray,
        indices: ShortArray,
    ): FloatArray {
        val normals = FloatArray(vertices.size)

        // Calculate face normals and accumulate vertex normals
        for (i in indices.indices step 3) {
            val i1 = indices[i] * 3
            val i2 = indices[i + 1] * 3
            val i3 = indices[i + 2] * 3

            val v1 = floatArrayOf(vertices[i1], vertices[i1 + 1], vertices[i1 + 2])
            val v2 = floatArrayOf(vertices[i2], vertices[i2 + 1], vertices[i2 + 2])
            val v3 = floatArrayOf(vertices[i3], vertices[i3 + 1], vertices[i3 + 2])

            val edge1 = floatArrayOf(v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2])
            val edge2 = floatArrayOf(v3[0] - v1[0], v3[1] - v1[1], v3[2] - v1[2])

            val normal =
                floatArrayOf(
                    edge1[1] * edge2[2] - edge1[2] * edge2[1],
                    edge1[2] * edge2[0] - edge1[0] * edge2[2],
                    edge1[0] * edge2[1] - edge1[1] * edge2[0],
                )

            // Normalize
            val length = sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2])
            if (length > 0) {
                normal[0] /= length
                normal[1] /= length
                normal[2] /= length
            }

            // Add to vertex normals
            for (j in 0..2) {
                val vertexIndex = indices[i + j] * 3
                normals[vertexIndex] += normal[0]
                normals[vertexIndex + 1] += normal[1]
                normals[vertexIndex + 2] += normal[2]
            }
        }

        // Normalize all vertex normals
        for (i in normals.indices step 3) {
            val length =
                sqrt(normals[i] * normals[i] + normals[i + 1] * normals[i + 1] + normals[i + 2] * normals[i + 2])
            if (length > 0) {
                normals[i] /= length
                normals[i + 1] /= length
                normals[i + 2] /= length
            }
        }

        return normals
    }

    private fun createFloatBuffer(array: FloatArray): FloatBuffer =
        ByteBuffer
            .allocateDirect(array.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(array)
            .apply { position(0) }

    private fun createShortBuffer(array: ShortArray): ShortBuffer =
        ByteBuffer
            .allocateDirect(array.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(array)
            .apply { position(0) }

    private fun setupShaders() {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)

        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        normalHandle = GLES20.glGetAttribLocation(shaderProgram, "vNormal")
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        lightPosHandle = GLES20.glGetUniformLocation(shaderProgram, "uLightPos")
        colorHandle = GLES20.glGetUniformLocation(shaderProgram, "uColor")
    }

    private fun loadShader(
        type: Int,
        shaderCode: String,
    ): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    private fun setupMatrices(
        width: Int,
        height: Int,
    ) {
        // Set up projection matrix
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)

        // Set up view matrix (camera position)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1f, 0f)

        // Model matrix will be set in generateBitmap() with rotations
        Matrix.setIdentityM(modelMatrix, 0)
    }

    private fun renderToBitmap(
        mesh: Mesh,
        width: Int,
        height: Int,
        color: FloatArray,
    ): Bitmap {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f) // White background
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        GLES20.glUseProgram(shaderProgram)

        // Calculate MVP matrix
        val tempMatrix = FloatArray(16)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

        // Set uniforms
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform3f(lightPosHandle, 0f, 0f, -1f) // Light position
        GLES20.glUniform4f(colorHandle, color[0], color[1], color[2], color[3])

        // Set vertex attributes
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mesh.vertices)

        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, mesh.normals)

        // Draw
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            mesh.indexCount,
            GLES20.GL_UNSIGNED_SHORT,
            mesh.indices,
        )

        // Read pixels
        val pixelBuffer = ByteBuffer.allocateDirect(width * height * 4)
        GLES20.glReadPixels(
            0,
            0,
            width,
            height,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            pixelBuffer,
        )

        // Convert to bitmap
        val bitmap = createBitmap(width, height)
        pixelBuffer.rewind()
        bitmap.copyPixelsFromBuffer(pixelBuffer)

        // Flip bitmap vertically (OpenGL coordinates are inverted)
        val matrix = android.graphics.Matrix()
        matrix.postScale(1f, -1f, width / 2f, height / 2f)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false)
    }

    private fun cleanup() {
        egl?.let { egl ->
            if (eglDisplay != null) {
                egl.eglMakeCurrent(
                    eglDisplay,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_CONTEXT,
                )
                eglSurface?.let { egl.eglDestroySurface(eglDisplay, it) }
                eglContext?.let { egl.eglDestroyContext(eglDisplay, it) }
                egl.eglTerminate(eglDisplay)
            }
        }

        if (shaderProgram != 0) {
            GLES20.glDeleteProgram(shaderProgram)
        }

        egl = null
        eglDisplay = null
        eglContext = null
        eglSurface = null
        shaderProgram = 0
    }
}
