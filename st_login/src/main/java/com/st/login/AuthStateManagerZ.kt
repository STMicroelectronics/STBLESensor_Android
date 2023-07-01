/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AuthStateManagerZ(context: Context, mStoreName: String) {

    private val mPrefsReader: SharedPreferences =
        context.getSharedPreferences(mStoreName, Context.MODE_PRIVATE)
    private val mPrefsWriter: SharedPreferences.Editor =
        mPrefsReader.edit()

    fun readState(): AuthZ {
        return AuthZ(
            mPrefsReader.getString("accessKeyZ", ""),
            mPrefsReader.getString("secretKeyZ", ""),
            mPrefsReader.getString("sessionTokenZ", ""),
            mPrefsReader.getLong("expirationZ", 0)
        )
    }

    fun writeState(response: JSONObject): AuthZ {
        val credentials = response.getJSONObject("Credentials")
        val accessKey = credentials.getString("AccessKeyId")
        val secretKey = credentials.getString("SecretKey")
        val sessionToken = credentials.getString("SessionToken")
        val expiration = credentials.getString("Expiration")

        val expirationTimestamp = toMillis(expiration)

        mPrefsWriter.putString("accessKeyZ", accessKey)
        mPrefsWriter.putString("secretKeyZ", secretKey)
        mPrefsWriter.putString("sessionTokenZ", sessionToken)
        mPrefsWriter.putLong("expirationZ", expirationTimestamp)
        mPrefsWriter.apply()

        return AuthZ(
            mPrefsReader.getString("accessKeyZ", ""),
            mPrefsReader.getString("secretKeyZ", ""),
            mPrefsReader.getString("sessionTokenZ", ""),
            mPrefsReader.getLong("expirationZ", 0)
        )
    }

    private fun toMillis(dateStr: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        val date = sdf.parse(dateStr) as Date
        return date.time
    }
}

class AuthZ(
    val accessKey: String?,
    val secretKey: String?,
    val sessionToken: String?,
    val expiration: Long?
)
