/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login

import androidx.lifecycle.MutableLiveData
import com.st.login.loginprovider.ILoginProvider

// internal data class AuthData(val token: String) : LoginProvider.AuthData

data class AuthData(
    override val accessKey: String,
    override val secretKey: String,
    override val token: String,
    override val expiration: String
) : ILoginProvider.AuthData

sealed class AuthDataLoading {
    object Requesting : AuthDataLoading()
    data class Loaded(val authenticationData: AuthData) : AuthDataLoading()
    object UnknownError : AuthDataLoading()
}

val mAuthProcess = MutableLiveData<AuthDataLoading>()
val authProcess: MutableLiveData<AuthDataLoading>
    get() = mAuthProcess
