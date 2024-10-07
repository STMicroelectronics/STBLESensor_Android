package com.st.gnss.utility

import android.webkit.JavascriptInterface
import com.st.gnss.LocationData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SetLocation(data: LocationData) {

    private val jsonLocationStr: String = Json.encodeToString(data)

    @JavascriptInterface
    fun setLocation(): String {
        return jsonLocationStr
    }
}