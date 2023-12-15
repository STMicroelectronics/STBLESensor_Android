/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.board_catalog.models.FirmwareMaturity
import com.st.catalog.CatalogViewModel
import com.st.catalog.R
import com.st.catalog.availableDemos
import com.st.ui.composables.StTopBar
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme

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
    firmwareList: List<BoardFirmware>,
) {
    Column(modifier = modifier.fillMaxSize()) {
        StTopBar(
            title = stringResource(id = R.string.st_catalog_fwList_title),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        LazyColumn(
            modifier = Modifier
                .weight(weight = 1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            items(firmwareList) { it ->
                FirmwareListItem(
                    name = it.fwName,
                    version = it.fwVersion,
                    boardName = it.brdName,
                    description = it.fwDesc,
                    fwMaturity = it.maturity ?: FirmwareMaturity.RELEASE,
                    listOfDemos = it.availableDemos().map{ it2 -> it2.displayName}.toString().removePrefix("[").removeSuffix("]")
                )
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
