/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

val GPR_USER: String by settings
val GPR_API_KEY: String by settings

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
        mavenLocal()

        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/SW-Platforms/BlueSTSDK_Android")
            credentials {
                username = System.getenv("GPR_USER") ?: GPR_USER
                password = System.getenv("GPR_API_KEY") ?: GPR_API_KEY
            }
        }
    }
}

rootProject.name = "BlueMS"
include(":app")
include(":st_compass")
include(":st_licenses")
include(":st_ui")
include(":st_welcome")
include(":st_user_profiling")
include(":st_preferences")
include(":st_terms")
include(":st_environmental")
include(":st_fitness")
include(":st_level")
include(":st_demo_showcase")
include(":st_high_speed_data_log")
include(":st_blue_voice")
include(":st_gesture_navigation")
include(":st_neai_anomaly_detection")
include(":st_neai_classification")
include(":st_event_counter")
include(":st_piano")
include(":st_core")
include(":st_pnpl")
include(":st_plot")
include(":st_nfc_writing")
include(":st_binary_content")
include(":st_ext_config")
include(":st_tof_objects_detection")
include(":st_color_ambient_light")
include(":st_gnss")
include(":st_electric_charge_variation")
include(":st_motion_intensity")
include(":st_activity_recognition")
include(":st_carry_position")
include(":st_mems_gesture")
include(":st_catalog")
include(":st_motion_algorithms")
include(":st_pedometer")
include(":st_proximity_gesture_recognition")
include(":st_switch_demo")
include(":st_login")
include(":st_legacy_demo")
include(":st_registers_demo")
include(":st_acceleration_event")
include(":st_source_localization")
include(":st_audio_classification_demo")
include(":st_led_control")
include(":st_node_status")
include(":st_textual_monitor")
include(":st_heart_rate_demo")
include(":st_sensor_fusion")
include(":st_predicted_maintenance")
include(":st_fft_amplitude")
include(":st_multi_neural_network")
include(":st_working_in_progress")
include(":st_flow_demo")
include(":st_raw_pnpl")
include(":st_smart_motor_control")
include(":st_cloud_azure_iot_central")
include(":st_cloud_mqtt")
include(":st_neai_extrapolation")
include(":st_medical_signal")
include(":st_asset_tracking_event")
