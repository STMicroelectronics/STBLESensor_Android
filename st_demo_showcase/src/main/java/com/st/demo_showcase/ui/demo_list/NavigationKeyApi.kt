package com.st.demo_showcase.ui.demo_list

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.st.acceleration_event.AccelerationEventDemoScreen
import com.st.acceleration_event.AccelerationEventViewModel
import com.st.activity_recognition.ActivityRecognitionDemoScreen
import com.st.activity_recognition.ActivityRecognitionViewModel
import com.st.asset_tracking_event.AssetTrackingEventFDemoScreen
import com.st.asset_tracking_event.AssetTrackingEventFViewModel
import com.st.audio_classification_demo.AudioClassificationDemoScreen
import com.st.audio_classification_demo.AudioClassificationDemoViewModel
import com.st.binary_content.BinaryContentViewModel
import com.st.binary_content.StBinaryContentScreen
import com.st.blue_voice.BlueVoiceDemoScreen
import com.st.blue_voice.BlueVoiceViewModel
import com.st.blue_voice.beamforming.BeamFormingDemoScreen
import com.st.blue_voice.full_duplex.BlueVoiceFullDuplexDemoScreen
import com.st.carry_position.CarryPositionDemoScreen
import com.st.carry_position.CarryPositionViewModel
import com.st.cloud_azure_iot_central.CloudAzureIotCentralScreen
import com.st.cloud_azure_iot_central.CloudAzureIotCentralViewModel
import com.st.cloud_mqtt.CloudMqttScreen
import com.st.cloud_mqtt.CloudMqttViewModel
import com.st.color_ambient_light.ColorAmbientLightDemoScreen
import com.st.color_ambient_light.ColorAmbientLightViewModel
import com.st.compass.CompassDemoScreen
import com.st.compass.CompassViewModel
import com.st.demo_showcase.ui.demo_show_case.DemoShowCaseFwUpdateNavKey
import com.st.environmental.EnvironmentalDemoScreen
import com.st.environmental.EnvironmentalViewModel
import com.st.event_counter.EventCounterDemoScreen
import com.st.event_counter.EventCounterViewModel
import com.st.ext_config.ui.ext_config.ExtConfigViewModel
import com.st.ext_config.ui.ext_config.ExtConfigurationNavScreen
import com.st.external_app.ExternalAppDemoScreen
import com.st.external_app.ExternalAppMap
import com.st.external_app.ExternalAppViewModel
import com.st.external_app.model.ExternalAppType
import com.st.fft_amplitude.FFTAmplitudeDemoScreen
import com.st.fft_amplitude.FFTAmplitudeViewModel
import com.st.fitness.FitnessActivityDemoScreen
import com.st.fitness.FitnessActivityViewModel
import com.st.flow_demo.FlowDemoScreen
import com.st.flow_demo.FlowDemoViewModel
import com.st.gesture_navigation.GestureNavigationDemoScreen
import com.st.gesture_navigation.GestureNavigationViewModel
import com.st.gnss.GnssFragmentDemoScreen
import com.st.gnss.GnssViewModel
import com.st.head_bone_conduction.HeadBoneConductionDemoScreen
import com.st.head_bone_conduction.HeadBoneConductionViewModel
import com.st.heart_rate_demo.HeartRateDemoViewModel
import com.st.heart_rate_demo.HeartRateFragmentDemoScreen
import com.st.high_speed_data_log.AIoTCraftHighSpeedDataLog
import com.st.high_speed_data_log.AIoTCraftHighSpeedDataLogViewModel
import com.st.high_speed_data_log.HighSpeedDataLog
import com.st.high_speed_data_log.HighSpeedDataLogViewModel
import com.st.high_speed_data_log.SimpleHighSpeedDataLog
import com.st.high_speed_data_log.SimpleHighSpeedDataLogViewModel
import com.st.led_control.LedControlDemoScreen
import com.st.led_control.LedControlViewModel
import com.st.level.LevelDemoScreen
import com.st.level.LevelViewModel
import com.st.medical_signal.MedicalSignalDemoScreen
import com.st.medical_signal.MedicalSignalViewModel
import com.st.mems_gesture.MemsGestureDemoScreen
import com.st.mems_gesture.MemsGestureViewModel
import com.st.motion_algorithms.MotionAlgorithmsDemoScreen
import com.st.motion_algorithms.MotionAlgorithmsViewModel
import com.st.motion_intensity.MotionIntensityDemoScreen
import com.st.motion_intensity.MotionIntensityViewModel
import com.st.multi_neural_network.MultiNeuralNetworkDemoScreen
import com.st.multi_neural_network.MultiNeuralNetworkViewModel
import com.st.neai_anomaly_detection.NeaiAnomalyDetectionDemoScreen
import com.st.neai_anomaly_detection.NeaiAnomalyDetectionViewModel
import com.st.neai_classification.NeaiClassificationDemoScreen
import com.st.neai_classification.NeaiClassificationViewModel
import com.st.neai_extrapolation.NeaiExtrapolationDemoScreen
import com.st.neai_extrapolation.NeaiExtrapolationViewModel
import com.st.nfc_writing.NfcWritingDemoScreen
import com.st.nfc_writing.NfcWritingViewModel
import com.st.node_status.NodeStatusDemoScreen
import com.st.node_status.NodeStatusViewModel
import com.st.pedometer.PedometerDemoScreen
import com.st.pedometer.PedometerViewModel
import com.st.piano.PianoDemoScreen
import com.st.piano.PianoViewModel
import com.st.plot.PlotDemoScreen
import com.st.plot.PlotViewModel
import com.st.pnpl.PnplViewModel
import com.st.pnpl.composable.StPnplScreen
import com.st.predicted_maintenance.PredictedMaintenanceDemoScreen
import com.st.predicted_maintenance.PredictedMaintenanceViewModel
import com.st.proximity_gesture_recognition.ProximityGestureRecognitionDemoScreen
import com.st.proximity_gesture_recognition.ProximityGestureRecognitionViewModel
import com.st.raw_pnpl.RawPnplScreen
import com.st.raw_pnpl.RawPnplViewModel
import com.st.registers_demo.RegistersDemoScreen
import com.st.registers_demo.RegistersDemoViewModel
import com.st.registers_demo.common.RegistersDemoType
import com.st.sensor_fusion.SensorFusionDemoScreen
import com.st.sensor_fusion.SensorFusionViewModel
import com.st.smart_motor_control.SmartMotorControlViewModel
import com.st.smart_motor_control.composable.MotorControlMainScreen
import com.st.source_localization.SourceLocalizationDemoScreen
import com.st.source_localization.SourceLocalizationViewModel
import com.st.switch_demo.SwitchDemoScreen
import com.st.switch_demo.SwitchDemoViewModel
import com.st.textual_monitor.TextualMonitorDemoScreen
import com.st.textual_monitor.TextualMonitorViewModel
import com.st.tof_objects_detection.TofObjectsDetectionDemoScreen
import com.st.tof_objects_detection.TofObjectsDetectionViewModel
import com.st.ui.theme.LocalDimensions
import kotlinx.serialization.Serializable

@Serializable
data object DemoListNavKey : NavKey

@Serializable
data class FlowNavKey(val nodeId: String) : NavKey

@Serializable
data class BeamFormingNavKey(val nodeId: String) : NavKey

@Serializable
data class EnvironmentalNavKey(val nodeId: String) : NavKey

@Serializable
data class LevelNavKey(val nodeId: String) : NavKey

@Serializable
data class FitnessActivityNavKey(val nodeId: String) : NavKey

@Serializable
data class CompassNavKey(val nodeId: String) : NavKey

@Serializable
data class HighSpeedDataLogNavKey(val nodeId: String) : NavKey

@Serializable
data class SimpleHighSpeedDataLog2NavKey(val nodeId: String) : NavKey

@Serializable
data class AIoTCraftHighSpeedDataLog2NavKey(val nodeId: String) : NavKey

@Serializable
data class BlueVoiceOpusNavKey(val nodeId: String) : NavKey

@Serializable
data class BlueVoiceFullDuplexNavKey(val nodeId: String) : NavKey

@Serializable
data class NavigationGestureNavKey(val nodeId: String) : NavKey

@Serializable
data class NEAIAnomalyDetectionNavKey(val nodeId: String) : NavKey

@Serializable
data class NEAIClassificationNavKey(val nodeId: String) : NavKey

@Serializable
data class NEAIExtrapolationNavKey(val nodeId: String) : NavKey

@Serializable
data class EventCounterNavKey(val nodeId: String) : NavKey

@Serializable
data class PianoNavKey(val nodeId: String) : NavKey

@Serializable
data class PnplNavKey(val nodeId: String, val demoName: String? = null) : NavKey

@Serializable
data class PlotNavKey(val nodeId: String, val isExpert: Boolean) : NavKey

@Serializable
data class NfcWritingNavKey(val nodeId: String) : NavKey

@Serializable
data class BinaryContentNavKey(val nodeId: String) : NavKey

@Serializable
data class ExtConfigNavKey(val nodeId: String) : NavKey

@Serializable
data class TofObjectsDetectionNavKey(val nodeId: String) : NavKey

@Serializable
data class ColorAmbientLightNavKey(val nodeId: String) : NavKey

@Serializable
data class GnssNavKey(val nodeId: String) : NavKey

@Serializable
data class ElectricChargeVariationNavKey(val nodeId: String) : NavKey

@Serializable
data class MotionIntensityNavKey(val nodeId: String) : NavKey

@Serializable
data class ActivityRecognitionNavKey(val nodeId: String) : NavKey

@Serializable
data class CarryPositionNavKey(val nodeId: String) : NavKey

@Serializable
data class MemsGestureNavKey(val nodeId: String) : NavKey

@Serializable
data class MotionAlgorithmsNavKey(val nodeId: String) : NavKey

@Serializable
data class PedometerNavKey(val nodeId: String) : NavKey

@Serializable
data class ProximityGestureRecognitionNavKey(val nodeId: String) : NavKey

@Serializable
data class SwitchNavKey(val nodeId: String) : NavKey

@Serializable
data class RegistersNavKey(val nodeId: String, val  demoType: RegistersDemoType = RegistersDemoType.MLC) : NavKey

@Serializable
data class AccelerationEventNavKey(val nodeId: String) : NavKey

@Serializable
data class SourceLocalizationNavKey(val nodeId: String) : NavKey

@Serializable
data class AudioClassificationNavKey(val nodeId: String) : NavKey

@Serializable
data class LedControlNavKey(val nodeId: String) : NavKey

@Serializable
data class NodeStatusNavKey(val nodeId: String) : NavKey

@Serializable
data class TextualMonitorNavKey(val nodeId: String) : NavKey

@Serializable
data class HeartRateNavKey(val nodeId: String) : NavKey

@Serializable
data class SensorFusionNavKey(val nodeId: String) : NavKey

@Serializable
data class PredictedMaintenanceNavKey(val nodeId: String) : NavKey

@Serializable
data class FftAmplitudeNavKey(val nodeId: String) : NavKey

@Serializable
data class MultiNeuralNetworkNavKey(val nodeId: String) : NavKey

@Serializable
data class ExternalAppNavKey(
    val nodeId: String,
    val externalAppType: ExternalAppType = ExternalAppType.BLESENSORCLASSIC
) : NavKey

@Serializable
data class AssetTrackingEventNavKey(val nodeId: String) : NavKey

@Serializable
data class RawPnplNavKey(val nodeId: String) : NavKey

@Serializable
data class SmartMotorControlNavKey(val nodeId: String) : NavKey

@Serializable
data class CloudAzureIotCentralNavKey(val nodeId: String) : NavKey

@Serializable
data class CloudMqttNavKey(val nodeId: String) : NavKey

@Serializable
data class MedicalSignalNavKey(val nodeId: String) : NavKey

@Serializable
data class WbsOtaFUOTA(val nodeId: String) : NavKey


@Serializable
data class HeadBoneConductionNavKey(val nodeId: String) : NavKey


@Composable
fun EntryProviderScope<NavKey>.FUOTA(
    externBackState: NavBackStack<NavKey>,
    backState: NavBackStack<NavKey>
) {
    entry<WbsOtaFUOTA> { key ->
        LaunchedEffect(key1 = Unit) {
            backState.removeLastOrNull()
            externBackState.add(DemoShowCaseFwUpdateNavKey(key.nodeId))
        }
    }
}

@Composable
fun EntryProviderScope<NavKey>.MedicalSignal() {
    entry<MedicalSignalNavKey> { key ->
        val demoViewModel: MedicalSignalViewModel = hiltViewModel()
        MedicalSignalDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.CloudMQTT() {
    entry<CloudMqttNavKey> { key ->
        val demoViewModel: CloudMqttViewModel = hiltViewModel()
        CloudMqttScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.CloudAzureIoTCentral() {
    entry<CloudAzureIotCentralNavKey> { key ->
        val demoViewModel: CloudAzureIotCentralViewModel = hiltViewModel()
        CloudAzureIotCentralScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.MotorControl() {
    entry<SmartMotorControlNavKey> { key ->
        val demoViewModel: SmartMotorControlViewModel = hiltViewModel()
        MotorControlMainScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.RawPnPL() {
    entry<RawPnplNavKey> { key ->
        val demoViewModel: RawPnplViewModel = hiltViewModel()
        RawPnplScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = LocalDimensions.current.paddingNormal),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.AssetTracking() {
    entry<AssetTrackingEventNavKey> { key ->
        val demoViewModel: AssetTrackingEventFViewModel = hiltViewModel()
        AssetTrackingEventFDemoScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = LocalDimensions.current.paddingNormal,
                    start = LocalDimensions.current.paddingNormal,
                    end = LocalDimensions.current.paddingNormal
                ),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.ExternalApplication() {
    entry<ExternalAppNavKey> { key ->
        val demoViewModel: ExternalAppViewModel = hiltViewModel()
        ExternalAppDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            externalAppDetail = ExternalAppMap[key.externalAppType]
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.MultiNeuralNetwork() {
    entry<MultiNeuralNetworkNavKey> { key ->
        val demoViewModel: MultiNeuralNetworkViewModel = hiltViewModel()
        MultiNeuralNetworkDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.FftAmplitude() {
    entry<FftAmplitudeNavKey> { key ->
        val demoViewModel: FFTAmplitudeViewModel = hiltViewModel()
        FFTAmplitudeDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.PredictedMaintenance() {
    entry<PredictedMaintenanceNavKey> { key ->
        val demoViewModel: PredictedMaintenanceViewModel = hiltViewModel()
        PredictedMaintenanceDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.SensorFusion() {
    entry<SensorFusionNavKey> { key ->
        val demoViewModel: SensorFusionViewModel = hiltViewModel()
        SensorFusionDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.HeartRate() {
    entry<HeartRateNavKey> { key ->
        val demoViewModel: HeartRateDemoViewModel = hiltViewModel()
        HeartRateFragmentDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.TextualMonitor() {
    entry<TextualMonitorNavKey> { key ->
        val demoViewModel: TextualMonitorViewModel = hiltViewModel()
        TextualMonitorDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.NodeStatus() {
    entry<NodeStatusNavKey> { key ->
        val demoViewModel: NodeStatusViewModel = hiltViewModel()
        NodeStatusDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.LedControl() {
    entry<LedControlNavKey> { key ->
        val demoViewModel: LedControlViewModel = hiltViewModel()
        LedControlDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.AudioClassification() {
    entry<AudioClassificationNavKey> { key ->
        val demoViewModel: AudioClassificationDemoViewModel = hiltViewModel()
        AudioClassificationDemoScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                ),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.SourceLocalization() {
    entry<SourceLocalizationNavKey> { key ->
        val demoViewModel: SourceLocalizationViewModel = hiltViewModel()
        SourceLocalizationDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.AccelerationEvent() {
    entry<AccelerationEventNavKey> { key ->
        val demoViewModel: AccelerationEventViewModel = hiltViewModel()
        AccelerationEventDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.Registers() {
    entry<RegistersNavKey> { key ->
        val demoViewModel: RegistersDemoViewModel = hiltViewModel()
        RegistersDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            demoType = key.demoType,
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.Switch() {
    entry<SwitchNavKey> { key ->
        val demoViewModel: SwitchDemoViewModel = hiltViewModel()
        SwitchDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.ProximityGestureRecognition() {
    entry<ProximityGestureRecognitionNavKey> { key ->
        val demoViewModel: ProximityGestureRecognitionViewModel = hiltViewModel()
        ProximityGestureRecognitionDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.Pedometer() {
    entry<PedometerNavKey> { key ->
        val demoViewModel: PedometerViewModel = hiltViewModel()
        PedometerDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.MotionAlgorithms() {
    entry<MotionAlgorithmsNavKey> { key ->
        val demoViewModel: MotionAlgorithmsViewModel = hiltViewModel()
        MotionAlgorithmsDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.MemsGesture() {
    entry<MemsGestureNavKey> { key ->
        val demoViewModel: MemsGestureViewModel = hiltViewModel()
        MemsGestureDemoScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                ),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.CarryPosition() {
    entry<CarryPositionNavKey> { key ->
        val demoViewModel: CarryPositionViewModel = hiltViewModel()
        CarryPositionDemoScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                ),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.ActivityRecognition() {
    entry<ActivityRecognitionNavKey> { key ->
        val demoViewModel: ActivityRecognitionViewModel = hiltViewModel()
        ActivityRecognitionDemoScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                ),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.MotionIntensity() {
    entry<MotionIntensityNavKey> { key ->
        val demoViewModel: MotionIntensityViewModel = hiltViewModel()
        MotionIntensityDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.GNSS() {
    entry<GnssNavKey> { key ->
        val demoViewModel: GnssViewModel = hiltViewModel()
        GnssFragmentDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.ColorAmbientLight() {
    entry<ColorAmbientLightNavKey> { key ->
        val demoViewModel: ColorAmbientLightViewModel = hiltViewModel()
        ColorAmbientLightDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.ToFObjectsDetection() {
    entry<TofObjectsDetectionNavKey> { key ->
        val demoViewModel: TofObjectsDetectionViewModel = hiltViewModel()
        TofObjectsDetectionDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.ExtConfiguration() {
    entry<ExtConfigNavKey> { key ->
        val demoViewModel: ExtConfigViewModel = hiltViewModel()
        ExtConfigurationNavScreen(
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.BinaryContent() {
    entry<BinaryContentNavKey> { key ->
        val demoViewModel: BinaryContentViewModel = hiltViewModel()
        StBinaryContentScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = LocalDimensions.current.paddingNormal,
                    start = LocalDimensions.current.paddingNormal,
                    end = LocalDimensions.current.paddingNormal
                ),
//                                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.NfcWriting() {
    entry<NfcWritingNavKey> { key ->
        val demoViewModel: NfcWritingViewModel = hiltViewModel()
        NfcWritingDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.Plot() {
    entry<PlotNavKey> { key ->
        val demoViewModel: PlotViewModel = hiltViewModel()
        PlotDemoScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                ),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.PnPL() {
    entry<PnplNavKey> { key ->
        val demoViewModel: PnplViewModel = hiltViewModel()
        StPnplScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = LocalDimensions.current.paddingNormal,
                    start = LocalDimensions.current.paddingNormal,
                    end = LocalDimensions.current.paddingNormal
                ),
            viewModel = demoViewModel,
            nodeId = key.nodeId,
            demoName = key.demoName
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.Piano() {
    entry<PianoNavKey> { key ->
        val demoViewModel: PianoViewModel = hiltViewModel()
        PianoDemoScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                ),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.EventCounter() {
    entry<EventCounterNavKey> { key ->
        val demoViewModel: EventCounterViewModel = hiltViewModel()
        EventCounterDemoScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                ),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.NEAIExtrapolation() {
    entry<NEAIExtrapolationNavKey> { key ->
        val demoViewModel: NeaiExtrapolationViewModel = hiltViewModel()
        NeaiExtrapolationDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.NEAIClassification() {
    entry<NEAIClassificationNavKey> { key ->
        val demoViewModel: NeaiClassificationViewModel = hiltViewModel()
        NeaiClassificationDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.NEAIAnomalyDetection() {
    entry<NEAIAnomalyDetectionNavKey> { key ->
        val demoViewModel: NeaiAnomalyDetectionViewModel = hiltViewModel()
        NeaiAnomalyDetectionDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.NavigationGesture() {
    entry<NavigationGestureNavKey> { key ->
        val demoViewModel: GestureNavigationViewModel = hiltViewModel()
        GestureNavigationDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.BlueVoiceFullDuplex() {
    entry<BlueVoiceFullDuplexNavKey> { key ->
        val demoViewModel: BlueVoiceViewModel = hiltViewModel()
        BlueVoiceFullDuplexDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.BlueVoiceOpus() {
    entry<BlueVoiceOpusNavKey> { key ->
        val demoViewModel: BlueVoiceViewModel = hiltViewModel()
        BlueVoiceDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.AIoTCraftHighSpeedDataLog2() {
    entry<AIoTCraftHighSpeedDataLog2NavKey> { key ->
        val demoViewModel: AIoTCraftHighSpeedDataLogViewModel = hiltViewModel()
        AIoTCraftHighSpeedDataLog(
            modifier = Modifier.fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.SimpleHighSpeedDataLog2() {
    entry<SimpleHighSpeedDataLog2NavKey> { key ->
        val demoViewModel: SimpleHighSpeedDataLogViewModel = hiltViewModel()
        SimpleHighSpeedDataLog(
            modifier = Modifier.fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.HighSpeedDataLog2() {
    entry<HighSpeedDataLogNavKey> { key ->
        val demoViewModel: HighSpeedDataLogViewModel = hiltViewModel()
        HighSpeedDataLog(
            modifier = Modifier.fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.Compass() {
    entry<CompassNavKey> { key ->
        val demoViewModel: CompassViewModel = hiltViewModel()
        CompassDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.FitnessActivity() {
    entry<FitnessActivityNavKey> { key ->
        val demoViewModel: FitnessActivityViewModel = hiltViewModel()
        FitnessActivityDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.Level() {
    entry<LevelNavKey> { key ->
        val demoViewModel: LevelViewModel = hiltViewModel()
        LevelDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.Environmental() {
    entry<EnvironmentalNavKey> { key ->
        val demoViewModel: EnvironmentalViewModel = hiltViewModel()
        EnvironmentalDemoScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = LocalDimensions.current.paddingNormal,
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
                    start = LocalDimensions.current.paddingNormal,
                    end = LocalDimensions.current.paddingNormal
                ),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.BeamForming() {
    entry<BeamFormingNavKey> { key ->
        val demoViewModel: BlueVoiceViewModel = hiltViewModel()
        BeamFormingDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.FlowDemo() {
    entry<FlowNavKey> { key ->
        val demoViewModel: FlowDemoViewModel = hiltViewModel()
        FlowDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.HeadBoneConduction() {
    entry<HeadBoneConductionNavKey> { key ->
        val demoViewModel: HeadBoneConductionViewModel = hiltViewModel()
        HeadBoneConductionDemoScreen(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = demoViewModel,
            nodeId = key.nodeId
        )
    }
}
