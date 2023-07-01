/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login

import com.st.login.loginprovider.ILoginProvider

sealed class LoginResult

/**
 * object return when the login completes, [data] contains the data needed to do authenticated
 * request to a service
 */
data class LoginSuccess(val data: AuthData) : LoginResult()

/**
 * Object return when token called auth_n is retrived
 */
data class LoginAuthNSuccess(val data: ILoginProvider.AuthData) : LoginResult()

/**
 * Object return when license agreement was accepted
 */
object LoginLicenseAgreed : LoginResult()

/**
 * Object return when an [exception] is fired during the login procedure
 */
data class LoginError(val exception: Exception) : LoginResult()

interface ILoginManager {
    /**
     * user authentication (user login) by his MySt.com Account
     */
    suspend fun login(): AuthData?

    suspend fun forceFreshLogin(): AuthData?

    suspend fun logout()

    suspend fun isLogged(): Boolean
}
