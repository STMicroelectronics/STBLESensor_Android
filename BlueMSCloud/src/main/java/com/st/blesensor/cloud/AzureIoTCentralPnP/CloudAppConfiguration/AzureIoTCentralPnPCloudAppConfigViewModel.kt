package com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueSTSDK.Node
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfigured
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.AzureCloudDevice
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.CloudDeviceCredentials
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.CloudDevicesList
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel.CloudTemplatesList

class AzureIoTCentralPnPCloudAppConfigViewModel : ViewModel() {

    private var mSelectedCloudApp: CloudAppConfigured?=null
    private var mListCloudApps = listOf<CloudAppConfigured>()
    private var mSelectedAzureDevice: AzureCloudDevice?=null

    private lateinit var mNode: Node
    private var mMcuId: String?=null
    private var mRunningFwVersion: String?=null

    private var mWeHaveOneValidApp=false

    sealed class IoTCentralPnPDestination {

        /**
         * Configured Cloud Application Selection
         */
        object ConfiguredCloudAppSelection : IoTCentralPnPDestination()

        /**
         * Cloud Application Selection
         */
        object CloudAppSelection : IoTCentralPnPDestination()

        /**
         * Cloud Application Configuration
         */
        object CloudAppConfiguration : IoTCentralPnPDestination()

        /**
         * Device Selection
         */
        object DeviceSelection : IoTCentralPnPDestination()

        /**
         * Final state
         */
        object CloudFinalState : IoTCentralPnPDestination()
    }

    private val _currentView = MutableLiveData<IoTCentralPnPDestination>(IoTCentralPnPDestination.ConfiguredCloudAppSelection)
    val currentView: LiveData<IoTCentralPnPDestination>
        get() = _currentView


    fun configurationDone(selectedCloudApp: CloudAppConfigured) {
        mSelectedCloudApp = selectedCloudApp
        _currentView.postValue(IoTCentralPnPDestination.ConfiguredCloudAppSelection)
    }

    fun configurationAborted() {
        _currentView.postValue(IoTCentralPnPDestination.CloudAppSelection)
    }

    fun goToCloudAppSelection() {
        _currentView.postValue(IoTCentralPnPDestination.CloudAppSelection)
    }

    fun goToCloudInitialPage() {
        _currentView.postValue(IoTCentralPnPDestination.CloudFinalState)
    }

    fun goToCloudAppConfiguration() {
        _currentView.postValue(IoTCentralPnPDestination.CloudAppConfiguration)
    }

    fun goToConfiguredCloudApplication() {
        _currentView.postValue(IoTCentralPnPDestination.ConfiguredCloudAppSelection)
    }

    fun goToDeviceSelection() {
        _currentView.postValue(IoTCentralPnPDestination.DeviceSelection)
    }


    fun getSelectedCloudApp() = mSelectedCloudApp

    fun setSelectedCloudApp(selectedApp: CloudAppConfigured) {
        mSelectedCloudApp = selectedApp
        mWeHaveOneValidApp = true
    }

    fun getWeHaveOneValidApp() = mWeHaveOneValidApp

    fun setListCloudApps(listCloudApps: List<CloudAppConfigured>) {
        mListCloudApps = listCloudApps
    }

    fun getListCloudApps() = mListCloudApps


    fun setNode(node: Node) {
        mNode=node
    }
    fun getNode() = mNode

    fun getSelectedDevice() = mSelectedAzureDevice

    fun setSelectedDevice(device: AzureCloudDevice?) {
        mSelectedAzureDevice = device
    }

    fun setMcuId(mcuId: String?) {
       mMcuId = mcuId
    }

    fun getMcuId() = mMcuId

    fun setRunningFwVersion(fwVersion: String?) {
        mRunningFwVersion = fwVersion
    }

    fun getRunningFwVersion() = mRunningFwVersion

    companion object {
        val APP_CONF_CLOUD_FRAGMENT_TAG = AzureIoTCentralPnPCloudAppConfigViewModel::class.java.name + ".APP_CONF_CLOUD_FRAGMENT_TAG"
        val APP_CLOUD_SEL_FRAGMENT_TAG = AzureIoTCentralPnPCloudAppConfigViewModel::class.java.name + ".APP_CLOUD_SEL_FRAGMENT_TAG"
        val APP_CLOUD_CONF_FRAGMENT_TAG = AzureIoTCentralPnPCloudAppConfigViewModel::class.java.name + ".APP_CLOUD_CONF_FRAGMENT_TAG"
        val DEVICE_SEL_FRAGMENT_TAG = AzureIoTCentralPnPCloudAppConfigViewModel::class.java.name + ".DEVICE_SEL_FRAGMENT_TAG"
    }

}