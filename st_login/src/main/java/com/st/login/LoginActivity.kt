/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.st.login.LoginActivityViewModel.LoginStatus
import com.st.login.loginprovider.ILoginProvider
import com.st.login.loginprovider.LoginProviderFactory

/**
 * Activity used to sign in the user with his account
 */
class LoginActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "LoginActivity"
    }

    //private val Auth_config_token = "TokenCollection"
    private val redirectUri: Uri = Uri.parse("stblesensor://callback")
    private lateinit var pbLoadingLogin: ProgressBar
    private lateinit var webView: WebView
    private lateinit var loginLayout: NestedScrollView

    private lateinit var ivLoginLogo: ImageView
    private lateinit var btnLogin: ExtendedFloatingActionButton

    private var clickLoginButton: Boolean = false

    private var networkConnection: Boolean = false
    private val strOffline: String = "You are offline. Please check your connectivity"

    /* view model that will manage the login logic */
    private val mLoginViewModel by viewModels<LoginActivityViewModel> {
        LoginActivityViewModel.Factory(loginProvider)
    }

    private lateinit var loginProvider: ILoginProvider
    private lateinit var mProgressLayoutBar: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_login)

        /* Init Layout */
        initLayout()

        /* Init Login Provider Type */
        val loginProviderType = intent.getStringExtra("PROVIDER")
        initLoginProvider(loginProviderType!!)

        clickLoginButton = intent.getBooleanExtra("automaticClickLogin", false)

        /* Change View - View Model */
        mLoginViewModel.currentView.observe(this, Observer { destination ->
            changeView(destination)
        })

        /* Check Phone connectivity */
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            try {
                it.registerDefaultNetworkCallback(
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            /* Take action when network connection is gained */
                            networkConnection = true

                            if (clickLoginButton) {
                                btnLogin.performClick()
                            }
                        }

                        override fun onLost(network: Network) {
                            /* Take action when network connection is lost */
                            networkConnection = false
                        }
                    })
            } catch (e: NoSuchMethodError) {
                networkConnection = true
            }
        }

        /* Handle SignIn Button */
        btnLogin.setOnClickListener {
            if (networkConnection) {
                /* Start Authorization Flow App Auth */
                pbLoadingLogin.visibility = View.VISIBLE
                mLoginViewModel.startAuthorizationFlow()
            } else {
                showSnackbar(strOffline)
            }
        }

        /* Initialize AppAuth */
        //mLoginViewModel.initializeAuth(LoginProviderFactory.LoginProviderType.valueOf(loginProviderType))

    }

    private fun initLayout() {
        webView = findViewById(R.id.webview)
        pbLoadingLogin = findViewById(R.id.assetTracking_login_progressBar)
        loginLayout = findViewById(R.id.login_page_layout)
        mProgressLayoutBar = findViewById(R.id.LoginProgress)
        ivLoginLogo = findViewById(R.id.login_logo)
        btnLogin = findViewById(R.id.loginSignInButton)
        mProgressLayoutBar.visibility = View.VISIBLE
    }

    private fun initLoginProvider(loginProviderType: String) {
        var configuration: Configuration

        val provider = LoginProviderFactory.LoginProviderType.valueOf(loginProviderType)

        when (LoginProviderFactory.LoginProviderType.valueOf(loginProviderType)) {
            LoginProviderFactory.LoginProviderType.VESPUCCI_DEV -> {
                setupLoginPage(
                    provider,
                    R.drawable.ic_st,
                    R.string.sign_in_with_myst,
                    R.drawable.myst_logo_login
                )
                configuration = Configuration.getInstance(
                    applicationContext,
                    R.raw.dev_auth_config_vespucci,
                    redirectUri
                )
            }

            LoginProviderFactory.LoginProviderType.VESPUCCI_PROD -> {
                setupLoginPage(
                    provider,
                    R.drawable.ic_st,
                    R.string.sign_in_with_myst,
                    R.drawable.myst_logo_login
                )
                configuration = Configuration.getInstance(
                    applicationContext,
                    R.raw.prod_auth_config_vespucci,
                    redirectUri
                )
            }

            LoginProviderFactory.LoginProviderType.KEYCLOAK -> {
                setupLoginPage(
                    provider,
                    R.drawable.logo_keycloak,
                    R.string.sign_in_with_keycloak,
                    R.drawable.keycloak_logo_login
                )
                configuration = Configuration.getInstance(
                    applicationContext,
                    R.raw.auth_config_keycloak,
                    redirectUri
                )
            }

            LoginProviderFactory.LoginProviderType.PREDMNT -> {
                setupLoginPage(
                    provider,
                    R.drawable.predictive_maintenance_light_blue,
                    R.string.sign_in_with_myst,
                    R.drawable.myst_logo_login
                )
                configuration = Configuration.getInstance(
                    applicationContext,
                    R.raw.auth_config_predictive,
                    redirectUri
                )
            }

            else -> {
                setupLoginPage(
                    provider,
                    R.drawable.logo_atr,
                    R.string.sign_in_with_myst,
                    R.drawable.myst_logo_login
                )
                configuration = Configuration.getInstance(
                    applicationContext,
                    R.raw.auth_config_cognito,
                    redirectUri
                )
            }
        }

        loginProvider = LoginProviderFactory.getLoginProvider(
            this,
            applicationContext,
            LoginProviderFactory.LoginProviderType.valueOf(loginProviderType),
            configuration
        )

    }

    private fun setupLoginPage(
        provider: LoginProviderFactory.LoginProviderType,
        imageType: Int,
        textType: Int,
        logoProvider: Int
    ) {
        if (provider == LoginProviderFactory.LoginProviderType.COGNITO) {
            ivLoginLogo.setImageResource(imageType)
        } else {
            Glide
                .with(applicationContext)
                .load(imageType)
                .fitCenter()
                .into(ivLoginLogo)
        }
        btnLogin.text = resources.getString(textType)
        btnLogin.icon = ContextCompat.getDrawable(applicationContext, logoProvider)
    }

    private fun changeView(destinationView: LoginActivityViewModel.Destination) {

        when (destinationView) {
            is LoginActivityViewModel.Destination.LoginPage -> {
                mProgressLayoutBar.visibility = View.GONE
                //showLoginPage()
            }

            is LoginActivityViewModel.Destination.LoginCompleted -> {
                mProgressLayoutBar.visibility = View.GONE
                //Send back AuthData
                val mBundle = Bundle()
                mBundle.putString("accessKey", destinationView.data.accessKey)
                mBundle.putString("secretKey", destinationView.data.secretKey)
                mBundle.putString("token", destinationView.data.token)
                mBundle.putString("expiration", destinationView.data.expiration)

                val intent = Intent()
                intent.putExtra("AuthData", mBundle)
                setResult(RESULT_OK, intent)

                Toast.makeText(
                    this,
                    "Login COMPLETED.",
                    Toast.LENGTH_SHORT
                )
                    .show()
                //close the activity
                finish()
            }

            is LoginActivityViewModel.Destination.LoginFailed -> {
                mProgressLayoutBar.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Login FAILED.",
                    Toast.LENGTH_SHORT
                )
                    .show()
                //close the activity
                finish()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != 0) {
            loginProvider.onCodeExchangeResult(data!!)

            mLoginViewModel.signInUser()

            mLoginViewModel.loginStatus.observe(this, Observer { status ->

                if (status is LoginStatus.Logging) {
                    webView.visibility = View.GONE
                    loginLayout.visibility = View.VISIBLE
                }

                if (status is LoginStatus.SuccessAuthN) {

                    if (loginProvider.getLoginProviderTag() == "PREDMNT_LOGIN_PROVIDER") {
                        setupWarningLayout()
                    } else {

                        val url =
                            "https://d3aqorzcqycube.cloudfront.net/?id_token=${status.data.token}&access_token=${status.data.accessKey}"
                        println(url)

                        /**
                         * Theese settings enable possibility to show License Agreement; Without dashboard home will displayed
                         */
                        webView.settings.javaScriptEnabled = true
                        webView.settings.domStorageEnabled = true
                        webView.settings.allowUniversalAccessFromFileURLs = true

                        webView.loadUrl(Uri.parse(url).toString())

                        webView.webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                loginLayout.visibility = View.GONE
                                webView.visibility = View.VISIBLE
                                super.onPageFinished(view, url)
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                url: String
                            ): Boolean {
                                //if (url.startsWith(mConfiguration.webDshLogout.toString())) {
                                if (url.startsWith("https://bowl-domain.auth.eu-central-1.amazoncognito.com/logout?")) {
                                    /*Toast.makeText(applicationContext, "Intercepted", Toast.LENGTH_LONG)
                                        .show()*/
                                    webView.visibility = View.GONE
                                    loginLayout.visibility = View.VISIBLE
                                    mLoginViewModel.setLoggingState()
                                    mLoginViewModel.startAuthorizationFlow()
                                    return false
                                }
                                return true
                            }
                        }
                        webView.webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.apply {
                                    Log.d(
                                        TAG,
                                        "WEB_VIEW - ${message()} -- From line ${lineNumber()} of ${sourceId()}"
                                    )
                                }
                                return true
                            }
                        }
                    }
                }

                if (status is LoginStatus.Success) {
                    webView.visibility = View.GONE
                    loginLayout.visibility = View.VISIBLE
                    showSnackbar("Login successfully")
                }
            })
        } else {
            mLoginViewModel.setErrorState()
        }

    }

    private fun setupWarningLayout() {
        ivLoginLogo.setImageResource(R.drawable.ic_warning)

        val layoutLoading = findViewById<LinearLayout>(R.id.loading_container)
        layoutLoading.visibility = View.GONE

        val layoutError = findViewById<LinearLayout>(R.id.error_container)
        layoutError.visibility = View.VISIBLE

        findViewById<Button>(R.id.assetTrackingRetryButton).visibility = View.GONE
        btnLogin.visibility = View.GONE

        val tvError = findViewById<TextView>(R.id.error_description)
        tvError.text =
            "For the first time, please make authentication at this link: https://dsh-predmnt.st.com/Signin"
        Linkify.addLinks(tvError, Linkify.WEB_URLS)
    }

    @MainThread
    private fun showSnackbar(message: String) {
        Snackbar.make(
            window.decorView.rootView,
            message,
            Snackbar.LENGTH_SHORT
        )
            .show()
    }

}