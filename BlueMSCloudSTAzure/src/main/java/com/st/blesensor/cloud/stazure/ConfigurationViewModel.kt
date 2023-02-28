package com.st.blesensor.cloud.stazure

import androidx.lifecycle.*
import com.st.BlueSTSDK.Node
import com.st.blesensor.cloud.stazure.communication.LoginResult
import com.st.blesensor.cloud.stazure.communication.RegistrationResult
import com.st.blesensor.cloud.stazure.communication.UserManager
import com.st.blesensor.cloud.stazure.communication.azure.AzureLogin
import com.st.blesensor.cloud.stazure.storage.RegisteredDeviceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ConfigurationViewModel(private val registeredDeviceDao: RegisteredDeviceDao ) : ViewModel() {

    enum class RegistrationStatus{
        UNKNOWN,
        DEVICE_INVALID_PARAM,
        LOGGING,
        LOGIN_INVALID_NAME,
        LOGIN_INVALID_PASSWORD,
        LOGIN_FORBIDDEN,
        ADDING_DEVICE,
        SUCCESS
    }

    private val mDeviceId = MutableLiveData<String>()
    val deviceId:LiveData<String>
    get() = mDeviceId

    private val mFriendlyName = MutableLiveData<String>()
    val friendlyName:LiveData<String>
    get() = mFriendlyName

    private val mShowEmptyNameError = MutableLiveData<Boolean>(false)
    val showEmptyNameError:LiveData<Boolean>
        get() = mShowEmptyNameError

    private val mConnectionString = MutableLiveData<String?>(null)
    val connectionString:LiveData<String?>
        get() = mConnectionString


    fun loadDataForNode(n: Node){
        if(mDeviceId.value == n.tag && mConnectionString.value!=null){
            return
        }
        mDeviceId.postValue(n.tag)
        mFriendlyName.postValue(n.friendlyName)
        viewModelScope.launch(Dispatchers.IO) {
            val knowDevice = registeredDeviceDao.getRegisterDevice(n.tag) ?: return@launch
            withContext(Dispatchers.Main){
                mFriendlyName.value = knowDevice.name
                mConnectionString.value =knowDevice.connectionString
            }

        }
    }

    fun setFriendlyName(newName:String){
        if(newName.isEmpty()){
            mShowEmptyNameError.postValue(true)
            return
        }
        mShowEmptyNameError.postValue(false)
        mFriendlyName.postValue(newName)
    }


    private val mRegistrationStatus = MutableLiveData<RegistrationStatus>(RegistrationStatus.UNKNOWN)
    val registrationStatus:LiveData<RegistrationStatus>
        get() = mRegistrationStatus

    private val loginManager = AzureLogin()

    fun registerDeviceForUser(name: CharSequence?, password:CharSequence?){
        if(name.isNullOrBlank()){
            mRegistrationStatus.postValue(RegistrationStatus.LOGIN_INVALID_NAME)
            return
        }

        if(password.isNullOrBlank()){
            mRegistrationStatus.postValue(RegistrationStatus.LOGIN_INVALID_PASSWORD)
            return
        }

        mRegistrationStatus.postValue(RegistrationStatus.LOGGING)
        viewModelScope.launch(Dispatchers.IO) {
            when(val loginResult = loginManager.login(name.toString(),password.toString())){
                is LoginResult.Success -> {
                    registerDevice(loginResult.data)
                }
                is LoginResult.AccessForbidden -> mRegistrationStatus.postValue(RegistrationStatus.LOGIN_FORBIDDEN)
                is LoginResult.InvalidParam -> mRegistrationStatus.postValue(RegistrationStatus.LOGIN_FORBIDDEN)
                else -> mRegistrationStatus.postValue(RegistrationStatus.UNKNOWN)
            }
        }
    }

    private suspend fun registerDevice(authData: UserManager.AuthData) {
        val id = deviceId.value
        val name = friendlyName.value
        if(id== null || name == null){
            mRegistrationStatus.postValue(RegistrationStatus.DEVICE_INVALID_PARAM)
            return
        }

        val deviceManager = loginManager.getDeviceManager(authData)
        if(deviceManager == null){
            mRegistrationStatus.postValue(RegistrationStatus.LOGIN_FORBIDDEN)
            return
        }
        mRegistrationStatus.postValue(RegistrationStatus.ADDING_DEVICE)
        val result = deviceManager.registerDevice(id,name)
        when(result) {
            is RegistrationResult.Success -> {
                registeredDeviceDao.add(result.device)
                mConnectionString.postValue(result.device.connectionString)
                mRegistrationStatus.postValue(RegistrationStatus.SUCCESS)
            }
            else -> {
                mRegistrationStatus.postValue(RegistrationStatus.LOGIN_FORBIDDEN)
            }
        }

    }

    class Factory(private val deviceDao: RegisteredDeviceDao) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ConfigurationViewModel(deviceDao) as T
        }
    }

}