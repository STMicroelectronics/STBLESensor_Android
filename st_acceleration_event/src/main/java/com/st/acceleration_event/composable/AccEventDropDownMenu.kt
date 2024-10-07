package com.st.acceleration_event.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.st.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccEventDropDownMenu(
    modifier: Modifier = Modifier,
    title: String,
    values: List<String>,
    initialValue: String,
    onValueSelected: (String) -> Unit = { /** NOOP **/ }
) {
    var expanded by remember { mutableStateOf(value = false) }
    var selectedValue by remember(initialValue) { mutableStateOf(value = initialValue) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(LocalDimensions.current.paddingNormal),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(
                    end = LocalDimensions.current.paddingNormal
                )
                .weight(0.3f),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            text = title
        )

        ExposedDropdownMenuBox(
            modifier = Modifier
                .padding(end = LocalDimensions.current.paddingNormal)
                .weight(0.7f),
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
}