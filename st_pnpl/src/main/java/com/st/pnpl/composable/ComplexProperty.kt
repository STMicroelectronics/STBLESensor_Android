/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.pnpl.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.blue_sdk.utils.toMapOfAny
import com.st.pnpl.R
import com.st.ui.composables.EnumProperty
import com.st.ui.composables.LocalLastStatusUpdatedAt
import com.st.ui.theme.LocalDimensions
import com.st.ui.utils.localizedDisplayName
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

@Composable
fun ComplexProperty(
    modifier: Modifier = Modifier,
    hideProperties: Array<String>?=null,
    schema: DtmiContent,
    data: JsonElement?,
    label: String,
    color: String,
    description: String,
    comment: String,
    unit: String,
    enabled: Boolean,
    commandBehavior: Boolean,
    onValueChange: (Any) -> Unit
) {
    when (schema) {
        is DtmiContent.DtmiEnumContent<*> -> {
            when (schema.enumType) {
                DtmiContent.DtmiEnumContent.EnumType.INTEGER -> {

                    var mustBeShowed = true
                    if(hideProperties!=null) {
                        mustBeShowed = !hideProperties.contains(schema.name)
                    }
                    if(mustBeShowed) {
                        var enumData: Int? = null
                        if (data is JsonPrimitive) {
                            enumData = data.intOrNull
                        }
                        EnumProperty(
                            modifier = modifier,
                            label = label,
                            data = enumData,
                            unit = unit,
                            enabled = enabled,
                            color = color,
                            description = description,
                            comment = comment,
                            initialValue = schema.initValue,
                            colors = schema.enumColors.map {
                                it.value to it.color
                            },
                            values = schema.enumValues.map {
                                it.displayName.localizedDisplayName to it.enumValue
                            },
                            onValueChange = onValueChange
                        )
                    }
                }

                DtmiContent.DtmiEnumContent.EnumType.STRING -> {
                    var mustBeShowed = true
                    if(hideProperties!=null) {
                        mustBeShowed = !hideProperties.contains(schema.name)
                    }
                    if(mustBeShowed) {
                        var enumData: String? = null
                        if (data is JsonPrimitive) {
                            enumData = data.contentOrNull
                        }
                        EnumProperty(
                            modifier = modifier,
                            label = label,
                            unit = unit,
                            data = enumData,
                            enabled = enabled,
                            color = color,
                            description = description,
                            comment = comment,
                            initialValue = schema.initValue,
                            colors = schema.enumColors.map {
                                it.value to it.color
                            },
                            values = schema.enumValues.map {
                                it.displayName.localizedDisplayName to it.enumValue
                            },
                            onValueChange = onValueChange
                        )
                    }
                }
            }
        }

        is DtmiContent.DtmiObjectContent -> ObjectProperty(
            modifier = modifier,
            hideProperties = hideProperties,
            data = data,
            label = label,
            unit = unit,
            enabled = enabled,
            color = color,
            description = description,
            comment = comment,
            initValue = schema.initValue,
            fields = schema.fields,
            onValueChange = onValueChange,
            commandBehavior = commandBehavior
        )

        is DtmiContent.DtmiMapContent -> MapProperty(
            modifier = modifier,
            data = data,
            label = label,
            unit = unit,
            enabled = enabled,
            color = color,
            description = description,
            comment = comment,
            initValue = schema.initValue,

            mapKeys = schema.mapKey,
            mapValues = schema.mapValue,
            onValueChange = onValueChange,
            commandBehavior = commandBehavior
        )

        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapEntryProperty(
    map: Map<String, Any>,
    onRemoveEntry: (String) -> Unit
) {
    map.keys.forEach { key ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(text = key)
            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingLarge))
            Text(text = map[key].toString())

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                onClick = {
                    onRemoveEntry(key)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapProperty(
    modifier: Modifier = Modifier,
    data: JsonElement?,
    initValue: JsonObject?,
    label: String,
    color: String,
    description: String,
    comment: String,
    unit: String,
    enabled: Boolean,

    mapKeys: DtmiContent.DtmiPropertyContent.DtmiStringPropertyContent,
    mapValues: DtmiContent.DtmiPropertyContent,
    commandBehavior: Boolean,
    onValueChange: (Any) -> Unit
) {
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    val internalStateMap = remember(key1 = lastStatusUpdatedAt) { SnapshotStateMap<String, Any>() }
    var internalStateNewKey by remember(key1 = lastStatusUpdatedAt) { mutableStateOf(value = "") }
    var internalStateNewValue by remember(key1 = lastStatusUpdatedAt) { mutableStateOf<Any>("") }

    LaunchedEffect(key1 = lastStatusUpdatedAt) {
        initValue?.toMapOfAny()?.map {
            it.value?.let { obj -> internalStateMap[it.key] = obj }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        val propName = if (unit.isEmpty()) label else "$label [$unit]"

        Text(
            fontSize = 12.sp,
            lineHeight = 17.37.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            text = propName
        )

        MapEntryProperty(
            map = internalStateMap
        ) {
            internalStateMap.remove(it)
        }

        Text(text = stringResource(id = R.string.st_pnpl_addMapEntryBtn))

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        Property(
            modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
            data = initValue,
            content = mapKeys,
            commandBehavior = true,
            enabled = true,
            onValueChange = {
                internalStateNewKey = it.second as String
            }
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        Property(
            modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
            data = initValue,
            content = mapValues,
            enabled = enabled,
            commandBehavior = true,
            onValueChange = {
                internalStateNewValue = it.second
            }
        )

        Surface(
            modifier = Modifier.align(alignment = Alignment.End),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            onClick = {
                if (internalStateNewKey.isNotEmpty()) {
                    internalStateMap[internalStateNewKey] = internalStateNewValue

                    onValueChange(internalStateMap)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
    }
}

@Composable
fun ObjectProperty(
    modifier: Modifier = Modifier,
    hideProperties: Array<String>?=null,
    data: JsonElement?,
    initValue: JsonObject?,
    label: String,
    color: String,
    description: String,
    comment: String,
    unit: String,
    enabled: Boolean,
    fields: List<DtmiContent>,
    commandBehavior: Boolean,
    onValueChange: (Any) -> Unit
) {
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    val objectValue = remember(key1 = lastStatusUpdatedAt) { mutableMapOf<String, Any>() }

    LaunchedEffect(key1 = lastStatusUpdatedAt) {
        initValue?.toMapOfAny()?.map {
            it.value?.let { obj -> objectValue[it.key] = obj }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        val propName = if (unit.isEmpty()) label else "$label [$unit]"

        Text(
            fontSize = 12.sp,
            lineHeight = 17.37.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            text = propName
        )

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

        fields.forEach { content ->
            val defaultData = initValue?.get(content.name)
            var contentData: JsonElement? = defaultData
            if (data is JsonObject) {
                contentData = data[content.name] ?: defaultData
            }
            Property(
                hideProperties = hideProperties,
                data = contentData,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LocalDimensions.current.paddingNormal),
                content = content as DtmiContent.DtmiPropertyContent,
                enabled = enabled,
                commandBehavior = commandBehavior,
                onValueChange = { newValue ->
                    if (enabled) {
                        val key = newValue.first
                        val value = newValue.second
                        objectValue[key] = value

                        onValueChange(objectValue)
                    }
                }
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
    }
}
