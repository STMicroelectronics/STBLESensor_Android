package com.st.pnpl.composable

import android.R
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.sp
import com.st.blue_sdk.features.extended.pnpl.PnPL.Companion.ERROR_MESSAGE_JSON_KEY
import com.st.blue_sdk.features.extended.pnpl.PnPL.Companion.INFO_MESSAGE_JSON_KEY
import com.st.blue_sdk.features.extended.pnpl.PnPL.Companion.WARNING_MESSAGE_JSON_KEY
import com.st.pnpl.composable.PnPLSpontaneousMessageType.ERROR
import com.st.pnpl.composable.PnPLSpontaneousMessageType.INFO
import com.st.pnpl.composable.PnPLSpontaneousMessageType.OK
import com.st.pnpl.composable.PnPLSpontaneousMessageType.WARNING
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.ErrorText
import com.st.ui.theme.InfoText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import com.st.ui.theme.WarningText
import com.st.ui.utils.asString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class PnPLSpontaneousMessageType {
    ERROR,
    WARNING,
    INFO,
    OK;

    var message: String = ""
    var extraMessage: String? = null
    var extraUrl: String? = null
}

fun PnPLSpontaneousMessageType.imageVector(): ImageVector {
    return when (this) {
        ERROR -> Icons.Filled.Error
        WARNING -> Icons.Filled.Warning
        INFO -> Icons.Filled.Info
        OK -> Icons.Filled.Done
    }
}

fun PnPLSpontaneousMessageType.color(): Color {
    return when (this) {
        ERROR -> ErrorText
        WARNING -> WarningText
        INFO -> InfoText
        OK -> SuccessText
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PnPLInfoWarningSpontaneousMessage(
    messageType: PnPLSpontaneousMessageType,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest)
    {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = Shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(all = LocalDimensions.current.paddingMedium)
                    .width(intrinsicSize = IntrinsicSize.Max)
            ) {

                Row(
                    modifier = Modifier
                        .padding(all = LocalDimensions.current.paddingNormal),
                    //.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.padding(end = LocalDimensions.current.paddingNormal),
                        tint = messageType.color(),
                        imageVector = messageType.imageVector(),
                        contentDescription = null
                    )

                    Text(
                        text = "$messageType:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        letterSpacing = 0.15.sp,
                        color = messageType.color()
                    )
                }
                Text(
                    modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                    text = messageType.message,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.25.sp,
                    color = MaterialTheme.colorScheme.primary,
                )

                messageType.extraMessage?.let { extra ->
                    val context = LocalContext.current
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = LocalDimensions.current.paddingNormal)
                    ) {
                        Text(
                            text = extra,
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            letterSpacing = 0.25.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        messageType.extraUrl?.let { url ->
                            TextButton(
                                modifier = Modifier
                                    .align(Alignment.End),
                                onClick = {
                                    Intent(Intent.ACTION_VIEW).also { intent ->
                                        intent.data = Uri.parse(url)
                                        context.startActivity(intent)
                                    }
                                }
                            ) {
                                Text(
                                    text = "Read More…",
                                    fontSize = 12.sp,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.25.sp
                                )
                            }
                        }
                    }
                }

                BlueMsButton(
                    modifier = Modifier
                        .padding(all = LocalDimensions.current.paddingNormal),
                    color = messageType.color(),
                    text = stringResource(id = R.string.ok),
                    onClick = onDismissRequest
                )
            }
        }
    }
}

fun searchInfoWarningError(json: List<JsonObject>): PnPLSpontaneousMessageType? {
    var message: PnPLSpontaneousMessageType? = null
    //Search Error Message
    json.firstOrNull { it.containsKey(ERROR_MESSAGE_JSON_KEY) }?.let { jsonError ->
        try {
            val messageString = jsonError[ERROR_MESSAGE_JSON_KEY]?.jsonPrimitive?.content
            message = ERROR
            if (messageString.isNullOrBlank()) {
                message!!.message = "Generic Error"
            } else {
                message!!.message = messageString
            }
        } catch (ex: Exception) {
            Log.e(ERROR_MESSAGE_JSON_KEY, ex.message, ex)
        }
    }
    //Search Warning Message
    json.firstOrNull { it.containsKey(WARNING_MESSAGE_JSON_KEY) }?.let { jsonError ->
        try {
            val messageString = jsonError[WARNING_MESSAGE_JSON_KEY]?.jsonPrimitive?.content
            message = WARNING
            if (messageString.isNullOrBlank()) {
            } else {
                message!!.message = messageString
            }
        } catch (ex: Exception) {
            Log.e(WARNING_MESSAGE_JSON_KEY, ex.message, ex)
        }
    }
    //Search Info Message
    json.firstOrNull { it.containsKey(INFO_MESSAGE_JSON_KEY) }?.let { jsonError ->
        try {
            val messageString = jsonError[INFO_MESSAGE_JSON_KEY]?.jsonPrimitive?.content
            message = INFO
            if (messageString.isNullOrBlank()) {
                message!!.message = "Generic Info"
            } else {
                message!!.message = messageString
            }
        } catch (ex: Exception) {
            Log.e(INFO_MESSAGE_JSON_KEY, ex.message, ex)
        }
    }
    return message
}

@Composable
@Preview
fun InfoMessage() {
    BlueMSTheme {
        val message = INFO
        message.message = LoremIpsum(20).asString()
        PnPLInfoWarningSpontaneousMessage(messageType = message, onDismissRequest = {})
    }
}

@Composable
@Preview
fun WarningMessage() {
    BlueMSTheme {
        val message = WARNING
        message.message = LoremIpsum(10).asString()
        PnPLInfoWarningSpontaneousMessage(messageType = message, onDismissRequest = {})
    }
}

@Composable
@Preview
fun ErrorMessage() {
    BlueMSTheme {
        val message = ERROR
        message.message = LoremIpsum(15).asString()
        PnPLInfoWarningSpontaneousMessage(messageType = message, onDismissRequest = {})
    }
}

@Composable
@Preview
fun ErrorMessageCustom() {
    BlueMSTheme {
        val message = ERROR
        message.message = LoremIpsum(15).asString()
        message.extraMessage = LoremIpsum(15).asString()
        message.extraUrl = "www.st.com"
        PnPLInfoWarningSpontaneousMessage(messageType = message, onDismissRequest = {})
    }
}

@Composable
@Preview
fun OkMessage() {
    BlueMSTheme {
        val message = OK
        message.message = LoremIpsum(4).asString()
        PnPLInfoWarningSpontaneousMessage(messageType = message, onDismissRequest = {})
    }
}