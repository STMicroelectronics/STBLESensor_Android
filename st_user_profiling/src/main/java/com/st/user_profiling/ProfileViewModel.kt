/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.user_profiling

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.st.user_profiling.model.LevelProficiency
import com.st.user_profiling.model.ProfileType
import com.st.user_profiling.model.RadioButtonItem

class ProfileViewModel : ViewModel() {

    val levelSelected = mutableStateOf(value = StUserProfilingConfig.defaultLevelProficiency)

    val profileTypeSelected = mutableStateOf(value = StUserProfilingConfig.defaultProfileType)
}

val levels = listOf(
    RadioButtonItem(
        data = LevelProficiency.BEGINNER,
        name = R.string.st_userProfiling_levelProficiency_beginnerName,
        description = R.string.st_userProfiling_levelProficiency_beginnerDescription
    ),
    RadioButtonItem(
        data = LevelProficiency.EXPERT,
        name = R.string.st_userProfiling_levelProficiency_expertName,
        description = R.string.st_userProfiling_levelProficiency_expertDescription
    )
)

val aiDeveloperItemList = if (StUserProfilingConfig.showAiDeveloper) listOf(
    RadioButtonItem(
        data = ProfileType.AI_DEVELOPER,
        name = R.string.st_userProfiling_profileSelection_aiName,
        description = R.string.st_userProfiling_profileSelection_aiDescription,
        image = R.drawable.ai
    )
)
else
    emptyList()

val profileTypes = aiDeveloperItemList +
        listOf(
    RadioButtonItem(
        data = ProfileType.DEVELOPER,
        name = R.string.st_userProfiling_profileSelection_developerName,
        description = R.string.st_userProfiling_profileSelection_developerDescription,
        image = R.drawable.developer
    ),
    RadioButtonItem(
        data = ProfileType.STUDENT,
        name = R.string.st_userProfiling_profileSelection_studentName,
        description = R.string.st_userProfiling_profileSelection_studentDescription,
        image = R.drawable.university
    ),
    RadioButtonItem(
        data = ProfileType.SALES,
        name = R.string.st_userProfiling_profileSelection_salesName,
        description = R.string.st_userProfiling_profileSelection_salesDescription,
        image = R.drawable.fae
    ),
    RadioButtonItem(
        data = ProfileType.OTHER,
        name = R.string.st_userProfiling_profileSelection_otherName,
        description = R.string.st_userProfiling_profileSelection_otherDescription,
        image = R.drawable.other
    )
)
