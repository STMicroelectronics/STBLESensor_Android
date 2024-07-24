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
val stTargetSdk: Int by rootProject.extra

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.arturboschDetekt)
    alias(libs.plugins.googleHilt)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.devtoolsKsp)
    alias(libs.plugins.jlleitschuhKtlint)
    alias(libs.plugins.appswithloveLoco)
    alias(libs.plugins.androidxSafeargs)
}

apply {
    from("st_dependencies.gradle")
}

android {
    namespace = "com.st.bluems"
    compileSdk = stCompileSdk

    defaultConfig {
        applicationId = "com.st.bluems"
        minSdk = stMinSdk
        targetSdk = stTargetSdk
        versionCode = 209
        versionName = "5.2.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        manifestPlaceholders["appAuthRedirectScheme"] = "stblesensor"

        buildConfigField(
            type = "String",
            name = "VESPUCCI_ENVIRONMENT",
            value = "\"PROD\"" // "\"PRE_PROD\" ""\"DEV\""
        )
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
        compose = true
        viewBinding = true
    }

    composeCompiler {
        enableStrongSkippingMode = true
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.generateKotlin", "true")
    }

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

detekt {
    config.setFrom("../detekt-config-compose.yml")
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    verbose.set(true)
    debug.set(true)
    android.set(true)
    version.set("0.22.0")
}

Loco {
    config {
        apiKey = stLocoApiKey
        // lang = ["it", "en"] // add as many languages as you want, they need to exist on localise.biz
        defLang = "en"
        fallbackLang = "en"
        tags = "st_bluems"
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
    // - User Profiling
    implementation(project(":st_user_profiling"))
    // - Welcome
    implementation(project(":st_welcome"))
    // - Terms
    implementation(project(":st_terms"))
    // - Demos
    implementation(project(":st_demo_showcase"))
    // - Discover Catalog
    implementation(project(":st_catalog"))
    // - Login
    implementation(project(":st_login"))

    // Blue ST SDK
    implementation(libs.st.sdk)


    // Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigationFragment)
    ksp(libs.hilt.compiler)

    // Dependency required for API desugaring.
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)

    debugImplementation(libs.androidx.compose.uitestmanifest)
}
