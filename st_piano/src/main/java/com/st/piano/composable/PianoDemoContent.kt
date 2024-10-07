package com.st.piano.composable

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.st.piano.PianoViewModel
import com.st.piano.model.PianoKeyRect
import com.st.piano.model.searchKey
import com.st.ui.theme.PrimaryYellow

private const val numberWhiteKeys = 14

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PianoDemoContent(
    modifier: Modifier = Modifier,
    viewModel: PianoViewModel,
    nodeId: String
) {

    var currentKey by remember { mutableStateOf<PianoKeyRect?>(null) }

    var whiteKeys by remember { mutableStateOf<List<PianoKeyRect>>(listOf()) }
    var blackKeys by remember { mutableStateOf<List<PianoKeyRect>>(listOf()) }
    var greyKeys by remember { mutableStateOf<List<PianoKeyRect>>(listOf()) }

    Canvas(modifier = modifier
        .fillMaxSize()
        .onSizeChanged { intSize ->
            //Decide the size of the piano's Keys
            val canvasWidth = intSize.width.toFloat()
            val canvasHeight = intSize.height.toFloat()
            val rectHeight = canvasHeight / numberWhiteKeys
            var keyNumber = 0
            val listWhite: MutableList<PianoKeyRect> = mutableListOf()
            val listBlack: MutableList<PianoKeyRect> = mutableListOf()
            val listGrey: MutableList<PianoKeyRect> = mutableListOf()
            for (i in 0..<numberWhiteKeys) {
                listWhite.add(
                    PianoKeyRect(
                        topLeft = Offset(x = 0f, rectHeight * i),
                        size = Size(canvasWidth, rectHeight),
                        sound = keyNumber,
                        white = true
                    )
                )
                keyNumber++
                if (i != 2 && i != 6 && i != 9 && i != 13) {
                    listBlack.add(
                        PianoKeyRect(
                            topLeft = Offset(
                                x = 0.3f * canvasWidth,
                                rectHeight * (i + 0.6f)
                            ),
                            size = Size(canvasWidth, rectHeight * 0.8f),
                            sound = keyNumber,
                            white = false
                        )
                    )
                    listGrey.add(
                        PianoKeyRect(
                            topLeft = Offset(x = 0.34f * canvasWidth, rectHeight * (i + 0.7f)),
                            size = Size(canvasWidth, rectHeight * 0.60f),
                            sound = keyNumber,
                            white = false
                        )
                    )
                    keyNumber++
                }
            }
            whiteKeys = listWhite.toList()
            blackKeys = listBlack.toList()
            greyKeys = listGrey.toList()
        }
        .pointerInteropFilter {
            //Handle the touch event and find the right piano's Key
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    val key = searchKey(
                        x = it.x,
                        y = it.y,
                        whiteKeys = whiteKeys,
                        blackKeys = blackKeys
                    )
                    key?.let {
                        currentKey = key
                        viewModel.writePianoStartCommand(sound = key.sound, nodeId)
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    val key = searchKey(
                        x = it.x,
                        y = it.y,
                        whiteKeys = whiteKeys,
                        blackKeys = blackKeys
                    )
                    key?.let {
                        if (currentKey == null) {
                            currentKey = key
                            viewModel.writePianoStartCommand(sound = key.sound, nodeId)
                        } else if (currentKey != key) {
                            currentKey = key
                            viewModel.writePianoStartCommand(sound = key.sound, nodeId)
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    currentKey = null
                    viewModel.writePianoStopCommand(nodeId)
                }

                else -> {}
            }
            true
        }
    ) {

        //White keys
        for (key in whiteKeys) {
            if (currentKey != null) {
                drawRect(
                    color = if (currentKey!!.sound == key.sound) PrimaryYellow else Color.Black,
                    topLeft = key.topLeft,
                    size = key.size,
                    style = if (currentKey!!.sound == key.sound) Fill else Stroke(width = 2.dp.toPx())
                )
            } else {
                drawRect(
                    color = Color.Black,
                    topLeft = key.topLeft,
                    size = key.size,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        //Black keys
        for (key in blackKeys) {
            if (currentKey != null) {
                drawRect(
                    color = if (currentKey!!.sound == key.sound) PrimaryYellow else Color.Black,
                    topLeft = key.topLeft,
                    size = key.size,
                    style = Fill
                )
            } else {
                drawRect(
                    color = Color.Black,
                    topLeft = key.topLeft,
                    size = key.size,
                    style = Fill
                )
            }
        }

        //Grey keys
        for (key in greyKeys) {
            if (currentKey != null) {
                if (currentKey!!.sound != key.sound) {
                    drawRect(
                        color = Color.Gray,
                        topLeft = key.topLeft,
                        size = key.size,
                        style = Fill
                    )
                }
            } else {
                drawRect(
                    color = Color.Gray,
                    topLeft = key.topLeft,
                    size = key.size,
                    style = Fill
                )
            }
        }
    }
}
