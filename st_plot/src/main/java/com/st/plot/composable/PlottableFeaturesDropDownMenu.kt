package com.st.plot.composable

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.st.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlottableFeaturesDropDownMenu(
    modifier: Modifier = Modifier,
    values: List<String>,
    initialValue: String,
    onValueSelected: (String) -> Unit = { /** NOOP **/ }
) {
    var expanded by remember { mutableStateOf(value = false) }
    var selectedValue by remember(initialValue) { mutableStateOf(value = initialValue) }

    ExposedDropdownMenuBox(
        modifier = modifier
            .padding(end = LocalDimensions.current.paddingNormal),
        expanded = expanded,
        onExpandedChange = { newValue ->
            expanded = newValue
        }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            values.forEach {
                DropdownMenuItem(
                    onClick = {
                        onValueSelected(it)

                        selectedValue = it
                        expanded = false
                    },
                    text = {
                        Text(it)
                    }
                )
            }
        }
    }
}