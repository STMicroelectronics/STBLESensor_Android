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
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.devtoolsKsp)
    alias(libs.plugins.appswithloveLoco)
}

apply {
    from("publish.gradle")
}

android {
    namespace = "com.st.ui"
    compileSdk = stCompileSdk

    defaultConfig {
        minSdk = stMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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
        tags = "st_ui"
        resDir = "$projectDir/src/main/res"
    }
}

dependencies {

    // Compose
    api(platform(libs.androidx.compose.bom))
    api(libs.bundles.accompanist)
    api(libs.bundles.coil)
    api(libs.bundles.compose)
    api(libs.bundles.composeUiTooling)

//    // Android Studio Preview support
//    api "androidx.compose.ui:ui-tooling-preview"
//    debugApi "androidx.compose.ui:ui-tooling"
//    // UI Tests
//    androidTestApi "androidx.compose.ui:ui-test-junit4"
//    debugApi "androidx.compose.ui:ui-test-manifest"
//    // Optional - Add full set of material icons
//    api "androidx.compose.material:material-icons-extended"
//    // Optional - Add window size utils
//    api "androidx.compose.material3:material3-window-size-class"
//    // Optional - Integration with activities
//    api "androidx.activity:activity-compose:$activity_compose_version"
//    // Optional - Integration with ViewModels
//    api "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version"
//    api "androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version"
//    // Optional - Integration with LiveData
//    api("androidx.compose.runtime:runtime-livedata")
//    // Optional - Integration with Hilt
//    api "androidx.hilt:hilt-navigation-compose:$hilt_compose_version"
//    // Optional - Integration with Navigation
//    api "androidx.navigation:navigation-compose:$navigation_compose_version"
//    // Optional - Integration with ViewBinding
//    api "androidx.compose.ui:ui-viewbinding"


    // Core
//    api(libs.bundles.core)
//    api "androidx.core:core-ktx:$core_ktx_version"
//    api "com.google.android.material:material:$google_material_version"
//    api "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version"
//    api "androidx.appcompat:appcompat:$appcompat_version"
//
    // Accompanist
    api(libs.bundles.accompanist)

    // Compat
    api(libs.bundles.compat)

    // Glide
    api(libs.glide)

    // Customize Splash screen
    api(libs.androidx.splashscreen)

    // UI Test
    androidTestApi(platform(libs.androidx.compose.bom))
    androidTestApi(libs.bundles.composeTest)
    androidTestApi(libs.bundles.test)

    // Test
    testApi(libs.junit.core)

    // Dependency required for API desugaring.
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)

    debugImplementation(libs.androidx.compose.uitestmanifest)
}
