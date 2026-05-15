package com.tejpratapsingh.motion.sdui.infra

import android.content.Context
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionAudio
import com.tejpratapsingh.motionlib.core.MotionEffect
import com.tejpratapsingh.motionlib.core.MotionPlugin
import com.tejpratapsingh.motionlib.core.MotionTransition
import com.tejpratapsingh.motionlib.core.MotionView

/**
 * Registry for polymorphic [MotionView] and [MotionEffect] serialization/deserialization.
 */
object MotionSdui {
    private val viewFactories = mutableMapOf<String, MotionViewFactory>()
    private val viewSerializers = mutableMapOf<Class<out MotionView>, MotionViewSerializer<out MotionView>>()

    private val effectFactories = mutableMapOf<String, MotionEffectFactory>()
    private val effectSerializers = mutableMapOf<Class<out MotionEffect>, MotionEffectSerializer<out MotionEffect>>()

    private val transitionFactories = mutableMapOf<String, MotionTransitionFactory>()
    private val transitionSerializers = mutableMapOf<Class<out MotionTransition>, MotionTransitionSerializer<out MotionTransition>>()

    private val pluginFactories = mutableMapOf<String, MotionPluginFactory>()
    private val pluginSerializers = mutableMapOf<Class<out MotionPlugin>, MotionPluginSerializer<out MotionPlugin>>()

    private val audioFactories = mutableMapOf<String, MotionAudioFactory>()
    private val audioSerializers = mutableMapOf<Class<out MotionAudio>, MotionAudioSerializer<out MotionAudio>>()

    /**
     * Register a [MotionView] for deserialization.
     */
    fun registerView(type: String, factory: MotionViewFactory) {
        viewFactories[type] = factory
    }

    /**
     * Register a [MotionView] for serialization.
     */
    fun <T : MotionView> registerViewSerializer(clazz: Class<T>, serializer: MotionViewSerializer<T>) {
        viewSerializers[clazz] = serializer
    }

    /**
     * Register a [MotionEffect] for deserialization.
     */
    fun registerEffect(type: String, factory: MotionEffectFactory) {
        effectFactories[type] = factory
    }

    /**
     * Register a [MotionEffect] for serialization.
     */
    fun <T : MotionEffect> registerEffectSerializer(clazz: Class<T>, serializer: MotionEffectSerializer<T>) {
        effectSerializers[clazz] = serializer
    }

    /**
     * Register a [MotionTransition] for deserialization.
     */
    fun registerTransition(type: String, factory: MotionTransitionFactory) {
        transitionFactories[type] = factory
    }

    /**
     * Register a [MotionTransition] for serialization.
     */
    fun <T : MotionTransition> registerTransitionSerializer(clazz: Class<T>, serializer: MotionTransitionSerializer<T>) {
        transitionSerializers[clazz] = serializer
    }

    /**
     * Register a [MotionPlugin] for deserialization.
     */
    fun registerPlugin(type: String, factory: MotionPluginFactory) {
        pluginFactories[type] = factory
    }

    /**
     * Register a [MotionPlugin] for serialization.
     */
    fun <T : MotionPlugin> registerPluginSerializer(clazz: Class<T>, serializer: MotionPluginSerializer<T>) {
        pluginSerializers[clazz] = serializer
    }

    /**
     * Register a [MotionAudio] for deserialization.
     */
    fun registerAudio(type: String, factory: MotionAudioFactory) {
        audioFactories[type] = factory
    }

    /**
     * Register a [MotionAudio] for serialization.
     */
    fun <T : MotionAudio> registerAudioSerializer(clazz: Class<T>, serializer: MotionAudioSerializer<T>) {
        audioSerializers[clazz] = serializer
    }

    internal fun getViewFactory(type: String): MotionViewFactory? = viewFactories[type]

    @Suppress("UNCHECKED_CAST")
    internal fun <T : MotionView> getViewSerializer(clazz: Class<out T>): MotionViewSerializer<T>? =
        viewSerializers[clazz] as? MotionViewSerializer<T>

    internal fun getEffectFactory(type: String): MotionEffectFactory? = effectFactories[type]

    @Suppress("UNCHECKED_CAST")
    internal fun <T : MotionEffect> getEffectSerializer(clazz: Class<out T>): MotionEffectSerializer<T>? =
        effectSerializers[clazz] as? MotionEffectSerializer<T>

    internal fun getTransitionFactory(type: String): MotionTransitionFactory? = transitionFactories[type]

    @Suppress("UNCHECKED_CAST")
    internal fun <T : MotionTransition> getTransitionSerializer(clazz: Class<out T>): MotionTransitionSerializer<T>? =
        transitionSerializers[clazz] as? MotionTransitionSerializer<T>

    internal fun getPluginFactory(type: String): MotionPluginFactory? = pluginFactories[type]

    @Suppress("UNCHECKED_CAST")
    internal fun <T : MotionPlugin> getPluginSerializer(clazz: Class<out T>): MotionPluginSerializer<T>? =
        pluginSerializers[clazz] as? MotionPluginSerializer<T>

    internal fun getAudioFactory(type: String): MotionAudioFactory? = audioFactories[type]

    @Suppress("UNCHECKED_CAST")
    internal fun <T : MotionAudio> getAudioSerializer(clazz: Class<out T>): MotionAudioSerializer<T>? =
        audioSerializers[clazz] as? MotionAudioSerializer<T>
}

fun interface MotionViewFactory {
    fun create(context: Context, json: JsonObject): MotionView
}

fun interface MotionViewSerializer<T : MotionView> {
    fun serialize(view: T, json: JsonObject)
}

fun interface MotionEffectFactory {
    fun create(json: JsonObject): MotionEffect
}

fun interface MotionEffectSerializer<T : MotionEffect> {
    fun serialize(effect: T, json: JsonObject)
}

fun interface MotionTransitionFactory {
    fun create(json: JsonObject): MotionTransition
}

fun interface MotionTransitionSerializer<T : MotionTransition> {
    fun serialize(transition: T, json: JsonObject)
}

fun interface MotionPluginFactory {
    fun create(context: Context, json: JsonObject): MotionPlugin
}

fun interface MotionPluginSerializer<T : MotionPlugin> {
    fun serialize(plugin: T, json: JsonObject)
}

fun interface MotionAudioFactory {
    fun create(context: Context, json: JsonObject): MotionAudio
}

fun interface MotionAudioSerializer<T : MotionAudio> {
    fun serialize(audio: T, json: JsonObject)
}
