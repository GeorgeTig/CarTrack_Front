plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.google.dagger.hilt.android)
}

android {
    namespace = "com.example.cartrack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cartrack"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Core & Lificycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jackpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)


    //Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.dagger.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.auth)

    // JWT auth
    implementation(libs.java.jwt)

    // Kotlinx Serialization (Required for Ktor JSON)
    implementation(libs.kotlinx.serialization.json)

    // Kotlinx Coroutines (Required by Ktor, ViewModels, etc.)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore (Local Storage)
    implementation(libs.androidx.datastore.preferences)

    // Local Unit Tests (test source set)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test) // Update the version if necessary

    // Instrumentation Tests (androidTest source set)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // BOM for Compose testing
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing) // Ensure this matches the version you're using for Hilt
    kspAndroidTest(libs.dagger.hilt.android.compiler) // Hilt compiler for Android tests using KSP

    // Debug Implementation (Only included in debug builds)
    debugImplementation(libs.androidx.ui.tooling) // Compose Layout Inspector, etc.
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.signalr)

    implementation(libs.kotlinx.coroutines.jdk8)
    implementation(libs.kotlinx.datetime)

    implementation(libs.kotlinx.coroutines.rx3)

    implementation(libs.coil.compose)
    implementation(libs.compose.m3)
    implementation(libs.accompanist.permissions)
}

