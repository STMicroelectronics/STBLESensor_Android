/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login

import androidx.lifecycle.*
import com.st.login.loginprovider.ILoginProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivityViewModel(private val loginProvider: ILoginProvider) : ViewModel() {

    /**
     * initial state -> Showing Login Page
     * end state -> Login completed.
     */
    sealed class Destination {
        /**
         * require the user login
         */
        object LoginPage : Destination()

        /**
         * failed the user authentication
         */
        object LoginFailed : Destination()

        /**
         * login completed
         */
        data class LoginCompleted(val data: ILoginProvider.AuthData) : Destination()
    }

    /**
     * possible states during Login
     */
    sealed class LoginStatus {
        /**
         * login completed, data is the data returned by [com.st.login.loginprovider.LoginProvider#signInUser]
         * interfaces that need the authentication knows what to do with that data...
         */
        data class Success(val data: ILoginProvider.AuthData) : LoginStatus()

        /**
         * Auth_N ok
         */
        data class SuccessAuthN(val data: ILoginProvider.AuthData) : LoginStatus()

        /**
         * communication error
         */
        object Error : LoginStatus()

        /**
         * requesting the login
         */
        object Logging : LoginStatus()

        /**
         * initial state
         */
        object Unknown : LoginStatus()
    }

    private val _currentView = MutableLiveData<Destination>(Destination.LoginPage)

    /**
     * view to display
     */
    val currentView: LiveData<Destination>
        get() = _currentView

    private val mLoginStatus = MutableLiveData<LoginStatus>(LoginStatus.Unknown)
    val loginStatus: LiveData<LoginStatus>
        get() = mLoginStatus

    /*fun initializeAuth(loginProviderType: LoginProviderFactory.LoginProviderType) {
        loginProvider.initAppAuth()
    }*/

    fun startAuthorizationFlow() {
        loginProvider.startAppAuthFlow()
    }

    fun signInUser() {
        mLoginStatus.postValue(LoginStatus.Logging)

        viewModelScope.launch(Dispatchers.IO) {
            when (val loginResult = loginProvider.login()) {
                is LoginAuthNSuccess ->
                    mLoginStatus.postValue(LoginStatus.SuccessAuthN(loginResult.data))
                is LoginSuccess -> {
                    mLoginStatus.postValue(LoginStatus.Success(loginResult.data))
                    mAuthProcess.postValue(AuthDataLoading.Loaded(loginResult.data))
                    _currentView.postValue(Destination.LoginCompleted(loginResult.data))
                }
                is LoginError ->
                    mLoginStatus.postValue(LoginStatus.Error)
                else ->
                    mLoginStatus.postValue(LoginStatus.Error)
            }
        }
    }

    fun setLoggingState() {
        mLoginStatus.postValue(LoginStatus.Logging)
    }

    fun setErrorState() {
        _currentView.postValue(Destination.LoginFailed)
    }

    class Factory(private val loginManager: ILoginProvider) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginActivityViewModel(loginManager) as T
        }
    }
}
