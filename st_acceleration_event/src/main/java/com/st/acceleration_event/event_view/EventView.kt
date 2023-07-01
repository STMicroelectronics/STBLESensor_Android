package com.st.acceleration_event.event_view

import androidx.annotation.DrawableRes
import com.st.acceleration_event.R
import com.st.blue_sdk.features.acceleration_event.AccelerationType
import com.st.blue_sdk.features.acceleration_event.DetectableEventType

/**
 * Interface to implement for a view that will show the acceleration event data
 */
interface EventView {
    /**
     * called when an event is enabled
     * @param eventType event that will be enable in the board
     */
    fun enableEvent(eventType: DetectableEventType)

    /**
     * called when a new event is detected
     * @param event detected event
     * @param data additional event data
     */
    fun displayEvent(eventTypeList: List<AccelerationType>, data: Int)

    fun isOrientationEvent(accType: AccelerationType) : Boolean {
        return when(accType) {
            AccelerationType.OrientationTopRight -> true
            AccelerationType.OrientationBottomRight -> true
            AccelerationType.OrientationBottomLeft -> true
            AccelerationType.OrientationTopLeft -> true
            AccelerationType.OrientationUp -> true
            AccelerationType.OrientationDown -> true
            else -> false
        }
    }

    @DrawableRes
    fun getEventIcon(eventType: AccelerationType): Int {
        return when (eventType) {
            AccelerationType.NoEvent -> R.drawable.acc_event_none
            AccelerationType.OrientationTopRight -> R.drawable.acc_event_orientation_top_right
            AccelerationType.OrientationBottomRight -> R.drawable.acc_event_orientation_bottom_right
            AccelerationType.OrientationBottomLeft -> R.drawable.acc_event_orientation_bottom_left
            AccelerationType.OrientationTopLeft -> R.drawable.acc_event_orientation_top_left
            AccelerationType.OrientationUp -> R.drawable.acc_event_orientation_up
            AccelerationType.OrientationDown -> R.drawable.acc_event_orientation_down
            AccelerationType.Tilt -> R.drawable.acc_event_tilt
            AccelerationType.FreeFall -> R.drawable.acc_event_free_fall
            AccelerationType.SingleTap -> R.drawable.acc_event_tap_single
            AccelerationType.DoubleTap -> R.drawable.acc_event_tap_double
            AccelerationType.WakeUp -> R.drawable.acc_event_wake_up
            AccelerationType.Pedometer -> R.drawable.acc_event_pedometer
            else -> R.drawable.acc_event_none
        }
    }


    @DrawableRes
    fun getDefaultIcon(eventType: DetectableEventType): Int {
        return when (eventType) {
            DetectableEventType.None -> getEventIcon(AccelerationType.NoEvent)
            DetectableEventType.Orientation -> getEventIcon(AccelerationType.OrientationTopLeft)
            DetectableEventType.Pedometer -> getEventIcon(AccelerationType.Pedometer)
            DetectableEventType.SingleTap -> getEventIcon(AccelerationType.SingleTap)
            DetectableEventType.DoubleTap -> getEventIcon(AccelerationType.DoubleTap)
            DetectableEventType.FreeFall -> getEventIcon(AccelerationType.FreeFall)
            DetectableEventType.WakeUp -> getEventIcon(AccelerationType.WakeUp)
            DetectableEventType.Tilt -> getEventIcon(AccelerationType.Tilt)
            else -> getEventIcon(AccelerationType.NoEvent)
        }
    }
}