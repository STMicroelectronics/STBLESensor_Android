/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.st.blue_sdk.services.debug.DebugMessage
import com.st.demo_showcase.R
import com.st.demo_showcase.ui.DebugConsoleMsg
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Grey6
import com.st.ui.theme.SuccessText
import kotlinx.coroutines.launch

const val HELP_MSG = "help"

@Composable
fun DebugConsoleScreen(
    modifier: Modifier = Modifier,
    debugMessages: List<DebugConsoleMsg> = emptyList(),
    onClear: () -> Unit = { /** NOOP **/ },
    onSendDebugMessage: (String) -> Unit = { /** NOOP **/ }
) {
    val scrollState = rememberScrollState()
    var autoScroll by rememberSaveable { mutableStateOf(value = true) }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = debugMessages.size) {
        coroutineScope.launch {
            if (autoScroll) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }

    var queryState by rememberSaveable { mutableStateOf(value = "") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val annotatedString: AnnotatedString = buildAnnotatedString {
        debugMessages.forEach {
            when (it) {
                is DebugConsoleMsg.DebugConsoleCommand -> {
                    withStyle(style = SpanStyle(SecondaryBlue)) {
                        append("[${it.time}>] ${it.command}")
                        append("\n")
                    }
                }

                is DebugConsoleMsg.DebugConsoleResponse -> {
                    withStyle(style = SpanStyle(if (it.response.isError) ErrorText else Grey6)) {
                        if(it.time!=null) {
                            append("[${it.time}<] ")
                        }
                        append(it.response.payload)
                        //append("\n\n")
                    }
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { onSendDebugMessage(HELP_MSG) }) {
                Icon(
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.AutoMirrored.Filled.Help,
                    contentDescription = null
                )
            }
            IconButton(onClick = onClear) {
                Icon(
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
            }
            IconButton(onClick = { autoScroll = autoScroll.not() }) {
                Icon(
                    tint = if (autoScroll) SuccessText else ErrorText,
                    imageVector = if (autoScroll)  Icons.Default.Download else Icons.Default.FileDownloadOff,
                    contentDescription = null
                )
            }
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f)
                .padding(all = LocalDimensions.current.paddingNormal)
                .verticalScroll(state = scrollState),
            style = MaterialTheme.typography.bodyMedium,
            text = annotatedString
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal),
            value = queryState,
            label = { Text(text = stringResource(id = R.string.st_debugConsole_hint)) },
            onValueChange = { queryState = it },
            trailingIcon = {
                IconButton(onClick = {
                    onSendDebugMessage(queryState)
                    queryState = ""
                    keyboardController?.hide()
                }) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null
                    )
                }
            }
        )

        Spacer(
            Modifier.windowInsetsBottomHeight(
                WindowInsets.navigationBars
            )
        )

        Spacer(modifier = Modifier.imePadding())
    }

}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun DebugConsoleScreenPreview() {
    PreviewBlueMSTheme {
        DebugConsoleScreen(
            debugMessages = listOf(
                DebugConsoleMsg.DebugConsoleCommand(
                    command = "help",
                    time = "11111111"
                ),
                DebugConsoleMsg.DebugConsoleResponse(
                    response = DebugMessage(
                        payload = "test", isError = false
                    ),
                    time = "11111111"
                ),
                DebugConsoleMsg.DebugConsoleCommand(
                    command = "info",
                    time = "11111111"
                ),
                DebugConsoleMsg.DebugConsoleResponse(
                    response = DebugMessage(
                        payload = "test", isError = true
                    ),
                    time = "11111111"
                )
            )
        )
    }
}
