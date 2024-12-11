/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    extra.apply {
        // Define versions in a single place
        set("stMinSdk", 26)
        set("stCompileSdk", 35)
        set("stTargetSdk", 35)
    }

    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.benManes) apply true
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.arturboschDetekt) apply false
    alias(libs.plugins.googleHilt) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.devtoolsKsp) apply false
    alias(libs.plugins.jlleitschuhKtlint) apply false
    alias(libs.plugins.littlerobotsVersionCatalogUpdate) apply true
    alias(libs.plugins.appswithloveLoco) apply false
    alias(libs.plugins.androidxSafeargs) apply false
}

fun isNonStable(version: String): Boolean {
    return version.contains("alpha", true) ||
            version.contains("beta", true) ||
            version.contains("dev", true)
}

// https://github.com/ben-manes/gradle-versions-plugin
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
