package com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.CloudDeviceCredentials
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.CloudDevicesList
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel.CloudTemplatesList

class AzureIoTPnPDeviceSelectionViewModel: ViewModel() {
    private val _httpDeviceOperationResponse = MutableLiveData<String?>()
    val mHttpDeviceOperationResponse: LiveData<String?>
        get() = _httpDeviceOperationResponse

    private val _availableDevicesList = MutableLiveData<CloudDevicesList?>()
    val mAvailableDevicesList: LiveData<CloudDevicesList?>
        get() = _availableDevicesList

    private val _deviceCredentials = MutableLiveData<CloudDeviceCredentials?>()
    val mDeviceCredentials: LiveData<CloudDeviceCredentials?>
        get() = _deviceCredentials

    private  val _deviceTemplates = MutableLiveData<CloudTemplatesList?>()
    val mDeviceTemplates: LiveData<CloudTemplatesList?>
        get() = _deviceTemplates

    fun setHTTPDeviceOperationResponse(result: String) {
        _httpDeviceOperationResponse.postValue(result)
    }

    fun receivedHTTPDeviceOperationResponse() {
        _httpDeviceOperationResponse.postValue(null)
    }

    fun setAvailableDevicesList (list : CloudDevicesList?){
        _availableDevicesList.postValue(list)
    }

    fun receivedAvailableDevicesList (){
        _availableDevicesList.postValue(null)
    }

    fun setDeviceCredentials(cred: CloudDeviceCredentials?) {
        _deviceCredentials.postValue(cred)
    }

    fun setTemplates(templates: CloudTemplatesList?) {
        _deviceTemplates.postValue(templates)
    }
}