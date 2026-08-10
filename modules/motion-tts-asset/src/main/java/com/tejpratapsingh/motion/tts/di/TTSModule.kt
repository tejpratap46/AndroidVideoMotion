package com.tejpratapsingh.motion.tts.di

import com.tejpratapsingh.motion.tts.TTSAudioAssetSdui
import org.koin.dsl.module

/**
 * Koin module for TTS assets.
 */
val ttsModule = module {
    // Register SDUI components
    TTSAudioAssetSdui.register()
}
