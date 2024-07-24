package com.st.ext_config.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
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
import com.st.blue_sdk.models.Boards
import com.st.ext_config.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDropdown(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    wbOnly: Boolean = false,
    boardModel: Boards.Model? = null,
    onSelection: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(value = false) }

    val items =
        when (boardModel) {
            Boards.Model.ASTRA1 -> mutableListOf(stringResource(id = R.string.st_extConfig_fwUpgrade_astra1))
            Boards.Model.PROTEUS -> mutableListOf(stringResource(id = R.string.st_extConfig_fwUpgrade_proteus1))
            Boards.Model.STDES_CBMLORABLE -> mutableListOf(stringResource(id = R.string.st_extConfig_fwUpgrade_cbmlorable))
            else -> {
                mutableListOf(
                    stringResource(id = R.string.st_extConfig_fwUpgrade_otaBoard1),
                    stringResource(id = R.string.st_extConfig_fwUpgrade_otaBoard2)
                    )
            }
        }
    if (!wbOnly) {
        items.add(stringResource(id = R.string.st_extConfig_fwUpgrade_otaBoard3))
    }
    // remember the selected item
    var selectedItem by remember {
        mutableStateOf(items[selectedIndex])
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (items.size > 1) {
                expanded = !expanded
            }
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
                }, text = { Text(text = selectedOption) })
            }
        }
    }
}