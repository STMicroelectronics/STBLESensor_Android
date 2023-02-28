package com.st.BlueSTSDK.fwDataBase.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class CloudAppDataConverter {
    @TypeConverter
    fun fromCloudAppData(cloudApp: List<CloudApp?>?): String? {
        if (cloudApp == null) {
            return null
        }
        val gson = Gson()
        val type: Type =
            object : TypeToken<List<CloudApp?>?>() {}.type
        return gson.toJson(cloudApp, type)
    }

    @TypeConverter
    fun toCloudAppData(cloudAppString: String?): List<CloudApp>? {
        if (cloudAppString == null) {
            return null
        }
        val gson = Gson()
        val type: Type =
            object : TypeToken<List<CloudApp?>?>() {}.type
        return gson.fromJson(cloudAppString, type)
    }
}