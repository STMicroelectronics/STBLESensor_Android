package com.st.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes

@Composable
fun BlueMSDialogCircularProgressIndicator(
    percentage: Float?=0f,
    percentageFontSize: TextUnit = 28.sp,
    message: String? = null,
    messageStyle: TextStyle? = null,
    radius: Dp = 50.dp,
    strokeWidth: Dp = 8.dp,
    color: Color = PrimaryBlue,
    colorFill: Color? = SecondaryBlue
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        BlueMSCircularProgressIndicator(
            percentage = percentage,
            percentageFontSize = percentageFontSize,
            message = message,
            messageStyle = messageStyle,
            radius = radius,
            strokeWidth = strokeWidth,
            color = color,
            colorFill = colorFill
        )
    }
}

@Composable
fun BlueMSCircularProgressIndicator(
    modifier: Modifier = Modifier,
    percentage: Float?=0f,
    percentageFontSize: TextUnit = 28.sp,
    message: String? = null,
    messageStyle: TextStyle? = null,
    radius: Dp = 50.dp,
    strokeWidth: Dp = 8.dp,
    color: Color = PrimaryBlue,
    colorFill: Color? = SecondaryBlue
) {
    val percentageInternal = percentage ?: 0f
    Surface(
        modifier = Modifier
            .padding(all = LocalDimensions.current.paddingNormal),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Column(
            modifier = modifier.padding(all = LocalDimensions.current.paddingNormal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
        ) {

            if (message != null) {
                Text(
                    text = message,
                    style = messageStyle ?: MaterialTheme.typography.titleMedium
                )
            }

            Box(modifier = Modifier.size(radius * 2.3f), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(radius * 2f)) {
                    if (colorFill != null) {
                        drawArc(
                            color = colorFill,
                            startAngle = -90f,
                            sweepAngle = 3.6f * percentageInternal,
                            useCenter = true,
                            style = Fill
                        )

                        drawArc(
                            color = color,
                            startAngle = -90f,
                            sweepAngle = 3.6f * percentageInternal,
                            useCenter = false,
                            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Butt)
                        )
                    } else {
                        drawArc(
                            color = color,
                            startAngle = -90f,
                            sweepAngle = 3.6f * percentageInternal,
                            useCenter = false,
                            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Text(
                    text = "${percentageInternal.toInt()}%",
                    color = Color.Black,
                    fontSize = percentageFontSize,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
@Preview
fun CircularProgressIndicatorPreview() {
    BlueMSCircularProgressIndicator(percentage = 25f, message = "Optional Message")
}

@Composable
@Preview
fun CircularProgressIndicatorPreview2() {
    BlueMSCircularProgressIndicator(percentage = 75f, colorFill = null)
}