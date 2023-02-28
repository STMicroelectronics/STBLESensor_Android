package com.st.blesensor.cloud.stazure.communication

internal sealed class LoginResult{
    data class Success(val data:UserManager.AuthData):LoginResult()
    object AccessForbidden:LoginResult()
    data class InvalidParam(val title:String,val message:String):LoginResult()
    data class CommunicationError(val error:Throwable):LoginResult()
}

internal interface UserManager{

    interface AuthData{}

    suspend fun login(name:String,password:String):LoginResult

    fun getDeviceManager(authData: AuthData):DeviceManager?

}