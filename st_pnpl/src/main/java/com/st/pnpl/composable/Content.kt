/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.pnpl.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.ui.composables.CommandRequest
import kotlinx.serialization.json.JsonElement

@Composable
fun Content(
    modifier: Modifier = Modifier,
    data: JsonElement?,
    enabled: Boolean,
    content: DtmiContent,
    onValueChange: (Pair<String, Any>) -> Unit,
    onSendCommand: (CommandRequest?) -> Unit
) {
    when (content) {
        is DtmiContent.DtmiPropertyContent -> Property(
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            data = data,
            content = content,
            onValueChange = onValueChange
        )

        is DtmiContent.DtmiCommandContent -> Command(
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            data = data,
            content = content,
            onSendCommand = onSendCommand
        )

        is DtmiContent.DtmiInterfaceContent -> Unit // No ui for Interface
        is DtmiContent.DtmiComponentContent -> Unit // No ui for Component

        is DtmiContent.DtmiEnumContent<*> -> Unit // Handle as complexProperty
        is DtmiContent.DtmiMapContent -> Unit // Handle as complexProperty
        is DtmiContent.DtmiObjectContent -> Unit // Handle as complexProperty
    }
}
