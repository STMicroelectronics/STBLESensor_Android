/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.st.bluems.ui.home.HomeFragmentDirections
import com.st.core.GlobalConfig
import com.st.core.api.ApplicationAnalyticsService.ApplicationNameEtna
import com.st.demo_showcase.DemoShowCaseConfig
import com.st.terms.StTermsConfig
import com.st.terms.TermsFragmentDirections
import com.st.user_profiling.StUserProfilingConfig
import com.st.user_profiling.model.LevelProficiency
import com.st.user_profiling.model.ProfileType
import com.st.welcome.StWelcomeConfig
import com.st.welcome.WelcomeFragmentDirections
import com.st.welcome.model.WelcomePage
import dagger.hilt.android.AndroidEntryPoint
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val nfcViewModel: NFCConnectionViewModel  by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT,Color.TRANSPARENT))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)

        if(BuildConfig.DEBUG) {
            viewModel.initApplicationAnalytics(
                ApplicationNameEtna.STBLESensorDev,
                application,
                this
            )
        } else {
            viewModel.initApplicationAnalytics(
                ApplicationNameEtna.STBLESensorRel,
                application,
                this
            )
        }
//
//        //Singleton
//        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
//        val configSettings = remoteConfigSettings {
//            minimumFetchIntervalInSeconds = 3600
//        }
//        remoteConfig.setConfigSettingsAsync(configSettings)
//
//        //Set the Default Values
//        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
//
//        //Read the Default Value
//        val numberDefault = remoteConfig.getLong(LOADING_PHRASE_CONFIG_KEY)
//        Log.d(TAG,"Default number=${numberDefault}")
//        TODO: Save on GlobalConfig
//
//        remoteConfig.fetchAndActivate()
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    val updated = task.result
//                    Log.d(TAG, "Config params updated: $updated")
//                    Toast.makeText(
//                        this,
//                        "Fetch and activate succeeded",
//                        Toast.LENGTH_SHORT,
//                    ).show()
//                } else {
//                    Toast.makeText(
//                        this,
//                        "Fetch failed",
//                        Toast.LENGTH_SHORT,
//                    ).show()
//                }
//            }
//
//        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
//            override fun onUpdate(configUpdate : ConfigUpdate) {
//                Log.d(TAG, "Updated keys: " + configUpdate.updatedKeys);
//
//                if (configUpdate.updatedKeys.contains(LOADING_PHRASE_CONFIG_KEY)) {
//                    remoteConfig.activate().addOnCompleteListener {
//                        val number = remoteConfig[LOADING_PHRASE_CONFIG_KEY].asLong()
//                        Log.d(TAG,"New number=${number}")
//                        TODO: Save on GlobalConfig
//                    }
//                }
//            }
//
//            override fun onError(error : FirebaseRemoteConfigException) {
//                Log.w(TAG, "Config update error with code: " + error.code, error)
//            }
//        })

        //for using NFC deep Link node autoconnect
        val nfcIntent = intent
        val appLinkData = nfcIntent.data
        if (appLinkData != null) {

            val sPairingPin: ByteArray? = appLinkData.getQueryParameter("Pin")?.toByteArray(
                    StandardCharsets.UTF_8
                )
            nfcViewModel.setNFCPairingPin(sPairingPin)

            val mNodeTag: String? = appLinkData.getQueryParameter("Add")
            nfcViewModel.setNFCNodeId(mNodeTag)
        }

        viewModel.reportApplicationAnalytics(applicationContext)

        setContentView(R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment_content_main)

        GlobalConfig.navigateBack = { nodeId ->
            navController.navigate(HomeFragmentDirections.actionToHomeFragment())
            viewModel.disconnect(nodeId = nodeId)
        }

        setUpDemoShowCase()
        setUpTerms()
        setUpWelcome()
        setUpUserProfiling()

        when {
            viewModel.shouldShowTerms -> HomeFragmentDirections.actionHomeFragmentToTermsFragment()
            viewModel.shouldShowWelcome -> HomeFragmentDirections.actionHomeFragmentToWelcomeFragment()
            viewModel.shouldShowProfile -> HomeFragmentDirections.actionHomeFragmentToProfileNavGraph()
            else -> null
        }?.let { destination ->
            val navOptions: NavOptions = navOptions {
                popUpTo(R.id.homeFragment) { inclusive = true }
            }
            navController.navigate(directions = destination, navOptions = navOptions)
        }
    }

    private fun setUpUserProfiling() {
        LevelProficiency.fromString(viewModel.level)?.let { level ->
            StUserProfilingConfig.defaultLevelProficiency = level
        }

        ProfileType.fromString(viewModel.type)?.let { type ->
            StUserProfilingConfig.defaultProfileType = type
        }

        StUserProfilingConfig.onDone = { level: LevelProficiency, type: ProfileType ->
            viewModel.profileShow(level = level, type = type)

            val navOptions: NavOptions = navOptions {
                popUpTo(com.st.user_profiling.R.id.user_profiling_nav_graph) { inclusive = true }
            }

            navController.navigate(
                directions = HomeFragmentDirections.actionUserProfilingNavGraphToHomeFragment(), navOptions = navOptions
            )
        }
    }

    private fun setUpWelcome() {
        StWelcomeConfig.welcomePages = listOf(
            WelcomePage(
                title = getString(R.string.st_welcome_title1),
                description = getString(R.string.st_welcome_message1),
                drawableRes = R.drawable.welcome1
            ),
            WelcomePage(
                title = getString(R.string.st_welcome_title2),
                description = getString(R.string.st_welcome_message2),
                drawableRes = R.drawable.welcome2
            ),
            WelcomePage(
                title = getString(R.string.st_welcome_title3),
                description = getString(R.string.st_welcome_message3),
                drawableRes = R.drawable.welcome3
            ),
            WelcomePage(
                title = getString(R.string.st_welcome_title4),
                description = getString(R.string.st_welcome_message4),
                drawableRes = R.drawable.welcome4
            )
        )

        StWelcomeConfig.onSkip = {
            viewModel.welcomeShow()

            when {
                viewModel.shouldShowProfile -> WelcomeFragmentDirections.actionWelcomeFragmentToProfileNavGraph()
                else -> null
            }?.let { destination ->
                navController.navigate(directions = destination)
            }
        }
    }

    private fun setUpTerms() {
        StTermsConfig.onDone = { isAccepted ->
            viewModel.termsAccepted(isAccepted)

            when {
                viewModel.shouldShowWelcome -> TermsFragmentDirections.actionTermsFragmentToWelcomeFragment()
                viewModel.shouldShowProfile -> TermsFragmentDirections.actionTermsFragmentToProfileNavGraph()
                else -> null
            }?.let { destination ->
                navController.navigate(directions = destination)
            } ?: run {
                navController.popBackStack()
            }
        }
    }

    private fun setUpDemoShowCase() {
        DemoShowCaseConfig.onClose = {
            navController.popBackStack()
        }
    }
//
//    companion object {
//        private const val TAG = "FireBase"
//        private const val LOADING_PHRASE_CONFIG_KEY = "D183G"
//    }
}
