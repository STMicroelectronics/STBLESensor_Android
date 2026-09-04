package com.st.compass.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.compass.CompassViewModel
import com.st.compass.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import com.st.ui.theme.Grey0
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText


@Composable
fun CompassDemoContent(
    modifier: Modifier,
    viewModel: CompassViewModel
) {

    val calibration by viewModel.calibrationStatus.collectAsStateWithLifecycle()
    val compassInfo by viewModel.compassInfo.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(value = false) }

    Box(
        modifier = modifier.padding(
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    top = LocalDimensions.current.paddingLarge,
                    bottom = LocalDimensions.current.paddingLarge,
                    start = LocalDimensions.current.paddingNormal,
                    end = LocalDimensions.current.paddingNormal
                ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = LocalDimensions.current.paddingLarge),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = getOrientationName(compassInfo.angle.value)
            )

            Box(
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.compass_background),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth
                )

                Image(
                    painter = painterResource(R.drawable.compass_needle),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.rotate(compassInfo.angle.value)
                )

            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = LocalDimensions.current.paddingLarge),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = String.format(
                    LocalLocale.current.platformLocale,
                    "Angle: %1\$3.2f°",
                    compassInfo.angle.value
                )
            )
        }

        Surface(
            modifier = Modifier
                .padding(all = LocalDimensions.current.paddingSmall)
                .align(Alignment.BottomEnd)
                .padding(end = LocalDimensions.current.paddingLarge),
            shape = Shapes.small,
            color = if (calibration) {
                SuccessText
            } else {
                PrimaryBlue
            }
        ) {
            Icon(
                modifier = Modifier
                    .width(100.dp)
                    .height(42.dp)
                    .clickable {
                        viewModel.startCalibration()
                        showDialog = true
                    },
                painter = painterResource(R.drawable.calibration),
                tint = Grey0,
                contentDescription = null
            )
        }
    }

    if (showDialog && !calibration) {
        AlertDialog(
            modifier = Modifier.alpha(0.90f),
            onDismissRequest = { showDialog = false },
            confirmButton = {
                BlueMsButton(
                    onClick = {
                        showDialog = false
                    },
                    text = "OK"
                )
            },
            title = {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Calibration on Going"
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingNormal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                ) {


                    Icon(
                        modifier = Modifier
                            .size(LocalDimensions.current.imageLarge),
                        painter = painterResource(
                            R.drawable.calibration
                        ),
                        tint = PrimaryBlue,
                        contentDescription = null
                    )

                    Text(
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Grey6,
                        text = "Usage: move the board as shown in the image"
                    )
                }
            }
        )
    }
}

fun getOrientationName(angle: Float): String {
    val orientations = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val nOrientation: Int = orientations.size
    val section = 360.0f / nOrientation
    val relativeAngle = angle - section / 2 + 360.0f
    val index = (relativeAngle / section).toInt() + 1
    return orientations[index % nOrientation]
}

