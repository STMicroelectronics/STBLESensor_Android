package com.st.ext_config.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextField
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

@OptIn(ExperimentalMaterial3Api::class)
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
            stringResource(id = R.string.st_extConfig_fwUpgrade_otaBoard2),
            stringResource(id = R.string.st_extConfig_fwUpgrade_otaBoard3)
        )

    // remember the selected item
    var selectedItem by remember {
        mutableStateOf(items[selectedIndex])
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier
            .wrapContentSize(align = Alignment.TopStart)
            .padding(horizontal = 8.dp)
    ) {
        // text field
        TextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Board type") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 60.dp)
                .wrapContentHeight()
                .menuAnchor(),
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        // menu
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // this is a column scope
            // all the items are added vertically
            items.forEachIndexed { index, selectedOption ->
                // menu item
                DropdownMenuItem(onClick = {
                    selectedItem = selectedOption
                    onSelection(index)
                    expanded = false
                }) {
                    Text(text = selectedOption)
                }
            }
        }
    }
}