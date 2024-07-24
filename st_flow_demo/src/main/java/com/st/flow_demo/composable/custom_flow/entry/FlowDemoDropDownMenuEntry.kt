package com.st.flow_demo.composable.custom_flow.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowDemoDropDownMenuEntry(
    modifier: Modifier = Modifier,
    title: String,
    values: List<String>,
    initialValue: String,
    errorText: String? = null,
    onValueSelected: (String) -> Unit = { /** NOOP **/ }
) {
    var expanded by remember { mutableStateOf(value = false) }
    var selectedValue by remember(initialValue) { mutableStateOf(value = initialValue) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(LocalDimensions.current.paddingSmall),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationSmall
    ) {
        Column(
            modifier = modifier.padding(LocalDimensions.current.paddingNormal)
        ) {
            Text(
                modifier = Modifier.padding(
                    bottom = LocalDimensions.current.paddingNormal
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                text = title
            )

            ExposedDropdownMenuBox(
                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                expanded = expanded,
                onExpandedChange = { newValue ->
                    expanded = newValue
                }
            ) {
                TextField(
                    value = selectedValue,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
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
            errorText?.let {
                Text(
                    modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                    fontSize = 14.sp,
                    color = ErrorText,
                    fontWeight = FontWeight.Bold,
                    text = errorText
                )
            }
        }
    }
}