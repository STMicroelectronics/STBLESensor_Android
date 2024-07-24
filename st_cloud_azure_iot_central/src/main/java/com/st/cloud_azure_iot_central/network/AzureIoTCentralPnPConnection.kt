package com.st.cloud_azure_iot_central.network

import android.util.Log
import com.microsoft.azure.sdk.iot.device.ClientOptions
import com.microsoft.azure.sdk.iot.device.DeviceClient
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol
import com.microsoft.azure.sdk.iot.device.Message
import com.microsoft.azure.sdk.iot.device.MessageSentCallback
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException
import com.microsoft.azure.sdk.iot.provisioning.device.AdditionalData
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClient
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol
import com.microsoft.azure.sdk.iot.provisioning.device.plugandplay.PnpHelper
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey
import com.st.cloud_azure_iot_central.CloudAzureIotCentralViewModel
import com.st.cloud_azure_iot_central.model.FieldModel
import com.st.cloud_azure_iot_central.network.from_azure_sdk.PnpConvention
import java.lang.StringBuilder
import java.util.Collections


class AzureIoTCentralPnPConnection {

    companion object {
        private var instance: AzureIoTCentralPnPConnection? = null

        private val globalEndpoint = "global.azure-devices-provisioning.net"
        private val TAG = "AzureIoTCentralPnPConnection"

        fun provideAzureIoTCentralPnPConnection(): AzureIoTCentralPnPConnection {
            if (instance == null) {
                instance = AzureIoTCentralPnPConnection()
            }
            return instance as AzureIoTCentralPnPConnection
        }
    }

    private var deviceClient: DeviceClient? = null

    fun initializeProvisionDeviceAndConnect(viewModel: CloudAzureIotCentralViewModel) {
        val selectedDevice = viewModel.selectedCloudDevice

        selectedDevice?.let {
            val securityClientSymmetricKey: SecurityProviderSymmetricKey =
                SecurityProviderSymmetricKey(
                    selectedDevice.credentials!!.symmetricKey!!.primaryKey!!,
                    selectedDevice.credentials!!.symmetricKey!!.secondaryKey!!,
                    selectedDevice.id
                )

            val provisioningDeviceClient: ProvisioningDeviceClient =
                ProvisioningDeviceClient.create(
                    globalEndpoint,
                    selectedDevice.credentials!!.idScope,
                    ProvisioningDeviceClientTransportProtocol.MQTT,
                    securityClientSymmetricKey
                )

            val additionalData = AdditionalData()
            val modelId = selectedDevice.templateModel!!.capabilityModel.id
            additionalData.provisioningPayload = PnpHelper.createDpsPayload(modelId)

            try {
                val registrationResult = provisioningDeviceClient.registerDeviceSync(additionalData)

                if (registrationResult.provisioningDeviceClientStatus == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED) {
                    Log.i(TAG, "IotHUb Uri : " + registrationResult.iothubUri)
                    Log.i(TAG, "Device ID : " + registrationResult.deviceId)
                    val iotHubUri = registrationResult.iothubUri
                    val deviceId = registrationResult.deviceId
                    Log.i(TAG, "Opening the device client.")

                    val options = ClientOptions.builder().modelId(modelId).build()

                    deviceClient = DeviceClient(
                        iotHubUri,
                        deviceId,
                        securityClientSymmetricKey,
                        IotHubClientProtocol.MQTT,
                        options
                    )
                    deviceClient?.open(true)

                    viewModel.markDeviceAsConnected()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error provisioningDeviceClient: " + e.localizedMessage)
                e.printStackTrace()
            }
        }
    }

    fun sendSimpleTelemetry(
        componentName: String,
        telemetryName: String,
        telemetry: Double
    ) {
        val message: Message =
            PnpConvention.createIotHubMessageUtf8(telemetryName, telemetry, componentName)
        deviceClient?.sendEventAsync(message, LocalMessageCallback(), message);
        Log.d(
            TAG,
            "SimpleTelemetry: Sent - {\"{$telemetryName}\": {$telemetry}} with message Id {${message.messageId}}."
        )
    }

    fun sendingComplexTelemetry(
        componentName: String,
        telemetryName: String,
        telemetryFormat: List<FieldModel?>,
        telemetry: List<Double>
    ) {
        val obj: MutableMap<String, Double> = HashMap()
        val sampleValues = StringBuilder()

        //for (index in telemetryFormat.indices) {
        for(index in 0 until minOf(telemetryFormat.size,telemetry.size)) {
                when(telemetryFormat[index]!!) {
                    is FieldModel.FieldModelObj -> {
                        val name = (telemetryFormat[index]!! as FieldModel.FieldModelObj).name
                        obj[name!!] = telemetry[index]
                    }
                    is FieldModel.FieldModelString -> {
                        val name =(telemetryFormat[index]!! as FieldModel.FieldModelString).name
                        obj[name!!] = telemetry[index]
                    }
                }

            sampleValues.append("a_x").append("=").append(telemetry[index].toString())

            //ToDo: is it still necessary?
            //For avoiding the null for sample type
//            if(sample.dataDesc[index].unit!=null) {
//                sampleValues.append(sample.dataDesc[index].unit)
//            }

            sampleValues.append(" ")
        }

        val payload = Collections.singletonMap<String, Any>(telemetryName, obj)

        val message: Message = PnpConvention.createIotHubMessageUtf8(
            payload,
            componentName
        )

        deviceClient?.sendEventAsync(message, LocalMessageCallback(), message);
        Log.d(
            TAG,
            "ComplexTelemetry: Sent - {\"{$telemetryName}\": {$telemetry}} with message Id {${message.messageId}}."
        )

    }

    fun closeDeviceConnection() {
        Log.i(TAG, "Closing the device client.")
        deviceClient?.close()
    }

    /**
     * The callback to be invoked when a telemetry response is received from IoT Hub.
     */
    class LocalMessageCallback : MessageSentCallback {
        override fun onMessageSent(
            sentMessage: Message?,
            clientException: IotHubClientException?,
            callbackContext: Any?
        ) {
            val msg = callbackContext as Message
            if (clientException != null)
                Log.i(
                    TAG,
                    "Telemetry - Response from IoT Hub: message Id={${msg.messageId}}, status={${clientException.statusCode}}"
                )
            else
                Log.i(
                    TAG,
                    "Telemetry - Response from IoT Hub: message Id={${msg.messageId}}, status={OK}"
                )
        }
    }
}