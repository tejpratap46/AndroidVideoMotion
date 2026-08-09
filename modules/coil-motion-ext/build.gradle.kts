import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

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

    namespace = "com.tejpratapsingh.motionlib.coil"
    lint {
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    testOptions {
        targetSdk = libs.versions.targetSdk.get().toInt()
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

dependencies {
    implementation(libs.koin.android)
    api(project(":modules:core"))
    api(project(":modules:motionlib"))
    api(project(":modules:sdui"))
    implementation(libs.androidx.appcompat)
    implementation(libs.coil)
    implementation(libs.coil.video)
    implementation(libs.coil.transformations)
    implementation(libs.coil.transformations.gpu)
    implementation(libs.coil.transformations.face.detection)

    testImplementation(libs.junit)
}
