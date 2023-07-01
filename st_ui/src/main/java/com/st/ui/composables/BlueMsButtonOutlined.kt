/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ui.composables

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.st.ui.R
import com.st.ui.theme.BTN_MAX_LINES
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.LocalDimensions

@Composable
fun BlueMsButtonOutlined(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    onClick: () -> Unit = { /** NOOP **/ }
) {
    OutlinedButton(
        shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal),
        modifier = modifier
            .defaultMinSize(minWidth = 120.dp, minHeight = 40.dp),
        onClick = onClick,
        enabled = enabled
    ) {
        if (iconPainter != null) {
            Icon(
                modifier = Modifier.padding(end = LocalDimensions.current.paddingMedium),
                painter = iconPainter,
                contentDescription = text
            )
        }
        Text(
            textAlign = TextAlign.Center,
            text = text.uppercase(),
            maxLines = BTN_MAX_LINES,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun BlueMsButtonOutlinedPreview() {
    BlueMSTheme {
        BlueMsButtonOutlined(
            iconPainter = painterResource(id = R.drawable.ic_home),
            text = "Button"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlueMsButtonOutlinedWithoutIconPreview() {
    BlueMSTheme {
        BlueMsButtonOutlined(text = "Button")
    }
}

@Preview(showBackground = true)
@Composable
private fun BlueMsButtonOutlinedDisabledPreview() {
    BlueMSTheme {
        BlueMsButtonOutlined(
            text = "Button",
            enabled = false,
            iconPainter = painterResource(id = R.drawable.ic_home)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlueMsButtonOutlinedDisabledWithoutIconPreview() {
    BlueMSTheme {
        BlueMsButtonOutlined(text = "Button", enabled = false)
    }
}
