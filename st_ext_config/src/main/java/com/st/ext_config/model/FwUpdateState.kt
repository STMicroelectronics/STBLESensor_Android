/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.model

import android.net.Uri
import com.st.blue_sdk.services.fw_version.FwVersionBoard
import com.st.blue_sdk.services.ota.FwUploadError

data class FwUpdateState(
    val fwUri: Uri? = null,
    val fwName: String = "",
    val fwSize: String = "",
    val downloadFinished: Boolean = true,
    val boardInfo: FwVersionBoard? = null,
    val isComplete: Boolean = false,
    val duration: Float = 0f,
    val isInProgress: Boolean = false,
    val progress: Float? = null,
    val error: FwUploadError? = null
)
