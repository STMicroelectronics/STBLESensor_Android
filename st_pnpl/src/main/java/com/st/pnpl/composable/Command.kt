/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.pnpl.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.pnpl.R
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.composables.CommandRequest
import com.st.ui.composables.LOAD_FILE_COMMAND_NAME
import com.st.ui.composables.UCF
import com.st.ui.theme.LocalDimensions
import com.st.ui.utils.localizedDisplayName
import kotlinx.serialization.json.JsonElement

@Composable
fun Command(
    modifier: Modifier = Modifier,
    data: JsonElement?,
    enabled: Boolean,
    content: DtmiContent.DtmiCommandContent,
    onBeforeUcf:() -> Unit,
    onAfterUcf:() -> Unit,
    componentName: String,
    onSendCommand: (CommandRequest?) -> Unit
) {
    if (content.name == LOAD_FILE_COMMAND_NAME) {
        UCF(
            label = content.displayName.localizedDisplayName,
            modifier = modifier.fillMaxWidth(),
            commandType = content.commandType,
            commandName = content.name,
            onBeforeUcf = onBeforeUcf,
            onAfterUcf = onAfterUcf,
            componentName = componentName,
            onSendCommand = onSendCommand
        )
    } else {
        val label = content.displayName.localizedDisplayName

        val request = remember { mutableMapOf<String, Any>() }

        Column(modifier = modifier.fillMaxWidth()) {
            Text(text = label, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(weight = 1f)) {
                    content.request?.let { contentRequest ->
                        Property(
                            data = data,
                            enabled = enabled,
                            content = contentRequest as DtmiContent.DtmiPropertyContent,
                            commandBehavior = true,
                            onValueChange = { newValue ->
                                val key = newValue.first
                                val value = newValue.second
                                request[key] = value
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingMedium))

                BlueMsButtonOutlined(
                    text = stringResource(id = R.string.st_pnpl_sendCommandBtn),
                    enabled = enabled,
                    onClick = {
                        onSendCommand(
                            CommandRequest(
                                commandType = content.commandType,
                                commandName = content.name,
                                request = request
                            )
                        )
                    }
                )
            }
        }
    }
}
