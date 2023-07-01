/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login.loginprovider

import android.app.Activity
import android.content.Context
import android.util.Log
import com.st.login.AuthData
import com.st.login.Configuration
import com.st.login.LoginAuthNSuccess
import com.st.login.LoginError
import com.st.login.LoginLicenseAgreed
import com.st.login.LoginResult
import com.st.login.LoginSuccess
import net.openid.appauth.TokenResponse
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Base64

class PredMntLoginProvider(
    val activity: Activity,
    private val ctx: Context,
    private val configuration: Configuration
) : KeycloakLoginProvider(activity, ctx, configuration) {

    companion object {
        private const val TAG = "PredMntLoginProvider"
        private const val LOGIN_PROVIDER_TAG = "PREDMNT_LOGIN_PROVIDER"
    }

    override fun getLoginProviderTag(): String {
        return LOGIN_PROVIDER_TAG
    }

    override suspend fun onAuthNExchangeCompleted(tokenResponse: TokenResponse?): LoginResult {
        return when (checkLicenseAgreement(tokenResponse!!.idToken!!)) {
            is LoginLicenseAgreed ->
                LoginSuccess(
                    AuthData(
                        mAuthStateManager.current.accessToken!!,
                        "SecretKey",
                        mAuthStateManager.current.idToken!!,
                        mAuthStateManager.current.accessTokenExpirationTime.toString()
                    )
                )
            is LoginAuthNSuccess ->
                LoginAuthNSuccess(
                    AuthData(
                        mAuthStateManager.current.accessToken!!,
                        "SecretKey",
                        mAuthStateManager.current.idToken!!,
                        mAuthStateManager.current.accessTokenExpirationTime.toString()
                    )
                )
            else -> {
                LoginError(Exception("login error"))
            }
        }
    }

    private fun checkLicenseAgreement(idTokenN: String): LoginResult {
        val splittedToken = idTokenN.split(".")
        val payload = splittedToken[1]

        val bytes: ByteArray =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Base64.getUrlDecoder().decode(payload)
            } else {
                android.util.Base64.decode(payload, android.util.Base64.DEFAULT)
            }

        val tokenPayloadDecoded = String(bytes, StandardCharsets.UTF_8)
        var tokenPayloadAsJson = JSONObject()
        try {
            tokenPayloadAsJson = JSONObject(tokenPayloadDecoded)
            Log.d(TAG, "Token N Payload as JSON - " + tokenPayloadAsJson.toString())
        } catch (t: Throwable) {
            Log.e(
                TAG,
                "Token N Payload as JSON - Could not parse malformed JSON: \"$tokenPayloadDecoded\""
            )
        }

        return if (tokenPayloadAsJson.has("zoneinfo")) {
            if (tokenPayloadAsJson.get("zoneinfo") == "1") {
                tokenEditor.putString("email_N", tokenPayloadAsJson.get("email") as String?)
                LoginLicenseAgreed
            } else {
                LoginAuthNSuccess(
                    AuthData(
                        mAuthStateManager.current.accessToken!!,
                        "SecretKey",
                        mAuthStateManager.current.idToken!!,
                        mAuthStateManager.current.accessTokenExpirationTime.toString()
                    )
                )
            }
        } else {
            LoginAuthNSuccess(
                AuthData(
                    mAuthStateManager.current.accessToken!!,
                    "SecretKey",
                    mAuthStateManager.current.idToken!!,
                    mAuthStateManager.current.accessTokenExpirationTime.toString()
                )
            )
        }
    }
}
