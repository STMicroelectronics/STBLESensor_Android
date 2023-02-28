package com.st.BlueSTSDK.Features.JsonNFCFeature

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
class JsonNFCParser {

    companion object {

        private val gsonEncDec = GsonBuilder()
            .create()

        @JvmStatic
        fun getJsonObj(rawData: ByteArray): JsonObject? {
            val commandString = rawData.toString(Charsets.UTF_8).dropLast(1)
            return try {
                gsonEncDec.fromJson(commandString, JsonObject::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e("JsonNFCParser", "error parsing the response: $e")
                Log.e("JsonNFCParser", commandString)
                null
            }
        }

        @JvmStatic
        fun extractAnswer(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("Answer")) {
                return try {
                    gsonEncDec.fromJson(
                        obj,
                        JsonReadModes::class.java
                    ).Answer
                } catch (e: JsonSyntaxException) {
                    Log.e("JsonNFCParser", "error parsing the Commands: $e")
                    Log.e("JsonNFCParser", obj.toString())
                    null
                }
            }
            return null
        }
    }
}