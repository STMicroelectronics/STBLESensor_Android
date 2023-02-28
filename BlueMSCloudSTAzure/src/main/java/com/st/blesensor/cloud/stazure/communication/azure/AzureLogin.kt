package com.st.blesensor.cloud.stazure.communication.azure

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.st.blesensor.cloud.stazure.communication.DeviceManager
import com.st.blesensor.cloud.stazure.communication.LoginResult
import com.st.blesensor.cloud.stazure.communication.UserManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.IllegalArgumentException

internal class AzureLogin : UserManager{

    private val mAzureLoginService:AzureLoginService

    init {
        val gson = GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create()

        mAzureLoginService = Retrofit.Builder()
                .baseUrl("https://stm32ode.azurewebsites.net")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(AzureLoginService::class.java)
    }

    override suspend fun login(name: String, password: String): LoginResult {
        val param = LoginParameters(name,password)
        val result = mAzureLoginService.login(param)

        if(result.isSuccessful){
            val body = result.body()
                    ?: return LoginResult.CommunicationError(IllegalArgumentException("Empty response"))
            return LoginResult.Success(body)
        }else{
            val body = result.errorBody()?.string() ?: return LoginResult.CommunicationError(IllegalArgumentException("Empty response"))
            return when(result.code()){
                403 -> LoginResult.AccessForbidden
                else -> parseErrorResponse(body)
            }
        }

    }

    private fun parseErrorResponse(body: String):LoginResult {
        return try {
            val parseResponse = Gson().fromJson(body, LoginErrorResponse::class.java)
            LoginResult.InvalidParam(parseResponse.message,parseResponse.errors[0].message)
        } catch (e: JsonSyntaxException) {
            LoginResult.CommunicationError(e)
        }
    }

    override fun getDeviceManager(authData: UserManager.AuthData): DeviceManager? {
        val azureAuthData = authData as? LoginSuccessResponse ?: return null
        return AzureDeviceManager(azureAuthData)
    }


}