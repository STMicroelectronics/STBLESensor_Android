/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.catalog.CatalogViewModel
import com.st.catalog.R
import com.st.catalog.availableDemos
import com.st.ui.composables.StTopBar
import com.st.ui.theme.Grey5
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.SecondaryBlue
import kotlinx.coroutines.launch

@Composable
fun FirmwareList(
    modifier: Modifier = Modifier,
    boardPart: String,
    navController: NavController,
    viewModel: CatalogViewModel
) {

    LaunchedEffect(key1 = boardPart) {
        viewModel.getFirmwareListForBoardPart(boardPart)
    }

    val firmwareList by viewModel.firmwareList.collectAsStateWithLifecycle()
    FirmwareList(
        modifier = modifier,
        currentDemoName = viewModel.selectedDemoName,
        firmwareList = firmwareList,
        onBack = {
            navController.popBackStack()
        }
    )
}

@Composable
fun FirmwareList(
    modifier: Modifier = Modifier,
    currentDemoName: String? = null,
    onBack: () -> Unit = { /** NOOP **/ },
    firmwareList: List<BoardFirmware>
) {

    var onlyLatest by remember { mutableStateOf(value = true) }

    val firmwareListDisplayed by remember(
        key1 = onlyLatest,
        key2 = firmwareList,
        key3 = currentDemoName
    ) {
        derivedStateOf {
            //Remove duplicated firmwares
            var firmwareListWithoutDuplicate = firmwareList.groupBy { it.fwName }
                .map { list -> list.value.distinctBy { it.fwVersion } }.flatten()

            if (onlyLatest) {
                firmwareListWithoutDuplicate = firmwareListWithoutDuplicate.groupBy { it.fwName }
                    .map { list -> list.value.maxByOrNull { it.fwVersion }!! }
//            } else {
//                firmwareListWithoutDuplicate
            }

            if (currentDemoName != null) {
                firmwareListWithoutDuplicate.filter {
                    it.availableDemos(
                        demoDecorator = it.demoDecorator,
                        addFlowForBoardType = false
                    ).map { it2 -> it2.displayName }.contains(currentDemoName)
                }
            } else {
                firmwareListWithoutDuplicate
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {

        val state = rememberLazyListState()

        val coroutineScope = rememberCoroutineScope()

        val showButton by remember {
            derivedStateOf {
                state.firstVisibleItemIndex > 0
            }
        }

        StTopBar(
            title = stringResource(id = R.string.st_catalog_fwList_title),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

        if (currentDemoName != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal),
                //verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "${firmwareListDisplayed.size} ${if (firmwareListDisplayed.size > 1) "Firmwares contain" else "Firmware contains"} Demo",
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    //modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                    modifier = Modifier.fillMaxWidth(),
                    text = currentDemoName,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(
                    start = LocalDimensions.current.paddingNormal,
                    end = LocalDimensions.current.paddingNormal,
                    bottom = LocalDimensions.current.paddingNormal
                ),
                thickness = 2.dp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalDimensions.current.paddingSmall)
                .clickable {
                    onlyLatest = !onlyLatest
                    coroutineScope.launch {
                        state.animateScrollToItem(index = 0)
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {

            Text(
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                text = "Show latest"
            )

            Icon(
                modifier = Modifier
                    .padding(start = LocalDimensions.current.paddingNormal),
                tint = SecondaryBlue,
                imageVector = if (onlyLatest) Icons.Default.FilterAlt else Icons.Default.FilterAltOff,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = state,
                contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
            ) {
//                if (currentDemoName != null) {
//                    item {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(all = LocalDimensions.current.paddingNormal),
//                            //verticalAlignment = Alignment.CenterVertically,
//                        ) {
//                            Text(
//                                modifier = Modifier.fillMaxWidth(),
//                                text = "${firmwareListDisplayed.size} ${if (firmwareListDisplayed.size > 1) "Firmwares" else "Firmware"} implementing Demo",
//                                textAlign = TextAlign.Center,
//                                maxLines = 1,
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//
//                            Text(
//                                //modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
//                                modifier = Modifier.fillMaxWidth(),
//                                text = currentDemoName,
//                                textAlign = TextAlign.Center,
//                                maxLines = 1,
//                                style = MaterialTheme.typography.titleMedium,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//                        }
//                    }
//                }
                items(firmwareListDisplayed) { it ->
                    FirmwareListItem(
                        name = it.fwName,
                        version = it.fwVersion,
                        boardName = it.brdName,
                        description = it.fwDesc,
                        fwMaturity = it.maturity,
                        listOfDemos = it.availableDemos(
                            demoDecorator = it.demoDecorator,
                            addFlowForBoardType = false
                        ).map { it2 -> it2.displayName }.toSet().toString()
                            .removePrefix("[").removeSuffix("]")
                    )
                }
                item {
                    Spacer(
                        Modifier.windowInsetsBottomHeight(
                            WindowInsets.systemBars
                        )
                    )
                }
            }

            if (showButton) {
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(LocalDimensions.current.paddingNormal),
                    contentColor = SecondaryBlue,
                    shape = CircleShape,
                    onClick = {
                        coroutineScope.launch {
                            state.animateScrollToItem(index = 0)
                        }
                    }) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        imageVector = Icons.Default.KeyboardDoubleArrowUp,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun FirmwareListPreview() {
    PreviewBlueMSTheme {
        FirmwareList(
            firmwareList = listOf(BoardFirmware.mock(), BoardFirmware.mock(), BoardFirmware.mock())
        )
    }
}
