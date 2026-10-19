plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "com.tejpratapsingh.lyricsmaker"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.tejpratapsingh.lyricsmaker"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

val parcelizeCompiler: Configuration = configurations.create("parcelizeCompiler")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            parcelizeCompiler.files.map { "-Xplugin=${it.absolutePath}" }
        )
    }
}

dependencies {
    parcelizeCompiler("org.jetbrains.kotlin:kotlin-parcelize-compiler:${libs.versions.kotlinVersion.get()}")
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:${libs.versions.kotlinVersion.get()}")
    implementation(libs.koin.android)
    implementation(libs.koin.workmanager)
    implementation(project(":modules:core"))
    implementation(project(":modules:motion-video-editor"))
    implementation(project(":modules:motion-download-manager"))
    implementation(project(":modules:motion-asset-store"))
    implementation(project(":modules:motion-tts-asset"))
    implementation(libs.androidx.appcompat)
    implementation(libs.google.android.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.picasso)

    implementation(project(":modules:motionlib"))
    implementation(project(":modules:motion-video-player"))
    implementation(project(":modules:motion-store"))
    implementation(project(":modules:metadata-extractor"))
    implementation(project(":modules:ffmpeg-motion-ext"))
    implementation(project(":modules:sdui"))
    implementation(project(":modules:templates"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.navigation)
    implementation(libs.androidx.adaptive.navigation.suite)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    testImplementation(libs.junit)
    testImplementation(project(":modules:paparazzi-motion-ext"))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
