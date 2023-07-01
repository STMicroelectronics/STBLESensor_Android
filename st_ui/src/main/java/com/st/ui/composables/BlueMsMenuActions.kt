package com.st.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.st.ui.theme.LocalDimensions

@Composable
fun BlueMsMenuActions(
    modifier: Modifier = Modifier,
    actions: List<ActionItem>,
    menuIcon: ImageVector = Icons.Default.MoreVert,
    content: @Composable () -> Unit = { /** NOOP **/ }
) {
    var menuExpanded by remember { mutableStateOf(value = false) }

    if (actions.isNotEmpty()) {
        Row(modifier = modifier) {
            if (actions.size == 1) {
                IconButton(onClick = actions[0].action) {
                    Icon(
                        imageVector = actions[0].imageVector ?: menuIcon,
                        contentDescription = actions[0].label
                    )
                }
            } else {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = menuIcon,
                        contentDescription = "more actions"
                    )
                }
                DropdownMenu(
                    modifier = Modifier.fillMaxWidth(fraction = 0.5f),
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    actions.forEach { action ->
                        DropdownMenuItem(
                            onClick = {
                                action.action()

                                menuExpanded = false
                            },
                            text = {
                                Row {
                                    action.imageVector?.let {
                                        Icon(
                                            imageVector = it,
                                            contentDescription = action.label
                                        )
                                        Spacer(
                                            modifier = Modifier.width(
                                                width = LocalDimensions.current.paddingNormal
                                            )
                                        )
                                    }
                                    Text(text = action.label.uppercase())
                                }
                            }
                        )
                    }

                    content()
                }
            }
        }
    }
}

data class ActionItem(
    val imageVector: ImageVector? = null,
    val label: String,
    val description: String = "",
    val action: () -> Unit = { /** NOOP **/ }
)
