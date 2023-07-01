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
import androidx.annotation.MainThread
import com.auth0.jwt.JWT
import com.st.login.AuthData
import com.st.login.AuthDataLoading
import com.st.login.AuthStateManagerZ
import com.st.login.Configuration
import com.st.login.LoginAuthNSuccess
import com.st.login.LoginError
import com.st.login.LoginLicenseAgreed
import com.st.login.LoginResult
import com.st.login.LoginSuccess
import com.st.login.mAuthProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.openid.appauth.TokenResponse
import okio.buffer
import okio.source
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class CognitoLoginProvider(
    private val activity: Activity,
    private val ctx: Context,
    private val configuration: Configuration
) : KeycloakLoginProvider(activity, ctx, configuration) {

    var mAuthStateManagerZ: AuthStateManagerZ

    companion object {
        private const val TAG = "CognitoLoginProvider"
        private const val LOGIN_PROVIDER_TAG = "COGNITO_LOGIN_PROVIDER"
    }

    init {
        mAuthStateManagerZ = AuthStateManagerZ(ctx, getLoginProviderTag())
    }

    override fun getLoginProviderTag(): String {
        return LOGIN_PROVIDER_TAG
    }

    override suspend fun getAuthData(): AuthData? {
        var authN: AuthData? = null
        var authZData: AuthData? = null

        /* we can runBlocking because this will run in a background thread */
        runBlocking {

            suspendCoroutine<Unit> { continuation ->

                val jwt = JWT()
                Log.d(
                    TAG,
                    "AUTH N NEEDS_REFRESH? - " + mAuthStateManager.current.needsTokenRefresh.toString()
                )

                mAuthStateManager.current.performActionWithFreshTokens(mAuthService) { accessToken, idToken, ex ->
                    if (ex != null) {
                        mAuthProcess.postValue(AuthDataLoading.UnknownError)
                    } else {
                        val jwtAccessToken = jwt.decodeJwt(accessToken)

                        val exp = jwtAccessToken.expiresAt.time

                        authN = AuthData(
                            accessToken!!,
                            "SecretKey",
                            idToken!!,
                            exp.toString()
                        )

                        Log.d(TAG, "AUTH N EXPIRATION - " + authN!!.expiration)
                        val authZ = mAuthStateManagerZ.readState()

                        if (authZ != null) {

                            val timestampToday = getTodayTime()

                            if (authZ.expiration!! > timestampToday) {
                                //registerNotificationService()
                                authZData = AuthData(
                                    authZ.accessKey!!,
                                    authZ.secretKey!!,
                                    authZ.sessionToken!!,
                                    authZ.expiration.toString()
                                )

                                mAuthProcess.postValue(AuthDataLoading.Loaded(authZData!!))

                            } else {

                                val refreshedAuthZToken = refreshAuthZToken(
                                    authN!!.token,
                                    authN!!.accessKey
                                ) //AuthZ expired - refresh it
                                if (refreshedAuthZToken != null) {
                                    authZData = AuthData(
                                        refreshedAuthZToken.accessKey,
                                        refreshedAuthZToken.secretKey,
                                        refreshedAuthZToken.token,
                                        refreshedAuthZToken.expiration
                                    )

                                    mAuthProcess.postValue(AuthDataLoading.Loaded(authZData!!))
                                }
                            }
                        } else {
                            mAuthProcess.postValue(AuthDataLoading.UnknownError)
                        }
                    }
                }
                continuation.resume(Unit)
            }
        }
        return authZData
    }

    fun getAtrAuthData(callback: (AuthData?) -> Unit) {
        var authN: AuthData?
        var authZData: AuthData?

        /* we can runBlocking because this will run in a background thread */
        runBlocking {
            val jwt = JWT()
            Log.d(
                TAG,
                "AUTH N NEEDS_REFRESH? " + mAuthStateManager.current.needsTokenRefresh.toString()
            )

            mAuthStateManager.current.performActionWithFreshTokens(mAuthService) { accessToken, idToken, ex ->
                if (ex != null) {
                    callback(null)
                } else {
                    val jwtAccessToken = jwt.decodeJwt(accessToken)

                    val exp = jwtAccessToken.expiresAt.time

                    authN = AuthData(
                        accessToken!!,
                        "SecretKey",
                        idToken!!,
                        exp.toString()
                    )

                    Log.d(TAG, "AUTH N EXPIRATION - " + authN!!.expiration)
                    val authZ = mAuthStateManagerZ.readState()

                    if (authZ != null) {

                        val timestampToday = getTodayTime()

                        if (authZ.expiration!! > timestampToday) {
                            //registerNotificationService()
                            authZData = AuthData(
                                authZ.accessKey!!,
                                authZ.secretKey!!,
                                authZ.sessionToken!!,
                                authZ.expiration.toString()
                            )
                            callback(authZData)

                        } else {

                            val refreshedAuthZToken = refreshAuthZToken(
                                authN!!.token,
                                authN!!.accessKey
                            ) //AuthZ expired - refresh it
                            if (refreshedAuthZToken != null) {
                                authZData = AuthData(
                                    refreshedAuthZToken.accessKey,
                                    refreshedAuthZToken.secretKey,
                                    refreshedAuthZToken.token,
                                    refreshedAuthZToken.expiration
                                )
                                callback(authZData)
                            }
                        }
                    } else {
                        callback(null)
                    }
                }
            }
        }
    }

    /*private fun registerNotificationService() = runBlocking(Dispatchers.IO){
        val token = CloudMessageReceiver.getNotificationToken(ctx) ?: return@runBlocking
        val tokenRegistration = TokenRegistrationUtility(ctx)
        tokenRegistration.registerWithSNS(token)
    }*/

    override suspend fun onAuthNExchangeCompleted(tokenResponse: TokenResponse?): LoginResult {
        return when (checkLicenseAgreement(tokenResponse!!.idToken!!)) {
            is LoginAuthNSuccess ->
                return LoginAuthNSuccess(
                    AuthData(
                        mAuthStateManager.current.accessToken!!,
                        "SecretKey",
                        mAuthStateManager.current.idToken!!,
                        mAuthStateManager.current.accessTokenExpirationTime.toString()
                    )
                )
            else -> {
                fetchAuthzToken(tokenResponse.idToken!!, tokenResponse.accessToken)
            }
        }
    }

    private fun refreshAuthZToken(idToken: String?, accessToken: String?): AuthData? {
        try {
            var response = ""
            runBlocking {
                val job: Job = launch(context = Dispatchers.Default) {
                    val authzTokenEndpoint = getAuthZTokenURL()
                    response = getAuthZRequest(
                        authzTokenEndpoint,
                        idToken,
                        accessToken
                    ).inputStream.source().buffer()
                        .readString(Charset.forName("UTF-8"))
                    response = extractAuthZToken(response)
                }
                job.join()
            }

            val authZ = mAuthStateManagerZ.writeState(JSONObject(response))

            return AuthData(
                authZ.accessKey!!,
                authZ.secretKey!!,
                authZ.sessionToken!!,
                authZ.expiration!!.toString()
            )

        } catch (urlEx: MalformedURLException) {
            Log.e(TAG, "Failed to construct user info endpoint URL", urlEx)
            return null
        } catch (ioEx: IOException) {
            Log.e(TAG, "Network error when querying authz-token endpoint", ioEx)
            return null
        } catch (jsonEx: JSONException) {
            Log.e(TAG, "Failed to parse authz-token response")
            return null
        } catch (ex: Exception) {
            Log.e(TAG, "unknown error")
            return null
        }
    }

    @MainThread
    suspend fun fetchAuthzToken(idToken: String?, accessToken: String?): LoginResult {

        try {

            var response = ""

            withContext(Dispatchers.IO) {
                val authzTokenEndpoint = getAuthZTokenURL()
                response =
                    getAuthZRequest(authzTokenEndpoint, idToken, accessToken).inputStream.source()
                        .buffer()
                        .readString(Charset.forName("UTF-8"))
                response = extractAuthZToken(response)
            }

            Log.i(TAG, response)
            val authZ = mAuthStateManagerZ.writeState(JSONObject(response))

            Log.i(TAG, "Auth_Z [idToken] -> ${authZ.sessionToken}")
            Log.i(TAG, "Auth_Z [AccessToken] -> ${authZ.accessKey}")
            Log.i(TAG, "Auth_Z [Expiration] -> ${authZ.expiration}")

            return LoginSuccess(
                AuthData(
                    authZ.accessKey!!,
                    authZ.secretKey!!,
                    authZ.sessionToken!!,
                    authZ.expiration!!.toString()
                )
            )

        } catch (urlEx: MalformedURLException) {
            Log.e(TAG, "Failed to construct user info endpoint URL", urlEx)
            return LoginError(urlEx)
        } catch (ioEx: IOException) {
            Log.e(TAG, "Network error when querying authz-token endpoint", ioEx)
            return LoginError(ioEx)
        } catch (jsonEx: JSONException) {
            Log.e(TAG, "Failed to parse authz-token response")
            return LoginError(jsonEx)
        } catch (ex: Exception) {
            Log.e(TAG, "unknown error")
            return LoginError(ex)
        }

    }

    protected open fun getAuthZTokenURL(): String {
        return mConfiguration.apiGateway.toString() + "v1/authz-token"
    }

    protected open fun getAuthZRequest(
        authzTokenEndpoint: String,
        idToken: String?,
        accessToken: String?
    ): HttpURLConnection {
        val conn = URL(authzTokenEndpoint).openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $idToken")

        return conn
    }

    protected open fun extractAuthZToken(response: String): String {
        return response
    }

    override suspend fun logout() {
        Log.i(TAG, "logout Not yet implemented")
    }

    private fun checkLicenseAgreement(idTokenN: String): LoginResult {
        val splittedToken = idTokenN.split(".")
        val payload = splittedToken[1]

        val bytes: ByteArray = Base64.getUrlDecoder().decode(payload)

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



        return if (tokenPayloadAsJson.has("custom:license_agreement")) {
            if (tokenPayloadAsJson.get("custom:license_agreement") == "1") {
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

    private fun getTodayTime(): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        val currentTime: Date = Calendar.getInstance().time
        val dateTimeTodayString = sdf.format(currentTime)
        val dateTimeToday = sdf.parse(dateTimeTodayString) as Date
        return dateTimeToday.time
    }

}

