/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import com.st.login.AuthStateManager
import com.st.login.Configuration
import com.st.login.R
import com.st.login.api.StAuthData
import com.st.login.api.StLoginManager
import com.st.login.di.RedirectUri
import dagger.hilt.android.qualifiers.ApplicationContext
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import net.openid.appauth.browser.BrowserDenyList
import net.openid.appauth.browser.Browsers
import net.openid.appauth.browser.VersionRange
import net.openid.appauth.browser.VersionedBrowserMatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Singleton
class StLoginManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @RedirectUri private val redirectUri: Uri
) : StLoginManager {

    companion object {
        private const val TAG = "StLoginManager"
    }

    private val configuration = Configuration.getInstance(
        context = context,
        //configurationJSON = R.raw.dev_auth_config_vespucci,
        configurationJSON = R.raw.prod_auth_config_vespucci,
        redirectUri = redirectUri
    )
    private var endRequest: EndSessionRequest? = null
    private var authRequest: AuthorizationRequest? = null
    private var authService: AuthorizationService? = null
    private var authStateManager: AuthStateManager? = null
    private var authorizationResponse: AuthorizationResponse? = null

    init {
        initAppAuth()
    }

    override suspend fun getStAuthData(): StAuthData? {
        if (needsAccessTokenRefresh()) {
            val refreshed = refreshAuthState()
            if (!refreshed) {
                return null
            }
        }

        return authStateManager?.current?.let { authState ->
            StAuthData(
                authState.accessToken!!,
                "SecretKey",
                authState.idToken!!,
                authState.accessTokenExpirationTime.toString()
            )
        }
    }

    private fun needsAccessTokenRefresh(): Boolean {
        val expiresAt: Long = authStateManager?.current?.accessTokenExpirationTime ?: return true
        return expiresAt <= System.currentTimeMillis()
    }

    private suspend fun refreshAuthState(): Boolean {
        suspendCoroutine { continuation ->
            authStateManager?.current?.createTokenRefreshRequest()?.let { tokenRefreshRequest ->
                authStateManager?.current?.clientAuthentication?.let { clientAuthentication ->
                    authService?.performTokenRequest(tokenRefreshRequest, clientAuthentication)
                    { tokenResponse, authException ->
                        authStateManager?.updateAfterTokenResponse(tokenResponse, authException)
                        continuation.resume(Unit)
                    }
                }
            }
        }

        return if (authStateManager?.current?.accessToken != null) {
            Log.i(TAG, "[ ACCESS TOKEN REFRESHED ] ${authStateManager?.current?.accessToken}")
            true
        } else {
            Log.i(TAG, "[ REFRESH TOKEN EXPIRED ]")
            false
        }
    }

    private fun initAppAuth() {
        configuration.acceptConfiguration()

        authStateManager = AuthStateManager.getInstance(context, TAG)

        val serviceConfig = AuthorizationServiceConfiguration(
            configuration.authEndpointUri!!,
            configuration.tokenEndpointUri!!,
            configuration.registrationEndpointUri,
            configuration.endSessionEndpoint
        )

        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            configuration.clientId!!,
            ResponseTypeValues.CODE,
            configuration.redirectUri
        )
            .setScope(configuration.scope)
            .setAdditionalParameters(mapOf("identity_provider" to configuration.identityProvider))

        authRequest = authRequestBuilder.build()

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
        builder.setConnectionBuilder(configuration.connectionBuilder)
        authService = AuthorizationService(context, builder.build())
    }

    override suspend fun login(activityResultRegistry: ActivityResultRegistry): StAuthData? {
        return if (isLoggedIn()) {
            getStAuthData()
        } else {
            startFlow(activityResultRegistry = activityResultRegistry)

            getStAuthData()
        }
    }

    private suspend fun startFlow(activityResultRegistry: ActivityResultRegistry) {
        authService = AuthorizationService(context)

        if (authRequest == null) {
            initAppAuth()
        }

        suspendCoroutine { continuation ->
            authService?.let { authService ->
                val intent = authService.getAuthorizationRequestIntent(authRequest!!)

                val launcher: ActivityResultLauncher<Unit> =
                    activityResultRegistry.register(
                        "key1",
                        object :
                            ActivityResultContract<Unit, Pair<AuthorizationResponse?, AuthorizationException?>?>() {
                            override fun createIntent(context: Context, input: Unit): Intent =
                                intent

                            override fun parseResult(
                                resultCode: Int,
                                intent: Intent?
                            ): Pair<AuthorizationResponse?, AuthorizationException?>? {
                                intent?.let { data ->
                                    if (resultCode != 0) {
                                        val response = AuthorizationResponse.fromIntent(data)
                                        val ex = AuthorizationException.fromIntent(data)

                                        return Pair(response, ex)
                                    }
                                }
                                return null
                            }
                        }
                    ) { result ->
                        result?.let {
                            val response = it.first
                            val ex = it.second

                            if (response != null) {
                                authorizationResponse = response
                            }

                            if (response != null || ex != null) {
                                authStateManager?.updateAfterAuthorization(response, ex)
                            }

                            if (response?.authorizationCode != null) {
                                authStateManager?.updateAfterAuthorization(response, ex)
                            }

                            authRequest = null

                            authorizationResponse?.createTokenExchangeRequest()
                                ?.let { tokenRequest ->
                                    performTokenRequest(tokenRequest) { tokenResponse: TokenResponse?,
                                                                        authException: AuthorizationException? ->
                                        authStateManager?.updateAfterTokenResponse(
                                            tokenResponse,
                                            authException
                                        )
                                        continuation.resume(Unit)
                                    }
                                }
                        }
                    }

                launcher.launch(Unit)
            }
        }
    }

    private fun performTokenRequest(
        request: TokenRequest,
        callback: AuthorizationService.TokenResponseCallback
    ) {
        val clientAuthentication: ClientAuthentication? = try {
            authStateManager?.current?.clientAuthentication
        } catch (ex: ClientAuthentication.UnsupportedAuthenticationMethod) {
            Log.d(
                TAG,
                "Token request cannot be made, client authentication for the token "
                        + "endpoint could not be constructed (%s)",
                ex
            )
            return
        }
        clientAuthentication?.let { clientAuth ->
            authService?.performTokenRequest(request, clientAuth, callback)
        }
    }

    override suspend fun logout(activityResultRegistry: ActivityResultRegistry) {
        suspendCoroutine { continuation ->
            authService?.let { authService ->

                authStateManager?.current?.let { authState ->
                    val serviceConfig = AuthorizationServiceConfiguration(
                        configuration.authEndpointUri!!,
                        configuration.tokenEndpointUri!!,
                        configuration.registrationEndpointUri,
                        configuration.endSessionEndpoint
                    )


                    endRequest = EndSessionRequest.Builder(serviceConfig)
                        .setAdditionalParameters(
                            mapOf(
                                "client_id" to configuration.clientId,
                                "response_type" to ResponseTypeValues.CODE,
                                "redirect_uri" to configuration.redirectUri.toString(),
                                "logout_uri" to configuration.redirectUri.toString(),
                                "identity_provider" to configuration.identityProvider
                            )
                        )
                        .setIdTokenHint(authState.idToken)
                        .build()

                    val intent = authService.getEndSessionRequestIntent(endRequest!!)

                    val launcher: ActivityResultLauncher<Unit> =
                        activityResultRegistry.register(
                            "key2",
                            object :
                                ActivityResultContract<Unit, Pair<AuthorizationResponse?, AuthorizationException?>?>() {
                                override fun createIntent(
                                    context: Context,
                                    input: Unit
                                ): Intent =
                                    intent

                                override fun parseResult(
                                    resultCode: Int,
                                    intent: Intent?
                                ): Pair<AuthorizationResponse?, AuthorizationException?>? {
                                    intent?.let { data ->
                                        if (resultCode != 0) {
                                            val response = AuthorizationResponse.fromIntent(data)
                                            val ex = AuthorizationException.fromIntent(data)

                                            return Pair(response, ex)
                                        }
                                    }
                                    return null
                                }
                            }
                        ) { result ->
                            result?.let {
                                val response = it.first
                                val ex = it.second

                                ex?.let {
                                    Log.w(TAG, it.localizedMessage)
                                }
                                val clearedState =
                                    AuthState(authState.authorizationServiceConfiguration!!)
                                if (authState.lastRegistrationResponse != null) {
                                    clearedState.update(authState.lastRegistrationResponse)
                                }
                                authStateManager?.replace(clearedState)

                                continuation.resume(Unit)
                            }
                        }

                    launcher.launch(Unit)
                }
            }
        }
    }

    override fun isLoggedIn(): Boolean =
        authStateManager?.current?.isAuthorized ?: false
}
