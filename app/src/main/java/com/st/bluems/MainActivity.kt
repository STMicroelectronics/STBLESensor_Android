/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.st.bluems.ui.home.HomeFragmentDirections
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

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
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

        viewModel.reportApplicationAnalytics(applicationContext)

        installSplashScreen()
        setContentView(R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment_content_main)

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
}
