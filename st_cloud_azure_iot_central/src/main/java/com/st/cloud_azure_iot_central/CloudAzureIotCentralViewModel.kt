/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.cloud_azure_iot_central

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.BleCharacteristic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.models.Node
import com.st.cloud_azure_iot_central.model.AzureCloudDevice
import com.st.cloud_azure_iot_central.model.CloudAPIToken
import com.st.cloud_azure_iot_central.model.CloudAppConfigured
import com.st.cloud_azure_iot_central.model.CloudTemplateRetrieved
import com.st.cloud_azure_iot_central.network.AzureIoTDeviceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.lang.Exception
import java.net.URLEncoder
import javax.inject.Inject
import com.st.cloud_azure_iot_central.model.CloudContentCapabilityModel
import com.st.cloud_azure_iot_central.model.CloudContentSchemaModel
import com.st.cloud_azure_iot_central.model.CloudDeviceCredentials
import com.st.cloud_azure_iot_central.model.FieldModel
import com.st.cloud_azure_iot_central.network.AzureIoTCentralPnPConnection
import com.st.preferences.StPreferences
import kotlinx.coroutines.Job
import java.util.Date

@HiltViewModel
class CloudAzureIotCentralViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    val deviceCloutNotSELECTED = -1

    private var firsTimeRetrieveCloudApp = true
    private var necessityToRetrieveCloudDevice = true

    private val localViewModel = this

    var availableFeatures: List<Feature<*>>? = null

    var featuresEnabled: MutableList<Boolean>?=null

    private var nodeId: String = ""

    private val selectedFeatures: MutableList<Feature<*>> = mutableListOf()

    private var node: Node? = null

    private var catalogBleCharsWithDtmiName: List<BleCharacteristic> = emptyList()

    private var mAzureIoTService: AzureIoTDeviceService? = null

    private val _cloudDevices = MutableStateFlow(value = listOf<AzureCloudDevice>())
    val cloudDevices: StateFlow<List<AzureCloudDevice>>
        get() = _cloudDevices

    private val _boardUid = MutableStateFlow("")
    val boardUid: StateFlow<String>
        get() = _boardUid.asStateFlow()

    private val _retValue = MutableStateFlow<String?>(null)
    val retValue: StateFlow<String?>
        get() = _retValue.asStateFlow()

    private val _isOneCloudAppConfig = mutableStateOf(value = false)
    val isOneCloudAppConfig: State<Boolean>
        get() = _isOneCloudAppConfig

    private val _selectedCloudAppNum = MutableStateFlow(value = deviceCloutNotSELECTED)
    val selectedCloudAppNum: StateFlow<Int>
        get() = _selectedCloudAppNum

    var selectedCloudApp: CloudAppConfigured? = null

    private val _selectedCloudDeviceNum = MutableStateFlow(value = deviceCloutNotSELECTED)
    val selectedCloudDeviceNum: StateFlow<Int>
        get() = _selectedCloudDeviceNum

    private val _isCloudDeviceConfigured = MutableStateFlow(value = false)
    val isCloudDeviceConfigured: StateFlow<Boolean>
        get() = _isCloudDeviceConfigured

    var selectedCloudDevice: AzureCloudDevice? = null

    private val _cloudTemplates = MutableStateFlow(value = listOf<CloudTemplateRetrieved>())
    val cloudTemplates: StateFlow<List<CloudTemplateRetrieved>>
        get() = _cloudTemplates

    private val _listCloudApps =
        MutableStateFlow<List<CloudAppConfigured>>(
            emptyList()
        )
    val listCloudApps: StateFlow<List<CloudAppConfigured>>
        get() = _listCloudApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean>
        get() = _isLoading.asStateFlow()


    private val _deviceConnected = MutableStateFlow(false)
    val deviceConnected: StateFlow<Boolean>
        get() = _deviceConnected.asStateFlow()

    private val _sendFeature = MutableStateFlow<String?>(null)
    val sendFeature: StateFlow<String?>
        get() = _sendFeature

    //Default Update Interval
    var updateInterval: Int = 1000

    //FeatureName -> Pair(ComponentName, Schema)
    private val mFeatureMap = mutableMapOf<String, Pair<String, CloudContentSchemaModel>>()

    //Job for controlling the features
    private var observeFeatureJob: Job? = null

    fun startDemo(nodeId: String) {
        this.nodeId = nodeId
        //Retrieve the node with Catalog FW info
        viewModelScope.launch(Dispatchers.IO) {

            //Retrieve the Node
            node = blueManager.getNodeWithFirmwareInfo(nodeId)

            node?.let {
                //Set like default BoardUid the BLE mac
                _boardUid.value = node!!.advertiseInfo?.getAddress()?.replace(":", "") ?: ""
            }

            //Retrieve all the BlueST-SDK Ble Chars with a dtmi_name
            catalogBleCharsWithDtmiName =
                blueManager.getBleCharacteristics().filter { it2 -> it2.dtmiName != null }

            //Retrieve the correct STM32 UID from the board
            blueManager.writeDebugMessage(
                nodeId = nodeId, msg = "uid"
            )
            val buffer = StringBuffer()
            blueManager.getDebugMessages(nodeId = nodeId)?.collect { message ->
                message.payload
                buffer.append(message.payload)
                if (buffer.endsWith('\n')) {
                    _boardUid.value = buffer.toString().split('_')[0]
                }
            }

        }

        _isOneCloudAppConfig.value = false
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

    suspend fun readAPITokenDetails(selectedApp: CloudAppConfigured): CloudAPIToken? {
        var token: CloudAPIToken? = null

        val tokenID =
            selectedApp.authorizationKey?.replace(
                ".*&skn=".toRegex(),
                ""
            )?.replace("&se=.*".toRegex(), "")

        if ((selectedApp.cloudApp.url != null) && (tokenID != null)) {
            mAzureIoTService = AzureIoTDeviceService.buildInstance(
                selectedApp.cloudApp.url!!,
                selectedApp.authorizationKey!!
            )
            mAzureIoTService?.let {
                try {
                    token = mAzureIoTService!!.getTokenDetailsByID(tokenID)
                    Log.d("readAPITokenDetails", "getTokenDetailsByID $token")
                } catch (e: Exception) {
                    Log.e("readAPITokenDetails", "Error sync: " + e.localizedMessage)
                    e.printStackTrace()
                }
            }
        }
        return token
    }

    suspend fun deleteDeviceById(id: String) {
        _isLoading.value = true
        mAzureIoTService?.let {
            _retValue.value = try {
                val result =
                    mAzureIoTService!!.deleteDevice(id)
                when (result.code()) {
                    204 -> {
                        "Device $id Deleted"
                    }

                    else -> {
                        "Error deleting Device $id: Result code=${result.code()}"
                    }
                }
            } catch (e: HttpException) {
                "Error deleting Device $id: ${e.message()}"
            }
        }
        _isLoading.value = false
        //we need to re-load the list of cloud Devices
        necessityToRetrieveCloudDevice = true
    }

    suspend fun createNewDevice(newDevice: AzureCloudDevice) {
        _isLoading.value = true
        val json = Json
        val bodyReq: RequestBody = json.encodeToString(newDevice)
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        mAzureIoTService?.let {
            try {
                val result = mAzureIoTService!!.addDevice(
                    URLEncoder.encode(newDevice.id, "utf-8"),
                    bodyReq
                )
                _retValue.value = when (result.code()) {
                    200 -> {
                        "Device Added"
                    }

                    else -> {
                        "Error Adding Device: Result code=${result.code()}"
                    }
                }

            } catch (e: HttpException) {
                _retValue.value = "Error Adding Device\n${e.message()}"
            }
        }
        //we need to re-load the list of cloud Devices
        necessityToRetrieveCloudDevice = true
        _isLoading.value = false
    }

    suspend fun readDevicesFromCloud() {
        if(necessityToRetrieveCloudDevice) {
            _isLoading.value = true
            mAzureIoTService?.let {
                try {
                    val list =
                        mAzureIoTService!!.getDevices().list.sortedBy { it.id != _boardUid.value }
                    _cloudDevices.value = list
                    Log.d("readDevicesFromCloud", "readDevicesFromCloud ${list.size}")
                } catch (e: Exception) {
                    Log.e("readDevicesFromCloud", "Error sync: " + e.localizedMessage)
                }
            }
            _isLoading.value = false
            necessityToRetrieveCloudDevice = false
        }
    }

    suspend fun readTemplatesFromCloud() {
        mAzureIoTService?.let {
            try {
                val templates = mAzureIoTService!!.getTemplates()
                _cloudTemplates.value = templates.list
                Log.d("IoTPnP", "readTemplatesFromCloud ${templates.list.size}")
            } catch (e: Exception) {
                val error = "readTemplatesFromCloud Error: " + e.localizedMessage
                Log.e(this::javaClass.name, error)
            }
        }
    }

    fun cloudAppConfigurationDone() {
        _isOneCloudAppConfig.value = true
    }

    fun loadCloudAppConfigurationSaved() {
        // retrieve the cloud apps from catalog
        if (firsTimeRetrieveCloudApp) {
            firsTimeRetrieveCloudApp = false
            val cloudAppsFromCatalog: MutableList<CloudAppConfigured> =
                (node?.let {
                    node!!.catalogInfo?.cloudApps?.mapIndexed { index, cloudApp ->
                        CloudAppConfigured(
                            cloudApp = cloudApp,
                            appIndex = index,
                        )
                    }
                } ?: emptyList()).toMutableList()

            //Retrieve the Cloud Apps saved from preferences
            cloudAppsFromCatalog.forEachIndexed { index, cloudApp ->
                if (cloudApp.cloudApp.url != null) {
                    val cloudAppFromPrefString =
                        stPreferences.getConfiguredAzureCloudApp(cloudApp.cloudApp.url!!)

                    //try to decode it
                    if (cloudAppFromPrefString != null) {
                        try {
                            val cloudAppFromPref =
                                Json.decodeFromString<CloudAppConfigured>(cloudAppFromPrefString)

                            // check if the saved token is expired
                            if (cloudAppFromPref.apiToken != null) {
                                cloudAppFromPref.apiTokenExpired =
                                    cloudAppFromPref.apiToken!!.expire < Date()
                            }

                            //check if the configuration is valid
                            _isOneCloudAppConfig.value =
                                (cloudAppFromPref.apiToken != null) && (!cloudAppFromPref.apiTokenExpired)

                            //Copy values
                            cloudAppFromPref.appIndex = cloudApp.appIndex

                            //Update the Current CloudApp
                            cloudAppsFromCatalog[index] = cloudAppFromPref
                        } catch (e: Exception) {
                            Log.d("loadCloudAppConfigurationSaved", e.stackTraceToString())
                        }
                    }
                }
            }
            _listCloudApps.value = cloudAppsFromCatalog.toList()
        }
    }

    fun checkIfOneCloudAppIsConfigured() {
        _isOneCloudAppConfig.value =
            _listCloudApps.value.firstOrNull { appCloudApp -> (appCloudApp.apiToken != null) && (!appCloudApp.apiTokenExpired) } != null
    }

    fun setSelectedCloudApp(index: Int) {
        _selectedCloudAppNum.value = index
        selectedCloudApp = _listCloudApps.value[index]
        if (mAzureIoTService == null) {
            mAzureIoTService = AzureIoTDeviceService.buildInstance(
                selectedCloudApp!!.cloudApp.url!!,
                selectedCloudApp!!.authorizationKey!!
            )
        }
    }

    fun setSelectedCloudDevice(index: Int) {
        _selectedCloudDeviceNum.value = index
        if (index != deviceCloutNotSELECTED) {
            selectedCloudDevice = _cloudDevices.value[index]

            selectedCloudDevice?.let {
                //check if we already have the device credentials
                if(selectedCloudDevice!!.credentials!=null) {
                    _isCloudDeviceConfigured.value = true
                } else {
                    readDeviceCredentialsFromCloud()
                }
            }
        }
    }

    private fun readDeviceCredentialsFromCloud() {
        var credentials: CloudDeviceCredentials?
        _isCloudDeviceConfigured.value = false
        CoroutineScope(Dispatchers.IO).launch {
            if ((selectedCloudDevice != null) && (mAzureIoTService != null)) {
                _isLoading.value = true
                try {
                    val result = mAzureIoTService!!.getDeviceCredentials(
                        URLEncoder.encode(
                            selectedCloudDevice!!.id,
                            "utf-8"
                        )
                    )
                    when (result.code()) {
                        200 -> {
                            val body = result.body()
                            if (body != null) {
                                val data = body.string()
                                val json = Json
                                credentials = json.decodeFromString(data)
                                selectedCloudDevice!!.credentials = credentials

                                _retValue.value = "Device Credential Read"
                                _isCloudDeviceConfigured.value = true
                            }
                        }
                        else -> {
                            _retValue.value =
                                "Error Reading Device Credentials: Result code=${result.code()}"
                        }
                    }
                } catch (e: Exception) {
                    val error = "readDeviceCredentialsFromCloud Error: " + e.localizedMessage
                    _retValue.value = error
                    Log.e(this::javaClass.name, error)
                }
                _isLoading.value = false
            }
        }
    }

    fun cleanError() {
        _retValue.value = null
    }

    fun disconnectDevice() {
        _deviceConnected.value = false

        if (selectedFeatures.isNotEmpty()) {
            coroutineScope.launch {
                selectedFeatures.forEach { feature ->
                    blueManager.disableFeatures(nodeId, listOf(feature))
                }
                selectedFeatures.clear()

                val client = AzureIoTCentralPnPConnection.provideAzureIoTCentralPnPConnection()
                client.closeDeviceConnection()
                _retValue.value = "Device Disconnected"

                //Clean the last transmitted value
                _sendFeature.value = ""
            }
        }
    }

    private fun sendSimpleTelemetry(
        componentName: String,
        telemetryName: String,
        telemetry: Double
    ) {
        val client = AzureIoTCentralPnPConnection.provideAzureIoTCentralPnPConnection()
        client.sendSimpleTelemetry(componentName, telemetryName, telemetry)
    }

    private fun sendingComplexTelemetry(
        componentName: String,
        telemetryName: String,
        telemetryFormat: List<FieldModel?>,
        telemetry: List<Double>
    ) {
        val client = AzureIoTCentralPnPConnection.provideAzureIoTCentralPnPConnection()
        client.sendingComplexTelemetry(componentName, telemetryName, telemetryFormat, telemetry)
    }

    fun connectDevice() {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            if (selectedCloudDevice != null) {
                _retValue.value = "Connecting the Device"
                //Device Template Id for the current device
                val currentDevTemplateId = selectedCloudDevice!!.template

                //Search the device template for the current device
                selectedCloudDevice!!.templateModel =
                    _cloudTemplates.value.firstOrNull { it.id == currentDevTemplateId }


                if (selectedCloudDevice!!.templateModel != null) {
                    //Search the std_comp insight the right DeviceTemplate
                    //std_comp Capability Model
                    val stdCompModel =
                        selectedCloudDevice!!.templateModel?.capabilityModel?.contents?.firstOrNull {
                            when (it) {
                                is CloudContentCapabilityModel.CloudContentCapabilityModelString -> {
                                    it.name.lowercase() == "std_comp"
                                }

                                is CloudContentCapabilityModel.CloudContentCapabilityModelObj -> {
                                    it.name.lowercase() == "std_comp"
                                }
                            }
                        }

                    if (stdCompModel != null) {
                        //Retrieve all the node features
                        availableFeatures =
                            blueManager.nodeFeatures(nodeId)
                                .filter { it.isSupportedFeature(stdCompModel) }

                        availableFeatures?.let {
                            featuresEnabled = (1..availableFeatures!!.size).map { false }.toMutableList()
                        }

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
                                            val log = featureUpdate.data.logDoubleValues

                                            //Read the right component name and schema
                                            val name = mFeatureMap[featureName]?.first
                                            val schema = mFeatureMap[featureName]?.second
                                            schema?.let {
                                                when (schema) {
                                                    is CloudContentSchemaModel.CloudContentSchemaModelObj -> {
                                                        sendingComplexTelemetry(
                                                            "std_comp",
                                                            name!!,
                                                            schema.schema!!.fields!!,
                                                            log
                                                        )
                                                    }

                                                    is CloudContentSchemaModel.CloudContentSchemaModelString -> {
                                                        sendSimpleTelemetry(
                                                            "std_comp",
                                                            name!!,
                                                            log.first()
                                                        )
                                                    }
                                                }
                                            }

                                            //Printing with only 1E-4 precision
                                            _sendFeature.value =
                                                "$featureName ${log.map { (it * 1000).toInt() / 1000.0 }}"

                                        }
                                        lastFeatureUpdate[featureName] = notificationTime
                                    }
                                }
                            }
                        }
                    }

                    //Make the real Connection
                    val client =
                        AzureIoTCentralPnPConnection.provideAzureIoTCentralPnPConnection()
                    client.initializeProvisionDeviceAndConnect(viewModel = localViewModel)
                    _retValue.value = "Device Connected"

                } else {
                    _deviceConnected.value = false
                    availableFeatures = null
                    featuresEnabled = null
                    _isLoading.value = false
                }
            } else {
                _deviceConnected.value = false
                availableFeatures = null
                featuresEnabled = null
                _isLoading.value = false
            }
        }
    }

    fun markDeviceAsConnected() {
        _deviceConnected.value = true
        _isLoading.value = false
    }

    private fun Feature<*>.isSupportedFeature(stdCompModel: CloudContentCapabilityModel): Boolean {
        var isSupported = false
        //Current Feature Name
        val featureName = name.lowercase().replace(" ", "_")

        if (stdCompModel is CloudContentCapabilityModel.CloudContentCapabilityModelObj) {
            //Check between catalog dtmi_name and Feature Name
            catalogBleCharsWithDtmiName.forEach { bleChar ->
                if (bleChar.dtmiName!!.lowercase() == featureName) {
                    val schema = stdCompModel.schema?.contents?.firstOrNull { it ->
                        when (it) {
                            is CloudContentSchemaModel.CloudContentSchemaModelObj -> {
                                it.name.lowercase() == featureName
                            }

                            is CloudContentSchemaModel.CloudContentSchemaModelString -> {
                                it.name.lowercase() == featureName
                            }
                        }
                    }
                    isSupported = schema != null
                    schema?.let {
                        mFeatureMap[name] = bleChar.dtmiName!!.lowercase() to schema
                    }
                }
            }

            //ToDo: is it still necessary?
            if (!isSupported) {
                //Check between uuids...
                val uuidFeature = uuid.toString()
                catalogBleCharsWithDtmiName.forEach { bleChar ->
                    val uuid = bleChar.uuid
                    if (uuid == uuidFeature) {
                        val schema = stdCompModel.schema?.contents?.firstOrNull { it ->
                            when (it) {
                                is CloudContentSchemaModel.CloudContentSchemaModelObj -> {
                                    it.name.lowercase() == featureName
                                }

                                is CloudContentSchemaModel.CloudContentSchemaModelString -> {
                                    it.name.lowercase() == featureName
                                }
                            }
                        }
                        isSupported = schema != null
                        schema?.let {
                            mFeatureMap[name] = bleChar.dtmiName!!.lowercase() to schema
                        }
                    }
                }
            }
        }
        return isSupported
    }

    fun resetSavedCurrentCloudApp(selectedApp: CloudAppConfigured) {
        _selectedCloudAppNum.value = deviceCloutNotSELECTED
        if (selectedApp.cloudApp.url != null) {
            stPreferences.deleteConfiguredAzureCloudApp(selectedApp.cloudApp.url!!)
        }
    }

    fun saveCurrentCloudApp(selectedApp: CloudAppConfigured) {
        if (selectedApp.authorizationKey != null && selectedApp.apiToken != null && selectedApp.cloudApp.url != null) {
            val json = Json
            val serializedString = json.encodeToString(selectedApp)
            stPreferences.setConfiguredAzureCloudApp(selectedApp.cloudApp.url!!, serializedString)
        }
    }
}
