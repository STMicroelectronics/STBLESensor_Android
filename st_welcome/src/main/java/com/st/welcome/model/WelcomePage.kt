/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.welcome.model

import androidx.annotation.DrawableRes

data class WelcomePage(
    val title: String,
    val description: String,
    @DrawableRes val drawableRes: Int
)
