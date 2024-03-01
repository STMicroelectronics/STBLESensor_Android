package com.st.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun BlueMSFeatureItem(isChecked: Boolean=false, featureName: String, onSelected: (Boolean) -> Unit = { /** Noop **/ }) {

    var isLocalChecked by remember {
        mutableStateOf(isChecked)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Row(
            modifier = Modifier.padding(LocalDimensions.current.paddingNormal),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isLocalChecked,
                colors = SwitchDefaults.colors(
                    uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedTrackColor = Grey6,
                    disabledUncheckedTrackColor = Grey3
                ),
                onCheckedChange = {
                    isLocalChecked = !isLocalChecked
                    onSelected(isLocalChecked)
                }
            )

            Text(
                modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = featureName
            )
        }
    }
}