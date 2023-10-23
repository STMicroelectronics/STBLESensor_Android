package com.st.flow_demo.composable.custom_flow.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowDemoCheckDecimalDropDownMenuEntry(
    modifier: Modifier = Modifier,
    title: String,
    subTitle1: String,
    subTitle2: String,
    checkDecimalVisible: Boolean,
    checkedCheckBox: Boolean,
    enabledCheckBox: Boolean,
    onCheckChange: (Boolean) -> Unit = { /** NOOP **/ },
    selectedValueDecimalEntry: Float,
    textDecimalEntry: String,
    onModifiedDecimalEntry: (Float) -> Unit = { /** NOOP **/ },
    values: List<String>,
    initialValueDropDown: String,
    errorTextDropDown: String? = null,
    onValueSelectedDropDown: (String) -> Unit = { /** NOOP **/ }
) {

    var expanded by remember { mutableStateOf(value = false) }
    var selectedValueDropDown by remember(initialValueDropDown) { mutableStateOf(value = initialValueDropDown) }
    var checkedInternalState by remember {
        mutableStateOf(checkedCheckBox)
    }

    var selectedValueDecimal by remember { mutableStateOf(value = selectedValueDecimalEntry.toString()) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(LocalDimensions.current.paddingSmall),
        shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal),
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

            if(checkDecimalVisible) {
                Text(
                    modifier = Modifier.padding(
                        bottom = LocalDimensions.current.paddingNormal, start = LocalDimensions.current.paddingNormal
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    text = subTitle1
                )


                Row(
                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = checkedInternalState,
                        enabled = enabledCheckBox,
                        onCheckedChange = {
                            checkedInternalState = it
                            onCheckChange(it)
                        })

                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        enabled = checkedInternalState,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        value = selectedValueDecimal,
                        onValueChange = {
                            it.toFloatOrNull()?.let { value ->
                                onModifiedDecimalEntry(value)
                            }
                            selectedValueDecimal = it
                        }
                    )

                    Text(
                        modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                        fontSize = 14.sp,
                        text = textDecimalEntry
                    )
                }
            }

            if(!checkedInternalState) {

                Text(
                    modifier = Modifier.padding(
                        bottom = LocalDimensions.current.paddingNormal, start = LocalDimensions.current.paddingNormal
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    text = subTitle2
                )

                ExposedDropdownMenuBox(
                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                    expanded = expanded,
                    onExpandedChange = { newValue ->
                        expanded = newValue
                    }
                ) {
                    TextField(
                        value = selectedValueDropDown,
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
                                    onValueSelectedDropDown(it)

                                    selectedValueDropDown = it
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
            errorTextDropDown?.let {
                Text(
                    modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                    fontSize = 14.sp,
                    color = ErrorText,
                    fontWeight = FontWeight.Bold,
                    text = errorTextDropDown
                )
            }
        }
    }
}