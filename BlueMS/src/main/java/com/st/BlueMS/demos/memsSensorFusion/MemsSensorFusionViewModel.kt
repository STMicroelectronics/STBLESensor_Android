package com.st.BlueMS.demos.memsSensorFusion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.st.BlueMS.R
import com.st.BlueMS.demos.memsSensorFusion.feature_listener.*
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.*
import com.st.BlueSTSDK.Node
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

class MemsSensorFusionViewModel(application: Application) : AndroidViewModel(application) {

    private val _countRateStateFlow = MutableStateFlow<SensorFusionData?>(null)
    val sensorFusionEvents = _countRateStateFlow.asStateFlow()

    private val _proximityStateFlow = MutableStateFlow<ProximityData?>(null)
    val proximityEvents = _proximityStateFlow.asStateFlow()

    private val _freeFallShareFlow =
        MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1, BufferOverflow.DROP_OLDEST)
    val freeFallEvents = _freeFallShareFlow.asSharedFlow()

    private val sensorFusionListener = SensorFusionListener {
        _countRateStateFlow.update { it }
    }

    private val proximityListener = FeatureListenerImpl {
        _proximityStateFlow.value = it
    }

    val notificationDelay =
        getApplication<Application>().resources.getInteger(R.integer.wesu_motion_fx_free_fall_notification_delay)
    private val freeFallListener = FreeFallListener(notificationDelay) {
        _freeFallShareFlow.tryEmit(Unit)
    }

    private var sensorFusionFeature: FeatureAutoConfigurable? = null

    private var proximityFeature: Feature? = null

    private var freeFallEvent: FeatureAccelerationEvent? = null

    fun enableSensorFusion(node: Node): Boolean {

        if (sensorFusionFeature == null) {
            val feature: FeatureMemsSensorFusion? =
                node.getFeature(FeatureMemsSensorFusionCompact::class.java)
            sensorFusionFeature = feature ?: node.getFeature(FeatureMemsSensorFusion::class.java)

            sensorFusionFeature?.let {
                sensorFusionListener.resetQuaternionRate()
                it.addFeatureListener(sensorFusionListener)
                node.enableNotification(it)
            }
        }

        return isSensorFusionEnabled()
    }

    fun disableSensorFusion(node: Node) {
        sensorFusionFeature?.let {
            it.removeFeatureListener(sensorFusionListener)
            node.disableNotification(it)
            sensorFusionFeature = null
        }
    }

    fun isSensorFusionEnabled() = sensorFusionFeature != null

    fun getSensorFusionFeature() = sensorFusionFeature

    fun enableProximity(node: Node, enableProximityNotification: Boolean = true): Boolean {

        if (proximityFeature == null) {
            proximityFeature = node.getFeature(FeatureProximity::class.java)
        }

        proximityFeature?.let {
            it.addFeatureListener(proximityListener)
            if (enableProximityNotification) {
                node.enableNotification(it)
            }
        }

        return isProximityEnabled()
    }

    fun disableProximity(node: Node) {
        proximityFeature?.let {
            it.removeFeatureListener(proximityListener)
            node.disableNotification(it)
        }
    }

    fun isProximityEnabled() = proximityFeature != null

    fun getProximityFeature() = proximityFeature

    fun enableFreeFall(node: Node): Boolean {

        if (freeFallEvent == null) {
            freeFallEvent = node.getFeature(FeatureAccelerationEvent::class.java)
            freeFallEvent?.let {
                it.detectEvent(FeatureAccelerationEvent.DEFAULT_ENABLED_EVENT, false)
                it.detectEvent(FeatureAccelerationEvent.DetectableEvent.FREE_FALL, true)
                it.addFeatureListener(freeFallListener)
                node.enableNotification(it)
            }
        }

        return freeFallEvent != null
    }

    fun disableFreeFall(node: Node) {
        freeFallEvent?.let {
            it.removeFeatureListener(freeFallListener)
            node.disableNotification(it)
            freeFallEvent = null
        }
    }
}



