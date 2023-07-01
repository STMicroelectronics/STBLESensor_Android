/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.piano

import android.graphics.RectF


class PianoKey(var rect: RectF, var sound: Int, var white: Boolean) {
    var pressed = false
}