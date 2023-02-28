package com.st.BlueSTSDK.fwDataBase.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.st.BlueSTSDK.fwDataBase.db.BleCharacteristic
import java.lang.reflect.Type

class BleCharacteristicDataConverter {
    @TypeConverter
    fun fromBleCharacteristic(bleChars: List<BleCharacteristic?>?): String? {
        if (bleChars == null) {
            return null
        }
        val gson = Gson()
        val type: Type =
            object : TypeToken<List<BleCharacteristic?>?>() {}.type
        return gson.toJson(bleChars, type)
    }

    @TypeConverter
    fun toBleCharacteristic(bleCharacteristicString: String?): List<BleCharacteristic>? {
        if (bleCharacteristicString == null) {
            return null
        }
        val gson = Gson()
        val type: Type =
            object : TypeToken<List<BleCharacteristic?>?>() {}.type
        return gson.fromJson(bleCharacteristicString, type)
    }
}