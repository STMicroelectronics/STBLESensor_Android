/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems.ui.composable

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.board_catalog.models.FirmwareMaturity
import com.st.blue_sdk.models.Node
import com.st.ui.theme.*
import com.st.ui.utils.getBlueStBoardImages
import com.st.ui.utils.getBlueStIcon

@SuppressLint("MissingPermission")
@Composable
fun DeviceListItem(
    modifier: Modifier = Modifier,
    item: Node,
    isPin: Boolean = false,
    onPinChange: (Boolean) -> Unit = { /** NOOP**/ },
    onNodeSelected: (Node) -> Unit = { /** NOOP**/ },
    isExpert: Boolean = false,
    onInfoClick: (Node) -> Unit = { /** NOOP**/ },
) {
    DeviceListItem(
        modifier = modifier,
        isPin = isPin,
        onPinChange = onPinChange,
        boardTypeName = item.boardType.name,
        name = item.device.name,
        rssi = item.rssi?.rssi,
        address = item.device.address,
        runningFw = item.runningFw,
        catalogInfo = item.catalogInfo,
        displayMessages = item.displayMessages,
        icons = item.icons,
        isSleeping = item.isSleeping,
        isCustomFw = item.isCustomFw,
        hasGeneralPurpose = item.hasGeneralPurpose,
        onNodeSelected = { onNodeSelected(item) },
        isExpert = isExpert,
        onInfoClick = { onInfoClick(item) },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceListItem(
    modifier: Modifier = Modifier,
    boardTypeName: String,
    name: String? = null,
    isPin: Boolean,
    rssi: Int?,
    address: String,
    runningFw: String? = null,
    catalogInfo: BoardFirmware? = null,
    displayMessages: List<String>,
    icons: List<Int>,
    isCustomFw: Boolean,
    isSleeping: Boolean,
    hasGeneralPurpose: Boolean,
    onPinChange: (Boolean) -> Unit = { /** NOOP**/ },
    onNodeSelected: () -> Unit = { /** NOOP**/ },
    isExpert: Boolean = false,
    onInfoClick: () -> Unit = { /** NOOP**/ }
) {
    val haptics = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onNodeSelected,
                onLongClick = {
                    if (isExpert) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onInfoClick()
                    }
                }
            ),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(size = LocalDimensions.current.imageNormal),
                    painter = painterResource(id = getBlueStBoardImages(boardTypeName)),
                    contentDescription = null
                )

                Column(modifier = Modifier.weight(weight = 1f)) {
                    name?.let {
                        Text(
                            modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    runningFw?.let {
                        Text(
                            modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                            text = it,
                            color = Grey6,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                IconButton(
                    onClick = { onPinChange(isPin.not()) }
                ) {
                    Icon(
                        tint = PrimaryYellow,
                        imageVector = if (isPin) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                if (isCustomFw) {
                    Text(
                        modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                        color = ErrorText,
                        text = stringResource(id = com.st.bluems.R.string.st_home_deviceListItem_fwCustomLabel)
                    )
                } else {
                    if (catalogInfo != null) {
                        if(catalogInfo.maturity!=FirmwareMaturity.RELEASE) {
                            Text(
                                modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                                color = ErrorText,
                                text = "${catalogInfo.maturity} FW"
                            )
                        }
                    }
                }

                displayMessages.forEach { msg ->
                    Text(
                        modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                        text = msg,
                        color = Grey6,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rssi?.let { dbm ->
                    Icon(
                        tint = Grey6,
                        imageVector = Icons.Default.SignalCellular4Bar,
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                        text = stringResource(
                            id = com.st.bluems.R.string.st_home_deviceListItem_rssiFormatter,
                            dbm
                        ),
                        color = Grey6,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.weight(weight = 1f))
                }
                icons.forEach { icon ->
                    Icon(
                        tint = Grey6,
                        painter = painterResource(id = getBlueStIcon(icon)),
                        modifier = Modifier.size(size = LocalDimensions.current.iconSmall),
                        contentDescription = null
                    )
                }
                if (isSleeping) {
                    Icon(
                        modifier = Modifier.size(size = LocalDimensions.current.iconSmall),
                        tint = Grey6,
                        painter = painterResource(id = com.st.ui.R.drawable.ic_sleeping),
                        contentDescription = null
                    )
                }
                if (hasGeneralPurpose) {
                    Icon(
                        modifier = Modifier.size(size = LocalDimensions.current.iconSmall),
                        tint = Grey6,
                        painter = painterResource(id = com.st.ui.R.drawable.ic_extra_gp),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.weight(weight = 1f))
                Text(
                    modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                    text = address,
                    color = Grey6,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun DeviceListItemPreview() {
    PreviewBlueMSTheme {
        DeviceListItem(
            boardTypeName = "ST Board",
            isPin = false,
            name = "Astra",
            rssi = 11,
            address = "E8:C3:90:47:8f:AC",
            runningFw = "FP-ATR-ASTRA1V2.0.0",
            displayMessages = listOf("message1", "message2"),
            icons = listOf(0, 1, 2),
            isCustomFw = false,
            isSleeping = false,
            hasGeneralPurpose = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceListItemCustomPreview() {
    PreviewBlueMSTheme {
        DeviceListItem(
            boardTypeName = "ST Board",
            name = "Astra",
            isPin = false,
            rssi = 11,
            address = "E8:C3:90:47:8f:AC",
            runningFw = "FP-ATR-ASTRA1V2.0.0",
            displayMessages = listOf("message1", "message2"),
            icons = listOf(0, 1, 2),
            isCustomFw = true,
            isSleeping = false,
            hasGeneralPurpose = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceListItemSleepingPreview() {
    PreviewBlueMSTheme {
        DeviceListItem(
            boardTypeName = "ST Board",
            name = "Astra",
            isPin = false,
            rssi = 11,
            address = "E8:C3:90:47:8f:AC",
            runningFw = "FP-ATR-ASTRA1V2.0.0",
            displayMessages = listOf("message1", "message2"),
            icons = listOf(0, 1, 2),
            isCustomFw = false,
            isSleeping = true,
            hasGeneralPurpose = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceListItemGPPreview() {
    PreviewBlueMSTheme {
        DeviceListItem(
            boardTypeName = "ST Board",
            name = "Astra",
            isPin = false,
            rssi = 11,
            address = "E8:C3:90:47:8f:AC",
            runningFw = "FP-ATR-ASTRA1V2.0.0",
            displayMessages = listOf("message1", "message2"),
            icons = listOf(0, 1, 2),
            isCustomFw = false,
            isSleeping = false,
            hasGeneralPurpose = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceListItemPinnedPreview() {
    PreviewBlueMSTheme {
        DeviceListItem(
            boardTypeName = "ST Board",
            name = "Astra",
            isPin = true,
            rssi = 11,
            address = "E8:C3:90:47:8f:AC",
            runningFw = "FP-ATR-ASTRA1V2.0.0",
            displayMessages = listOf("message1", "message2"),
            icons = listOf(0, 1, 2),
            isCustomFw = false,
            isSleeping = false,
            hasGeneralPurpose = true
        )
    }
}
