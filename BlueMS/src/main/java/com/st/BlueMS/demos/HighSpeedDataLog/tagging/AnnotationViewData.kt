package com.st.BlueMS.demos.HighSpeedDataLog.tagging

import androidx.annotation.StringRes

internal data class AnnotationViewData(
        val id: Int,
        var label: String,
        var pinDesc: String?,
        @StringRes val tagType: Int,
        var isSelected:Boolean = false,
        var userCanEditLabel:Boolean = false,
        var userCanSelect:Boolean = false)