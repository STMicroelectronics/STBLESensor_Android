/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.environmental.model

data class EnvironmentalValues(
    val temperatures: Map<Int, Float>,
    val hasTemperatures: Boolean,
    val humidity: Float,
    val hasHumidity: Boolean,
    val pressure: Float,
    val hasPressure: Boolean
)