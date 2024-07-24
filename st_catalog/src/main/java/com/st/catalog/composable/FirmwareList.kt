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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.catalog.CatalogViewModel
import com.st.catalog.R
import com.st.catalog.availableDemos
import com.st.ui.composables.StTopBar
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.SecondaryBlue
import kotlinx.coroutines.launch

@Composable
fun FirmwareList(
    modifier: Modifier = Modifier,
    boardId: String,
    navController: NavController,
    viewModel: CatalogViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = boardId) {
        viewModel.getFirmwareList(boardId)
    }
    val firmwareList by viewModel.firmwareList.collectAsStateWithLifecycle()
    FirmwareList(
        modifier = modifier,
        firmwareList = firmwareList,
        onBack = {
            navController.popBackStack()
        }
    )
}

@Composable
fun FirmwareList(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { /** NOOP **/ },
    firmwareList: List<BoardFirmware>
) {

    var onlyLatest by remember { mutableStateOf(value = true) }

    val firmwareListDisplayed by remember(key1 = onlyLatest, key2 = firmwareList) {
        derivedStateOf {
            if (onlyLatest) {
                firmwareList.groupBy { it.fwName }.map { list -> list.value.maxByOrNull { it.fwVersion }!! }
            } else {
                firmwareList
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalDimensions.current.paddingSmall)
                .clickable { onlyLatest = !onlyLatest
                    coroutineScope.launch {
                        state.animateScrollToItem(index = 0)
                    }},
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
                items(firmwareListDisplayed) { it ->
                    FirmwareListItem(
                        name = it.fwName,
                        version = it.fwVersion,
                        boardName = it.brdName,
                        description = it.fwDesc,
                        fwMaturity = it.maturity,
                        listOfDemos = it.availableDemos().map { it2 -> it2.displayName }.toString()
                            .removePrefix("[").removeSuffix("]")
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
