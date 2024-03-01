package com.st.cloud_azure_iot_central.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.st.cloud_azure_iot_central.model.CloudAPIToken
import com.st.cloud_azure_iot_central.model.CloudDevicesList
import com.st.cloud_azure_iot_central.model.CloudTemplatesList
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path

interface AzureIoTDeviceService {

    @GET("/api/devices?api-version=1.0")
    suspend fun getDevices(): CloudDevicesList

    @GET("/api/deviceTemplates?api-version=1.0")
    suspend fun getTemplates(): CloudTemplatesList

    @PUT("/api/devices/{deviceId}?api-version=1.0")
    @Headers("Content-Type: application/json")
    suspend fun addDevice(@Path("deviceId", encoded = true) deviceId: String, @Body body: RequestBody): Response<ResponseBody>

    @DELETE("/api/devices/{deviceId}?api-version=1.0")
    @Headers("Content-Type: application/json")
    suspend fun deleteDevice(@Path("deviceId", encoded = true) deviceId: String): Response<ResponseBody>

    @GET("/api/devices/{deviceId}/credentials?api-version=1.0")
    suspend fun getDeviceCredentials(@Path("deviceId", encoded = true) deviceId: String): Response<ResponseBody>

    @GET("/api/apiTokens/{tokenId}?api-version=1.0")
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


            val contentType = "application/json".toMediaType()

            val json = Json{ignoreUnknownKeys = true}

            return Retrofit.Builder()
                .baseUrl(url)
                .client(httpClient.build())
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(AzureIoTDeviceService::class.java)
        }
    }
}