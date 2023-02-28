package com.st.BlueSTSDK.fwDataBase.network

import com.google.gson.GsonBuilder
import com.st.BlueSTSDK.fwDataBase.db.BoardCatalog
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

interface BoardCatalogService {

    @GET("catalog.json")
    suspend fun getFirmwares(): BoardCatalog

    @GET
    suspend fun getFirmwaresFromUrl(@Url baseUrl: String): BoardCatalog

    @GET("chksum.json")
    suspend fun getDBVersion():BoardCatalog


    companion object{

        fun buildInstance(baseUrl:String): BoardCatalogService {

            val gsonConvert = GsonBuilder()
                .setDateFormat("dd-MM-yyyy")
                .create()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gsonConvert))
                .build()
                .create(BoardCatalogService::class.java)
        }
    }
}