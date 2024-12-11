/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.log_settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.di.LogDirectoryPath
import com.st.blue_sdk.logger.CsvFileLogger
import com.st.blue_sdk.logger.LogCatLogger
import com.st.blue_sdk.logger.db_logger.DbLogger
import com.st.demo_showcase.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LogSettingsViewModel @Inject constructor(
    private val blueManager: BlueManager,
    @ApplicationContext context: Context,
    @LogDirectoryPath val logDirectory: String
) : ViewModel() {
    private val packageName = context.packageName
    private val packageManager = context.packageManager
    private val resources = context.resources
    private val _isLogging: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLogging: StateFlow<Boolean> = _isLogging.asStateFlow()
    private val _logType: MutableStateFlow<LogType> = MutableStateFlow(LogType.CSV)
    val logType: StateFlow<LogType> = _logType.asStateFlow()

    private val _numberLogs: MutableStateFlow<Int> = MutableStateFlow(0)
    val numberLogs: StateFlow<Int> = _numberLogs.asStateFlow()

    fun startLogging(nodeId: String) {
        _isLogging.value = true

        enableDisableLogger(nodeId = nodeId, newState = true)
    }

    fun stopLogging(nodeId: String) {
        _isLogging.value = false

        enableDisableLogger(nodeId = nodeId, newState = false)
    }

    fun clearLogging(nodeId: String) {
        blueManager.clearAllLoggers(nodeId = nodeId, loggerTags = LogType.entries.map { it.tag })
        _numberLogs.value = 0
    }

    private fun isLogCatLoggerEnabled(nodeId: String): Boolean =
        blueManager.getAllLoggers(nodeId = nodeId)
            .find { it.id == LogCatLogger.TAG && it.isEnabled } != null

    private fun isCsvLoggerEnabled(nodeId: String): Boolean =
        blueManager.getAllLoggers(nodeId = nodeId)
            .find { it.id == CsvFileLogger.TAG && it.isEnabled } != null

    private fun isDbLoggerEnabled(nodeId: String): Boolean =
        blueManager.getAllLoggers(nodeId = nodeId)
            .find { it.id == DbLogger.TAG && it.isEnabled } != null


    private fun enableDisableLogger(nodeId: String, newState: Boolean) {
        when (_logType.value) {
            LogType.LOG_CAT -> enableDisableLogCatLogger(nodeId = nodeId, newState = newState)
            LogType.CSV -> enableDisableCsvLogger(nodeId = nodeId, newState = newState)
            LogType.DB -> enableDisableDbLogger(nodeId = nodeId, newState = newState)
        }
    }

    private fun enableDisableLogCatLogger(nodeId: String, newState: Boolean) {
        if (newState) {
            blueManager.enableAllLoggers(nodeId = nodeId, loggerTags = listOf(LogCatLogger.TAG))
        } else {
            blueManager.disableAllLoggers(nodeId = nodeId, loggerTags = listOf(LogCatLogger.TAG))
        }
    }

    private fun enableDisableCsvLogger(nodeId: String, newState: Boolean) {
        if (newState) {
            blueManager.enableAllLoggers(nodeId = nodeId, loggerTags = listOf(CsvFileLogger.TAG))
        } else {
            blueManager.disableAllLoggers(nodeId = nodeId, loggerTags = listOf(CsvFileLogger.TAG))
        }
    }

    private fun enableDisableDbLogger(nodeId: String, newState: Boolean) {
        if (newState) {
            blueManager.enableAllLoggers(nodeId = nodeId, loggerTags = listOf(DbLogger.TAG))
        } else {
            blueManager.disableAllLoggers(nodeId = nodeId, loggerTags = listOf(DbLogger.TAG))
        }
    }

    fun fetchLoggingStatus(nodeId: String) {
        _isLogging.value =
            isLogCatLoggerEnabled(nodeId = nodeId) || isDbLoggerEnabled(nodeId = nodeId)
                    || isCsvLoggerEnabled(nodeId = nodeId)

        _logType.value = when {
            isCsvLoggerEnabled(nodeId = nodeId) ->
                LogType.CSV
            isDbLoggerEnabled(nodeId = nodeId) ->
                LogType.DB
            isLogCatLoggerEnabled(nodeId = nodeId) ->
                LogType.LOG_CAT
            else ->
                LogType.CSV
        }
    }

    fun checkLogDir() {
        val logDir = File(logDirectory)
        if (logDir.exists() && logDir.listFiles()?.isEmpty() == false) {
            _numberLogs.value = logDir.listFiles()!!.size
        } else {
            _numberLogs.value = 0
        }
    }

    fun changeLogType(logType: LogType) {
        _logType.value = logType
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun shareLog(context: Context) {
        Intent(Intent.ACTION_SEND_MULTIPLE).also {
            it.type = "message/rfc822"
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }
            val strAppName =
                pInfo.applicationInfo?.let { it1 -> packageManager.getApplicationLabel(it1).toString() } ?: "STMicroelectronics Application"
            val strAppPackage = pInfo.packageName
            val emailTitle =
                resources.getString(R.string.st_demoShowcase_logSettings_shareEmailTitle)
            var strEmail = resources.getString(
                R.string.st_demoShowcase_logSettings_shareEmail,
                strAppName,
                Build.MANUFACTURER.uppercase(Locale.getDefault()),
                Build.MODEL,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT
            )
            val logDir = File(logDirectory)
            if (logDir.exists() && logDir.listFiles()?.isEmpty() == false) {
                val folder = logDir.path
                strEmail += resources.getString(
                    R.string.st_demoShowcase_logSettings_shareEmailWithAttach, folder
                )
                val uris = ArrayList<Uri>()
                logDir.listFiles()?.forEach { file ->
                    uris.add(
                        FileProvider.getUriForFile(
                            context, "$strAppPackage.logFileProvider", file
                        )
                    )
                }
                it.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            }

            it.putExtra(Intent.EXTRA_SUBJECT, "[$strAppName] $emailTitle")
            it.putExtra(Intent.EXTRA_TEXT, strEmail)
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (it.resolveActivity(packageManager) != null) {
                context.startActivity(it)
            }
        }
    }
}

enum class LogType(val tag: String) {
    LOG_CAT(tag = LogCatLogger.TAG), CSV(tag = CsvFileLogger.TAG), DB(tag = DbLogger.TAG)
}