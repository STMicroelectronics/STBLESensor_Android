package com.st.asset_tracking_event.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.st.asset_tracking_event.R
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventData
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventType
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingOrientationType
import com.st.blue_sdk.features.extended.asset_tracking_event.model.ShockAssetTrackingEvent
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue2
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import java.util.Locale

@Composable
fun ShockEventInfoDetails(
    event: AssetTrackingEventData,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = Shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
            ) {
                Text(
                    text = "Shock Event Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                    //textDecoration = TextDecoration.Underline
                )
                HorizontalDivider(thickness = 1.dp, color = Grey3)

                if (event.shock!!.orientations.isNotEmpty()) {
                    Box(
                        modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(size = LocalDimensions.current.iconMedium),
                            painter = painterResource(R.drawable.cube_icon),
                            tint = SecondaryBlue2,
                            contentDescription = null
                        )
                        Icon(
                            modifier = Modifier
                                .size(size = LocalDimensions.current.iconMedium),
                            painter = painterResource(R.drawable.plus_x_icon),
                            tint = if (event.shock!!.orientations[0] == AssetTrackingOrientationType.Positive) {
                                SuccessText
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentDescription = null
                        )
                        Icon(
                            modifier = Modifier
                                .size(size = LocalDimensions.current.iconMedium),
                            painter = painterResource(R.drawable.minu_x_icon),
                            tint = if (event.shock!!.orientations[0] == AssetTrackingOrientationType.Negative) {
                                ErrorText
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentDescription = null
                        )
                        Icon(
                            modifier = Modifier
                                .size(size = LocalDimensions.current.iconMedium),
                            painter = painterResource(R.drawable.plus_y_icon),
                            tint = if (event.shock!!.orientations[1] == AssetTrackingOrientationType.Positive) {
                                SuccessText
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentDescription = null
                        )
                        Icon(
                            modifier = Modifier
                                .size(size = LocalDimensions.current.iconMedium),
                            painter = painterResource(R.drawable.minus_y_icon),
                            tint = if (event.shock!!.orientations[1] == AssetTrackingOrientationType.Negative) {
                                ErrorText
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentDescription = null
                        )
                        Icon(
                            modifier = Modifier
                                .size(size = LocalDimensions.current.iconMedium),
                            painter = painterResource(R.drawable.plus_z_icon),
                            tint = if (event.shock!!.orientations[2] == AssetTrackingOrientationType.Positive) {
                                SuccessText
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentDescription = null
                        )
                        Icon(
                            modifier = Modifier
                                .size(size = LocalDimensions.current.iconMedium),
                            painter = painterResource(R.drawable.minus_z_icon),
                            tint = if (event.shock!!.orientations[2] == AssetTrackingOrientationType.Negative) {
                                ErrorText
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentDescription = null
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier
                            .weight(0.25f)
                            .padding(LocalDimensions.current.paddingSmall),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        text = "Acc [g]",
                    )

                    Column(
                        modifier = Modifier
                            .weight(0.25f)
                            .padding(LocalDimensions.current.paddingSmall)
                    ) {

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            text = "X",
                        )

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            color = Grey6,
                            overflow = TextOverflow.Ellipsis,
                            text = "%.2f".format(
                                Locale.getDefault(),
                                event.shock!!.intensityG[0]
                            )
                        )

                    }

                    Column(
                        modifier = Modifier
                            .weight(0.25f)
                            .padding(LocalDimensions.current.paddingSmall)
                    ) {

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            text = "Y",
                        )

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            color = Grey6,
                            overflow = TextOverflow.Ellipsis,
                            text = "%.2f".format(
                                Locale.getDefault(),
                                event.shock!!.intensityG[1]
                            )
                        )

                    }

                    Column(
                        modifier = Modifier
                            .weight(0.25f)
                            .padding(LocalDimensions.current.paddingSmall)
                    ) {

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            text = "Z",
                        )

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            color = Grey6,
                            overflow = TextOverflow.Ellipsis,
                            text = "%.2f".format(
                                Locale.getDefault(),
                                event.shock!!.intensityG[2]
                            )
                        )

                    }
                }

                if (event.shock!!.angles.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            start = LocalDimensions.current.paddingNormal,
                            end = LocalDimensions.current.paddingNormal
                        ),
                        thickness = 1.dp, color = PrimaryBlue
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            modifier = Modifier
                                .weight(0.25f)
                                .padding(LocalDimensions.current.paddingSmall),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            text = "Angle [°]",
                        )

                        Column(
                            modifier = Modifier
                                .weight(0.25f)
                                .padding(LocalDimensions.current.paddingSmall)
                        ) {

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                text = "X",
                            )

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = Grey6,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                text = if (event.shock!!.angles[0] != ShockAssetTrackingEvent.UNDEF_ANGLE) {
                                    "%.2f".format(Locale.getDefault(), event.shock!!.angles[0])
                                } else {
                                    "--"
                                }
                            )

                        }

                        Column(
                            modifier = Modifier
                                .weight(0.25f)
                                .padding(LocalDimensions.current.paddingSmall)
                        ) {

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                text = "Y",
                            )

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = Grey6,
                                overflow = TextOverflow.Ellipsis,
                                text = if (event.shock!!.angles[1] != ShockAssetTrackingEvent.UNDEF_ANGLE) {
                                    "%.2f".format(Locale.getDefault(), event.shock!!.angles[1])
                                } else {
                                    "--"
                                }
                            )

                        }

                        Column(
                            modifier = Modifier
                                .weight(0.25f)
                                .padding(LocalDimensions.current.paddingSmall)
                        ) {

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                text = "Z",
                            )

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = Grey6,
                                overflow = TextOverflow.Ellipsis,
                                text = if (event.shock!!.angles[2] != ShockAssetTrackingEvent.UNDEF_ANGLE) {
                                    "%.2f".format(Locale.getDefault(), event.shock!!.angles[2])
                                } else {
                                    "--"
                                }
                            )

                        }
                    }
                }

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    BlueMsButton(
                        text = "Ok",
                        onClick = onDismissRequest
                    )
                }

            }

        }
    }
}

@Preview
@Composable
private fun ShockEventInfoDetailsPreview1() {
    PreviewBlueMSTheme {
        ShockEventInfoDetails(
            event = AssetTrackingEventData(
                type = AssetTrackingEventType.Shock,
                shock = ShockAssetTrackingEvent(
                    durationMSec = 0.12f,
                    intensityG = floatArrayOf(12.0f, -12.0f, 20.0f),
                    orientations = arrayOf(),
                    angles = floatArrayOf()
                )
            ),
            onDismissRequest = {}
        )
    }
}

@Preview
@Composable
private fun ShockEventInfoDetailsPreview2() {
    PreviewBlueMSTheme {
        ShockEventInfoDetails(
            event = AssetTrackingEventData(
                type = AssetTrackingEventType.Shock,
                shock = ShockAssetTrackingEvent(
                    durationMSec = 0.12f,
                    intensityG = floatArrayOf(12.0f, -12.0f, 20.0f),
                    orientations = arrayOf(
                        AssetTrackingOrientationType.Negative,
                        AssetTrackingOrientationType.Undef,
                        AssetTrackingOrientationType.Negative
                    ),
                    angles = floatArrayOf()
                )
            ),
            onDismissRequest = {}
        )
    }
}

@Preview
@Composable
private fun ShockEventInfoDetailsPreview3() {
    PreviewBlueMSTheme {
        ShockEventInfoDetails(
            event = AssetTrackingEventData(
                type = AssetTrackingEventType.Shock,
                shock = ShockAssetTrackingEvent(
                    durationMSec = 0.12f,
                    intensityG = floatArrayOf(12.0f, -12.0f, 20.0f),
                    orientations = arrayOf(
                        AssetTrackingOrientationType.Negative,
                        AssetTrackingOrientationType.Positive,
                        AssetTrackingOrientationType.Undef
                    ),
                    angles = floatArrayOf(25.3f, -32.1f, ShockAssetTrackingEvent.UNDEF_ANGLE)
                )
            ),
            onDismissRequest = {}
        )
    }
}