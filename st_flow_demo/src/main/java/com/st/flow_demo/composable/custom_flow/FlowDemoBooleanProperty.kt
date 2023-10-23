package com.st.flow_demo.composable.custom_flow

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.st.ui.composables.LocalLastStatusUpdatedAt
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions

@Composable
fun FlowDemoBooleanProperty(
    modifier: Modifier = Modifier,
    label: String = "",
    unit: String = "",
    value: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (Boolean) -> Unit
) {
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    var internalState by rememberSaveable(
        value,
        lastStatusUpdatedAt
    ) { mutableStateOf(value = value) }

    Row(
        modifier = modifier.padding(start = LocalDimensions.current.paddingNormal,end = LocalDimensions.current.paddingNormal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (unit.isEmpty()) label else "$label [$unit]"

        Switch(
            modifier = Modifier.padding(end = LocalDimensions.current.paddingLarge),
            enabled = enabled,
            checked = internalState,
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                uncheckedTrackColor = Grey6,
                disabledUncheckedTrackColor = Grey3
            ),
            onCheckedChange = {
                internalState = it
                onValueChange(internalState)
            }
        )

        Text(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            text = text
        )

    }
}