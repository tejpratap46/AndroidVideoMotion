package com.tejpratapsingh.motionlib.filamentrenderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.view.Surface
import androidx.core.graphics.createBitmap
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.Texture
import com.google.android.filament.View
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin

class FilamentOffscreenCapturer(private val context: Context) {
    private lateinit var engine: Engine
    private var renderer: Renderer? = null
    private var scene: Scene? = null
    private var view: View? = null
    private var swapChain: SwapChain? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private var camera: Camera? = null

    private var assetLoader: AssetLoader? = null
    private var resourceLoader: ResourceLoader? = null
    private var asset: FilamentAsset? = null

    enum class RotationAxis {
        X, Y, Z
    }

    fun init(width: Int, height: Int) {
        engine = Engine.create()
        renderer = engine.createRenderer()

        surfaceTexture = SurfaceTexture(0)
        surfaceTexture?.setDefaultBufferSize(width, height)
        surface = Surface(surfaceTexture)
        swapChain = engine.createSwapChain(surface as Any)

        view = engine.createView()
        scene = engine.createScene()
        view?.scene = scene

        // Create an entity for the camera
        val cameraEntity = EntityManager.get().create()
        camera = engine.createCamera(cameraEntity)
        view?.camera = camera

        camera?.setProjection(
            45.0,
            width.toDouble() / height.toDouble(),
            0.1,
            100.0,
            Camera.Fov.VERTICAL
        )
        camera?.lookAt(
            0.0, 0.0, 3.0, // eye
            0.0, 0.0, 0.0, // center
            0.0, 1.0, 0.0  // up
        )

        val materialProvider = UbershaderProvider(engine)
        assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())
        resourceLoader = ResourceLoader(engine)
    }

    fun loadModelFromAssets(assetFileName: String) {
        val assetManager = context.assets
        val inputStream = assetManager.open(assetFileName)
        val bytes = inputStream.readBytes()
        val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
        buffer.put(bytes)
        buffer.flip()
        inputStream.close()

        asset = assetLoader?.createAsset(buffer)
        if (asset == null) throw RuntimeException("Unable to load model: $assetFileName")

        resourceLoader?.loadResources(asset!!)
        asset?.releaseSourceData()

        scene?.addEntities(asset!!.entities)
    }

    fun loadModelFromFile(filePath: String) {
        val file = File(filePath)
        val buffer = readFileToBuffer(file)
        asset = assetLoader?.createAsset(buffer)
        if (asset == null) throw RuntimeException("Unable to load model: $filePath")

        resourceLoader?.loadResources(asset!!)
        asset?.releaseSourceData()

        scene?.addEntities(asset!!.entities)
    }

    fun setRotation(axis: RotationAxis, degrees: Float) {
        asset?.let {
            val radians = Math.toRadians(degrees.toDouble())
            val cos = cos(radians).toFloat()
            val sin = sin(radians).toFloat()

            val transform = FloatArray(16)
            when (axis) {
                RotationAxis.X -> {
                    // Rotate around X-axis
                    transform[0] = 1f; transform[4] = 0f; transform[8] = 0f; transform[12] = 0f
                    transform[1] = 0f; transform[5] = cos; transform[9] = -sin; transform[13] = 0f
                    transform[2] = 0f; transform[6] = sin; transform[10] = cos; transform[14] = 0f
                    transform[3] = 0f; transform[7] = 0f; transform[11] = 0f; transform[15] = 1f
                }

                RotationAxis.Y -> {
                    // Rotate around Y-axis
                    transform[0] = cos; transform[4] = 0f; transform[8] = sin; transform[12] = 0f
                    transform[1] = 0f; transform[5] = 1f; transform[9] = 0f; transform[13] = 0f
                    transform[2] = -sin; transform[6] = 0f; transform[10] = cos; transform[14] = 0f
                    transform[3] = 0f; transform[7] = 0f; transform[11] = 0f; transform[15] = 1f
                }

                RotationAxis.Z -> {
                    // Rotate around Z-axis
                    transform[0] = cos; transform[4] = -sin; transform[8] = 0f; transform[12] = 0f
                    transform[1] = sin; transform[5] = cos; transform[9] = 0f; transform[13] = 0f
                    transform[2] = 0f; transform[6] = 0f; transform[10] = 1f; transform[14] = 0f
                    transform[3] = 0f; transform[7] = 0f; transform[11] = 0f; transform[15] = 1f
                }
            }

            val transformManager = engine.transformManager
            val instance = transformManager.getInstance(it.root)
            if (instance != 0) {
                transformManager.setTransform(instance, transform)
            }
        }
    }

    fun rotateModelX(degrees: Float) {
        setRotation(RotationAxis.X, degrees)
    }

    fun rotateModelY(degrees: Float) {
        setRotation(RotationAxis.Y, degrees)
    }

    fun rotateModelZ(degrees: Float) {
        setRotation(RotationAxis.Z, degrees)
    }

    fun capture(width: Int, height: Int): Bitmap? {
        try {
            if (renderer?.beginFrame(swapChain!!, 0L) == true) {
                renderer?.render(view!!)
                renderer?.endFrame()
            }

            val pixelCount = width * height
            val buf = ByteBuffer.allocateDirect(pixelCount * 4).order(ByteOrder.nativeOrder())
            val descriptor = Texture.PixelBufferDescriptor(
                buf,
                Texture.Format.RGBA,
                Texture.Type.UBYTE
            )
            renderer?.readPixels(0, 0, width, height, descriptor)

            buf.rewind()
            val pixels = IntArray(pixelCount)
            var pi = 0
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val r = buf.get().toInt() and 0xFF
                    val g = buf.get().toInt() and 0xFF
                    val b = buf.get().toInt() and 0xFF
                    val a = buf.get().toInt() and 0xFF
                    pixels[pi++] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
            }

            val flippedPixels = IntArray(pixelCount)
            for (row in 0 until height) {
                val srcRow = height - 1 - row
                System.arraycopy(pixels, srcRow * width, flippedPixels, row * width, width)
            }

            val bitmap = createBitmap(width, height)
            bitmap.setPixels(flippedPixels, 0, width, 0, 0, width, height)

            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun destroy() {
        try {
            swapChain?.let { engine.destroySwapChain(it) }
        } catch (_: Throwable) {
        }
        try {
            surface?.release()
        } catch (_: Throwable) {
        }
        try {
            surfaceTexture?.release()
        } catch (_: Throwable) {
        }
        try {
            asset?.let { assetLoader?.destroyAsset(it) }
        } catch (_: Throwable) {
        }
        try {
            view?.let { engine.destroyView(it) }
        } catch (_: Throwable) {
        }
        try {
            scene?.let { engine.destroyScene(it) }
        } catch (_: Throwable) {
        }
        try {
            renderer?.let { engine.destroyRenderer(it) }
        } catch (_: Throwable) {
        }
        try {
            engine.destroy()
        } catch (_: Throwable) {
        }
    }

    private fun readFileToBuffer(file: File): ByteBuffer {
        val inputStream = FileInputStream(file)
        val buffer = ByteBuffer.allocateDirect(file.length().toInt()).order(ByteOrder.nativeOrder())
        val bytes = ByteArray(file.length().toInt())
        inputStream.read(bytes)
        buffer.put(bytes)
        buffer.flip()
        inputStream.close()
        return buffer
    }
}
