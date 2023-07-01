/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login.loginprovider

import android.content.Intent
import com.st.login.LoginResult
import net.openid.appauth.TokenResponse

/**
 * Interface for a service that permit to register and login a new user
 */
interface ILoginProvider {
    /** interface used to data produced by a login*/
    interface AuthData {
        val accessKey: String
        val secretKey: String
        val token: String
        val expiration: String
    }

    /**
     * user authentication (user login) by his MySt.com Account
     */
    suspend fun login(): LoginResult

    /**
     * Logout user
     */
    suspend fun logout()

    /**
     * if available it return the last login data, otherwise it will return null.
     */
    fun isLogged(): Boolean

    /**
     * Function that return Authentication Tokens after user authentication
     */
    suspend fun getAuthData(): com.st.login.AuthData?

    /**
     * Function triggered after token N retrieved
     */
    suspend fun onAuthNExchangeCompleted(tokenResponse: TokenResponse?): LoginResult

    /**
     * Initialize AppAuth
     */
    fun initAppAuth()

    /**
     * Start AppAuth Flow
     */
    fun startAppAuthFlow()

    /**
     * Function called after code exchanged
     * TODO: Add package level visibility modifier
     */
    fun onCodeExchangeResult(data: Intent)

    /**
     * Function that return Login Provider Type
     */
    fun getLoginProviderTag(): String
}