/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WifiCredentials(
    @SerialName("enable") val enable: Boolean? = true,
    @SerialName("ssid") val ssid: String?,
    @SerialName("password") val password: String?,
    @SerialName("securityType") val securityType: String?
)
