package com.st.ui.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.st.ui.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.st.ui.theme.Grey10
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.SecondaryBlue

@Composable
fun BlueMSCheckBox(
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    tintCheckedImage: Color = SecondaryBlue,
    label: String,
    onClicked: () -> Unit = { /** NOOP **/ }
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
    ) {
        Box(
            modifier = Modifier
                .size(LocalDimensions.current.iconSmall + 1.dp)
                .border(1.dp, Grey10)
                .clickable { onClicked() }) {
            if (checked) {
                Icon(
                    modifier = Modifier.size(LocalDimensions.current.iconSmall),
                    painter = painterResource(id = R.drawable.single_check),
                    contentDescription = "check image",
                    tint = tintCheckedImage
                )
            }
        }
        Text(
            text = label,
            maxLines = 1
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/


@Preview(showBackground = true)
@Composable
private fun BlueMSCheckBoxCheckedPreview() {
    PreviewBlueMSTheme {
        BlueMSCheckBox(label = "Check", checked = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun BlueMSCheckBoxPreview() {
    PreviewBlueMSTheme {
        BlueMSCheckBox(label = "No Checked")
    }
}