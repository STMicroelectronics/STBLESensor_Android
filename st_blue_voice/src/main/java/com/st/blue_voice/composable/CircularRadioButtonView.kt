package com.st.blue_voice.composable

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun CircularRadioButtonView(
    modifier: Modifier = Modifier,
    radius: Float = 250f,
    values: List<Pair<String,Boolean>>,
    initialValue: String,
    visualizeLabels: Boolean = true,
    onValueSelected: (String) -> Unit = { /** NOOP **/ }
) {
    val selectedValue by remember(initialValue) { mutableStateOf(value = initialValue) }

    CircularLayout(modifier = modifier, radius = radius) {
        values.forEach { element ->
            Column(
                Modifier
                    .selectable(
                        selected = (element.first == selectedValue),
                        onClick = {
                            if(element.second) {
                                onValueSelected(element.first)
                            }
                        }
                    ).graphicsLayer(alpha = if(element.second) 1f else 0f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RadioButton(
                    selected = (element.first == selectedValue),
                    enabled = element.second,
                    onClick = { onValueSelected(element.first) }
                )
                if(visualizeLabels) {
                    Text(
                        text = element.first,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}