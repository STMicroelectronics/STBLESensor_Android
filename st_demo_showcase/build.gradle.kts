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
    namespace = "com.st.demo_showcase"

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
        tags = "st_demo_showcase"
        resDir = "$projectDir/src/main/res"
    }
}

dependencies {
    // Blue ST module:
    // - UI
    implementation(project(":st_ui"))
    // - Core
    implementation(project(":st_core"))
    // - Prefs
    implementation(project(":st_preferences"))
    // - Login
    implementation(project(":st_login"))
    // - User Profiling
    implementation(project(":st_user_profiling"))
    // - Demos
    implementation(project(":st_compass"))
    implementation(project(":st_level"))
    implementation(project(":st_fitness"))
    implementation(project(":st_environmental"))
    implementation(project(":st_high_speed_data_log"))
    implementation(project(":st_blue_voice"))
    implementation(project(":st_gesture_navigation"))
    implementation(project(":st_neai_anomaly_detection"))
    implementation(project(":st_neai_classification"))
    implementation(project(":st_event_counter"))
    implementation(project(":st_piano"))
    implementation(project(":st_pnpl"))
    implementation(project(":st_plot"))
    implementation(project(":st_nfc_writing"))
    implementation(project(":st_binary_content"))
    implementation(project(":st_ext_config"))
    implementation(project(":st_tof_objects_detection"))
    implementation(project(":st_color_ambient_light"))
    implementation(project(":st_gnss"))
    implementation(project(":st_electric_charge_variation"))
    implementation(project(":st_motion_intensity"))
    implementation(project(":st_activity_recognition"))
    implementation(project(":st_carry_position"))
    implementation(project(":st_mems_gesture"))
    implementation(project(":st_motion_algorithms"))
    implementation(project(":st_pedometer"))
    implementation(project(":st_proximity_gesture_recognition"))
    implementation(project(":st_switch_demo"))
    implementation(project(":st_legacy_demo"))
    implementation(project(":st_registers_demo"))
    implementation(project(":st_acceleration_event"))
    implementation(project(":st_source_localization"))
    implementation(project(":st_audio_classification_demo"))
    implementation(project(":st_led_control"))
    implementation(project(":st_node_status"))
    implementation(project(":st_textual_monitor"))
    implementation(project(":st_heart_rate_demo"))
    implementation(project(":st_sensor_fusion"))
    implementation(project(":st_predicted_maintenance"))
    implementation(project(":st_fft_amplitude"))
    implementation(project(":st_multi_neural_network"))
    implementation(project(":st_working_in_progress"))
    implementation(project(":st_flow_demo"))
    implementation(project(":st_raw_pnpl"))
    implementation(project(":st_smart_motor_control"))
    implementation(project(":st_cloud_azure_iot_central"))
    implementation(project(":st_cloud_mqtt"))
    implementation(project(":st_neai_extrapolation"))
    implementation(project(":st_medical_signal"))
    // NEW_DEMO_ANCHOR
    // !!! Leave a row before this comment to allow the script to add the new demo !!!

    // Blue ST SDK
    implementation(libs.st.sdk)

    // Reorderable
    implementation(libs.burnoutcrew.reorderable)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigationFragment)
    ksp(libs.hilt.compiler)

    // Dependency required for API desugaring.
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)
}
