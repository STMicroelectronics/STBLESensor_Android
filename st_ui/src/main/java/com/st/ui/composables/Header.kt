package com.st.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.st.ui.R
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.LocalDimensions

@Composable
fun Header(
    modifier: Modifier = Modifier,
    icon: Int,
    title: String,
    subtitle: String? = null,
    showArrows: Boolean = true,
    isOpen: Boolean = false,
    content: @Composable () -> Unit = { /** NOOP **/ }
) {
    Header(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        showArrows = showArrows,
        isOpen = isOpen,
        content = content,
        icon = {
            Icon(
                modifier = Modifier.size(size = LocalDimensions.current.iconNormal),
                painter = painterResource(id = icon),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )
        }
    )
}

@Composable
fun Header(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    showArrows: Boolean = true,
    isOpen: Boolean = false,
    content: @Composable () -> Unit = { /** NOOP **/ }
) {
    Header(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        showArrows = showArrows,
        isOpen = isOpen,
        content = content,
        icon = {
            Icon(
                modifier = Modifier.size(size = LocalDimensions.current.iconNormal),
                imageVector = icon,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )
        }
    )
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = { /** NOOP **/ },
    title: String,
    subtitle: String?,
    showArrows: Boolean,
    isOpen: Boolean,
    content: @Composable () -> Unit = { /** NOOP **/ }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = LocalDimensions.current.paddingNormal)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = LocalDimensions.current.paddingNormal),
            verticalAlignment = Alignment.Top,
        ) {
            icon()

            Spacer(modifier = Modifier.weight(weight = 1f))

            if (showArrows) {
                if (isOpen) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = LocalDimensions.current.paddingNormal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal)) {
                Text(
                    color = MaterialTheme.colorScheme.primary,
                    text = title,
                    fontWeight = FontWeight.Bold
                )

                subtitle?.let {
                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

                    Text(text = it)
                }
            }

            Spacer(modifier = Modifier.weight(weight = 1f))

            content()
        }

        if (isOpen) {
            Divider()
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun HeaderPreview() {
    BlueMSTheme {
        Header(
            icon = R.drawable.ic_home,
            title = "title",
            subtitle = "subtitle",
            showArrows = true,
            isOpen = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HeaderWithoutSubtitlePreview() {
    BlueMSTheme {
        Header(
            icon = R.drawable.ic_home,
            title = "title",
            showArrows = true,
            isOpen = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HeaderWithExtraContentPreview() {
    BlueMSTheme {
        Header(
            icon = R.drawable.ic_home,
            title = "title",
            subtitle = "subtitle",
            showArrows = true,
            isOpen = false
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null
            )
        }
    }
}
