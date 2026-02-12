import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.10"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.seyone22.cook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.seyone22.cook"
        minSdk = 30
        targetSdk = 36
        versionCode = 5
        versionName = "v5.0.0-beta3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // --- MERGED: Gemini API Key Logic ---
        // This safely reads the key or defaults to an empty string if missing
        val geminiApiKey: String = try {
            val propertiesFile = project.rootProject.file("local.properties")
            if (propertiesFile.exists()) {
                propertiesFile.inputStream().use { stream ->
                    Properties().apply { load(stream) }.getProperty("GEMINI_API_KEY")
                } ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }

        // Exposes BuildConfig.GEMINI_API_KEY to your Kotlin code
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // --- MERGED: Build Features ---
    buildFeatures {
        compose = true
        buildConfig = true // Required for BuildConfig.GEMINI_API_KEY to work
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

dependencies {

    // AndroidX Core
    implementation(libs.androidx.core.ktx)

    // AndroidX Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // AndroidX Activity Compose
    implementation(libs.androidx.activity.compose)

    // AndroidX Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // AndroidX Compose Material3
    implementation(libs.androidx.material3)
    implementation(libs.material)
    implementation(libs.androidx.compose.runtime)

    // JUnit
    testImplementation(libs.junit)

    // AndroidX Test
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // AndroidX Compose Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // AndroidX Compose Material Icons Extended
    implementation(libs.androidx.material.icons.extended)

    // AndroidX Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // AndroidX Compose Material3 Adaptive Navigation Suite
    implementation(libs.androidx.material3.adaptive.navigation.suite)

    // AndroidX Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // AndroidX Preferences DataStore
    implementation(libs.androidx.datastore.preferences)

    // AndroidX Lifecycle ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Retrofit
    implementation(libs.retrofit)

    // Retrofit with Kotlin Serialization Converter
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Kotlinx Serialization JSON
    implementation(libs.kotlinx.serialization.json)

    // AndroidX Window
    implementation(libs.androidx.window)

    // For `compose`. Creates a `ChartStyle` based on an M3 Material Theme.
    implementation(libs.vico.compose.m3)

    // Workers
    implementation(libs.androidx.work.runtime.ktx)

    // Biometrics
    implementation(libs.androidx.biometric.ktx)

    // Permission handling with accompanist
    implementation(libs.accompanist.permissions)

    implementation(libs.coil.compose)

    implementation(project(":atproto-auth2"))

    implementation(libs.ktor.client.core.v235) // core client
    implementation(libs.ktor.client.cio.v235)  // CIO engine

    implementation(libs.ktor.client.android) // For Android-specific support
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // The compose calendar library for Android
    implementation(libs.compose)

    implementation(libs.recipe.importer)

    // To recognize Latin script
    implementation(libs.text.recognition)

    // …
    implementation(libs.play.services.mlkit.document.scanner)

    // Task.await() helpers to await Play Services Tasks
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.generativeai)

    implementation(libs.openai.client)
// Google Gemini API
}