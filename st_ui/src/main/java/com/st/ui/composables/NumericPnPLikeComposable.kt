/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.st.ui.R
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.toDouble
import com.st.ui.theme.toFloat
import com.st.ui.theme.toInt
import com.st.ui.theme.toLong
import com.st.ui.utils.Number
import java.util.Locale

@Composable
fun DoubleProperty(
    modifier: Modifier = Modifier,
    label: String = "",
    unit: String = "",
    value: Double = .0,
    description: String = "",
    color: String = "",
    comment: String = "",
    decimalPlaces: Int? = null,
    minValue: Double? = null,
    maxValue: Double? = null,
    enabled: Boolean = true,
    commandBehavior: Boolean = false,
    onValueChange: (Double) -> Unit
) {
    val formatter = if (decimalPlaces != null) "%.${decimalPlaces}f" else "%f"
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    var internalState by rememberSaveable(value, lastStatusUpdatedAt) {
        mutableStateOf(value = String.format(Locale.getDefault(), formatter, value))
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (unit.isEmpty()) label else "$label [$unit]"

        Text(
            fontSize = 12.sp,
            lineHeight = 17.37.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            text = text
        )

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            keyboardOptions = KeyboardOptions.Number,
            keyboardActions = KeyboardActions(
                onSend = {
                    onValueChange(internalState.toDouble())
                }
            ),
            onValueChange = {
                internalState = it

                if (commandBehavior) {
                    onValueChange(internalState.toDouble())
                }
            },
            value = internalState
        )
    }
}

@Composable
fun FloatProperty(
    modifier: Modifier = Modifier,
    label: String = "",
    unit: String = "",
    value: Float = 0f,
    description: String = "",
    color: String = "",
    comment: String = "",
    decimalPlaces: Int? = null,
    minValue: Float? = null,
    maxValue: Float? = null,
    enabled: Boolean = true,
    commandBehavior: Boolean = false,
    onValueChange: (Float) -> Unit
) {
    val formatter = if (decimalPlaces != null) "%.${decimalPlaces}f" else "%f"
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    var internalState by rememberSaveable(value, lastStatusUpdatedAt) {
        mutableStateOf(value = String.format(Locale.getDefault(), formatter, value))
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (unit.isEmpty()) label else "$label [$unit]"

        Text(
            fontSize = 12.sp,
            lineHeight = 17.37.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            text = text
        )

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            keyboardOptions = KeyboardOptions.Number,
            keyboardActions = KeyboardActions(
                onSend = {
                    onValueChange(internalState.toFloat())
                }
            ),
            onValueChange = {
                internalState = it

                if (commandBehavior) {
                    onValueChange(internalState.toFloat())
                }
            },
            value = internalState
        )
    }
}

@Composable
fun <T : Comparable<T>> rememberIsValid(
    value: T,
    minValue: T?,
    maxValue: T?
) = remember (key1=value){
    derivedStateOf {
        when {
            minValue != null && maxValue != null ->
                minValue <= value && value <= maxValue

            minValue != null -> minValue <= value
            maxValue != null -> value <= maxValue
            else -> true
        }
    }
}

@Composable
fun IntegerProperty(
    modifier: Modifier = Modifier,
    label: String = "",
    unit: String = "",
    value: Int = 0,
    description: String = "",
    color: String = "",
    comment: String = "",
    minValue: Int? = null,
    maxValue: Int? = null,
    enabled: Boolean = true,
    commandBehavior: Boolean = false,
    onValueChange: (Int, Boolean) -> Unit
) {
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    var internalState by rememberSaveable(value, lastStatusUpdatedAt) {
        mutableStateOf(value = String.format(Locale.getDefault(), "%d", value))
    }

    val isValid by rememberIsValid(
        value = internalState.toInt(),
        minValue = minValue,
        maxValue = maxValue
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (unit.isEmpty()) label else "$label [$unit]"

        Text(
            text = text,
            fontSize = 12.sp,
            lineHeight = 17.37.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            value = internalState,
            keyboardOptions = KeyboardOptions.Number,
            keyboardActions = KeyboardActions(
                onSend = {
                    onValueChange(internalState.toInt(), isValid)
                }
            ),
            trailingIcon = {
                if (isValid.not()) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = ErrorText
                    )
                }
            },
            supportingText = {
                val supportingText = when {
                    minValue != null && maxValue != null -> "$minValue ≤ value ≤ $maxValue"
                    minValue != null -> "$minValue ≤ value"
                    maxValue != null -> "value ≤ $maxValue"
                    else -> ""
                }
                Text(
                    text = supportingText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    color = if (isValid.not()) ErrorText else Color.Unspecified
                )
            },
            onValueChange = {
                internalState = it

                if (commandBehavior) {
                    internalState.toIntOrNull()?.let {currentValue->
                        val valid =  when {
                            minValue != null && maxValue != null ->
                                minValue <= currentValue && currentValue <= maxValue

                            minValue != null -> minValue <= currentValue
                            maxValue != null -> currentValue <= maxValue
                            else -> true
                        }
                        onValueChange(currentValue, valid)
                    }
                }
            }
        )
    }
}

@Composable
fun LongProperty(
    modifier: Modifier = Modifier,
    label: String = "",
    unit: String = "",
    value: Long = 0L,
    description: String = "",
    color: String = "",
    comment: String = "",
    minValue: Long? = null,
    maxValue: Long? = null,
    enabled: Boolean = true,
    commandBehavior: Boolean = false,
    onValueChange: (Long) -> Unit
) {
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    var internalState by rememberSaveable(value, lastStatusUpdatedAt) {
        mutableStateOf(value = String.format(Locale.getDefault(), "%d", value))
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (unit.isEmpty()) label else "$label [$unit]"

        Text(
            fontSize = 12.sp,
            lineHeight = 17.37.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            text = text
        )

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            value = internalState,
            keyboardOptions = KeyboardOptions.Number,
            keyboardActions = KeyboardActions(
                onSend = {
                    onValueChange(internalState.toLong())
                }
            ),
            onValueChange = {
                internalState = it

                if (commandBehavior) {
                    onValueChange(internalState.toLong())
                }
            }
        )
    }
}

@Composable
fun rememberVectorProperty(
    x: Int,
    y: Int,
    z: Int
): Map<Int, MutableState<String>> {
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current

    val internalStateX = rememberSaveable(x, lastStatusUpdatedAt) {
        mutableStateOf(value = String.format(Locale.getDefault(), "%d", x))
    }
    val internalStateY = rememberSaveable(y, lastStatusUpdatedAt) {
        mutableStateOf(value = String.format(Locale.getDefault(), "%d", y))
    }
    val internalStateZ = rememberSaveable(z, lastStatusUpdatedAt) {
        mutableStateOf(value = String.format(Locale.getDefault(), "%d", z))
    }

    return mapOf(
        R.string.vector_x to internalStateX,
        R.string.vector_y to internalStateY,
        R.string.vector_z to internalStateZ
    )
}

@Composable
fun VectorProperty(
    modifier: Modifier = Modifier,
    label: String = "",
    x: Int = 0,
    y: Int = 0,
    z: Int = 0,
    unit: String = "",
    color: String = "",
    description: String = "",
    comment: String = "",
    enabled: Boolean = true,
    commandBehavior: Boolean = false,
    onValueChange: (Int, Int, Int) -> Unit
) {
    val internalState = rememberVectorProperty(x, y, z)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (unit.isEmpty()) label else "$label [$unit]"

        Text(
            fontSize = 12.sp,
            lineHeight = 17.37.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            text = text
        )

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        Column {
            internalState.toList().forEachIndexed { index, (res, state) ->
                OutlinedTextField(
                    label = {
                        Text(text = stringResource(id = res))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Send,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            val numberX = internalState[res]?.value?.toInt() ?: 0
                            val numberY = internalState[res]?.value?.toInt() ?: 0
                            val numberZ = internalState[res]?.value?.toInt() ?: 0
                            onValueChange(numberX, numberY, numberZ)
                        }
                    ),
                    onValueChange = {
                        state.value = it

                        if (commandBehavior) {
                            val numberX = internalState[res]?.value?.toInt() ?: 0
                            val numberY = internalState[res]?.value?.toInt() ?: 0
                            val numberZ = internalState[res]?.value?.toInt() ?: 0
                            onValueChange(numberX, numberY, numberZ)
                        }
                    },
                    value = state.value
                )

                if (index != internalState.toList().lastIndex) {
                    Spacer(
                        modifier =
                        Modifier.height(height = LocalDimensions.current.paddingNormal)
                    )
                }
            }
        }
    }
}

@Composable
fun rememberGeoProperty(
    lat: Double,
    lon: Double,
    alt: Double
): Map<Int, MutableState<String>> {
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current

    val internalStateLat = rememberSaveable(lat, lastStatusUpdatedAt) {
        mutableStateOf(value = String.format(Locale.getDefault(), "%f", lat))
    }
    val internalStateLon = rememberSaveable(lon, lastStatusUpdatedAt) {
        mutableStateOf(value = String.format(Locale.getDefault(), "%f", lon))
    }
    val internalStateAlt = rememberSaveable(alt, lastStatusUpdatedAt) {
        mutableStateOf(value = String.format(Locale.getDefault(), "%f", alt))
    }

    return mapOf(
        R.string.geo_lat to internalStateLat,
        R.string.geo_lon to internalStateLon,
        R.string.geo_alt to internalStateAlt
    )
}

@Composable
fun GeoProperty(
    modifier: Modifier = Modifier,
    label: String = "",
    lat: Double = .0,
    lon: Double = .0,
    alt: Double = .0,
    unit: String = "",
    color: String = "",
    description: String = "",
    comment: String = "",
    enabled: Boolean = true,
    commandBehavior: Boolean = false,
    onValueChange: (Double, Double, Double) -> Unit
) {
    val internalState = rememberGeoProperty(lat, lon, alt)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (unit.isEmpty()) label else "$label [$unit]"

        Text(
            fontSize = 12.sp,
            lineHeight = 17.37.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            text = text
        )

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        Column {
            internalState.toList().forEachIndexed { index, (res, state) ->
                OutlinedTextField(
                    label = {
                        Text(text = stringResource(id = res))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Send,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            val numberLat = internalState[res]?.value?.toDouble() ?: .0
                            val numberLon = internalState[res]?.value?.toDouble() ?: .0
                            val numberLAlt = internalState[res]?.value?.toDouble() ?: .0
                            onValueChange(numberLat, numberLon, numberLAlt)
                        }
                    ),
                    onValueChange = {
                        state.value = it

                        if (commandBehavior) {
                            val numberLat = internalState[res]?.value?.toDouble() ?: .0
                            val numberLon = internalState[res]?.value?.toDouble() ?: .0
                            val numberLAlt = internalState[res]?.value?.toDouble() ?: .0
                            onValueChange(numberLat, numberLon, numberLAlt)
                        }
                    },
                    value = state.value
                )

                if (index != internalState.toList().lastIndex) {
                    Spacer(
                        modifier =
                        Modifier.height(height = LocalDimensions.current.paddingNormal)
                    )
                }
            }
        }
    }
}
