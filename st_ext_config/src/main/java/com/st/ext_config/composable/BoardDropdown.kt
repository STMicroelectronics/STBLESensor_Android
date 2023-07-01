/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.st.ext_config.R
import com.st.ui.theme.Grey6

@Composable
fun BoardDropdown(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    onSelection: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(value = false) }
    val items =
        listOf(
            stringResource(id = R.string.st_extConfig_fwUpgrade_otaBoard1),
            stringResource(id = R.string.st_extConfig_fwUpgrade_otaBoard2)
        )

    Box(
        modifier = modifier
            .wrapContentSize(align = Alignment.TopStart)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            items[selectedIndex],
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 60.dp)
                .wrapContentHeight()
                .clickable(onClick = { expanded = true })
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Grey6)
        ) {
            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    onSelection(index)
                    expanded = false
                }) {
                    Text(text = s)
                }
            }
        }
    }
}
