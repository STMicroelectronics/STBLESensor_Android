package com.st.ui.composables

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey7
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.PrimaryYellow
import com.st.ui.theme.SuccessText
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun STGaugeMeter(
    modifier: Modifier = Modifier,
    inputValue: Int,
    minValue: Int,
    maxValue: Int,
    listOfColor: List<Color>,
    gaugeSize: STGaugeMeterSize,
    name: String = "",
    displayNeedle: Boolean = false,
    showSmallNeedle: Boolean = false
) {

    val ratio = (inputValue - minValue).toFloat() / (maxValue - minValue).toFloat()

    val scalingFactor = gaugeSize.getScalingFactor()
    val context = LocalContext.current

    Box(modifier = modifier
        .size((100*scalingFactor).dp)
        .padding(4.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val endAngle = 240f
            val inputValueAngle = ratio * endAngle
            val height = size.height
            val width = size.width
            val startAngle = 150f
            val arcHeight = height - (10*scalingFactor).dp.toPx()

            val topLeft = Offset((width - height + (30f*scalingFactor)) / 2f, (height - arcHeight) / 2f)
            val size = Size(arcHeight, arcHeight)
            val style = Stroke(width = (25f*scalingFactor), cap = StrokeCap.Round)

            //Grey Section
            drawArc(
                color = Grey3,
                startAngle = inputValueAngle + startAngle,
                sweepAngle = endAngle - inputValueAngle,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = style
            )

            //Progress Section
            drawArc(
                brush = Brush.horizontalGradient(
                    listOfColor
                ),
                startAngle = startAngle,
                sweepAngle = inputValueAngle,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = style
            )

            if(displayNeedle) {
                val centerOffset = Offset(width / 2f, height / 2f)

                if(showSmallNeedle.not()) {
                    drawCircle(Grey7, 20f * scalingFactor, centerOffset)
                }

                // Calculate needle angle based on inputValue
                val needleAngle = inputValueAngle + startAngle
                val needleLength = 100f*scalingFactor
                val needleBaseWidth = 15f*scalingFactor

                val needlePath = Path().apply {
                    // Calculate the top point of the needle
                    val topX = centerOffset.x + needleLength * cos(
                        Math.toRadians(needleAngle.toDouble()).toFloat()
                    )
                    val topY = centerOffset.y + needleLength * sin(
                        Math.toRadians(needleAngle.toDouble()).toFloat()
                    )

                    val baseOffsetX: Float
                    val baseOffsetY: Float
                    if(showSmallNeedle) {
                        baseOffsetX = 50f*scalingFactor * cos(
                            Math.toRadians(needleAngle.toDouble()).toFloat()
                        )

                        baseOffsetY = 50f*scalingFactor * sin(
                            Math.toRadians(needleAngle.toDouble()).toFloat()
                        )
                    } else {
                        baseOffsetX = 0f
                        baseOffsetY = 0f
                    }

                    // Calculate the base points of the needle
                    val baseLeftX = centerOffset.x + needleBaseWidth * cos(
                        Math.toRadians((needleAngle - 90).toDouble()).toFloat()
                    ) + baseOffsetX
                    val baseLeftY = centerOffset.y + needleBaseWidth * sin(
                        Math.toRadians((needleAngle - 90).toDouble()).toFloat()
                    ) + baseOffsetY
                    val baseRightX = centerOffset.x + needleBaseWidth * cos(
                        Math.toRadians((needleAngle + 90).toDouble()).toFloat()
                    ) + baseOffsetX
                    val baseRightY = centerOffset.y + needleBaseWidth * sin(
                        Math.toRadians((needleAngle + 90).toDouble()).toFloat()
                    )+ baseOffsetY

                    moveTo(topX, topY)
                    lineTo(baseLeftX, baseLeftY)
                    lineTo(baseRightX, baseRightY)
                    close()
                }

                drawPath(
                    color = Grey7,
                    path = needlePath
                )

                if(!showSmallNeedle) {
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "$inputValue",
                            centerOffset.x,
                            centerOffset.y + 5f * scalingFactor,
                            Paint().apply {
                                textSize = 20f * scalingFactor
                                textAlign = Paint.Align.CENTER
                                color = ContextCompat.getColor(context, com.st.ui.R.color.Grey0)
                            }
                        )
                    }
                }
            }
        }

        if((!displayNeedle || showSmallNeedle)) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "$inputValue",
                style = gaugeSize.getTextStyle(),
                color = PrimaryBlue
            )
        }

        if(name.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                text = name,
                style = gaugeSize.getTextStyle(),
                color = PrimaryBlue
            )
        }
    }
}

enum class STGaugeMeterSize {
    SMALL,
    MEDIUM,
    BIG;

    @Composable
    fun getTextStyle(): TextStyle {
        return when (this) {
            SMALL -> MaterialTheme.typography.bodySmall
            MEDIUM -> MaterialTheme.typography.titleLarge
            BIG -> MaterialTheme.typography.displaySmall
        }
    }

    fun getScalingFactor(): Int {
        return when (this) {
            SMALL -> 1
            MEDIUM -> 2
            BIG -> 3
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun STGaitMeter2Preview() {
    STGaugeMeter(
        inputValue = 90, listOfColor = listOf(
            SuccessText,
            PrimaryYellow,
            ErrorText
        ), minValue = 0, maxValue = 100, gaugeSize = STGaugeMeterSize.SMALL,
        displayNeedle = true,
        showSmallNeedle = true
    )
}

@Preview(showBackground = true)
@Composable
private fun STGaitMeter1Preview() {
    STGaugeMeter(
        inputValue = 10, listOfColor = listOf(
            ErrorText,
            PrimaryYellow,
            SuccessText
        ), minValue = 0, maxValue = 100,gaugeSize = STGaugeMeterSize.MEDIUM, displayNeedle = true,
        showSmallNeedle = true,
        name = "Test Numb2"
    )
}

@Preview(showBackground = true)
@Composable
private fun STGaitMeter0Preview() {
    STGaugeMeter(
        inputValue = 60, listOfColor = listOf(
            ErrorText,
            PrimaryYellow,
            SuccessText
        ), minValue = 0, maxValue = 100,gaugeSize = STGaugeMeterSize.BIG,
        name = "Test Numb1"
    )
}