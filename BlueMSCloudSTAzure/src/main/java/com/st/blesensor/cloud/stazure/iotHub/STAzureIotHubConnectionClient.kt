package com.st.blesensor.cloud.stazure.iotHub

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.microsoft.azure.sdk.iot.device.*
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard
import com.st.blesensor.cloud.CloudIotClientConnectionFactory
import java.io.IOException
import java.lang.Exception

internal class STAzureIotHubConnectionClient(connectionString: String) : CloudIotClientConnectionFactory.CloutIotClient{

    val mDeviceClient = DeviceClient(connectionString,IotHubClientProtocol.MQTT)
    var isConnected:Boolean = false
    private set

    private var requestDisconnection = false

    fun connect(callback: CloudIotClientConnectionFactory.ConnectionListener){
        try {
            isConnected = false
            requestDisconnection = false
            mDeviceClient.open()
            mDeviceClient.startDeviceTwin(ConnectionEventCallback(callback),null,
                    LogTwinPropertiesCallback,null)

        }catch (e:Exception){
            callback.onFailure(e)
        }

    }

    fun disconnect(){
        mDeviceClient.closeNow()
        requestDisconnection = true
        isConnected = false
    }

    fun enableCloudFwUpgrade(callback: CloudIotClientConnectionFactory.FwUpgradeAvailableCallback) {
        val methods = mapOf("FirmwareUpdate--FwPackageUri-string" to "Updates device Firmware.")
        val property = Property("SupportedMethods",methods)
        mDeviceClient.sendReportedProperties(setOf(property))
        mDeviceClient.subscribeToDeviceMethod(FwUpgradeMethodInvocationCallback(callback),null,
                LogIotHubEvent,null)
    }


    inner class ConnectionEventCallback(private val callback: CloudIotClientConnectionFactory.ConnectionListener) : IotHubEventCallback{

        override fun execute(responseStatus: IotHubStatusCode?, callbackContext: Any?) {
            Log.d("Azure IotHub","response: $responseStatus")
            if(!isConnected && !requestDisconnection) {
                if (responseStatus == IotHubStatusCode.OK || responseStatus == IotHubStatusCode.OK_EMPTY) {
                    callback.onSuccess()
                    isConnected = true
                } else {
                    callback.onFailure(IOException("Error: $responseStatus"))
                }
            }
        }

    }


    private class FwUpgradeMethodInvocationCallback(private val callback: CloudIotClientConnectionFactory.FwUpgradeAvailableCallback) : DeviceMethodCallback{
        override fun call(methodName: String?, methodData: Any?, context: Any?): DeviceMethodData {
            Log.d("Azure IotHub","Method: $methodName -> data: $methodData")
            if(methodName != "FirmwareUpdate" ){
                return DeviceMethodData(UNKNOWN_METHOD,"")
            }
            val rawParam = methodData as? ByteArray ?: return DeviceMethodData(UNKNOWN_METHOD,"")
            val str = String(bytes = rawParam)
            val obj = Gson().fromJson(str,JsonObject::class.java)

            if(!obj.has("FwPackageUri")){
                DeviceMethodData(UNKNOWN_METHOD,"Missing parameter")
            }
            val urlStr = obj.getAsJsonPrimitive("FwPackageUri").asString
            callback.onFwUpgradeAvailable(urlStr)
            return DeviceMethodData(SUCCESS,"")
        }

        companion object{
            const val SUCCESS = 200
            const val UNKNOWN_METHOD = 404
        }

    }

    private object LogIotHubEvent : IotHubEventCallback{
        override fun execute(responseStatus: IotHubStatusCode?, callbackContext: Any?) {
            Log.d("Azure IotHub","Status: ${responseStatus}")
        }

    }

    private object LogTwinPropertiesCallback: TwinPropertyCallBack{
        override fun TwinPropertyCallBack(property: Property?, context: Any?) {
            if(property == null)
                return
            Log.d("Azure IotHub","Property: ${property.key} = ${property.value}")
        }

    }

    fun createFeatureListener(minUpdateIntervalMs:Long): Feature.FeatureListener {
        return STAzureFeatureListener(mDeviceClient, minUpdateIntervalMs)
    }

    fun reportFwInfo(version: FwVersionBoard) {
        val property = Property("AzureFwVersion",
                "${version.name} V${version.majorVersion}.${version.minorVersion}.${version.patchVersion}")
        mDeviceClient.sendReportedProperties(setOf(property))

    }


}