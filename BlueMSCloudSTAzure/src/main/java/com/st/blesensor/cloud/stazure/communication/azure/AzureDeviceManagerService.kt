package com.st.blesensor.cloud.stazure.communication.azure

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

internal data class RegisterDeviceParameters(
        @SerializedName("deviceId")
        val deviceId:String,
        @SerializedName("userDeviceName")
        val deviceName:String
){

    @Expose
    @SerializedName("isGateway")
    private val isGateway=false

}

internal data class RegisterDeviceResponse(
        @SerializedName("connectionString")
        val connectionString:String
)

internal interface AzureDeviceManagerService {

    @POST("/devices?autoprovisioned=false")
    suspend fun registerDevice(@Body param:RegisterDeviceParameters):Response<RegisterDeviceResponse>

}