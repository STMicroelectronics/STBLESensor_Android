/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.st.demo_showcase.R
import com.st.ui.composables.ActionItem
import com.st.ui.composables.BlueMsMenuActions
import com.st.ui.theme.PreviewBlueMSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoShowCaseTopBar(
    modifier: Modifier = Modifier,
    demoName: String?,
    showSettingsMenu: Boolean,
    boardActions: List<ActionItem>,
    demoActions: List<ActionItem>
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        colors = topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        scrolledContainerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
    ),
        title = {
            Text(demoName ?: stringResource(id = R.string.st_demoShowcase_title))
        },
        actions = {
            if (showSettingsMenu) {
                if (demoName != null) {
                    if (demoActions.isNotEmpty()) {
                        BlueMsMenuActions(actions = demoActions)
                    }
                } else {
                    BlueMsMenuActions(
                        menuIcon = Icons.Default.Settings,
                        actions = boardActions
                    )
                }
            }
        }
    )
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun DemoShowCaseTopBarPreview() {
    PreviewBlueMSTheme {
        DemoShowCaseTopBar(
            demoName = null,
            showSettingsMenu = true,
            demoActions = emptyList(),
            boardActions = emptyList()
        )
    }
}