/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login.api

import androidx.activity.result.ActivityResultRegistry

interface StLoginManager {

    suspend fun getStAuthData(): StAuthData?

    suspend fun login(activityResultRegistry: ActivityResultRegistry): StAuthData?

    suspend fun logout(activityResultRegistry: ActivityResultRegistry)

    fun isLoggedIn(): Boolean
}