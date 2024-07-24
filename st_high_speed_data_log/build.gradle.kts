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
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleHilt)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.devtoolsKsp)
    alias(libs.plugins.appswithloveLoco)
    alias(libs.plugins.androidxSafeargs)
}

apply {
    from("publish.gradle")
}

android {
    namespace = "com.st.high_speed_data_log"
    compileSdk = stCompileSdk

    defaultConfig {
        minSdk = stMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
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
        compose = true
        buildConfig = true
        viewBinding = true
    }

    composeCompiler {
        enableStrongSkippingMode = true
    }
}

Loco {
    config {
        apiKey = stLocoApiKey
        // lang = ["it", "en"] // add as many languages as you want, they need to exist on localise.biz
        defLang = "en"
        fallbackLang = "en"
        tags = "st_high_speed_data_log"
        resDir = "$projectDir/src/main/res"
    }
}

dependencies {
    // Blue ST module:
    // - Core
    implementation(project(":st_core"))
    // - UI
    implementation(project(":st_ui"))
    // - Preferences
    implementation(project(":st_preferences"))
    // - PnP-L
    implementation(project(":st_pnpl"))

    // Blue ST SDK
    implementation(libs.st.sdk)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigationFragment)
    ksp(libs.hilt.compiler)

    // Dependency required for API desugaring.
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)
}
