package com.st.ui.utils

import androidx.compose.ui.text.intl.Locale

val Map<String, String>.localizedDisplayName: String
    get() {
        if (isEmpty()) return ""
        val locale = Locale.current.language.uppercase()
        if (containsKey(locale)) {
            return getValue(locale)
        }
        return getValue(keys.first())
    }

val Map<String, String>.localizedDisplayNameSensor: String
    get() =
        localizedDisplayName.split("_").first()
