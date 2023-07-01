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
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.MainThread
import com.st.login.AuthData
import com.st.login.AuthStateManager
import com.st.login.Configuration
import com.st.login.LoginError
import com.st.login.LoginResult
import com.st.login.LoginSuccess
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationService.TokenResponseCallback
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import net.openid.appauth.browser.BrowserDenyList
import net.openid.appauth.browser.Browsers
import net.openid.appauth.browser.VersionRange
import net.openid.appauth.browser.VersionedBrowserMatcher
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


open class KeycloakLoginProvider(
    private val activity: Activity,
    private val ctx: Context,
    private val configuration: Configuration
) : ILoginProvider {

    companion object {
        private const val TAG = "Login_Provider"
        private const val RC_AUTH = 100
        private const val LOGIN_PROVIDER_TAG = "KEYCLOAK_LOGIN_PROVIDER"
    }

    lateinit var mAuthService: AuthorizationService
    private lateinit var authorizationResponse: AuthorizationResponse

    // These may be hidden from children classes
    protected lateinit var mAuthStateManager: AuthStateManager
    protected lateinit var mConfiguration: Configuration

    // This may be removed when not needed from CognitoLoginProvider
    protected lateinit var mExecutor: ExecutorService

    private var mAuthRequest: AuthorizationRequest? = null
    private val authConfigToken = "TokenCollection"

    protected val tokenEditor: SharedPreferences.Editor = ctx.getSharedPreferences(
        authConfigToken,
        Context.MODE_PRIVATE
    ).edit()

    init {
        initAppAuth()
    }

    /**
     * AppAuth initialization
     */
    final override fun initAppAuth() {
        // TO REMOVE
        mExecutor = Executors.newSingleThreadExecutor()

        Log.i(TAG, "Initializing AppAuth")

        mConfiguration = configuration
        mConfiguration.acceptConfiguration()

        mAuthStateManager = AuthStateManager.getInstance(ctx, getLoginProviderTag())

        val serviceConfig = AuthorizationServiceConfiguration(
            mConfiguration.authEndpointUri!!,
            mConfiguration.tokenEndpointUri!!,
            mConfiguration.registrationEndpointUri
        )

        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            mConfiguration.clientId!!,
            ResponseTypeValues.CODE,
            mConfiguration.redirectUri
        )
            .setScope(mConfiguration.scope)
            .setAdditionalParameters(mapOf("identity_provider" to mConfiguration.identityProvider))

        mAuthRequest = authRequestBuilder.build()

        Log.i(TAG, "Creating Authorization Service")
        val builder = AppAuthConfiguration.Builder()
        builder.setBrowserMatcher(
            BrowserDenyList(
                VersionedBrowserMatcher(
                    Browsers.SBrowser.PACKAGE_NAME,
                    Browsers.SBrowser.SIGNATURE_SET,
                    true,  // when this browser is used via a custom tab
                    VersionRange.atMost("5.3")
                )
            )
        )
        builder.setConnectionBuilder(mConfiguration.connectionBuilder)
        mAuthService = AuthorizationService(ctx, builder.build())

    }

    override fun getLoginProviderTag(): String {
        return LOGIN_PROVIDER_TAG
    }

    override fun isLogged(): Boolean {
        return mAuthStateManager.current.isAuthorized
    }

    override suspend fun getAuthData(): AuthData? {

        if (needsAccessTokenRefresh()) {
            val refreshed = refreshAuthState()
            if (!refreshed) {
                return null
            }
        }

        val authState = mAuthStateManager.current
        return AuthData(
            authState.accessToken!!,
            "SecretKey",
            authState.idToken!!,
            authState.accessTokenExpirationTime.toString()
        )

    }

    private fun needsAccessTokenRefresh(): Boolean {
        val expiresAt: Long = mAuthStateManager.current.accessTokenExpirationTime ?: return true
        return expiresAt <= System.currentTimeMillis()
    }

    private suspend fun refreshAuthState(): Boolean {

        //val expRefreshTime = extractTokenRefreshExpirationTime(mAuthStateManager.current.refreshToken!!)
        //if (expRefreshTime > System.currentTimeMillis()) {

        suspendCoroutine<Unit> { continuation ->
            val tokenRefreshRequest = mAuthStateManager.current.createTokenRefreshRequest()
            val clientAuthentication = mAuthStateManager.current.clientAuthentication
            mAuthService.performTokenRequest(tokenRefreshRequest, clientAuthentication)
            { tokenResponse, authException ->
                mAuthStateManager.updateAfterTokenResponse(tokenResponse, authException)
                continuation.resume(Unit)
            }
        }

        return if (mAuthStateManager.current.accessToken != null) {
            Log.i(TAG, "[ ACCESS TOKEN REFRESHED ] ${mAuthStateManager.current.accessToken}")
            true
        } else {
            Log.i(TAG, "[ REFRESH TOKEN EXPIRED ]")
            false
        }
    }

    override fun startAppAuthFlow() {
        val authService = AuthorizationService(ctx)

        if (mAuthRequest == null) {
            initAppAuth()
        }

        val intent = authService.getAuthorizationRequestIntent(
            mAuthRequest!!
        )
        activity.startActivityForResult(intent, RC_AUTH)
    }

    override suspend fun login(): LoginResult {

        mAuthRequest = null

        var tokResponse: TokenResponse? = null
        var autException: AuthorizationException? = null

        suspendCoroutine<Unit> { result ->

            performTokenRequest(
                authorizationResponse.createTokenExchangeRequest(),
                TokenResponseCallback { tokenResponse: TokenResponse?,
                                        authException: AuthorizationException? ->
                    tokResponse = tokenResponse
                    autException = authException

                    result.resume(Unit)
                }
            )

        }

        return handleCodeExchangeResponse(
            tokResponse,
            autException
        )

    }


    @MainThread
    protected fun performTokenRequest(
        request: TokenRequest,
        callback: TokenResponseCallback
    ) {

        val clientAuthentication: ClientAuthentication = try {
            mAuthStateManager.current.clientAuthentication
        } catch (ex: ClientAuthentication.UnsupportedAuthenticationMethod) {
            Log.d(
                TAG, "Token request cannot be made, client authentication for the token "
                        + "endpoint could not be constructed (%s)", ex
            )
            return
        }
        mAuthService.performTokenRequest(
            request,
            clientAuthentication,
            callback
        )
    }

    @MainThread
    protected open suspend fun handleCodeExchangeResponse(
        tokenResponse: TokenResponse?,
        authException: AuthorizationException?
    ): LoginResult {

        mAuthStateManager.updateAfterTokenResponse(tokenResponse, authException)

        if (tokenResponse != null) {

            Log.i(TAG, "Auth_N [idToken] -> ${tokenResponse.idToken}")
            Log.i(TAG, "Auth_N [AccessToken] -> ${tokenResponse.accessToken}")
            Log.i(TAG, "Auth_N [RefreshToken] -> ${tokenResponse.refreshToken}")

        }

        if (authException != null) {
            return LoginError(authException)
        }

        return onAuthNExchangeCompleted(tokenResponse)
    }

    override suspend fun onAuthNExchangeCompleted(tokenResponse: TokenResponse?): LoginResult {
        return LoginSuccess(getAuthData()!!)
    }

    override suspend fun logout() {
        Log.i(TAG, "logout Not yet implemented")
    }

    override fun onCodeExchangeResult(data: Intent) {
        val response = AuthorizationResponse.fromIntent(data)
        val ex = AuthorizationException.fromIntent(data)

        if (response != null) {
            authorizationResponse = response
        }

        if (response != null || ex != null) {
            mAuthStateManager.updateAfterAuthorization(response, ex)
        }

        if (response?.authorizationCode != null) {
            // authorization code exchange is required
            mAuthStateManager.updateAfterAuthorization(response, ex)
        }
    }

    /**
     * Function that extract Expiration Time of Refresh Token
     */
    private fun extractTokenRefreshExpirationTime(refreshToken: String): Long {

        var expirationTime: Long = System.currentTimeMillis()

        val splittedToken = refreshToken.split(".")
        val payload = splittedToken[1]

        val bytes: ByteArray = Base64.getUrlDecoder().decode(payload)

        val tokenPayloadDecoded = String(bytes, StandardCharsets.UTF_8)
        var tokenPayloadAsJson = JSONObject()
        try {
            tokenPayloadAsJson = JSONObject(tokenPayloadDecoded)
        } catch (t: Throwable) {
            Log.e(
                TAG,
                "Token N Payload as JSON - Could not parse malformed JSON: \"$tokenPayloadDecoded\""
            )
        }

        if (tokenPayloadAsJson.has("exp")) {
            var expirationTimeStr = tokenPayloadAsJson.get("exp").toString()
            expirationTimeStr += "000"
            expirationTime = expirationTimeStr.toLong()
        }

        return expirationTime
    }

}
