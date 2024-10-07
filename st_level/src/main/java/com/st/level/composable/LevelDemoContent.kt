package com.st.level.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.level.LevelViewModel
import com.st.level.R
import com.st.level.model.LevelDemo
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey3
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.PrimaryYellow
import com.st.ui.theme.SuccessHover
import com.st.ui.theme.SuccessText
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin

private fun computeXFactor(currentLevelMode: LevelDemo, pitch: Float): Float {
    return when (currentLevelMode) {
        LevelDemo.PITCH_ROLL -> {
            sin(Math.toRadians(pitch.toDouble())).toFloat()
        }

        LevelDemo.PITCH -> {
            sin(Math.toRadians(pitch.toDouble())).toFloat()
        }

        LevelDemo.ROLL -> {
            0f
        }
    }
}

private fun computeYFactor(currentLevelMode: LevelDemo, roll: Float): Float {
    return when (currentLevelMode) {
        LevelDemo.PITCH_ROLL -> {
            sin(Math.toRadians(roll.toDouble())).toFloat()
        }

        LevelDemo.PITCH -> {
            0f
        }

        LevelDemo.ROLL -> {
            sin(Math.toRadians(roll.toDouble())).toFloat()
        }
    }
}

@Composable
fun LevelDemoContent(
    modifier: Modifier,
    viewModel: LevelViewModel
) {
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    var mZeroPitch by remember { mutableFloatStateOf(value = 0f) }
    var mZeroRoll by remember { mutableFloatStateOf(value = 0f) }

    val levelData by viewModel.levelData.collectAsStateWithLifecycle()

    var currentLevelMode by remember { mutableStateOf(value = LevelDemo.PITCH_ROLL) }

    val pitch by remember(key1 = levelData) {
        derivedStateOf { levelData.pitch.value - mZeroPitch }
    }

    val roll by remember(key1 = levelData) {
        derivedStateOf { levelData.roll.value - mZeroRoll }
    }


    val animatedColorCircles by animateColorAsState(
        if ((abs(
                computeXFactor(
                    currentLevelMode,
                    pitch
                )
            ) < 0.05f) && (abs(
                computeYFactor(
                    currentLevelMode,
                    roll
                )
            ) < 0.05f)
        ) SuccessText else PrimaryYellow,
        label = "color"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
    ) {
//        LevelDropDownMenu(
//            title = "Mode:",
//            initialValue = currentLevelMode.toString(),
//            values = LevelDemo.entries.map { it.toString() },
//            onValueSelected = { eventSelected ->
//                currentLevelMode = LevelDemo.fromString(eventSelected)
//            })

        LevelModeRadioButton(
            initialValue = currentLevelMode.toString(),
            values = LevelDemo.entries.map { it.toString() },
            onValueSelected = { eventSelected ->
                currentLevelMode = LevelDemo.fromString(eventSelected)
            })

        Canvas(
            modifier = Modifier
                .weight(1f)
                //.aspectRatio(ratio = 1f)
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingLarge)
        ) {

            drawLine(
                start = Offset(x = size.width * 0.05f, y = size.height / 2),
                end = Offset(x = size.width * 0.95f, y = size.height / 2),
                color = Grey3,
                strokeWidth = 5f,
                pathEffect = pathEffect
            )

            drawLine(
                start = Offset(x = size.width / 2, y = 0f),
                end = Offset(x = size.width / 2, y = size.height),
                color = Grey3,
                strokeWidth = 5f,
                pathEffect = pathEffect
            )

            //Big Circle
            drawCircle(
                color = animatedColorCircles,
                radius = size.width * 0.9f / 2f,
                style = Stroke(
                    width = 50f
                )
            )

            //60% Dashed Fixed Circle
            drawCircle(
                color = Grey3,
                center = center,
                radius = size.width * 0.6f / 2f,
                style = Stroke(
                    width = 5f,
                    pathEffect = pathEffect
                )
            )

            //30% Dashed Fixed Circle
            drawCircle(
                color = Grey3,
                center = center,
                radius = size.width * 0.3f / 2f,
                style = Stroke(
                    width = 5f,
                    pathEffect = pathEffect
                )
            )

            //15% Little Dashed Fixed Circle
            drawCircle(
                color = SuccessHover,
                center = center,
                radius = size.width * 0.15f / 2f,
                style = Stroke(
                    width = 5f,
                    pathEffect = pathEffect
                )
            )


            //Little Fixed Circle
            drawCircle(
                color = Grey3,
                center = center,
                radius = size.width * 0.1f / 2f,
            )

            //Moving Circle
            drawCircle(
                center = Offset(
                    x = size.width / 2f + size.width * 0.50f * computeXFactor(
                        currentLevelMode,
                        pitch
                    ),
                    y = size.height / 2f + size.height * 0.50f * computeYFactor(
                        currentLevelMode,
                        roll
                    )
                ),
                color = animatedColorCircles,
                radius = size.width * 0.1f / 2f,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
            ) {
                BlueMsButton(text = "Set zero") {
                    mZeroPitch = pitch
                    mZeroRoll = roll
                }

                Text(
                    text = "Pitch",
                    style = MaterialTheme.typography.bodySmall
                )

                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium)
                        .rotate(pitch),
                    painter = painterResource(R.drawable.ic_half_circle),
                    tint = PrimaryBlue,
                    contentDescription = null
                )

                Text(
                    text = String.format(
                        Locale.getDefault(), "Offset: %3.2f", pitch
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
            ) {
                BlueMsButton(text = "Reset zero") {
                    mZeroPitch = 0f
                    mZeroRoll = 0f
                }

                Text(
                    text = "Roll",
                    style = MaterialTheme.typography.bodySmall
                )

                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium)
                        .rotate(roll),
                    painter = painterResource(R.drawable.ic_half_circle),
                    tint = PrimaryBlue,
                    contentDescription = null
                )

                Text(
                    text = String.format(
                        Locale.getDefault(), "Offset: %3.2f", roll
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}