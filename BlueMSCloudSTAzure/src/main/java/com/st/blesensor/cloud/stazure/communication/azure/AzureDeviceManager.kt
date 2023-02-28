package com.st.blesensor.cloud.stazure.communication.azure

import com.st.blesensor.cloud.stazure.communication.DeviceManager
import com.st.blesensor.cloud.stazure.communication.RegistrationResult
import com.st.blesensor.cloud.stazure.storage.RegisteredDevice
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class AzureDeviceManager(authData:LoginSuccessResponse ) : DeviceManager{

    private val mAzureDeviceManager:AzureDeviceManagerService

    init {
        val httpClient = OkHttpClient.Builder()
                .addInterceptor {chain ->
                    val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer "+authData.token)
                            .build()
                    return@addInterceptor chain.proceed(request)
                }.build()

        mAzureDeviceManager = Retrofit.Builder()
                .baseUrl("https://stm32ode.azurewebsites.net")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(AzureDeviceManagerService::class.java)
    }


    override suspend fun registerDevice(deviceId: String, deviceName: String): RegistrationResult {
        val response = mAzureDeviceManager.registerDevice(RegisterDeviceParameters(deviceId,deviceName))
        if(response.isSuccessful){
            val cs = response.body()?.connectionString!!
            return RegistrationResult.Success(RegisteredDevice(deviceId,deviceName,cs))
        }else{
            return RegistrationResult.AccessForbidden
        }
    }


}