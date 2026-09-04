/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.preferences

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.st.core.api.ApplicationAnalyticsService
import com.st.preferences.di.PreferencesScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StPreferencesImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:ApplicationContext private val context: Context,
    @param:PreferencesScope private val coroutineScope: CoroutineScope,
    private val appAnalyticsService: Set<@JvmSuppressWildcards ApplicationAnalyticsService>
) : StPreferences {

    init {
        coroutineScope.launch { dataStore.data.first() } // prefetch prefs in datastore cache
    }


    private val _backupOperationOngoing = MutableStateFlow(false)
    override val backupOperationOngoing: StateFlow<Boolean>
        get() = _backupOperationOngoing.asStateFlow()

    private val _message: MutableStateFlow<String?> = MutableStateFlow(null)
    override val message: StateFlow<String?>
        get() = _message.asStateFlow()

    override fun mustShowFwUpdate(nodeId: String, currentFw: String): Boolean {
        val fwUpdateKey = booleanPreferencesKey(String.format(FORMATTER_KEY, currentFw, nodeId))
        return runBlocking { dataStore.data.first()[fwUpdateKey]?.not() ?: true }
    }

    override fun doNotShowAgainFwUpdate(nodeId: String, currentFw: String) {
        coroutineScope.launch {
            val fwUpdateKey =
                booleanPreferencesKey(String.format(FORMATTER_KEY, currentFw, nodeId))
            dataStore.edit { prefs -> prefs[fwUpdateKey] = true }
        }
    }

    override fun termsPrefsUpdates(): Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[TERMS_KEY] ?: false
        }

    override fun hasAcceptedTerms(): Boolean {
        return runBlocking { dataStore.data.first()[TERMS_KEY] ?: false }
    }

    override fun hasAcceptedDownloadTerms(): Boolean {
        //return runBlocking { dataStore.data.first()[DOWNLOAD_TERMS_KEY] ?: false }
        //Tmp for waiting to receive the real License
        return runBlocking { dataStore.data.first()[DOWNLOAD_TERMS_KEY] ?: true }
    }

    override fun getLevelProficiency(): String {
        val retValue: String = runBlocking { dataStore.data.first()[LEVEL_KEY] ?: "" }

        if (retValue.isNotEmpty()) {
            appAnalyticsService.forEach {
                it.reportLevel(retValue)
            }
        }
        return retValue
    }

    override fun getProfileType(): String {
        val retValue: String = runBlocking { dataStore.data.first()[TYPE_KEY] ?: "" }

        if (retValue.isNotEmpty()) {
            appAnalyticsService.forEach {
                it.reportProfile(retValue)
            }
        }
        return retValue
    }

    override fun setTermsFlag(accepted: Boolean) {
        runBlocking {
            dataStore.edit { prefs -> prefs[TERMS_KEY] = accepted }
        }
    }

    override fun setDownloadTermsFlag(accepted: Boolean) {
        runBlocking {
            dataStore.edit { prefs -> prefs[DOWNLOAD_TERMS_KEY] = accepted }
        }
    }

    override fun setLevelProficiency(level: String) {
        runBlocking {
            dataStore.edit { prefs -> prefs[LEVEL_KEY] = level }
        }


        appAnalyticsService.forEach {
            it.reportLevel(level)
        }
    }

    override fun setProfileType(profile: String) {
        runBlocking {
            dataStore.edit { prefs -> prefs[TYPE_KEY] = profile }
        }


        appAnalyticsService.forEach {
            it.reportProfile(profile)
        }
    }

    override fun welcomePrefsUpdates(): Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[WELCOME_KEY] ?: false
        }

    override fun mustShowWelcome(): Boolean {
        return runBlocking { dataStore.data.first()[WELCOME_KEY]?.not() ?: true }
    }

    override fun setWelcomeFlag(completed: Boolean) {
        runBlocking {
            dataStore.edit { prefs -> prefs[WELCOME_KEY] = completed }
        }
    }

    override fun setBetaApplicationFlag(enableBeta: Boolean) {
        runBlocking {
            dataStore.edit { prefs -> prefs[BETA_KEY] = enableBeta }
        }
    }

    override fun setServerForcedFlag(serverForced: Boolean) {
        coroutineScope.launch {
            dataStore.edit { prefs -> prefs[SERVER_FORCED_KEY] = serverForced }
        }
    }

    override fun isBetaApplication(): Boolean {
        return runBlocking { dataStore.data.first()[BETA_KEY] ?: false }
    }

    override fun setDisableHiddenDemos(disableHiddenDemos: Boolean) {
        coroutineScope.launch {
            dataStore.edit { prefs -> prefs[DISABLE_HIDDEN_DEMOS_KEY] = disableHiddenDemos }
        }
    }

    override fun isDisableHiddenDemos(): Boolean {
        return runBlocking { dataStore.data.first()[DISABLE_HIDDEN_DEMOS_KEY] ?: false }
    }

    override fun isServerForced(): Boolean {
        return runBlocking { dataStore.data.first()[SERVER_FORCED_KEY] ?: false }
    }


    override fun profilePrefsUpdates() = dataStore.data
        .map { preferences ->
            preferences[PROFILE_KEY] ?: false
        }

    override fun hasSelectedProfile(): Boolean {
        return runBlocking { dataStore.data.first()[PROFILE_KEY] ?: false }
    }

    override fun setProfileFlag(selected: Boolean) {
        coroutineScope.launch {
            dataStore.edit { prefs -> prefs[PROFILE_KEY] = selected }
        }
    }

    override fun getDemoOrder(nodeId: String): List<String> {
        val idKey = stringPreferencesKey(nodeId)
        return runBlocking { dataStore.data.first()[idKey]?.split(", ") ?: emptyList() }
    }

    override fun setDemoOrder(nodeId: String, demos: List<String>) {
        coroutineScope.launch {
            val idKey = stringPreferencesKey(nodeId)
            dataStore.edit { prefs -> prefs[idKey] = demos.joinToString() }
        }
    }

    override fun setConfiguredAzureCloudApp(cloudAppUrl: String, serializedString: String) {
        runBlocking {
            val idKey = stringPreferencesKey(cloudAppUrl)
            dataStore.edit { prefs -> prefs[idKey] = serializedString }
        }
    }

    override fun getConfiguredAzureCloudApp(cloudAppUrl: String): String? {
        val idKey = stringPreferencesKey(cloudAppUrl)
        return runBlocking { dataStore.data.first()[idKey] }
    }

    override fun deleteConfiguredAzureCloudApp(cloudAppUrl: String) {
        coroutineScope.launch {
            val idKey = stringPreferencesKey(cloudAppUrl)
            dataStore.edit { prefs -> prefs.remove(idKey) }
        }
    }

    override fun setConfiguredMqttCloudApp(serializedString: String) {
        runBlocking {
            dataStore.edit { prefs -> prefs[MQTT_SERVER_KEY] = serializedString }
        }
    }

    override fun getConfiguredMqttCloudApp(): String? {
        return runBlocking { dataStore.data.first()[MQTT_SERVER_KEY] }
    }

    override fun deleteConfiguredMqttCloudApp() {
        coroutineScope.launch {
            dataStore.edit { prefs -> prefs.remove(MQTT_SERVER_KEY) }
        }
    }

    override fun getFavouriteDevices(): Flow<List<String>> = dataStore.data
        .map { preferences ->
            preferences[FAVOURITE_DEVICES_KEY]?.split(", ") ?: emptyList()
        }


    override fun getCustomNames(): Flow<List<Pair<String, String?>>> =
        dataStore.data.map { preferences ->
            preferences[CUSTOM_NAMES_KEY]?.let { customNamesString ->
                customNamesString.toBoardSetting().map { Pair(it.nodeId, it.customName) }
            } ?: emptyList()
        }

    override fun getBoardsSetting(): Flow<List<Pair<String, BoardSetting>>> =
        dataStore.data.map { preferences ->
            preferences[CUSTOM_NAMES_KEY]?.let { boardsSettingString ->
                boardsSettingString.toBoardSetting().map { Pair(it.nodeId, it) }
            } ?: emptyList()
        }



    override fun setBoardSetting(nodeId: String, customName: String?, boardTypeName: String) {
        runBlocking {
            dataStore.edit { prefs ->
                val currentList = (dataStore.data.first()[CUSTOM_NAMES_KEY]?.toBoardSetting()
                    ?: emptyList())
                val currentBoardSetting = currentList.firstOrNull { it.nodeId == nodeId }
                if(currentBoardSetting!=null) {
                    currentBoardSetting.customName = customName
                    prefs[CUSTOM_NAMES_KEY] = Json.encodeToString(currentList)
                } else {
                    val newList = listOf(BoardSetting(nodeId = nodeId, customName = customName, boardTypeName = boardTypeName))+ currentList
                    prefs[CUSTOM_NAMES_KEY] = Json.encodeToString(newList)
                }
            }
        }
    }

    override fun setFavouriteDevice(nodeId: String) {
        runBlocking {
            dataStore.edit { prefs ->
                val favourites = (dataStore.data.first()[FAVOURITE_DEVICES_KEY]?.split(", ")
                    ?: emptyList()) + listOf(nodeId)
                prefs[FAVOURITE_DEVICES_KEY] = favourites.joinToString()
            }
        }
    }

    override fun unsetFavouriteDevice(nodeId: String) {
        runBlocking {
            dataStore.edit { prefs ->
                val favourites =
                    (dataStore.data.first()[FAVOURITE_DEVICES_KEY]?.split(", ")
                        ?: emptyList()).filter { it != nodeId }
                prefs[FAVOURITE_DEVICES_KEY] = favourites.joinToString()
            }
        }
    }

    //Custom Entries
    override fun setCustomStringForKey(key: String, serializedString: String) {
        runBlocking {
            dataStore.edit { prefs -> prefs[stringPreferencesKey(key)] = serializedString }
        }
    }

    override fun getCustomStringFromKey(key: String): String? {
        return runBlocking { dataStore.data.first()[stringPreferencesKey(key)] }
    }

    override fun deleteCustomStringFromKey(key: String) {
        coroutineScope.launch {
            dataStore.edit { prefs -> prefs.remove(stringPreferencesKey(key)) }
        }
    }

    override fun setCustomBooleanForKey(key: String, value: Boolean) {
        runBlocking {
            dataStore.edit { prefs -> prefs[booleanPreferencesKey(key)] = value }
        }
    }

    override fun getCustomBooleanFromKey(key: String): Boolean? {
        return runBlocking { dataStore.data.first()[booleanPreferencesKey(key)] }
    }

    override fun deleteCustomBooleanFromKey(key: String) {
        coroutineScope.launch {
            dataStore.edit { prefs -> prefs.remove(booleanPreferencesKey(key)) }
        }
    }

    override fun triggerBackup(uri: Uri) {
        coroutineScope.launch(Dispatchers.IO) {
            _message.value = "Exporting backup..."
            try {
                _backupOperationOngoing.value = true
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    ZipOutputStream(outputStream).use { zos ->
                        val filesToBackup = mutableListOf<File>()
                        filesToBackup.add(File(context.filesDir, "datastore/st-prefs.preferences_pb"))

                        filesToBackup.forEach { file ->
                            if (file.exists()) {
                                val entry = ZipEntry(file.name)
                                zos.putNextEntry(entry)
                                FileInputStream(file).use { fis ->
                                    fis.copyTo(zos)
                                }
                                zos.closeEntry()
                            }
                        }
                    }
                }
                _message.value = BACKUP_DONE_MESSAGE
                _backupOperationOngoing.value = false
            } catch (e: kotlin.Exception) {
                Log.e(TAG, "Export failed", e)
                _message.value ="Export failed: ${e.message}"
                _backupOperationOngoing.value = false
            }
        }
    }

    override fun restoreBackup(uri: Uri) {
        coroutineScope.launch(Dispatchers.IO) {
            _message.value ="Importing backup..."
            try {
                // Close databases before replacing files
                _backupOperationOngoing.value = true

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            val targetFile = when {
                                entry.name.contains("st-prefs") ->
                                    File(context.filesDir, "datastore/st-prefs.preferences_pb")
                                else -> null
                            }

                            targetFile?.let { file ->
                                Log.i(TAG, "Restoring file: ${entry.name} to ${file.absolutePath}")
                                if (file.exists()) file.delete()
                                file.parentFile?.let { parent ->
                                    if (!parent.exists()) parent.mkdirs()
                                }
                                FileOutputStream(file).use { fos ->
                                    zis.copyTo(fos)
                                }
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                }
                _message.value = BACKUP_RESTORED_MESSAGE
                _backupOperationOngoing.value = false
            } catch (e: kotlin.Exception) {
                Log.e(TAG, "Import failed", e)
                _backupOperationOngoing.value = false
                _message.value ="Import failed: ${e.message}"
            }
        }
    }

    override fun restartApplication() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    companion object {
        private const val TAG = "StPreferencesImpl"
        private const val FORMATTER_KEY = "propose_fw_update_for%s_%s"
        private val TERMS_KEY = booleanPreferencesKey("terms_key")
        private val DOWNLOAD_TERMS_KEY = booleanPreferencesKey("download_terms_key")
        private val FAVOURITE_DEVICES_KEY = stringPreferencesKey("favourite_devices_key")
        private val WELCOME_KEY = booleanPreferencesKey("welcome_key")
        private val PROFILE_KEY = booleanPreferencesKey("profile_key")
        private val LEVEL_KEY = stringPreferencesKey("level_key")
        private val TYPE_KEY = stringPreferencesKey("type_key")
        private val BETA_KEY = booleanPreferencesKey("beta_key")
        private val DISABLE_HIDDEN_DEMOS_KEY = booleanPreferencesKey("disable_hidden_demos_key")
        private val SERVER_FORCED_KEY = booleanPreferencesKey("server_forced_key")
        private val MQTT_SERVER_KEY = stringPreferencesKey("mqtt_server_key")
        private val CUSTOM_NAMES_KEY = stringPreferencesKey("custom_names_key")
        const val BACKUP_RESTORED_MESSAGE = "Backup restored successfully"
        const val BACKUP_DONE_MESSAGE = "Backup exported successfully"
    }
}
