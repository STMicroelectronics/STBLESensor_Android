package com.st.core.impl

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.st.core.api.ApplicationAnalyticsService
import com.st.core.api.ApplicationAnalyticsService.ApplicationNameEtna
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class ApplicationAnalyticsDefaultImpl @Inject constructor() : ApplicationAnalyticsService {

    companion object {
        private const val TAG = "ApplicationAnalyticsService"
    }

    override fun createAnalytics(etnaApplication: ApplicationNameEtna, application: Application, activity: Activity) {
        Log.d(TAG, "Default Impl for createAnalytics")
    }

    override fun reportApplicationAnalytics(context: Context) {
        Log.d(TAG, "Default Impl for reportApplicationAnalytics")
    }

    override fun reportNodeAnalytics(nodeName: String, nodeType: String, fwVersion: String, FwFullName: String) {
        Log.d(TAG, "Default Impl for reportNodeAnalytics")
    }

    override fun startDemoAnalytics(demoName: String) {
        Log.d(TAG, "Default Impl for startDemoAnalytics")
    }

    override fun stopDemoAnalytics() {
        Log.d(TAG, "Default Impl for stopDemoAnalytics")
    }
}