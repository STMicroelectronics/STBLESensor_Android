/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.login

import android.net.Uri

data class STLoginConfig(
    var redirectUri: Uri = Uri.parse(""),
    var loginConfiguration: Int = R.raw.prod_auth_config_vespucci,
    var customLogoutUri: Uri = Uri.parse(""),
    var isProdEnvironment: Boolean = true
)