plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "com.tejpratapsingh.motion.metadataextractor"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
    publishing {
        singleVariant("release")
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
            }
        }
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
    implementation(project(":modules:core"))
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.material)

    implementation(libs.jsoup)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
