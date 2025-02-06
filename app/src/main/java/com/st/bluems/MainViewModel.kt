/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import com.st.blue_sdk.BlueManager
import com.st.core.api.ApplicationAnalyticsService
import com.st.preferences.StPreferences
import com.st.user_profiling.model.LevelProficiency
import com.st.user_profiling.model.ProfileType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferences: StPreferences,
    private val blueManager: BlueManager,
    private val appAnalyticsService: Set<@JvmSuppressWildcards ApplicationAnalyticsService>
) : ViewModel() {

    val level = preferences.getLevelProficiency()
    val type = preferences.getProfileType()
    val shouldShowWelcome = preferences.mustShowWelcome()
    val shouldShowProfile = preferences.hasSelectedProfile().not()
    val shouldShowTerms = preferences.hasAcceptedTerms().not()

    fun initApplicationAnalytics(etnaApplication: ApplicationAnalyticsService.ApplicationNameEtna, application: Application, activity: Activity) {
        appAnalyticsService.forEach {
            it.createAnalytics(etnaApplication,application,activity)
        }
    }

    fun reportApplicationAnalytics(context: Context) {
        appAnalyticsService.forEach {
            it.reportApplicationAnalytics(context)
        }
    }

    fun termsAccepted(accepted: Boolean) {
        preferences.setTermsFlag(accepted = accepted)
    }

    fun updateBoardCatalogStatus(boardCatalogStatus: String) {
        preferences.setBoardCatalogStatus(boardCatalogStatus)
    }

    fun welcomeShow() {
        preferences.setWelcomeFlag(completed = true)
    }

    fun profileShow(level: LevelProficiency, type: ProfileType) {
        preferences.setProfileFlag(selected = true)
        preferences.setLevelProficiency(level = level.name)
        preferences.setProfileType(profile = type.name)
    }

    fun disconnect(nodeId: String) {
        blueManager.disconnect(nodeId = nodeId)
    }
}
