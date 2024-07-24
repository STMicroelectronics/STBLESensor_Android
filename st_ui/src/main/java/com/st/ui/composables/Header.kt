package com.st.ui.composables

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.sp
import com.st.ui.R
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.WarningText

@Composable
fun Header(
    modifier: Modifier = Modifier,
    icon: Int,
    title: String,
    subtitle: String? = null,
    showArrows: Boolean = true,
    isOpen: Boolean = false,
    isMounted: Boolean = true,
    content: @Composable () -> Unit = { /** NOOP **/ }
) {
    Header(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        showArrows = showArrows,
        isOpen = isOpen,
        content = content,
        isMounted = isMounted,
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
    isMounted: Boolean = true,
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
            horizontalArrangement = Arrangement.Absolute.SpaceBetween
        ) {
            icon()

            //Spacer(modifier = Modifier.weight(weight = 1f))

            if (!isMounted) {
                Text(
                    modifier = Modifier.padding(top = LocalDimensions.current.paddingSmall),
                    text = "Not Mounted", color = WarningText, fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 22.68.sp,
                    letterSpacing = 0.14.sp
                    )
            }
            //Spacer(modifier = Modifier.weight(weight = 1f))

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal)) {
                Text(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 22.68.sp,
                    letterSpacing = 0.14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    text = title
                )

                subtitle?.let {
                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

                    Text(
                        fontSize = 12.sp,
                        lineHeight = 18.9.sp,
                        letterSpacing = 0.24.sp,
                        color = Grey6,
                        text = it
                    )
                }
            }

            Spacer(modifier = Modifier.weight(weight = 1f))

            content()
        }

        if (isOpen) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = LocalDimensions.current.paddingNormal)
            )
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
            isMounted = false,
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
