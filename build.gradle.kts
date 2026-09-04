/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    extra.apply {
        // Define versions in a single place
        set("stMinSdk", 26)
        set("stCompileSdk", 37)
        set("stTargetSdk", 37)
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

// Extend the DefaultTask class to create a CustomSTmTask class
abstract class CustomSTmTask : DefaultTask() {
    @TaskAction
    fun assembleLibraries() {
        println("All specified libraries have been assembled.")
    }

    @TaskAction
    fun publishLibrariesOnLocalMaven() {
        println("All specified libraries have been published on Local Maven.")
    }
}

// Register the Tasks with type CustomSTmTask
tasks.register<CustomSTmTask>("assembleLibraries") {
    group = "Custom STM Task"
    description = "Compiles the common STM libraries used by other STM applications."

    val stCatalogTask = tasks.getByPath(":st_catalog:assemble")
    val stCoreTask = tasks.getByPath(":st_core:assemble")
    val stExtConfigTask = tasks.getByPath(":st_ext_config:assemble")
    val stHighSpeedDataLogTask = tasks.getByPath(":st_high_speed_data_log:assemble")
    val stLoginTask = tasks.getByPath(":st_login:assemble")
    val stPnplTask = tasks.getByPath(":st_pnpl:assemble")
    val stPreferencesTask = tasks.getByPath(":st_preferences:assemble")
    val stRegistersDemoTask = tasks.getByPath(":st_registers_demo:assemble")
    val stTermsTask = tasks.getByPath(":st_terms:assemble")
    val stDownloadTermsTask = tasks.getByPath(":st_download_terms:assemble")
    val stUiTask = tasks.getByPath(":st_ui:assemble")
    val stUserProfilingTask = tasks.getByPath(":st_user_profiling:assemble")
    val stWelcomeTask = tasks.getByPath(":st_welcome:assemble")

    stWelcomeTask.mustRunAfter(stUserProfilingTask)
    stUserProfilingTask.mustRunAfter(stUiTask)
    stUiTask.mustRunAfter(stTermsTask)
    stTermsTask.mustRunAfter(stDownloadTermsTask)
    stDownloadTermsTask.mustRunAfter(stRegistersDemoTask)
    stRegistersDemoTask.mustRunAfter(stPreferencesTask)
    stPreferencesTask.mustRunAfter(stPnplTask)
    stPnplTask.mustRunAfter(stLoginTask)
    stLoginTask.mustRunAfter(stHighSpeedDataLogTask)
    stHighSpeedDataLogTask.mustRunAfter(stExtConfigTask)
    stExtConfigTask.mustRunAfter(stCoreTask)
    stCoreTask.mustRunAfter(stCatalogTask)

    dependsOn(stCatalogTask,
        stCoreTask,
        stExtConfigTask,
        stHighSpeedDataLogTask,
        stLoginTask,
        stPnplTask,
        stPreferencesTask,
        stRegistersDemoTask,
        stTermsTask,
        stDownloadTermsTask,
        stUiTask,
        stUserProfilingTask,
        stWelcomeTask)
}

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
}
tasks.register<CustomSTmTask>("publishLibrariesOnLocalMaven") {
    group = "Custom STM Task"
    description = "publish on local Maven the common STM libraries used by other STM applications."

    val stCatalogTask = tasks.getByPath(":st_catalog:publishToMavenLocal")
    val stCoreTask = tasks.getByPath(":st_core:publishToMavenLocal")
    val stExtConfigTask = tasks.getByPath(":st_ext_config:publishToMavenLocal")
    val stHighSpeedDataLogTask = tasks.getByPath(":st_high_speed_data_log:publishToMavenLocal")
    val stLoginTask = tasks.getByPath(":st_login:publishToMavenLocal")
    val stPnplTask = tasks.getByPath(":st_pnpl:publishToMavenLocal")
    val stPreferencesTask = tasks.getByPath(":st_preferences:publishToMavenLocal")
    val stRegistersDemoTask = tasks.getByPath(":st_registers_demo:publishToMavenLocal")
    val stTermsTask = tasks.getByPath(":st_terms:publishToMavenLocal")
    val stDownloadTermsTask = tasks.getByPath(":st_download_terms:publishToMavenLocal")
    val stUiTask = tasks.getByPath(":st_ui:publishToMavenLocal")
    val stUserProfilingTask = tasks.getByPath(":st_user_profiling:publishToMavenLocal")
    val stWelcomeTask = tasks.getByPath(":st_welcome:publishToMavenLocal")

    stWelcomeTask.mustRunAfter(stUserProfilingTask)
    stUserProfilingTask.mustRunAfter(stUiTask)
    stUiTask.mustRunAfter(stTermsTask)
    stTermsTask.mustRunAfter(stDownloadTermsTask)
    stDownloadTermsTask.mustRunAfter(stRegistersDemoTask)
    stRegistersDemoTask.mustRunAfter(stPreferencesTask)
    stPreferencesTask.mustRunAfter(stPnplTask)
    stPnplTask.mustRunAfter(stLoginTask)
    stLoginTask.mustRunAfter(stHighSpeedDataLogTask)
    stHighSpeedDataLogTask.mustRunAfter(stExtConfigTask)
    stExtConfigTask.mustRunAfter(stCoreTask)
    stCoreTask.mustRunAfter(stCatalogTask)

    dependsOn(stCatalogTask,
        stCoreTask,
        stExtConfigTask,
        stHighSpeedDataLogTask,
        stLoginTask,
        stPnplTask,
        stPreferencesTask,
        stRegistersDemoTask,
        stTermsTask,
        stDownloadTermsTask,
        stUiTask,
        stUserProfilingTask,
        stWelcomeTask)
}
