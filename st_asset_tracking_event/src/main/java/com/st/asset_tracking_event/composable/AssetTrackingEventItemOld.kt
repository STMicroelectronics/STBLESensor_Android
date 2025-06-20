package com.st.asset_tracking_event.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.st.asset_tracking_event.R
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventData
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventType
import com.st.blue_sdk.features.extended.asset_tracking_event.model.ShockAssetTrackingEvent
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingOrientationType
import com.st.ui.theme.Grey3
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.PrimaryBlue3
import com.st.ui.theme.PrimaryYellow
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.st.ui.composables.bottomBorder
import kotlin.math.sqrt
import java.util.Locale

@Composable
fun AssetTrackingEventItemOld(
    modifier: Modifier = Modifier,
    timestamp: Long,
    index: Int,
    even: Boolean = index % 2 == 0,
    event: AssetTrackingEventData,
    isTheLastElement: Boolean = false,
) {

    var isAnimated by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        if (isTheLastElement) {
            isAnimated = true
            delay(1000)
            isAnimated = false
        }
    }

    val animatedColorBorder by animateColorAsState(
        if (isAnimated) {
            if (even) SecondaryBlue else PrimaryYellow
        } else {
            Color.Unspecified
        },
        label = "color"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(4.dp, animatedColorBorder),
                Shapes.small
            ),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Column(
            modifier = Modifier.padding(LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${event.type}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Timestamp: $timestamp",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider(thickness = 1.dp, color = Grey3)
            when (event.type) {
                AssetTrackingEventType.Reset -> {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.padding(all = LocalDimensions.current.paddingSmall),
                            shape = Shapes.small,
                            color = if (even) SecondaryBlue else PrimaryYellow
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(LocalDimensions.current.paddingSmall)
                                    .size(LocalDimensions.current.iconNormal),
                                tint = PrimaryBlue3,
                                painter = painterResource(R.drawable.event_reset),
                                contentDescription = null
                            )
                        }

                        VerticalDivider(
                            thickness = 1.dp,
                            color = Grey3,
                            modifier = Modifier.fillMaxHeight()
                        )

                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                AssetTrackingEventType.Fall -> {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.padding(all = LocalDimensions.current.paddingSmall),
                            shape = Shapes.small,
                            color = if (even) SecondaryBlue else PrimaryYellow
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(LocalDimensions.current.paddingSmall)
                                    .size(LocalDimensions.current.iconNormal),
                                tint = PrimaryBlue3,
                                painter = painterResource(R.drawable.event_fall),
                                contentDescription = null
                            )
                        }

                        VerticalDivider(
                            thickness = 1.dp,
                            color = Grey3,
                            modifier = Modifier.fillMaxHeight()
                        )

                        Text(
                            text = "Height = ${event.fall!!.heightCm} cm",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                AssetTrackingEventType.Shock -> {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.padding(all = LocalDimensions.current.paddingSmall),
                            shape = Shapes.small,
                            color = if (even) SecondaryBlue else PrimaryYellow
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(LocalDimensions.current.paddingSmall)
                                    .size(LocalDimensions.current.iconNormal),
                                tint = PrimaryBlue3,
                                painter = painterResource(R.drawable.event_shock),
                                contentDescription = null
                            )
                        }

                        VerticalDivider(
                            thickness = 1.dp,
                            color = Grey3,
                            modifier = Modifier.fillMaxHeight()
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingSmall),
                            horizontalAlignment = Alignment.Start
                        ) {

                            Text(
                                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                                text = "Duration = ${event.shock!!.durationMSec} ms",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                modifier = Modifier
                                    .padding(bottom = LocalDimensions.current.paddingSmall)
                                    .bottomBorder(strokeWidth = 1.dp, color = Grey3),
                                text = "Intensity = ${event.shock!!.intensityG.computeNorm()} g",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

//                            Row(
//                            verticalAlignment = Alignment.CenterVertically) {
//                                Box(
//                                    modifier = Modifier
//                                        .size(LocalDimensions.current.iconNormal)
//                                ) {
//                                    Icon(
//                                        modifier = Modifier
//                                            .padding(LocalDimensions.current.paddingSmall)
//                                            .size(LocalDimensions.current.iconSmall)
//                                            .align(Alignment.TopCenter),
//                                        tint = PrimaryBlue3,
//                                        painter = painterResource(R.drawable.north_24px),
//                                        contentDescription = null
//                                    )
//                                    Icon(
//                                        modifier = Modifier
//                                            .padding(LocalDimensions.current.paddingSmall)
//                                            .size(LocalDimensions.current.iconSmall)
//                                            .align(Alignment.BottomStart),
//                                        tint = PrimaryBlue3,
//                                        painter = painterResource(R.drawable.south_west_24px),
//                                        contentDescription = null
//                                    )
//                                    Icon(
//                                        modifier = Modifier
//                                            .padding(LocalDimensions.current.paddingSmall)
//                                            .size(LocalDimensions.current.iconSmall)
//                                            .align(Alignment.BottomEnd),
//                                        tint = PrimaryBlue3,
//                                        painter = painterResource(R.drawable.south_east_24px),
//                                        contentDescription = null
//                                    )
//                                }

                                Column(modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start) {

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(modifier = Modifier
                                            .weight(0.25f)
                                            .border(width = 1.dp, color = Color.Transparent)
                                            .padding(LocalDimensions.current.paddingSmall),
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            text = "",
                                            )
                                        Text(modifier = Modifier
                                            .weight(0.25f)
                                            .border(width = 1.dp, color = Grey3)
                                            .padding(LocalDimensions.current.paddingSmall),
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            text = "x",
                                        )
                                        Text(modifier = Modifier
                                            .weight(0.25f)
                                            .border(width = 1.dp, color = Grey3)
                                            .padding(LocalDimensions.current.paddingSmall),
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            text = "y",
                                        )
                                        Text(modifier = Modifier
                                            .weight(0.25f)
                                            .border(width = 1.dp, color = Grey3)
                                            .padding(LocalDimensions.current.paddingSmall),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            text = "z",
                                        )
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(modifier = Modifier
                                            .weight(0.25f)
                                            .border(width = 1.dp, color = Grey3)
                                            .padding(LocalDimensions.current.paddingSmall),
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = FontWeight.Bold,
                                            text = "Acc [g]",
                                        )
                                        Text(modifier = Modifier
                                            .weight(0.25f)
                                            .border(width = 1.dp, color = Grey3)
                                            .padding(LocalDimensions.current.paddingSmall),
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            text = "%.2f".format(Locale.getDefault(), event.shock!!.intensityG[0])
                                        )
                                        Text(modifier = Modifier
                                            .weight(0.25f)
                                            .border(width = 1.dp, color = Grey3)
                                            .padding(LocalDimensions.current.paddingSmall),
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            text = "%.2f".format(Locale.getDefault(), event.shock!!.intensityG[1])
                                        )
                                        Text(modifier = Modifier
                                            .weight(0.25f)
                                            .border(width = 1.dp, color = Grey3)
                                            .padding(LocalDimensions.current.paddingSmall),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodySmall,
                                            text = "%.2f".format(Locale.getDefault(), event.shock!!.intensityG[2])
                                        )
                                    }
                                    if(event.shock!!.angles.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                modifier = Modifier
                                                    .weight(0.25f)
                                                    .border(width = 1.dp, color = Grey3)
                                                    .padding(LocalDimensions.current.paddingSmall),
                                                style = MaterialTheme.typography.bodySmall,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = FontWeight.Bold,
                                                text = "Angle [°]",
                                            )
                                            Text(
                                                modifier = Modifier
                                                    .weight(0.25f)
                                                    .border(width = 1.dp, color = Grey3)
                                                    .padding(LocalDimensions.current.paddingSmall),
                                                style = MaterialTheme.typography.bodySmall,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                text = if(event.shock!!.angles[0]!=ShockAssetTrackingEvent.UNDEF_ANGLE)  {
                                                    "%.2f".format(Locale.getDefault(), event.shock!!.angles[0])
                                                } else {
                                                    "--"
                                                }
                                            )
                                            Text(
                                                modifier = Modifier
                                                    .weight(0.25f)
                                                    .border(width = 1.dp, color = Grey3)
                                                    .padding(LocalDimensions.current.paddingSmall),
                                                style = MaterialTheme.typography.bodySmall,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                text = if(event.shock!!.angles[1]!=ShockAssetTrackingEvent.UNDEF_ANGLE)  {
                                                    "%.2f".format(Locale.getDefault(), event.shock!!.angles[1])
                                                } else {
                                                    "--"
                                                }
                                            )
                                            Text(
                                                modifier = Modifier
                                                    .weight(0.25f)
                                                    .border(width = 1.dp, color = Grey3)
                                                    .padding(LocalDimensions.current.paddingSmall),
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                text = if(event.shock!!.angles[2]!=ShockAssetTrackingEvent.UNDEF_ANGLE)  {
                                                    "%.2f".format(Locale.getDefault(), event.shock!!.angles[2])
                                                } else {
                                                    "--"
                                                }
                                            )
                                        }
                                    }


                                    if (event.shock!!.orientations.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(intrinsicSize = IntrinsicSize.Max),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier
                                                .weight(0.25f)
                                                .border(width = 1.dp, color = Grey3)
                                                .padding(LocalDimensions.current.paddingSmall)
                                                .fillMaxHeight(), contentAlignment = Alignment.Center) {
                                                Text(
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    text = "Dir",
                                                )
                                            }

                                            FromOrientationToIcon(modifier = Modifier
                                                .weight(0.25f)
                                                .border(width = 1.dp, color = Grey3)
                                                .padding(LocalDimensions.current.paddingSmall)
                                                .fillMaxHeight(),orientation =event.shock!!.orientations[0])
                                            FromOrientationToIcon(modifier =Modifier
                                                .weight(0.25f)
                                                .border(width = 1.dp, color = Grey3)
                                                .padding(LocalDimensions.current.paddingSmall)
                                                .fillMaxHeight(),orientation =event.shock!!.orientations[1])
                                            FromOrientationToIcon(modifier =Modifier
                                                .weight(0.25f)
                                                .border(width = 1.dp, color = Grey3)
                                                .padding(LocalDimensions.current.paddingSmall)
                                                .fillMaxHeight(),orientation =event.shock!!.orientations[2])

                                        }
                                    }
                                }
//                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun FromOrientationToIcon(
    modifier: Modifier = Modifier,
    orientation: AssetTrackingOrientationType) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
    when (orientation) {
        AssetTrackingOrientationType.Undef -> {
//            Icon(
//                modifier = Modifier.size(LocalDimensions.current.iconSmall),
//                tint = PrimaryBlue3,
//                painter = painterResource(R.drawable.cancel_24px),
//                contentDescription = null
//            )
        }

        AssetTrackingOrientationType.Positive -> Icon(
            modifier = Modifier.size(LocalDimensions.current.iconSmall),
            tint = PrimaryBlue3,
            painter = painterResource(R.drawable.north_24px),
            contentDescription = null
        )

        AssetTrackingOrientationType.Negative -> Icon(
            modifier = Modifier.size(LocalDimensions.current.iconSmall),
            tint = PrimaryBlue3,
            painter = painterResource(R.drawable.south_24px),
            contentDescription = null
        )
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

//@FontScalePreviews
@Preview(showBackground = true)
@Composable
private fun AssetTrackingEventItemDirectionPreview(
    @PreviewParameter(AssetTrackingEventProvider::class) event: AssetTrackingEventData
) {
    PreviewBlueMSTheme {
        AssetTrackingEventItemOld(
            timestamp = 789L,
            event = event,
            index = 4
        )
    }
}

