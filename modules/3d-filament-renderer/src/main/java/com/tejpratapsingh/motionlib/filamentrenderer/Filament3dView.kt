package com.tejpratapsingh.motionlib.filamentrenderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.Surface
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.Texture
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.utils.Utils
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionView
import com.tejpratapsingh.motionlib.core.provideCurrentConfig
import timber.log.Timber
import java.nio.ByteBuffer

class Filament3dView(
    private val context: Context,
    private val modelAssetPath: String,
    override val startFrame: Int,
    override val endFrame: Int,
    override val loop: Pair<Int, Int> = Pair(0, 0),
) : FrameLayout(context),
    MotionView {
    override val effects: List<MotionEffect> = emptyList()

    private lateinit var engine: Engine
    private lateinit var swapChain: SwapChain
    private lateinit var scene: Scene
    private lateinit var view: View
    private lateinit var renderer: Renderer
    private lateinit var camera: Camera
    private var modelEntity: Int = 0
    private lateinit var surfaceTexture: SurfaceTexture
    private lateinit var surface: Surface

    private val imageView =
        ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

    init {
        initializeFilament()
        loadModel()
        setupCamera()
        imageView.setImageResource(android.R.drawable.btn_star) // Set a transparent background
        addView(imageView)
    }

    private fun initializeFilament() {
        Utils.init()
        surfaceTexture = SurfaceTexture(0)
        surfaceTexture.setDefaultBufferSize(
            provideCurrentConfig().aspectRatio.width,
            provideCurrentConfig().aspectRatio.height,
        )
        surface = Surface(surfaceTexture)
        engine = Engine.create()
        swapChain = engine.createSwapChain(surface, 0)
        scene = engine.createScene()
        view = engine.createView()
        view.scene = scene
        renderer = engine.createRenderer()
        view.viewport =
            Viewport(
                0,
                0,
                provideCurrentConfig().aspectRatio.width,
                provideCurrentConfig().aspectRatio.height,
            )
    }

    private fun loadModel() {
        val materialProvider = UbershaderProvider(engine)
        val assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())
        val buffer = loadAssetBuffer(modelAssetPath)
        val asset =
            assetLoader.createAsset(buffer) ?: throw IllegalStateException("Failed to load model")
        // Add all entities to the scene
        scene.addEntities(asset.entities)
        modelEntity = asset.root
    }

    private fun loadAssetBuffer(assetPath: String): ByteBuffer {
        val inputStream = context.assets.open(assetPath)
        val bytes = inputStream.readBytes()
        inputStream.close()
        val buffer = ByteBuffer.allocateDirect(bytes.size)
        buffer.put(bytes)
        buffer.rewind()
        return buffer
    }

    private fun setupCamera() {
        val cameraEntity = EntityManager.get().create()
        camera = engine.createCamera(cameraEntity)
        camera.setProjection(
            45.0,
            provideCurrentConfig().aspectRatio.width.toDouble() / provideCurrentConfig().aspectRatio.height.toDouble(),
            0.1,
            1000.0,
            Camera.Fov.VERTICAL,
        )
        camera.lookAt(
            0.0,
            0.0,
            5.0, // eyeX, eyeY, eyeZ
            0.0,
            0.0,
            0.0, // centerX, centerY, centerZ
            0.0,
            1.0,
            0.0, // upX, upY, upZ
        )
        view.camera = camera
    }

    private fun rotateModel(angle: Float) {
        Timber.i("rotateModel: $angle")
        val transformManager = engine.transformManager
        val instance = transformManager.getInstance(modelEntity)
        val matrix = FloatArray(16)
        android.opengl.Matrix.setIdentityM(matrix, 0)
        android.opengl.Matrix.setRotateM(matrix, 0, angle, 0f, 1f, 0f)
        transformManager.setTransform(instance, matrix)
    }

    fun renderAndCapture(): ByteBuffer {
        if (renderer.beginFrame(swapChain, 0L)) {
            renderer.render(view)
            val width = view.viewport.width
            val height = view.viewport.height
            val buffer = ByteBuffer.allocateDirect(width * height * 4)
            renderer.readPixels(
                0,
                0,
                width,
                height,
                Texture.PixelBufferDescriptor(
                    buffer,
                    Texture.Format.RGBA,
                    Texture.Type.UBYTE,
                ),
            )
            buffer.rewind()
            renderer.endFrame()
            return buffer
        } else {
            Timber.e("Failed to begin frame rendering")
            throw IllegalStateException("Failed to render frame")
        }
    }

    private fun getModelBitmap(buffer: ByteBuffer): Bitmap {
        val width = view.viewport.width
        val height = view.viewport.height
        val bitmap = createBitmap(width, height)
        bitmap.copyPixelsFromBuffer(buffer)
        val matrix = Matrix()
        matrix.preScale(1.0f, -1.0f) // Flip vertically to match OpenGL's coordinate system
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    private fun cleanupFilament() {
        engine.destroyEntity(camera.entity)
        EntityManager.get().destroy(camera.entity)
        engine.destroyRenderer(renderer)
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroySwapChain(swapChain)
        engine.destroy()
        surface.release()
        surfaceTexture.release()
    }

    override fun forFrame(frame: Int): MotionView {
        Timber.i("forFrame: $frame")
        // Update the model or camera based on the frame if needed
        // For example, you could animate the model or camera position
        rotateModel(frame.toFloat())
        val buffer = renderAndCapture()
        imageView.setImageBitmap(getModelBitmap(buffer))
        return this
    }

    override fun getViewBitmap(): Bitmap {
        val buffer = renderAndCapture()
        val bitmap = getModelBitmap(buffer)
        return bitmap
    }

    fun destroy() {
        cleanupFilament()
    }
}
