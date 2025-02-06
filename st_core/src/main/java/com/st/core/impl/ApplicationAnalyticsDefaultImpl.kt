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

    override fun createAnalytics(
        etnaApplication: ApplicationNameEtna,
        application: Application,
        activity: Activity
    ) {
        Log.d(TAG, "Default Impl for createAnalytics")
    }

    override fun reportApplicationAnalytics(context: Context) {
        Log.d(TAG, "Default Impl for reportApplicationAnalytics")
    }

    override fun reportNodeAnalytics(
        nodeName: String,
        nodeType: String,
        fwVersion: String,
        fwFullName: String
    ) {
        Log.d(TAG, "Default Impl for reportNodeAnalytics")
    }

    override fun trackEvent(eventName: String, context: Map<String, String>) {
        Log.d(TAG, "Default Impl for reportProfile")
    }

    override fun reportProfile(profile: String) {
        Log.d(TAG, "Default Impl for reportProfile")
    }

    override fun reportLevel(level: String) {
        Log.d(TAG, "Default Impl for reportLevel")
    }

    override fun startDemoAnalytics(demoName: String) {
        Log.d(TAG, "Default Impl for startDemoAnalytics")
    }

    override fun stopDemoAnalytics() {
        Log.d(TAG, "Default Impl for stopDemoAnalytics")
    }

    override fun flowExampleAppAnalytics(flowName: String) {
        Log.d(TAG, "Default Impl for flowExampleAppAnalytics")
    }

    override fun flowExpertAppAnalytics(flowName: String) {
        Log.d(TAG, "Default Impl for flowExpertAppAnalytics")
    }

    override fun flowExpertAppInputSensorAnalytics(id: String, model: String, odr: Double?) {
        Log.d(TAG, "Default Impl for flowExpertAppInputSensorAnalytics")
    }

    override fun flowExpertAppFunctionAnalytics(id: String, desc: String) {
        Log.d(TAG, "Default Impl for flowExpertAppFunctionAnalytics")
    }

    override fun flowExpertAppOutputAnalytics(id: String, desc: String) {
        Log.d(TAG, "Default Impl for flowExpertAppOutputAnalytics")
    }

    override fun trackQRCodeScanFlow(projectName: String, projectType: Int) {
        Log.d(TAG, "Default Impl for trackQRCodeScanFlow")
    }

    override fun trackProjectsFlows(projectType: Int, useCase: Int, projectName: String, firmwareName: String, boardType: String) {
        Log.d(TAG, "Default Impl for trackProjectsFlows")
    }

    override fun trackDatasetFlows(useCase: Int, datasetName: String, firmwareName: String?, boardType: String?) {
        Log.d(TAG, "Default Impl for trackDatasetFlows")
    }

    override fun trackCatalogFlow(boardName: String) {
        Log.d(TAG, "Default Impl for trackCatalogFlow")
    }

    override fun trackDocumentationFlow() {
        Log.d(TAG, "Default Impl for trackDocumentationFlow")
    }
}