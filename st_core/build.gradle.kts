/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */

val stLocoApiKey: String by project
val stCompileSdk: Int by rootProject.extra
val stMinSdk: Int by rootProject.extra

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.googleHilt)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.devtoolsKsp)
}

apply {
    from("publish.gradle")
}

android {
    namespace = "com.st.core"
    compileSdk = stCompileSdk

    defaultConfig {
        minSdk = stMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    hilt {
        enableAggregatingTask = true
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.generateKotlin", "true")
    }
}

dependencies {
    // Blue ST SDK
    implementation(libs.st.sdk)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigationFragment)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)

    // Coroutines
    api(libs.kotlinx.coroutines.core)

    // Compat
    api(libs.bundles.compat)

    // KTX
    api(libs.bundles.ktx)

    // Navigation
    api(libs.bundles.navigation)

    // Retrofit
    api(libs.bundles.network)

    // Test
    testApi(libs.junit.core)

    // Dependency required for API desugaring.
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)
}
