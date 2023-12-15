package com.st.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.st.ui.theme.Grey0
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Shapes

@Composable
fun BottomAppBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    painter: Painter? = null,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = Shapes.small,
        color = MaterialTheme.colorScheme.primary,
        contentColor = Grey0,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(all = LocalDimensions.current.paddingSmall),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon?.let {
                Icon(
                    modifier = Modifier.size(LocalDimensions.current.iconSmall),
                    imageVector = it,
                    contentDescription = label
                )
            }
            painter?.let {
                Icon(
                    modifier = Modifier.size(LocalDimensions.current.iconSmall),
                    painter = it,
                    contentDescription = label
                )
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

            Text(
                lineHeight = 20.sp,
                letterSpacing = 0.15.sp,
                fontSize = 12.sp,
                text = label
            )
        }
    }
}

@Composable
fun BottomAppBarItemColor(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    painter: Painter? = null,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = Shapes.small,
        color = MaterialTheme.colorScheme.primary,
        contentColor = Grey0,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(all = LocalDimensions.current.paddingSmall),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon?.let {
                Icon(
                    modifier = Modifier.size(LocalDimensions.current.iconSmall),
                    imageVector = it,
                    contentDescription = label
                )
            }
            painter?.let {
                Icon(
                    modifier = Modifier.size(LocalDimensions.current.iconSmall),
                    painter = it,
                    contentDescription = label,
                    tint = color
                )
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

            Text(
                lineHeight = 20.sp,
                letterSpacing = 0.15.sp,
                fontSize = 12.sp,
                text = label,
                color = color
            )
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun BottomAppBarItemPreview() {
    PreviewBlueMSTheme {
        BottomAppBarItem(
            icon = Icons.Default.Home,
            label = "Item",
            onClick = {}
        )
    }
}

