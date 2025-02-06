package com.st.core.api

import android.app.Activity
import android.app.Application
import android.content.Context

interface ApplicationAnalyticsService {
    fun createAnalytics(
        etnaApplication: ApplicationNameEtna,
        application: Application,
        activity: Activity
    )

    enum class ApplicationNameEtna {
        STBLESensorDev,
        STBLESensorRel,
        STAssetTrackingDev,
        STAssetTrackingRel,
        STVespucciDev,
        STVespucciQa,
        STVespucciRel
    }

    fun reportApplicationAnalytics(context: Context)

    fun reportNodeAnalytics(
        nodeName: String,
        nodeType: String,
        fwVersion: String,
        fwFullName: String
    )

    fun reportProfile(profile: String)

    fun trackEvent(eventName: String, context: Map<String, String>)

    fun reportLevel(level: String)

    fun startDemoAnalytics(demoName: String)

    fun stopDemoAnalytics()

    fun flowExampleAppAnalytics(flowName: String)

    fun flowExpertAppAnalytics(flowName: String)

    fun flowExpertAppInputSensorAnalytics(id: String, model: String, odr: Double?)

    fun flowExpertAppFunctionAnalytics(id: String, desc: String)

    fun flowExpertAppOutputAnalytics(id: String, desc: String)

    fun trackQRCodeScanFlow(projectName: String, projectType: Int)

    fun trackProjectsFlows(projectType: Int, useCase: Int, projectName: String, firmwareName: String, boardType: String)

    fun trackDatasetFlows(useCase: Int, datasetName: String, firmwareName: String?, boardType: String?)

    fun trackCatalogFlow(boardName: String)

    fun trackDocumentationFlow()
}