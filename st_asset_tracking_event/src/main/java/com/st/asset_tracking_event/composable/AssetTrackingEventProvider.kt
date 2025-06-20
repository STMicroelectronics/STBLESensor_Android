package com.st.asset_tracking_event.composable

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventData
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventType
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingOrientationType
import com.st.blue_sdk.features.extended.asset_tracking_event.model.FallAssetTrackingEvent
import com.st.blue_sdk.features.extended.asset_tracking_event.model.ShockAssetTrackingEvent

class AssetTrackingEventProvider : PreviewParameterProvider<AssetTrackingEventData> {
    override val values: Sequence<AssetTrackingEventData> = sequenceOf(
        AssetTrackingEventData(type = AssetTrackingEventType.Reset),
        AssetTrackingEventData(
            type = AssetTrackingEventType.Fall,
            fall = FallAssetTrackingEvent(heightCm = 123.0f)
        ),
        AssetTrackingEventData(
            type = AssetTrackingEventType.Shock,
            shock = ShockAssetTrackingEvent(
                durationMSec = 0.12f,
                intensityG = floatArrayOf(12.0f, -12.0f, 20.0f),
                orientations = emptyArray(),
                angles = floatArrayOf()
            )
        ),
        AssetTrackingEventData(
            type = AssetTrackingEventType.Shock,
            shock = ShockAssetTrackingEvent(
                durationMSec = 0.12f,
                intensityG = floatArrayOf(12.0f, -12.0f, 20.0f),
                orientations = emptyArray(),
                angles = floatArrayOf(25.3f, -32.1f, ShockAssetTrackingEvent.UNDEF_ANGLE)
            )
        ),
        AssetTrackingEventData(
            type = AssetTrackingEventType.Shock,
            shock = ShockAssetTrackingEvent(
                durationMSec = 0.12f,
                intensityG = floatArrayOf(12.0f, -12.0f, 20.0f),
                orientations = arrayOf(
                    AssetTrackingOrientationType.Negative,
                    AssetTrackingOrientationType.Positive,
                    AssetTrackingOrientationType.Undef
                ),
                angles = floatArrayOf()
            )
        ),
        AssetTrackingEventData(
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
        )
    )
}