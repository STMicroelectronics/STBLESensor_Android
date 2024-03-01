/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.cloud_mqtt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.models.Node
import com.st.cloud_mqtt.model.CloudMqttServerConfig
import com.st.cloud_mqtt.network.CloudMqttV3Connection
import com.st.preferences.StPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class CloudMqttViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private val selectedFeatures: MutableList<Feature<*>> = mutableListOf()
    var availableFeatures: List<Feature<*>>? = null

    var featuresEnabled: MutableList<Boolean>?=null

    private var node: Node? = null
    private var nodeId: String = ""

    private val _cloudMqttServerConfig: MutableStateFlow<CloudMqttServerConfig?> =
        MutableStateFlow(value = null)
    val cloudMqttServerConfig: StateFlow<CloudMqttServerConfig?>
        get() = _cloudMqttServerConfig

    //Default Update Interval
    var updateInterval: Int = 1000

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean>
        get() = _isLoading.asStateFlow()

    private val _deviceConnected = MutableStateFlow(false)
    val deviceConnected: StateFlow<Boolean>
        get() = _deviceConnected.asStateFlow()

    private val _retValue = MutableStateFlow<String?>(null)
    val retValue: StateFlow<String?>
        get() = _retValue.asStateFlow()

    private val _sendFeatureValue = MutableStateFlow<String?>(null)
    val sendFeatureValue: StateFlow<String?>
        get() = _sendFeatureValue

    private val _sendTopic = MutableStateFlow<String?>(null)
    val sendTopic: StateFlow<String?>
        get() = _sendTopic

    private val _isBrokerConfigured = MutableStateFlow(false)
    val isBrokerConfigured: StateFlow<Boolean>
        get() = _isBrokerConfigured.asStateFlow()

    //Job for controlling the features
    private var observeFeatureJob: Job? = null

    private val localViewModel = this

    fun startDemo(nodeId: String) {
        this.nodeId = nodeId

        //Retrieve the node
        viewModelScope.launch(Dispatchers.IO) {

            //Retrieve the Node
            node = blueManager.getNode(nodeId)

            //Retrieve all the node features that notify something
            availableFeatures =
                blueManager.nodeFeatures(nodeId)
                    .filter { it.isDataNotifyFeature }

            availableFeatures?.let {
                featuresEnabled = (1..availableFeatures!!.size).map { false }.toMutableList()
            }
        }
    }

    fun stopDemo(nodeId: String) {
        //Disable all the features
        if (selectedFeatures.isNotEmpty()) {
            coroutineScope.launch {
                selectedFeatures.forEach { feature ->
                    blueManager.disableFeatures(nodeId, listOf(feature))
                }
            }
        }
    }

    fun configureAndSaveMqttCloudApp(cloudMqttServerConfig: CloudMqttServerConfig) {
        _cloudMqttServerConfig.value = cloudMqttServerConfig
        //Save the Cloud configuration on preference
        val json = Json
        val serializedString = json.encodeToString(cloudMqttServerConfig)
        stPreferences.setConfiguredMqttCloudApp(serializedString)
        _isBrokerConfigured.value = true
    }

    fun resetAndDeletedSavedMqttCloudApp() {
        _cloudMqttServerConfig.value = null
        _isBrokerConfigured.value = false
        stPreferences.deleteConfiguredMqttCloudApp()
    }

    fun loadMqttCloudAppConfigurationSaved() {
        val serializedString = stPreferences.getConfiguredMqttCloudApp()
        //try to decode it
        if (serializedString != null) {
            try {
                val cloudAppFromPref =
                    Json.decodeFromString<CloudMqttServerConfig>(serializedString)

                _cloudMqttServerConfig.value = cloudAppFromPref
            } catch (e: Exception) {
                Log.d("loadMqttCloudAppConfigurationSaved", e.stackTraceToString())
            }
        }
        //For forcing to press again the Ok button
        _isBrokerConfigured.value = false
        setIsLoading(false)
        observeFeatureJob?.cancel()
    }

    fun enableFeature(feature: Feature<*>) {
        coroutineScope.launch {
            blueManager.enableFeatures(nodeId, listOf(feature))
            selectedFeatures.add(feature)
        }
    }

    fun disableFeature(feature: Feature<*>) {
        coroutineScope.launch {
            blueManager.disableFeatures(nodeId, listOf(feature))
            selectedFeatures.remove(feature)
        }
    }

    fun cleanError() {
        _retValue.value = null
    }

    fun setIsLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun disconnectDevice() {
        cloudMqttServerConfig.value?.let {
            _deviceConnected.value = false

            if (selectedFeatures.isNotEmpty()) {
                coroutineScope.launch {
                    selectedFeatures.forEach { feature ->
                        blueManager.disableFeatures(nodeId, listOf(feature))
                    }
                    selectedFeatures.clear()

                    //Disconnect the Device
                    val client = CloudMqttV3Connection.provideCloudMqttConnection()
                    _retValue.value = client.closeDeviceConnection()

                    //Clean the last transmitted value
                    _sendFeatureValue.value = ""
                    _sendTopic.value = ""
                }
            }
        }
    }

    fun connectDevice() {
        cloudMqttServerConfig.value?.let {
            setIsLoading(true)
            CoroutineScope(Dispatchers.IO).launch {
                _retValue.value = "Connecting the Device"
                observeFeatureJob?.cancel()
                if (!availableFeatures.isNullOrEmpty()) {

                    val lastFeatureUpdate: HashMap<String, Long> = hashMapOf()
                    observeFeatureJob = viewModelScope.launch {
                        blueManager.getFeatureUpdates(
                            nodeId,
                            availableFeatures!!,
                            autoEnable = false
                        ).collect { featureUpdate ->
                            val featureName = featureUpdate.featureName
                            val notificationTime = featureUpdate.notificationTime.time
                            val lastNotification = lastFeatureUpdate[featureName] ?: 0L
                            if (notificationTime - lastNotification > updateInterval) {
                                if (selectedFeatures.firstOrNull { feature -> feature.name == featureName } != null) {

                                    val namesSplit = featureUpdate.data.logHeader.split(',')
                                        .map { it ->
                                            it.substringBeforeLast('(').trim().replace(' ', '_')
                                                .lowercase()
                                        }
                                    val valuesSplit = featureUpdate.data.logValue.split(',')
                                        .map { it -> it.trim() }

                                    val minSize = minOf(namesSplit.size, valuesSplit.size)

                                    val jsonString = buildJsonObject {
                                        for (i in 0..<minSize) {
                                            put(namesSplit[i], valuesSplit[i])
                                        }
                                    }.toString()

                                    val topicName = cloudMqttServerConfig.value!!.deviceId + "/" + featureName

                                    //Send the Feature Update to Cloud
                                    val client = CloudMqttV3Connection.provideCloudMqttConnection()
                                    _retValue.value = client.publish(
                                        topicName,
                                        jsonString
                                    )

                                    if(_retValue.value!=null) {
                                        _sendFeatureValue.value = ""
                                        _sendTopic.value = ""
                                    } else {
                                        _sendFeatureValue.value =jsonString
                                        _sendTopic.value = topicName
                                    }
                                }
                                lastFeatureUpdate[featureName] = notificationTime
                            }
                        }
                    }

                    //Make the Connection
                    val client = CloudMqttV3Connection.provideCloudMqttConnection()
                    _retValue.value = client.createConnection(viewModel = localViewModel)
                } else {
                    _deviceConnected.value = false
                    setIsLoading(false)
                }
            }
        }
    }

    fun markDeviceAsConnected() {
        _deviceConnected.value = true
        setIsLoading(false)
    }
}
