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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.st.ui.R
import com.st.ui.theme.BTN_MAX_LINES
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.Grey0
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun BlueMsButton(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    textColor: Color = Grey0,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    onClick: () -> Unit = { /** NOOP **/ }
) {
    val buttonColors =
        ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = textColor
        )

    Button(
        shape = Shapes.small,
        modifier = modifier
            .defaultMinSize(
                minWidth = LocalDimensions.current.btnMinWidth,
                minHeight = LocalDimensions.current.btnMinHeight
            ),
        onClick = onClick,
        enabled = enabled,
        colors = buttonColors
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

@Composable
fun BlueMsButton(
    modifier: Modifier = Modifier,
    text: String,
    color: Color? = null,
    enabled: Boolean = true,
    imageVector: ImageVector,
    onClick: () -> Unit = { /** NOOP **/ }
) {
    val buttonColors =
        if (color != null) {
            ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = Grey0
            )
        } else {
            ButtonDefaults.buttonColors()
        }

    Button(
        shape = Shapes.small,
        modifier = modifier
            .defaultMinSize(
                minWidth = LocalDimensions.current.btnMinWidth,
                minHeight = LocalDimensions.current.btnMinHeight
            ),
        onClick = onClick,
        enabled = enabled,
        colors = buttonColors
    ) {
        Icon(
            modifier = Modifier.padding(end = LocalDimensions.current.paddingMedium),
            imageVector = imageVector,
            contentDescription = text
        )
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
private fun BlueMsButtonPreview() {
    BlueMSTheme {
        BlueMsButton(
            iconPainter = painterResource(id = R.drawable.ic_home),
            text = "Button"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlueMsButtonWithoutIconPreview() {
    BlueMSTheme {
        BlueMsButton(text = "Button")
    }
}

@Preview(showBackground = true)
@Composable
private fun BlueMsButtonDisabledPreview() {
    BlueMSTheme {
        BlueMsButton(
            text = "Button",
            enabled = false,
            iconPainter = painterResource(id = R.drawable.ic_home)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlueMsButtonDisabledWithoutIconPreview() {
    BlueMSTheme {
        BlueMsButton(text = "Button", enabled = false)
    }
}
