package com.st.ui.composables

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
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
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = featureName
            )

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
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
fun BlueMSFeatureItemPreview() {
    PreviewBlueMSTheme {
        BlueMSFeatureItem(isChecked = false, featureName = "TestFeature")
    }
}