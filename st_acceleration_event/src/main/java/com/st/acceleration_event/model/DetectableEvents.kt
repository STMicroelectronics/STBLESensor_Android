package com.st.acceleration_event.model

import androidx.annotation.DrawableRes
import com.st.acceleration_event.R
import com.st.blue_sdk.features.acceleration_event.AccelerationType
import com.st.blue_sdk.features.acceleration_event.DetectableEventType
import com.st.blue_sdk.models.Boards

/**
 * list of possible events supported by the nucleo board
 */
private val nucleoSupportedEvents = arrayOf(
    DetectableEventType.None,
    DetectableEventType.Multiple,
    DetectableEventType.Orientation,
    DetectableEventType.DoubleTap,
    DetectableEventType.FreeFall,
    DetectableEventType.Pedometer,
    DetectableEventType.SingleTap,
    DetectableEventType.Tilt,
    DetectableEventType.WakeUp
)

private val sensorTileBoxSupportedEvents = arrayOf(
    DetectableEventType.None,
    //DetectableEventType.Multiple, //Tmp Add
    DetectableEventType.Orientation,
    DetectableEventType.DoubleTap,
    DetectableEventType.FreeFall,
    DetectableEventType.SingleTap,
    DetectableEventType.Tilt,
    DetectableEventType.WakeUp
)

private val stwinSupportedEvents = arrayOf(
    DetectableEventType.None,
    DetectableEventType.Orientation,
    DetectableEventType.DoubleTap,
    DetectableEventType.FreeFall,
    DetectableEventType.SingleTap,
    DetectableEventType.Tilt,
    DetectableEventType.WakeUp
)

private val idB008SupportedEvents = arrayOf(
    DetectableEventType.None,
    DetectableEventType.FreeFall,
    DetectableEventType.SingleTap,
    DetectableEventType.WakeUp,
    DetectableEventType.Tilt,
    DetectableEventType.Pedometer
)

private val bcn002v1SupportedEvents = arrayOf(
    DetectableEventType.None,
    DetectableEventType.WakeUp,
    DetectableEventType.SingleTap,
    DetectableEventType.Pedometer,
    DetectableEventType.Tilt,
    DetectableEventType.FreeFall
)

private val proteusSupportedEvents = arrayOf(
    DetectableEventType.None,
    DetectableEventType.WakeUp
)

/**
 * get the list of supported event by the node
 * @param type type of node that we are using
 * @return supported events
 */
fun getDetectableEvent(nodeType: Boards.Model): Array<DetectableEventType> {
    return when (nodeType) {
        Boards.Model.STEVAL_IDB008VX -> idB008SupportedEvents
        Boards.Model.STEVAL_BCN002V1 -> bcn002v1SupportedEvents
        Boards.Model.SENSOR_TILE_BOX -> sensorTileBoxSupportedEvents
        Boards.Model.STEVAL_STWINKIT1 -> stwinSupportedEvents
        Boards.Model.STEVAL_STWINKT1B -> stwinSupportedEvents
        Boards.Model.SENSOR_TILE_BOX_PRO -> sensorTileBoxSupportedEvents
        Boards.Model.SENSOR_TILE_BOX_PROB -> sensorTileBoxSupportedEvents
        Boards.Model.STWIN_BOX -> stwinSupportedEvents
        Boards.Model.STWIN_BOXB -> stwinSupportedEvents
        Boards.Model.PROTEUS -> proteusSupportedEvents
        Boards.Model.SENSOR_TILE -> nucleoSupportedEvents
        Boards.Model.BLUE_COIN -> nucleoSupportedEvents
        Boards.Model.WB55_NUCLEO_BOARD -> nucleoSupportedEvents
        Boards.Model.NUCLEO -> nucleoSupportedEvents
        Boards.Model.NUCLEO_F401RE -> nucleoSupportedEvents
        Boards.Model.NUCLEO_L476RG -> nucleoSupportedEvents
        Boards.Model.NUCLEO_L053R8 -> nucleoSupportedEvents
        Boards.Model.NUCLEO_F446RE -> nucleoSupportedEvents
        Boards.Model.NUCLEO_U575ZIQ -> nucleoSupportedEvents
        Boards.Model.NUCLEO_U5A5ZJQ -> nucleoSupportedEvents
        else -> arrayOf(DetectableEventType.None)
    }
}

fun getDefaultEvent(nodeType: Boards.Model): DetectableEventType {
    return when (nodeType) {
        Boards.Model.STEVAL_WESU1 -> DetectableEventType.Multiple
        Boards.Model.SENSOR_TILE -> DetectableEventType.Orientation
        Boards.Model.BLUE_COIN -> DetectableEventType.Orientation
        Boards.Model.STEVAL_IDB008VX -> DetectableEventType.FreeFall
        Boards.Model.STEVAL_BCN002V1 -> DetectableEventType.Orientation
        Boards.Model.SENSOR_TILE_BOX -> DetectableEventType.Orientation
        Boards.Model.STEVAL_STWINKIT1 -> DetectableEventType.Orientation
        Boards.Model.STEVAL_STWINKT1B -> DetectableEventType.Orientation
        Boards.Model.SENSOR_TILE_BOX_PRO -> DetectableEventType.Orientation
        Boards.Model.SENSOR_TILE_BOX_PROB -> DetectableEventType.Orientation
        Boards.Model.STWIN_BOX -> DetectableEventType.Orientation
        Boards.Model.STWIN_BOXB -> DetectableEventType.Orientation
        Boards.Model.PROTEUS -> DetectableEventType.WakeUp
        Boards.Model.WB55_NUCLEO_BOARD -> DetectableEventType.Orientation
        Boards.Model.NUCLEO -> DetectableEventType.Orientation
        Boards.Model.NUCLEO_F401RE -> DetectableEventType.Orientation
        Boards.Model.NUCLEO_L476RG -> DetectableEventType.Orientation
        Boards.Model.NUCLEO_L053R8 -> DetectableEventType.Orientation
        Boards.Model.NUCLEO_F446RE -> DetectableEventType.Orientation
        Boards.Model.NUCLEO_U575ZIQ -> DetectableEventType.Orientation
        Boards.Model.NUCLEO_U5A5ZJQ -> DetectableEventType.Orientation
        else -> DetectableEventType.None
    }
}

@DrawableRes
fun getEventIconResource(eventType: AccelerationType): Int {
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
fun getDefaultIconResource(eventType: DetectableEventType): Int {
    return when (eventType) {
        DetectableEventType.None -> getEventIconResource(AccelerationType.NoEvent)
        DetectableEventType.Orientation -> getEventIconResource(AccelerationType.OrientationTopLeft)
        DetectableEventType.Pedometer -> getEventIconResource(AccelerationType.Pedometer)
        DetectableEventType.SingleTap -> getEventIconResource(AccelerationType.SingleTap)
        DetectableEventType.DoubleTap -> getEventIconResource(AccelerationType.DoubleTap)
        DetectableEventType.FreeFall -> getEventIconResource(AccelerationType.FreeFall)
        DetectableEventType.WakeUp -> getEventIconResource(AccelerationType.WakeUp)
        DetectableEventType.Tilt -> getEventIconResource(AccelerationType.Tilt)
        else -> getEventIconResource(AccelerationType.NoEvent)
    }
}

fun isOrientationEvent(accType: AccelerationType): Boolean {
    return when (accType) {
        AccelerationType.OrientationTopRight -> true
        AccelerationType.OrientationBottomRight -> true
        AccelerationType.OrientationBottomLeft -> true
        AccelerationType.OrientationTopLeft -> true
        AccelerationType.OrientationUp -> true
        AccelerationType.OrientationDown -> true
        else -> false
    }
}