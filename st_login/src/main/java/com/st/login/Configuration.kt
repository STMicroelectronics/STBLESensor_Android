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
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import net.openid.appauth.connectivity.ConnectionBuilder
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import okio.Buffer
import okio.buffer
import okio.source
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

/**
 * Reads and validates the app configuration from `res/raw/auth_config.json`. Configuration
 * changes are detected by comparing the hash of the last known configuration to the read
 * configuration. When a configuration change is detected, the app state is reset.
 */
class Configuration(mContext: Context, private val configurationJSON: Int, val redirectUri: Uri) {
    private val packageName: String = mContext.packageName
    private val packageManager: PackageManager = mContext.packageManager
    private val mPrefs: SharedPreferences =
        mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mResources: Resources = mContext.resources
    private var mConfigJson: JSONObject? = null
    private var mConfigHash: String? = null

    /**
     * Returns a description of the configuration error, if the configuration is invalid.
     */
    var configurationError: String? = null
    var clientId: String? = null
        private set
    private var mScope: String? = null
    var discoveryUri: Uri? = null
        private set
    var authEndpointUri: Uri? = null
        private set
    var tokenEndpointUri: Uri? = null
        private set
    var registrationEndpointUri: Uri? = null
        private set
    var endSessionEndpoint: Uri? = null
        private set
    var userInfoEndpointUri: Uri? = null
        private set
    var isHttpsRequired = false
        private set
    var identityProvider: String? = null
        private set
    var apiGateway: Uri? = null
        private set
    var webDshEndpoint: Uri? = null
        private set
    var webDshLogout: Uri? = null
        private set

    /**
     * Indicates whether the configuration has changed from the last known valid state.
     */
    fun hasConfigurationChanged(): Boolean {
        val lastHash = lastKnownConfigHash
        return mConfigHash != lastHash
    }

    /**
     * Indicates whether the current configuration is valid.
     */
    val isValid: Boolean
        get() = configurationError == null

    /**
     * Indicates that the current configuration should be accepted as the "last known valid"
     * configuration.
     */
    fun acceptConfiguration() {
        mPrefs.edit().putString(KEY_LAST_HASH, mConfigHash).apply()
    }

    val scope: String
        get() = mScope!!

    val connectionBuilder: ConnectionBuilder
        get() = DefaultConnectionBuilder.INSTANCE

    private val lastKnownConfigHash: String?
        get() = mPrefs.getString(KEY_LAST_HASH, null)

    @Throws(InvalidConfigurationException::class)
    private fun readConfiguration() {
        val configSource = mResources.openRawResource(configurationJSON).source().buffer()
        val configData = Buffer()
        mConfigJson = try {
            configSource.readAll(configData)
            JSONObject(configData.readString(Charset.forName("UTF-8")))
        } catch (ex: IOException) {
            throw InvalidConfigurationException(
                "Failed to read configuration: " + ex.message
            )
        } catch (ex: JSONException) {
            throw InvalidConfigurationException(
                "Unable to parse configuration: " + ex.message
            )
        }
        mConfigHash = configData.sha256().base64()
        clientId = getConfigString("client_id")
        mScope = getRequiredConfigString("authorization_scope")
        endSessionEndpoint = getRequiredConfigWebUri("end_session")
//        mRedirectUri = getRequiredConfigUri("redirect_uri")
        if (!isRedirectUriRegistered) {
            throw InvalidConfigurationException(
                "redirect_uri is not handled by any activity in this app! "
                        + "Ensure that the appAuthRedirectScheme in your build.gradle file "
                        + "is correctly configured, or that an appropriate intent filter "
                        + "exists in your app manifest."
            )
        }
        if (getConfigString("discovery_uri") == null) {
            authEndpointUri = getRequiredConfigWebUri("authorization_endpoint_uri")
            tokenEndpointUri = getRequiredConfigWebUri("token_endpoint_uri")
            userInfoEndpointUri = getRequiredConfigWebUri("user_info_endpoint_uri")
            if (clientId == null) {
                registrationEndpointUri = getRequiredConfigWebUri("registration_endpoint_uri")
            }
        } else {
            discoveryUri = getRequiredConfigWebUri("discovery_uri")
        }
        isHttpsRequired = mConfigJson!!.optBoolean("https_required", true)
        identityProvider = getConfigString("identity_provider")
        apiGateway = getRequiredConfigWebUri("api_gateway")
        webDshEndpoint = getRequiredConfigWebUri("web_dsh_endpoint")
        webDshLogout = getRequiredConfigWebUri("web_dsh_logout")
    }

    fun getConfigString(propName: String?): String? {
        var value = mConfigJson!!.optString(propName) ?: return null
        value = value.trim { it <= ' ' }
        return if (TextUtils.isEmpty(value)) {
            null
        } else {
            value
        }
    }

    @Throws(InvalidConfigurationException::class)
    private fun getRequiredConfigString(propName: String): String {
        return getConfigString(propName)
            ?: throw InvalidConfigurationException(
                "$propName is required but not specified in the configuration"
            )
    }

    @Throws(InvalidConfigurationException::class)
    fun getRequiredConfigUri(propName: String): Uri {
        val uriStr = getRequiredConfigString(propName)
        val uri: Uri = try {
            Uri.parse(uriStr)
        } catch (ex: Throwable) {
            throw InvalidConfigurationException("$propName could not be parsed", ex)
        }
        if (!uri.isHierarchical || !uri.isAbsolute) {
            throw InvalidConfigurationException(
                "$propName must be hierarchical and absolute"
            )
        }
        if (!TextUtils.isEmpty(uri.encodedUserInfo)) {
            throw InvalidConfigurationException("$propName must not have user info")
        }
        if (!TextUtils.isEmpty(uri.encodedQuery)) {
            throw InvalidConfigurationException("$propName must not have query parameters")
        }
        if (!TextUtils.isEmpty(uri.encodedFragment)) {
            throw InvalidConfigurationException("$propName must not have a fragment")
        }
        return uri
    }

    @Throws(InvalidConfigurationException::class)
    fun getRequiredConfigWebUri(propName: String): Uri {
        val uri = getRequiredConfigUri(propName)
        val scheme = uri.scheme
        if (TextUtils.isEmpty(scheme) || !("http" == scheme || "https" == scheme)) {
            throw InvalidConfigurationException(
                "$propName must have an http or https scheme"
            )
        }
        return uri
    }

    // ensure that the redirect URI declared in the configuration is handled by some activity
    // in the app, by querying the package manager speculatively
    private val isRedirectUriRegistered: Boolean
        @SuppressLint("QueryPermissionsNeeded")
        get() {
            // ensure that the redirect URI declared in the configuration is handled by some activity
            // in the app, by querying the package manager speculatively
            val redirectIntent = Intent()
            redirectIntent.setPackage(packageName)
            redirectIntent.action = Intent.ACTION_VIEW
            redirectIntent.addCategory(Intent.CATEGORY_BROWSABLE)
            redirectIntent.data = redirectUri
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.resolveActivity(
                    redirectIntent,
                    PackageManager.ResolveInfoFlags.of(0)
                ) != null
            } else {
                packageManager.queryIntentActivities(redirectIntent, 0).isNotEmpty()
            }
        }

    class InvalidConfigurationException : Exception {
        internal constructor(reason: String?) : super(reason) {
            /** NOOP **/
        }

        internal constructor(reason: String?, cause: Throwable?) : super(reason, cause) {
            /** NOOP **/
        }
    }

    companion object {
        private const val TAG = "Configuration"
        private const val PREFS_NAME = "config"
        private const val KEY_LAST_HASH = "lastHash"
        fun getInstance(context: Context, configurationJSON: Int, redirectUri: Uri): Configuration {
            return Configuration(context, configurationJSON, redirectUri)
        }
        /* OLD getIstanceConfiguration */

        /*private var sInstance = WeakReference<Configuration?>(null)
        fun getInstance(context: Context, configurationJSON: Int): Configuration {
            var config = sInstance.get()
            if (config == null) {
                config = Configuration(context, configurationJSON)
                sInstance = WeakReference(config)
            }
            return config
        }*/
    }

    init {
        try {
            readConfiguration()
        } catch (ex: InvalidConfigurationException) {
            configurationError = ex.message
        }
    }
}
