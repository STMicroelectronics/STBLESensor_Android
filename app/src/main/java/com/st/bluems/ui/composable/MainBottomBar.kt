/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.st.bluems.R
import com.st.ui.theme.Grey0
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Shapes
import androidx.compose.material3.BottomAppBar
import com.st.ui.theme.PrimaryBlue3

@Composable
fun MainBottomBar(
    floatingActionButton: @Composable () -> Unit = {},
    openFilter: () -> Unit = { /** NOOP **/ },
    openCatalog: () -> Unit = { /** NOOP **/ }
) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),//
        containerColor = PrimaryBlue3, //MaterialTheme.colorScheme.primary
        contentColor = Grey0,
        contentPadding =
        PaddingValues(all = LocalDimensions.current.paddingNormal)
    ) {
        BottomAppBarItem(
            modifier = Modifier.weight(weight = 0.20f),//
            icon = Icons.Default.ContentCopy,
            label = stringResource(id = R.string.st_home_bottomBar_catalog),
            onClick = openCatalog
        )
        //Spacer(modifier = Modifier.size(LocalDimensions.current.paddingNormal))
        Spacer(modifier = Modifier.weight(weight = 0.20f))

        floatingActionButton()

        Spacer(modifier = Modifier.weight(weight = 0.20f))

        BottomAppBarItem(
            modifier = Modifier.weight(weight = 0.20f),//
            icon = Icons.Default.FilterList,
            label = stringResource(id = R.string.st_home_bottomBar_filter),
            onClick = openFilter
        )
    }
}

@Composable
fun BottomAppBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = Shapes.small,
        //color = MaterialTheme.colorScheme.primary,
        color = PrimaryBlue3,
        contentColor = Grey0,
        onClick = onClick
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                modifier = Modifier.size(LocalDimensions.current.iconSmall),
                imageVector = icon,
                contentDescription = label
            )
            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = label
            )
        }
    }
}

@Composable
fun BottomAppBarItemNoText(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = Shapes.small,
        color = MaterialTheme.colorScheme.primary,
        contentColor = Grey0,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(LocalDimensions.current.iconSmall),
            imageVector = icon,
            contentDescription = label
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun MainBottomBarPreview() {
    PreviewBlueMSTheme {
        MainBottomBar()
    }
}
