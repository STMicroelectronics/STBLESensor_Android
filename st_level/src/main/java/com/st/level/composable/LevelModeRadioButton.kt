package com.st.level.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.st.ui.theme.LocalDimensions

@Composable
fun LevelModeRadioButton(
    modifier: Modifier = Modifier,
    values: List<String>,
    initialValue: String,
    onValueSelected: (String) -> Unit = { /** NOOP **/ }
) {

    val selectedValue by remember(initialValue) { mutableStateOf(value = initialValue) }

    Row(
        modifier = modifier
            .fillMaxWidth().padding(all = LocalDimensions.current.paddingNormal),
        horizontalArrangement = Arrangement.Absolute.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        values.forEach { text ->
            Column(
                Modifier
                    .selectable(
                        selected = (text == selectedValue),
                        onClick = {
                            onValueSelected(text)
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                )

                RadioButton(
                    selected = (text == selectedValue),
                    onClick = { onValueSelected(text) }
                )
            }
        }
    }
}