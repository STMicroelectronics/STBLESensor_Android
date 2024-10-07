package com.st.heart_rate_demo.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.st.blue_sdk.features.external.std.BodySensorLocationType
import com.st.heart_rate_demo.R
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey3
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme


@Composable
fun BodySensorLocationView(
    modifier: Modifier = Modifier,
    location: BodySensorLocationType
) {
    Box(modifier = modifier.size(LocalDimensions.current.imageMedium)) {
        Icon(
            modifier = Modifier.matchParentSize(),
            painter = painterResource(id = R.drawable.ic_standing_human_body_silhouette_svgrepo_com),
            tint = Grey3,
            contentDescription = "Body View"
        )

        Icon(
            modifier = Modifier
                .size(6.dp)
                .offset(
                    x = xOffsetForBodyLocation(location).dp,
                    y = yOffsetForBodyLocation(location).dp
                ),
            painter = painterResource(id = R.drawable.ic_circle),
            tint = ErrorText,
            contentDescription = "Sensor Position"
        )
    }
}

private fun xOffsetForBodyLocation(location: BodySensorLocationType): Int {
    return when (location) {
        BodySensorLocationType.Other -> 0
        BodySensorLocationType.Chest -> 60
        BodySensorLocationType.Wrist -> 38
        BodySensorLocationType.Finger -> 80
        BodySensorLocationType.Hand -> 36
        BodySensorLocationType.EarLobe -> 50
        BodySensorLocationType.Foot -> 52
        BodySensorLocationType.NotKnown -> 0*2
    }
}

private fun yOffsetForBodyLocation(location: BodySensorLocationType): Int {
    return when (location) {
        BodySensorLocationType.Other -> 0
        BodySensorLocationType.Chest -> 30
        BodySensorLocationType.Wrist -> 52
        BodySensorLocationType.Finger -> 66
        BodySensorLocationType.Hand -> 60
        BodySensorLocationType.EarLobe -> 6
        BodySensorLocationType.Foot -> 110
        BodySensorLocationType.NotKnown -> 0
    }
}

@Preview(showBackground = true)
@Composable
fun BodySensorLocationViewFootPreview() {
    PreviewBlueMSTheme {
        BodySensorLocationView(location = BodySensorLocationType.Foot)
    }
}

@Preview(showBackground = true)
@Composable
fun BodySensorLocationViewHandPreview() {
    PreviewBlueMSTheme {
        BodySensorLocationView(location = BodySensorLocationType.Hand)
    }
}

@Preview(showBackground = true)
@Composable
fun BodySensorLocationViewChestPreview() {
    PreviewBlueMSTheme {
        BodySensorLocationView(location = BodySensorLocationType.Chest)
    }
}

@Preview(showBackground = true)
@Composable
fun BodySensorLocationViewWristPreview() {
    PreviewBlueMSTheme {
        BodySensorLocationView(location = BodySensorLocationType.Wrist)
    }
}

@Preview(showBackground = true)
@Composable
fun BodySensorLocationViewFingerPreview() {
    PreviewBlueMSTheme {
        BodySensorLocationView(location = BodySensorLocationType.Finger)
    }
}

@Preview(showBackground = true)
@Composable
fun BodySensorLocationViewEarLobePreview() {
    PreviewBlueMSTheme {
        BodySensorLocationView(location = BodySensorLocationType.EarLobe)
    }
}
