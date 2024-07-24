/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.st.demo_showcase.models.Demo
import com.st.demo_showcase.utils.getDescription
import com.st.ui.theme.*


@Composable
fun DemoListItem(
    modifier: Modifier = Modifier,
    item: Demo,
    even: Boolean,
    isLoginLock: Boolean = false,
    isExpertLock: Boolean = false,
    isPnPLLock: Boolean = false,
    isLastFwLock: Boolean = false,
    isLastOne: Boolean,
    onLoginRequired: () -> Unit = { /** NOOP **/ },
    onExpertRequired: () -> Unit = { /** NOOP **/ },
    onPnPLRequired: () -> Unit = { /** NOOP **/ },
    onExpertLoginRequired: () -> Unit = { /** NOOP **/ },
    onLastFwRequired: () -> Unit = { /** NOOP **/ },
    onDemoSelected: (Demo) -> Unit = { /** NOOP **/ }
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            if (isLoginLock && isExpertLock) {
                onExpertLoginRequired()
            } else {
                if (isLoginLock) {
                    onLoginRequired()
                } else {
                    if (isExpertLock) {
                        onExpertRequired()
                    } else {
                        if(isPnPLLock) {
                            onPnPLRequired()
                        } else {
                            if(isLastFwLock) {
                                onLastFwRequired()
                            } else {
                                onDemoSelected(item)
                            }
                        }
                    }
                }
            }
        }
    ) {
        val alpha = if (isLoginLock || isExpertLock || isPnPLLock || isLastFwLock) 0.4f else 1f

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconMedium)
                        .padding(all = LocalDimensions.current.paddingSmall)
                        .alpha(alpha),
                    shape = Shapes.small,
                    color = if (even) SecondaryBlue else PrimaryYellow
                ) {
                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.iconMedium)
                            .padding(all = LocalDimensions.current.paddingNormal),
                        tint = PrimaryBlue3,
                        painter = painterResource(id = item.icon),
                        contentDescription = null
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .padding(all = LocalDimensions.current.paddingNormal)
                        .alpha(alpha)
                ) {
                    Text(
                        maxLines = TITLE_MAX_LINES,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        text = item.displayName
                    )
                    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))
                    Text(
                        maxLines = CAPTION_MAX_LINES,
                        style = MaterialTheme.typography.bodySmall,
                        color = Grey6,
                        text = item.getDescription(context = context),
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isLoginLock || isExpertLock || isPnPLLock || isLastFwLock) {
                    Icon(
                        modifier = Modifier.size(size = LocalDimensions.current.iconSmall),
                        tint = Grey6,
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )
                }
            }

            if (isLastOne.not()) {
                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
                HorizontalDivider()
            }
        }

    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun DemoListItemPreview() {
    PreviewBlueMSTheme {
        DemoListItem(
            item = Demo.Environmental,
            even = false,
            isLastOne = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoListItemEvenPreview() {
    PreviewBlueMSTheme {
        DemoListItem(
            item = Demo.Environmental,
            even = true,
            isLastOne = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoListItemLastOnePreview() {
    PreviewBlueMSTheme {
        DemoListItem(
            item = Demo.Environmental,
            even = true,
            isLastOne = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoListItemLockPreview() {
    PreviewBlueMSTheme {
        DemoListItem(
            item = Demo.Environmental,
            even = true,
            isLoginLock = true,
            isLastOne = true
        )
    }
}
