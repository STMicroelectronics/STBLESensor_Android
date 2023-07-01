/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.user_profiling

import com.st.user_profiling.model.LevelProficiency
import com.st.user_profiling.model.ProfileType

object StUserProfilingConfig {
    var defaultLevelProficiency: LevelProficiency = LevelProficiency.BEGINNER
    var defaultProfileType: ProfileType = ProfileType.DEVELOPER
    lateinit var onDone: (LevelProficiency, ProfileType) -> Unit
}
