plugins {
        alias(libs.plugins.androidApplication)
        alias(libs.plugins.jetbrainsKotlinAndroid)
        alias(libs.plugins.compose.compiler)

        id("com.google.devtools.ksp")
        id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"

        id("com.google.gms.google-services")
    }

android {
    namespace = "com.seyone22.cook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.seyone22.cook"
        minSdk = 30
        targetSdk = 36
        versionCode = 4
        versionName = "Cook v4.0.0-beta2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
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

    implementation("io.ktor:ktor-client-core:2.3.5") // core client
    implementation("io.ktor:ktor-client-cio:2.3.5")  // CIO engine

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