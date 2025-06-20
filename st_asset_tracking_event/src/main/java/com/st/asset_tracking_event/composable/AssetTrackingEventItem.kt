package com.st.asset_tracking_event.composable

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.st.asset_tracking_event.R
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventData
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventType
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Shapes
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.st.ui.theme.PrimaryBlue
import kotlin.math.sqrt
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import java.util.Locale

@Composable
fun AssetTrackingEventItem(
    modifier: Modifier = Modifier,
    timestamp: Long,
    event: AssetTrackingEventData,
    //isTheLastElement: Boolean = false,
    onShockInfoClick: () -> Unit = {}
) {

//    var isAnimated by remember { mutableStateOf(false) }
//
//    LaunchedEffect(key1 = Unit) {
//        if (isTheLastElement) {
//            isAnimated = true
//            delay(500)
//            isAnimated = false
//        }
//    }
//
//    val animatedColorBorder by animateColorAsState(
//        if (isAnimated) {
//            PrimaryYellow
//        } else {
//            Color.Unspecified
//        },
//        label = "color"
//    )

    Surface(
        modifier = modifier
            .fillMaxWidth(),
//            .border(
//                BorderStroke(2.dp, animatedColorBorder),
//                Shapes.small
//            ),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalDimensions.current.paddingNormal).height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
        ) {
            when (event.type) {
                AssetTrackingEventType.Fall -> Icon(
                    modifier = Modifier
                        .padding(LocalDimensions.current.paddingSmall)
                        .size(64.dp),
                    tint = PrimaryBlue,
                    painter = painterResource(R.drawable.event_fall),
                    contentDescription = null
                )

                AssetTrackingEventType.Shock -> Icon(
                    modifier = Modifier
                        .padding(LocalDimensions.current.paddingSmall)
                        .size(64.dp),
                    tint = PrimaryBlue,
                    painter = painterResource(R.drawable.event_shock),
                    contentDescription = null
                )

                else -> {}
            }

            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
                horizontalAlignment = Alignment.Start
            ) {

                Text(
                    style = MaterialTheme.typography.bodySmall,
                    text = "Time: ${timestamp.convertLongToTime()}",
                )

                Text(
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    text = event.type.toString()
                )
                when (event.type) {
                    AssetTrackingEventType.Fall -> {
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = "Height: ${event.fall!!.heightCm} cm",
                        )
                    }

                    AssetTrackingEventType.Shock -> {
                        Text(
                            text = "Duration: ${event.shock!!.durationMSec} ms",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            modifier = Modifier
                                .padding(bottom = LocalDimensions.current.paddingSmall),
                            text = "Intensity: %.2f g".format(
                                Locale.getDefault(),
                                event.shock!!.intensityG.computeNorm()),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    else -> {}
                }
            }

            if (event.type == AssetTrackingEventType.Shock) {
                Box( modifier = modifier.fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter) {
                    Icon(
                        modifier = Modifier
                            .padding(LocalDimensions.current.paddingSmall)
                            .size(LocalDimensions.current.iconSmall)
                            .clickable { onShockInfoClick() },
                        tint = PrimaryBlue,
                        imageVector = Icons.Filled.Info,
                        contentDescription = null
                    )
                }
            }
        }
    }
}


private fun FloatArray.computeNorm(): Float {
    var norm = 0.0f
    this.forEach {
        norm += it * it
    }
    return sqrt(norm)
}

/** ----------------------- PREVIEW --------------------------------------- **/

@PreviewFontScale
@Preview(showBackground = true)
@Composable
private fun AssetTrackingEventItemPreview(
    @PreviewParameter(AssetTrackingEventProvider::class) event: AssetTrackingEventData
) {
    PreviewBlueMSTheme {
        AssetTrackingEventItem(
            timestamp = 456L,
            event = event
        )
    }
}