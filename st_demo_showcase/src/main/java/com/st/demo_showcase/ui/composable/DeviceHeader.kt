/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.st.demo_showcase.R
import com.st.demo_showcase.utils.DTMIModelLoadedStatus
import com.st.ui.theme.ErrorText
import com.st.ui.theme.PrimaryYellow
import com.st.ui.theme.SuccessText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import com.st.ui.utils.getBlueStBoardImages

@Composable
fun DeviceHeader(
    modifier: Modifier = Modifier,
    boardTypeName: String,
    isPin: Boolean,
    showEdit: Boolean = false,
    boardHasCustomName: String? = null,
    onCustomNameSelected: () -> Unit = { /** NOOP**/ },
    name: String? = null,
    runningFw: String? = null,
    statusModelDTMI: DTMIModelLoadedStatus = DTMIModelLoadedStatus.NotNecessary,
    onCustomDTMIClicked: () -> Unit = { /** NOOP **/ },
    onPinChange: (Boolean) -> Unit = { /** NOOP **/ }
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .padding(all = LocalDimensions.current.paddingNormal),
                    horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingSmall),
                    verticalAlignment = Alignment.Top
                ) {

                    if (boardHasCustomName != null) {
                        Text(
                            text = boardHasCustomName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = name ?: "NoName",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (showEdit) {
                        Icon(
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onCustomNameSelected() },
                            tint = SecondaryBlue,
                            imageVector = Icons.Filled.BorderColor,
                            contentDescription = null
                        )
                    }
                }

                IconButton(onClick = { onPinChange(isPin.not()) }) {
                    Icon(
                        tint = PrimaryYellow,
                        imageVector = if (isPin) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null
                    )
                }
            }

            if (runningFw != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = LocalDimensions.current.imageMedium)
                ) {
                    Image(
                        modifier = Modifier
                            //.size(size = LocalDimensions.current.imageNormal)
                            .fillMaxHeight()
                            .align(alignment = Alignment.Center),
                        painter = painterResource(id = getBlueStBoardImages(boardType = boardTypeName)),
                        contentDescription = null
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = LocalDimensions.current.imageMedium)
                        .padding(bottom = LocalDimensions.current.paddingNormal)
                ) {
                    Image(
                        modifier = Modifier
                            //.size(size = LocalDimensions.current.imageNormal)
                            .fillMaxHeight()
                            .align(alignment = Alignment.Center),
                        painter = painterResource(id = getBlueStBoardImages(boardType = boardTypeName)),
                        contentDescription = null
                    )
                }
            }

            if (runningFw != null) {
                HorizontalDivider(modifier = Modifier.padding(top = LocalDimensions.current.paddingSmall))

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = LocalDimensions.current.paddingNormal,
                            end = LocalDimensions.current.paddingSmall,
                            top = LocalDimensions.current.paddingSmall,
                            bottom = LocalDimensions.current.paddingSmall
                        ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = Grey6,
                    text = runningFw
                )

                when (statusModelDTMI) {

                    DTMIModelLoadedStatus.NotNecessary -> {
                    }

                    DTMIModelLoadedStatus.Loaded -> {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = LocalDimensions.current.paddingNormal,
                                    end = LocalDimensions.current.paddingSmall,
                                    top = LocalDimensions.current.paddingSmall,
                                    bottom = LocalDimensions.current.paddingSmall
                                ),
                            //color = SuccessText,
                            textAlign = TextAlign.Left,
                            style = MaterialTheme.typography.bodySmall,
                            text = stringResource(
                                id = R.string.st_demoShowcase_validDTMI
                            )
                        )
                    }

                    DTMIModelLoadedStatus.CustomLoaded -> {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = LocalDimensions.current.paddingNormal,
                                    end = LocalDimensions.current.paddingSmall,
                                    top = LocalDimensions.current.paddingSmall,
                                    bottom = LocalDimensions.current.paddingSmall
                                )
                                .clickable { onCustomDTMIClicked() },
                            color = SuccessText,
                            textAlign = TextAlign.Left,
                            style = MaterialTheme.typography.bodySmall,
                            text = stringResource(
                                id = R.string.st_demoShowcase_customDTMI
                            )
                        )
                    }

                    DTMIModelLoadedStatus.CustomNotLoaded -> {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = LocalDimensions.current.paddingNormal,
                                    end = LocalDimensions.current.paddingSmall,
                                    top = LocalDimensions.current.paddingSmall,
                                    bottom = LocalDimensions.current.paddingSmall
                                )
                                .clickable { onCustomDTMIClicked() },
                            color = ErrorText,
                            textAlign = TextAlign.Left,
                            style = MaterialTheme.typography.bodySmall,
                            text = stringResource(
                                id = R.string.st_demoShowcase_DTMI_notpresent
                            )
                        )
                    }
                }
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun DeviceHeaderPreview() {
    PreviewBlueMSTheme {
        DeviceHeader(
            isPin = false,
            boardTypeName = "ST Board",
            name = "Astra",
            showEdit = true,
            boardHasCustomName = "Luca Test",
            runningFw = "FP-ATR-ASTRA1V2.0.0"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceHeaderPinnedPreview() {
    PreviewBlueMSTheme {
        DeviceHeader(
            isPin = true,
            boardTypeName = "ST Board",
            name = "Astra",
            runningFw = "Test with Custom",
            statusModelDTMI = DTMIModelLoadedStatus.Loaded
        )
    }
}