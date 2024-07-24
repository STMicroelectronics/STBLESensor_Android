/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.st.catalog.R
import com.st.demo_showcase.models.DemoGroup
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey1
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogFilterDialog(
    modifier: Modifier = Modifier,
    demoGroups: List<DemoGroup> = DemoGroup.entries,
    filters: CatalogFilter,
    boardOrder:BoardOrder = BoardOrder.NONE,
    releaseDatesPresent: Boolean = false,
    onFilterChange: (CatalogFilter,BoardOrder) -> Unit = { _: CatalogFilter, _: BoardOrder -> /** NOOP**/ }
) {
    var internalBoardOrder by remember { mutableStateOf(value = boardOrder) }
    var internalFilters by remember(key1 = filters) { mutableStateOf(value = filters) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(id = R.string.st_catalog_boardList_orderDialog_title)
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal)
            ) {
                DemoGroupChip(
                    groupName = "None",
                    checked = internalBoardOrder == BoardOrder.NONE,
                    onCheckedChange = { internalBoardOrder = BoardOrder.NONE })
                DemoGroupChip(
                    groupName = "Alphabetical",
                    checked = internalBoardOrder == BoardOrder.ALPHABETICAL,
                    onCheckedChange = { internalBoardOrder = BoardOrder.ALPHABETICAL })
                if(releaseDatesPresent) {
                    DemoGroupChip(
                        groupName = "Release Date",
                        checked = internalBoardOrder == BoardOrder.RELEASE_DATE,
                        onCheckedChange = { internalBoardOrder = BoardOrder.RELEASE_DATE })
                }
            }

            HorizontalDivider()

            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(id = R.string.st_catalog_boardList_filterDialog_title)
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            FlowRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                demoGroups.forEach { demoGroup ->
                    DemoGroupChip(
                        groupName = demoGroup.displayName,
                        checked = internalFilters.demoGroups.contains(demoGroup.name)
                    ) { checked ->
                        val currentDemoGroups = internalFilters.demoGroups.toMutableList()
                        if (checked) {
                            currentDemoGroups.add(demoGroup.name)
                        } else {
                            currentDemoGroups.remove(demoGroup.name)
                        }
                        internalFilters = internalFilters.copy(demoGroups = currentDemoGroups)
                    }
                }
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                BlueMsButton(
                    text = stringResource(id = R.string.st_catalog_boardList_filterDialog_okBtn),
                    onClick = { onFilterChange(internalFilters,internalBoardOrder) }
                )
            }
        }
    }
}

data class CatalogFilter(
    val demoGroups: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoGroupChip(
    modifier: Modifier = Modifier,
    groupName: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = { /** NOOP **/ }
) {
    Surface(
        modifier = modifier.padding(horizontal = LocalDimensions.current.paddingSmall),
        onClick = { onCheckedChange(checked.not()) },
        shape = Shapes.medium,
        color = if (checked) SecondaryBlue else Grey1,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Text(
            modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
            style = MaterialTheme.typography.bodyMedium,
            text = groupName
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun CatalogFilterDialogPreview() {
    PreviewBlueMSTheme {
        CatalogFilterDialog(
            filters = CatalogFilter(
                demoGroups = listOf(
                    DemoGroup.EnvironmentalSensors.name
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoGroupChipSelectedPreview() {
    PreviewBlueMSTheme {
        DemoGroupChip(
            groupName = "Environmental",
            checked = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoGroupChipNotSelectedPreview() {
    PreviewBlueMSTheme {
        DemoGroupChip(
            groupName = "Environmental",
            checked = false
        )
    }
}
