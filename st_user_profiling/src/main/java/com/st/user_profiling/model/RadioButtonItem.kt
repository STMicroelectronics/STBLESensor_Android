/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.user_profiling.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class RadioButtonItem<T>(
    val data: T,
    @StringRes val name: Int,
    @StringRes val description: Int,
    @DrawableRes val image: Int? = null
)
