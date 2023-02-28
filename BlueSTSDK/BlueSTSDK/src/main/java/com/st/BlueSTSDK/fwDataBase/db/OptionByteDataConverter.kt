package com.st.BlueSTSDK.fwDataBase.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.st.BlueSTSDK.fwDataBase.db.OptionByte
import java.lang.reflect.Type

class OptionByteDataConverter {
    @TypeConverter
    fun fromOptionByte(optBytes: List<OptionByte?>?): String? {
        if (optBytes == null) {
            return null
        }
        val gson = Gson()
        val type: Type =
            object : TypeToken<List<OptionByte?>?>() {}.type
        return gson.toJson(optBytes, type)
    }

    @TypeConverter
    fun toOptionByte(optBytesString: String?): List<OptionByte>? {
        if (optBytesString == null) {
            return null
        }
        val gson = Gson()
        val type: Type =
            object : TypeToken<List<OptionByte?>?>() {}.type
        return gson.fromJson(optBytesString, type)
    }
}