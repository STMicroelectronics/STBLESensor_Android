/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.pnpl.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.pnpl.util.imageResource
import com.st.pnpl.util.nameResource
import com.st.ui.R
import com.st.ui.composables.CommandRequest
import com.st.ui.composables.ENABLE_PROPERTY_NAME
import com.st.ui.composables.Header
import com.st.ui.composables.HeaderEnabledProperty
import com.st.ui.composables.LOAD_FILE_COMMAND_NAME
import com.st.ui.composables.LOAD_FILE_RESPONSE_PROPERTY_NAME
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import com.st.ui.utils.localizedDisplayName
import com.st.ui.utils.localizedDisplayNameSensor
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

@Composable
fun Component(
    modifier: Modifier = Modifier,
    hideProperties: Array<String>? = null,
    name: String,
    data: JsonElement?,
    enableCollapse: Boolean,
    enabled: Boolean,
    showNotMounted: Boolean = true,
    isOpen: Boolean,
    interfaceModel: DtmiContent.DtmiInterfaceContent,
    componentModel: DtmiContent.DtmiComponentContent,
    onValueChange: (Pair<String, Any>) -> Unit,
    onSendCommand: (CommandRequest?) -> Unit,
    onBeforeUcf:() -> Unit,
    onAfterUcf:() -> Unit,
    onOpenComponent: (String) -> Unit
) {

    val isMounted = interfaceModel.contents
        .filterIsInstance<DtmiContent.DtmiPropertyContent.DtmiBooleanPropertyContent>()
        .find { it.name == "mounted" }
        ?.let { enableProperty ->
            val defaultData = true
            var booleanData = true
            if (data is JsonObject && data[enableProperty.name] is JsonPrimitive) {
                booleanData =
                    (data[enableProperty.name] as JsonPrimitive).booleanOrNull
                        ?: defaultData
            }
            if (data == null) {
                booleanData = false
            }
            booleanData
        } ?: true

    if (isMounted || showNotMounted) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = Shapes.small,
            //enabled = isMounted,
            shadowElevation = LocalDimensions.current.elevationNormal,
            onClick = { onOpenComponent(name) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal),
                horizontalAlignment = Alignment.End
            ) {
                val icon = when (componentModel.contentType) {
                    DtmiContent.DtmiComponentContent.ContentType.SENSOR ->
                        componentModel.sensorType.imageResource

                    DtmiContent.DtmiComponentContent.ContentType.ALGORITHM ->
                        R.drawable.sensor_type_algorithm

                    DtmiContent.DtmiComponentContent.ContentType.ACTUATORS ->
                        R.drawable.actuator_type_class

                    DtmiContent.DtmiComponentContent.ContentType.OTHER,
                    DtmiContent.DtmiComponentContent.ContentType.NONE ->
                        R.drawable.ic_component_info
                }

                val title =
                    if (componentModel.contentType == DtmiContent.DtmiComponentContent.ContentType.SENSOR) {
                        stringResource(componentModel.sensorType.nameResource)
                    } else {
                        componentModel.displayName.localizedDisplayName
                    }

                val subtitle =
                    if (componentModel.contentType == DtmiContent.DtmiComponentContent.ContentType.SENSOR) {
                        componentModel.displayName.localizedDisplayNameSensor
                    } else {
                        null
                    }

                Header(
                    showArrows = enableCollapse,
                    isOpen = isOpen,
                    icon = icon,
                    title = title,
                    isMounted = isMounted,
                    subtitle = subtitle
                ) {
                    if (isOpen.not()) {
                        interfaceModel.contents
                            .filterIsInstance<DtmiContent.DtmiPropertyContent.DtmiBooleanPropertyContent>()
                            .find { it.name == ENABLE_PROPERTY_NAME }
                            ?.let { enableProperty ->
                                val defaultData = enableProperty.initValue
                                var booleanData = false
                                if (data is JsonObject && data[enableProperty.name] is JsonPrimitive) {
                                    booleanData =
                                        (data[enableProperty.name] as JsonPrimitive).booleanOrNull
                                            ?: defaultData
                                }

                                HeaderEnabledProperty(
                                    value = booleanData,
                                    name = enableProperty.name,
                                    enabled = enableProperty.writable && enabled && isMounted,
                                    onValueChange = onValueChange
                                )
                            }
                    }
                }

                AnimatedVisibility(
                    visible = isOpen || enableCollapse.not(),
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = LocalDimensions.current.paddingLarge),
                        horizontalAlignment = Alignment.End
                    ) {
                        interfaceModel.contents.forEachIndexed { index, content ->
                            var contentData: JsonElement? = null
                            if (data is JsonObject) {
                                contentData = data[content.name]
                            }
                            Content(
                                hideProperties = hideProperties,
                                enabled = enabled,
                                content = content,
                                data = contentData,
                                onValueChange = onValueChange,
                                onBeforeUcf = onBeforeUcf,
                                onAfterUcf = onAfterUcf,
                                onSendCommand = onSendCommand
                            )

                            if (index != interfaceModel.contents.lastIndex) {
                                val isLoadFileCommand =
                                    content.name == LOAD_FILE_COMMAND_NAME && interfaceModel.contents[index + 1].name == LOAD_FILE_RESPONSE_PROPERTY_NAME
                                if (isLoadFileCommand.not()) {
                                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
