package com.st.core.api

import android.app.Activity
import android.app.Application
import android.content.Context

interface ApplicationAnalyticsService {
    fun createAnalytics(etnaApplication: ApplicationNameEtna , application: Application, activity: Activity)

    enum class ApplicationNameEtna {
        STBLESensorDev,
        STBLESensorRel,
        STAssetTrackingDev,
        STAssetTrackingRel
    }

    fun reportApplicationAnalytics(context: Context)

    fun reportNodeAnalytics(nodeName: String, nodeType: String, fwVersion: String, FwFullName: String)

    fun startDemoAnalytics(demoName: String)

    fun stopDemoAnalytics()
}