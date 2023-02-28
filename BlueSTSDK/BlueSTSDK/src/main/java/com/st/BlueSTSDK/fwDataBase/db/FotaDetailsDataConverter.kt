package com.st.BlueSTSDK.fwDataBase.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class FotaDetailsDataConverter {
    @TypeConverter
    fun fromFotaDetails(fotaDetails: FotaDetails?): String? {
        if (fotaDetails == null) {
            return null
        }
        val gson = Gson()
        val type: Type =
            object : TypeToken<FotaDetails?>() {}.type
        return gson.toJson(fotaDetails, type)
    }

    @TypeConverter
    fun toFotaDetails(fotaDetailsString: String?): FotaDetails? {
        if (fotaDetailsString == null) {
            return null
        }
        val gson = Gson()
        val type: Type =
            object : TypeToken<FotaDetails?>() {}.type
        return gson.fromJson(fotaDetailsString, type)
    }
}