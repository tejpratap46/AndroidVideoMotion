package com.tejpratapsingh.motion.tts

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.net.toUri
import com.google.gson.JsonObject
import com.tejpratapsingh.motionlib.core.MotionAsset
import com.tejpratapsingh.motionlib.core.MotionAssetManager
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber
import java.io.File
import java.security.MessageDigest

/**
 * Implementation of [MotionAsset] that generates audio from text using Android TTS.
 */
class TTSAudioAsset(
    private val text: String,
    private val metadata: JsonObject? = null,
) : MotionAsset {

    private val textHash: String by lazy {
        MessageDigest
            .getInstance("MD5")
            .digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    override fun getUri(): Uri = "content://tts_$textHash".toUri()

    override fun getMetadata(): JsonObject? = metadata

    override suspend fun prepare(context: Context): Boolean {
        val outputDir = File(context.cacheDir, "tts_assets")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val outputFile = File(outputDir, "tts_$textHash.wav")
        if (outputFile.exists()) {
            Timber.d("TTS asset already exists at: ${outputFile.absolutePath}")
            return true
        }

        val deferred = CompletableDeferred<Boolean>()
        var tts: TextToSpeech? = null

        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            Timber.d("TTS started for: $text")
                        }

                        override fun onDone(utteranceId: String?) {
                            Timber.d("TTS completed for: $text")
                            deferred.complete(true)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            Timber.e("TTS error for: $text")
                            deferred.complete(false)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(
                            utteranceId: String?,
                            errorCode: Int,
                        ) {
                            Timber.e("TTS error ($errorCode) for: $text")
                            deferred.complete(false)
                        }
                    },
                )

                val params = Bundle()
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, textHash)
                val result = tts?.synthesizeToFile(text, params, outputFile, textHash)
                if (result == TextToSpeech.ERROR) {
                    Timber.e("TTS synthesizeToFile failed immediately")
                    deferred.complete(false)
                } else if (result == null) {
                    Timber.e("TTS instance was null during synthesizeToFile")
                    deferred.complete(false)
                }
            } else {
                Timber.e("TTS initialization failed with status: $status")
                deferred.complete(false)
            }
        }.also { tts = it }

        val success =
            try {
                deferred.await()
            } catch (e: Exception) {
                Timber.e(e, "Error during TTS synthesis")
                false
            } finally {
                tts?.stop()
                tts?.shutdown()
            }

        return success
    }

    override fun getCachedUri(
        context: Context,
        cacheManager: MotionAssetManager,
    ): Uri? {
        val file = getLocalFile(context)
        return if (file.exists()) Uri.fromFile(file) else null
    }

    override fun isPrepared(
        context: Context,
        cacheManager: MotionAssetManager,
    ): Boolean {
        return getCachedUri(context, cacheManager) != null
    }

    /**
     * Helper to get the local file path after preparation.
     */
    fun getLocalFile(context: Context): File {
        val outputDir = File(context.cacheDir, "tts_assets")
        return File(outputDir, "tts_$textHash.wav")
    }
}
