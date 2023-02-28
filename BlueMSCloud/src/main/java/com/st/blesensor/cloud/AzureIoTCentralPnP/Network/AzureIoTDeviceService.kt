package com.st.blesensor.cloud.AzureIoTCentralPnP.Network

import com.google.gson.GsonBuilder
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAPIToken
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.CloudDeviceCredentials
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.CloudDevicesList
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel.CloudTemplatesList
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*


interface AzureIoTDeviceService {

    @GET("/api/devices?api-version=1.0")
    suspend fun getDevices(): CloudDevicesList

    @GET("/api/deviceTemplates?api-version=1.0")
    suspend fun getTemplates(): CloudTemplatesList

    @PUT("/api/devices/{deviceId}?api-version=1.0")
    @Headers("Content-Type: application/json")
    suspend fun addDevice(@Path("deviceId", encoded = true) deviceId: String,@Body body: RequestBody): Response<ResponseBody>

    @DELETE("/api/devices/{deviceId}?api-version=1.0")
    @Headers("Content-Type: application/json")
    suspend fun deleteDevice(@Path("deviceId", encoded = true) deviceId: String): Response<ResponseBody>

    @GET("/api/devices/{deviceId}/credentials?api-version=1.0")
    suspend fun getDeviceCredentials(@Path("deviceId", encoded = true) deviceId: String): Response<ResponseBody>

    @GET ("/api/apiTokens/{tokenId}?api-version=1.0")
    suspend fun getTokenDetailsByID(@Path("tokenId", encoded = true) tokenId: String): CloudAPIToken?

    companion object{

        fun buildInstance(url: String, key: String): AzureIoTDeviceService {

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val httpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", key)
                        .build()
                    return@addInterceptor chain.proceed(request)
                }
                .addInterceptor(loggingInterceptor)


            val gsonConvert = GsonBuilder()
                .create()

            return Retrofit.Builder()
                .baseUrl(url)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create(gsonConvert))
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(AzureIoTDeviceService::class.java)
        }
    }
}