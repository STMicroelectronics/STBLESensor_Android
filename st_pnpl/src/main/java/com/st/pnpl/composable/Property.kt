/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.pnpl.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.ui.composables.BooleanProperty
import com.st.ui.composables.DateTimeProperty
import com.st.ui.composables.DoubleProperty
import com.st.ui.composables.FloatProperty
import com.st.ui.composables.GeoProperty
import com.st.ui.composables.IntegerProperty
import com.st.ui.composables.LOAD_FILE_RESPONSE_PROPERTY_NAME
import com.st.ui.composables.LongProperty
import com.st.ui.composables.StringProperty
import com.st.ui.composables.TimeProperty
import com.st.ui.composables.UploadFileResultProperty
import com.st.ui.composables.VectorProperty
import com.st.ui.utils.localizedDisplayName
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

@Composable
fun Property(
    modifier: Modifier = Modifier,
    hideProperties: Array<String>?=null,
    data: JsonElement?,
    enabled: Boolean,
    content: DtmiContent.DtmiPropertyContent,
    onValueChange: (Pair<String, Any>) -> Unit,
    commandBehavior: Boolean = false
) {
    if (content.name == LOAD_FILE_RESPONSE_PROPERTY_NAME && data is JsonPrimitive?) {
        UploadFileResultProperty(
            modifier = modifier,
            result = data?.booleanOrNull ?: false
        )
    } else {
        val label = content.displayName.localizedDisplayName
        val displayUnit = content.displayUnit.localizedDisplayName
        val description = content.description.localizedDisplayName
        val comment = content.comment
        val color = content.color
        val unit = displayUnit.ifEmpty { content.unit }
        val propEnabled = content.writable && enabled

        when (content) {
            is DtmiContent.DtmiPropertyContent.DtmiBooleanPropertyContent -> {

                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    val initData = content.initValue
                    var booleanData = initData
                    if (data is JsonPrimitive) {
                        booleanData = data.booleanOrNull ?: initData
                    }

                    BooleanProperty(
                        modifier = modifier,
                        label = label,
                        description = description,
                        comment = comment,
                        color = color,
                        trueLabel = content.trueName.localizedDisplayName,
                        falseLabel = content.falseName.localizedDisplayName,
                        value = booleanData,
                        unit = unit,
                        enabled = propEnabled,
                        onValueChange = { value -> onValueChange(content.name to value) }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiStringPropertyContent -> {

                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    val defaultData = content.initValue
                    var stringData = defaultData
                    if (data is JsonPrimitive) {
                        stringData = data.contentOrNull ?: defaultData
                    }

                    val minLength = content.minLength
                    val maxLength = content.maxLength
                    val trimWhitespace = content.trimWhitespace

                    StringProperty(
                        modifier = modifier,
                        label = label,
                        value = stringData,
                        unit = unit,
                        description = description,
                        color = color,
                        comment = comment,
                        enabled = propEnabled,
                        minLength = minLength,
                        maxLength = maxLength,
                        trimWhitespace = trimWhitespace,
                        commandBehavior = commandBehavior,
                        onValueChange = { value, _ -> onValueChange(content.name to value) }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiIntegerPropertyContent -> {

                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    val defaultData = content.initValue
                    var intData = defaultData
                    if (data is JsonPrimitive) {
                        intData = data.intOrNull ?: defaultData
                    }

                    val minValue = content.minValue
                    val maxValue = content.maxValue

                    IntegerProperty(
                        modifier = modifier,
                        label = label,
                        value = intData,
                        unit = unit,
                        color = color,
                        description = description,
                        comment = comment,
                        minValue = minValue,
                        maxValue = maxValue,
                        enabled = propEnabled,
                        commandBehavior = commandBehavior,
                        onValueChange = { value, _ -> onValueChange(content.name to value) }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiLongPropertyContent -> {

                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    val defaultData = content.initValue
                    var intData = defaultData
                    if (data is JsonPrimitive) {
                        intData = data.longOrNull ?: defaultData
                    }

                    val minValue = content.minValue
                    val maxValue = content.maxValue

                    LongProperty(
                        modifier = modifier,
                        label = label,
                        value = intData,
                        unit = unit,
                        description = description,
                        color = color,
                        comment = comment,
                        minValue = minValue,
                        maxValue = maxValue,
                        enabled = propEnabled,
                        commandBehavior = commandBehavior,
                        onValueChange = { value -> onValueChange(content.name to value) }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiDoublePropertyContent -> {
                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    val defaultData = content.initValue
                    var doubleData = defaultData
                    if (data is JsonPrimitive) {
                        doubleData = data.doubleOrNull ?: defaultData
                    }

                    val minValue = content.minValue
                    val maxValue = content.maxValue
                    val decimalPlaces = content.decimalPlaces

                    DoubleProperty(
                        modifier = modifier,
                        label = label,
                        value = doubleData,
                        unit = unit,
                        color = color,
                        description = description,
                        comment = comment,
                        decimalPlaces = decimalPlaces,
                        minValue = minValue,
                        maxValue = maxValue,
                        enabled = propEnabled,
                        commandBehavior = commandBehavior,
                        onValueChange = { value -> onValueChange(content.name to value) }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiFloatPropertyContent -> {
                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    val defaultData = content.initValue
                    var floatData = defaultData
                    if (data is JsonPrimitive) {
                        floatData = data.floatOrNull ?: defaultData
                    }
                    val minValue = content.minValue
                    val maxValue = content.maxValue
                    val decimalPlaces = content.decimalPlaces
                    FloatProperty(
                        modifier = modifier,
                        label = label,
                        value = floatData,
                        unit = unit,
                        color = color,
                        description = description,
                        comment = comment,
                        decimalPlaces = decimalPlaces,
                        minValue = minValue,
                        maxValue = maxValue,
                        enabled = propEnabled,
                        commandBehavior = commandBehavior,
                        onValueChange = { value -> onValueChange(content.name to value) }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiDurationPropertyContent -> {
                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    var stringData = ""
                    if (data is JsonPrimitive) {
                        stringData = data.contentOrNull ?: ""
                    }

                    StringProperty(
                        modifier = modifier,
                        label = label,
                        value = stringData,
                        unit = unit,
                        description = description,
                        color = color,
                        comment = comment,
                        enabled = propEnabled,
                        commandBehavior = commandBehavior,
                        onValueChange = { value, _ -> onValueChange(content.name to value) }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiVectorPropertyContent -> {
                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    val defaultData = content.initValue
                    var vectorData = defaultData
                    if (data is JsonObject) {
                        vectorData = Json.decodeFromJsonElement(data) ?: defaultData
                    }

                    VectorProperty(
                        modifier = modifier,
                        label = label,
                        x = vectorData.x,
                        y = vectorData.y,
                        z = vectorData.z,
                        unit = unit,
                        color = color,
                        description = description,
                        comment = comment,
                        enabled = propEnabled,
                        commandBehavior = commandBehavior,
                        onValueChange = { x, y, z ->
                            onValueChange(content.name to listOf(x, y, z))
                        }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiDateTimePropertyContent -> {
                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    val defaultData = content.initValue
                    var stringData = defaultData
                    if (data is JsonPrimitive) {
                        stringData = data.contentOrNull ?: defaultData
                    }

                    val hideTime = content.hideTime

                    DateTimeProperty(
                        modifier = modifier,
                        label = label,
                        value = stringData,
                        unit = unit,
                        description = description,
                        color = color,
                        comment = comment,
                        enabled = propEnabled,
                        hideTime = hideTime,
                        onValueChange = { value -> onValueChange(content.name to value) }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiDatePropertyContent -> {
                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    val defaultData = content.initValue
                    var stringData = defaultData
                    if (data is JsonPrimitive) {
                        stringData = data.contentOrNull ?: defaultData
                    }

                    DateTimeProperty(
                        modifier = modifier,
                        label = label,
                        value = stringData,
                        unit = unit,
                        description = description,
                        color = color,
                        comment = comment,
                        enabled = propEnabled,
                        hideTime = true,
                        onValueChange = { value -> onValueChange(content.name to value) }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiTimePropertyContent -> {
                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    val defaultData = content.initValue
                    var stringData = defaultData
                    if (data is JsonPrimitive) {
                        stringData = data.contentOrNull ?: defaultData
                    }

                    TimeProperty(
                        modifier = modifier,
                        label = label,
                        value = stringData,
                        unit = unit,
                        description = description,
                        color = color,
                        comment = comment,
                        enabled = propEnabled,
                        onValueChange = { value -> onValueChange(content.name to value) }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiGeoPropertyContent -> {
                var mustBeShowed = true
                if(hideProperties!=null) {
                    mustBeShowed = !hideProperties.contains(content.name)
                }
                if(mustBeShowed) {
                    val defaultData = content.initValue
                    var geoData = defaultData
                    if (data is JsonObject) {
                        geoData = Json.decodeFromJsonElement(data) ?: defaultData
                    }

                    GeoProperty(
                        modifier = modifier,
                        label = label,
                        lat = geoData.lat,
                        lon = geoData.lon,
                        alt = geoData.alt,
                        unit = unit,
                        color = color,
                        description = description,
                        comment = comment,
                        enabled = propEnabled,
                        commandBehavior = commandBehavior,
                        onValueChange = { lat, lon, alt ->
                            onValueChange(content.name to listOf(lat, lon, alt))
                        }
                    )
                }
            }

            is DtmiContent.DtmiPropertyContent.DtmiComplexPropertyContent -> {
                val schema = content.schema
                ComplexProperty(
                    modifier = modifier,
                    hideProperties = hideProperties,
                    data = data,
                    label = label,
                    description = description,
                    comment = comment,
                    color = color,
                    unit = unit,
                    enabled = propEnabled,
                    schema = schema,
                    commandBehavior = commandBehavior,
                    onValueChange = { value -> onValueChange(content.name to value) }
                )
            }
        }
    }
}
