package com.st.flow_demo.composable.custom_flow.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun FlowDemoBooleanEntry(
    modifier: Modifier = Modifier,
    title: String,
    label: String? = null,
    defaultValue: Boolean,
    onModified: (Boolean) -> Unit = { /** NOOP **/ }

) {
    var selectedValue by remember { mutableStateOf(value = defaultValue) }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(LocalDimensions.current.paddingSmall),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationSmall
    ) {
        Column(
            modifier = Modifier.padding(LocalDimensions.current.paddingNormal)
        ) {
            Text(
                modifier = Modifier.padding(
                    bottom = LocalDimensions.current.paddingNormal
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                text = title
            )
            Row(
                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                label?.let {
                    Text(
                        modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                        fontSize = 14.sp,
                        text = label
                    )
                }

                Switch(
                    checked = selectedValue, onCheckedChange = {
                        selectedValue = it
                        onModified(it)
                    },
                    colors = SwitchDefaults.colors(
                        uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedTrackColor = Grey6,
                        disabledUncheckedTrackColor = Grey3
                    )
                )
            }
        }
    }
}