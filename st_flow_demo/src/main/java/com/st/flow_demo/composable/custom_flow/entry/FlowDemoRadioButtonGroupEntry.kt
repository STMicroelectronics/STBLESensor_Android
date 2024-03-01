package com.st.flow_demo.composable.custom_flow.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun FlowDemoRadioButtonGroupEntry(
    modifier: Modifier = Modifier,
    title: String,
    values: List<Pair<Int, String>>,
    defaultValue: Int,
    onValueSelected: (Int) -> Unit = { /** NOOP **/ }
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(LocalDimensions.current.paddingSmall),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationSmall
    ) {
        var selected by remember { mutableIntStateOf(value = defaultValue) }
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
            values.forEach {
                Row(
                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = it.first == selected,
                        onClick = {
                            onValueSelected(it.first)
                            selected = it.first
                        }
                    )

                    Text(
                        modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                        fontSize = 14.sp,
                        text = it.second
                    )
                }
            }
        }
    }
}