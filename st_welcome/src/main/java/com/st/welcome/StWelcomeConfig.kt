/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.welcome

import com.st.welcome.model.WelcomePage

object StWelcomeConfig {

    lateinit var onSkip: () -> Unit

    var welcomePages: List<WelcomePage> = emptyList()
}
