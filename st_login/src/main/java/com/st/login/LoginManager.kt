/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import com.st.login.loginprovider.CognitoLoginProvider
import com.st.login.loginprovider.LoginProviderFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginManager(
    activity: Activity,
    private val redirectUri: Uri = Uri.parse("stblesensor://callback"),
    private val activityResultRegistry: ActivityResultRegistry,
    private val loginProviderType: LoginProviderFactory.LoginProviderType = LoginProviderFactory.LoginProviderType.VESPUCCI_DEV
    ) : ILoginManager {

    private val ctx: Context = activity.applicationContext
    private val configuration: Configuration = when (loginProviderType) {
        LoginProviderFactory.LoginProviderType.VESPUCCI_DEV ->
            Configuration.getInstance(
                context = ctx,
                configurationJSON = R.raw.dev_auth_config_vespucci,
                redirectUri = redirectUri
            )

        LoginProviderFactory.LoginProviderType.VESPUCCI_PROD ->
            Configuration.getInstance(
                context = ctx,
                configurationJSON = R.raw.prod_auth_config_vespucci,
                redirectUri = redirectUri
            )

        LoginProviderFactory.LoginProviderType.COGNITO ->
            Configuration.getInstance(
                context = ctx,
                configurationJSON = R.raw.auth_config_cognito,
                redirectUri = redirectUri
            )

        LoginProviderFactory.LoginProviderType.KEYCLOAK ->
            Configuration.getInstance(
                context = ctx,
                configurationJSON = R.raw.auth_config_keycloak,
                redirectUri = redirectUri
            )

        LoginProviderFactory.LoginProviderType.PREDMNT ->
            Configuration.getInstance(
                context = ctx,
                configurationJSON = R.raw.auth_config_predictive,
                redirectUri = redirectUri
            )
    }
    private val loginProvider =
        LoginProviderFactory.getLoginProvider(
            activity = activity,
            ctx = ctx,
            loginProviderType = loginProviderType,
            configuration = configuration
        )

    private var auth: AuthData? = null

    fun getAtrAuthData(callback: (AuthData?) -> Unit) {
        if (loginProvider.isLogged()) {
            (loginProvider as CognitoLoginProvider).getAtrAuthData { authData ->
                if (authData != null) {
                    callback(authData)
                }
            }
        }
    }

    override suspend fun login(): AuthData? =
        if (loginProvider.isLogged()) {
            // doLogin(autoClickButton = true)
            loginProvider.getAuthData()
        } else {
            doLogin(autoClickButton = false)
        }

    override suspend fun forceFreshLogin(): AuthData? =
        doLogin(autoClickButton = false)

    override suspend fun logout() {
        loginProvider.logout()
    }

    override suspend fun isLogged(): Boolean =
        loginProvider.isLogged()

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun doLogin(autoClickButton: Boolean): AuthData? {
        val def = CompletableDeferred<AuthData?>()

        val job = GlobalScope.launch {
            withContext(coroutineContext) {
                val loginActivityLauncher: ActivityResultLauncher<String> =
                    activityResultRegistry.register(
                        "key",
                        LoginActivityResultContract(automaticLoginButtonClick = autoClickButton)
                    ) { result ->
                        if (result != null) {
                            auth = result
                            def.complete(auth!!)
                        } else {
                            def.complete(null)
                        }
                    }
                loginActivityLauncher.launch(loginProviderType.toString())
                def.await()
            }
        }
        job.join()

        return auth
    }
}
