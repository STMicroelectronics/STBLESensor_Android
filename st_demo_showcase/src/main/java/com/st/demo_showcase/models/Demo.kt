/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.models

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.navigation.NavController
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.acceleration_event.AccelerationEvent
import com.st.blue_sdk.features.activity.Activity
import com.st.blue_sdk.features.audio.adpcm.AudioADPCMFeature
import com.st.blue_sdk.features.audio.adpcm.AudioADPCMSyncFeature
import com.st.blue_sdk.features.battery.Battery
import com.st.blue_sdk.features.beam_forming.BeamForming
import com.st.blue_sdk.features.carry_position.CarryPosition
import com.st.blue_sdk.features.co_sensor.COSensor
import com.st.blue_sdk.features.direction_of_arrival.DirectionOfArrival
import com.st.blue_sdk.features.extended.ai_logging.AiLogging
import com.st.blue_sdk.features.extended.audio.opus.AudioOpusConfFeature
import com.st.blue_sdk.features.extended.audio.opus.AudioOpusFeature
import com.st.blue_sdk.features.extended.audio_classification.AudioClassification
import com.st.blue_sdk.features.extended.binary_content.BinaryContent
import com.st.blue_sdk.features.extended.color_ambient_light.ColorAmbientLight
import com.st.blue_sdk.features.extended.euler_angle.EulerAngle
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfiguration
import com.st.blue_sdk.features.extended.gnss.GNSS
import com.st.blue_sdk.features.extended.hs_datalog_config.HSDataLogConfig
import com.st.blue_sdk.features.extended.json_nfc.JsonNFC
import com.st.blue_sdk.features.extended.motion_algorithm.MotionAlgorithm
import com.st.blue_sdk.features.extended.piano.Piano
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.predictive.PredictiveAccelerationStatus
import com.st.blue_sdk.features.extended.predictive.PredictiveFrequencyStatus
import com.st.blue_sdk.features.extended.predictive.PredictiveSpeedStatus
import com.st.blue_sdk.features.extended.qvar.QVAR
import com.st.blue_sdk.features.extended.registers_feature.RegistersFeature
import com.st.blue_sdk.features.extended.tof_multi_object.ToFMultiObject
import com.st.blue_sdk.features.external.std.HeartRate
import com.st.blue_sdk.features.external.stm32.led_and_reboot.ControlLedAndReboot
import com.st.blue_sdk.features.external.stm32.switch_status.SwitchStatus
import com.st.blue_sdk.features.fft.FFTAmplitudeFeature
import com.st.blue_sdk.features.humidity.Humidity
import com.st.blue_sdk.features.logging.sd.SDLoggingFeature
import com.st.blue_sdk.features.mems_gesture.MemsGesture
import com.st.blue_sdk.features.motion_intensity.MotionIntensity
import com.st.blue_sdk.features.pedometer.Pedometer
import com.st.blue_sdk.features.pressure.Pressure
import com.st.blue_sdk.features.proximity_gesture.ProximityGesture
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusion
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusionCompat
import com.st.blue_sdk.features.switchfeature.SwitchFeature
import com.st.blue_sdk.features.temperature.Temperature
import com.st.blue_sdk.services.audio.AudioService
import com.st.demo_showcase.DemoShowcaseNavGraphDirections
import com.st.demo_showcase.ui.demo_list.DemoListFragmentDirections
import com.st.plot.utils.PLOTTABLE_FEATURE
import com.st.plot.utils.PLOT_SETTINGS
import com.st.blue_sdk.features.compass.Compass as CompassFeature
import com.st.blue_sdk.features.event_counter.EventCounter as EventCounterFeature
import com.st.blue_sdk.features.extended.fitness_activity.FitnessActivity as FitnessActivityFeature
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigation as GestureNavigationFeature
import com.st.blue_sdk.features.extended.neai_anomaly_detection.NeaiAnomalyDetection as NeaiAnomalyDetectionFeature
import com.st.blue_sdk.features.extended.neai_class_classification.NeaiClassClassification as NeaiClassClassificationFeature

const val LOG_SETTINGS = "Log Settings"
const val SERIAL_CONSOLE = "Serial Console"

val DEFAULT_MENU_ACTIONS = listOf(
    LOG_SETTINGS
)

@Keep
enum class Demo(
    @DrawableRes val icon: Int,
    val displayName: String,
    val group: List<DemoGroup> = emptyList(),
    val features: List<String>,
    val featuresNotAllowed: List<String>? = null,
    val requireAllFeatures: Boolean = false,
    val requireServer: Boolean = false,
    val couldBeEnabledOutside: Boolean = false,
    val settings: List<String> = DEFAULT_MENU_ACTIONS
) {
    Environmental(
        displayName = "Environmental",
        group = listOf(DemoGroup.EnvironmentalSensors),
        icon = com.st.environmental.R.drawable.environmental_icon,
        features = listOf(Temperature.NAME, Humidity.NAME, Pressure.NAME)
    ),
    Plot(
        displayName = "Plot Data",
        group = listOf(DemoGroup.Graphs, DemoGroup.Log),
        icon = com.st.plot.R.drawable.plot_icon,
        features = PLOTTABLE_FEATURE,
        settings = listOf(LOG_SETTINGS, PLOT_SETTINGS)
    ),
    FftAmplitude(
        displayName = "FFT Amplitude",
        group = listOf(DemoGroup.PredictiveMaintenance, DemoGroup.Graphs),
        icon = com.st.fft_amplitude.R.drawable.fft_amplitude_icon,
        features = listOf(FFTAmplitudeFeature.NAME)
    ),
    NEAIAnomalyDetection(
        displayName = "NEAI Anomaly Detection",
        group = listOf(DemoGroup.AI, DemoGroup.PredictiveMaintenance),
        icon = com.st.neai_anomaly_detection.R.drawable.neai_logo,
        features = listOf(NeaiAnomalyDetectionFeature.NAME)
    ),
    NEAIClassification(
        displayName = "NEAI Classification",
        group = listOf(DemoGroup.AI),
        icon = com.st.neai_classification.R.drawable.neai_icon,
        features = listOf(NeaiClassClassificationFeature.NAME)
    ),
    PredictedMaintenance(
        displayName = "Board Pred Maintenance",
        group = listOf(DemoGroup.PredictiveMaintenance, DemoGroup.Status),
        icon = com.st.predicted_maintenance.R.drawable.predicted_maintenance_icon,
        features = listOf(
            PredictiveSpeedStatus.NAME, PredictiveAccelerationStatus.NAME,
            PredictiveFrequencyStatus.NAME
        )
    ),
    HighSpeedDataLog2(
        displayName = "HighSpeed DataLog2",
        group = listOf(DemoGroup.AI, DemoGroup.DataLog),
        icon = com.st.high_speed_data_log.R.drawable.high_speed_data_log_icon,
        requireAllFeatures = true,
        features = listOf(HSDataLogConfig.NAME, PnPL.NAME)
    ),
    HighSpeedDataLog1(
        displayName = "HighSpeed DataLog",
        group = listOf(DemoGroup.AI, DemoGroup.DataLog),
        icon = com.st.high_speed_data_log.R.drawable.high_speed_data_log_icon,
        features = listOf(HSDataLogConfig.NAME),
        featuresNotAllowed = listOf(PnPL.NAME)
    ),
    Pnpl(
        displayName = "PnPL",
        group = listOf(DemoGroup.Control, DemoGroup.Configuration),
        icon = com.st.pnpl.R.drawable.pnpl_icon,
        features = listOf(PnPL.NAME)
    ),
    ExtConfig(
        displayName = "Board Configuration",
        group = listOf(DemoGroup.Configuration),
        icon = com.st.ext_config.R.drawable.ext_config_icon,
        requireAllFeatures = true,
        features = listOf(ExtConfiguration.NAME)
    ),
    SwitchDemo(
        displayName = "Switch",
        group = listOf(DemoGroup.Control),
        icon = com.st.switch_demo.R.drawable.switch_demo_icon,
        features = listOf(SwitchFeature.NAME)
    ),
    LedControl(
        displayName = "Led Control",
        group = listOf(DemoGroup.Control),
        icon = com.st.led_control.R.drawable.led_control_icon,
        features = listOf(SwitchStatus.NAME, ControlLedAndReboot.NAME),
        requireAllFeatures = true
    ),
    HeartRateDemo(
        displayName = "Heart Rate",
        icon = com.st.heart_rate_demo.R.drawable.heart_rate_demo_icon,
        group = listOf(DemoGroup.Health),
        features = listOf(HeartRate.NAME)
    ),
    BlueVoiceADPCM(
        displayName = "BlueVoice ADPCM",
        group = listOf(DemoGroup.Audio),
        icon = com.st.blue_voice.R.drawable.blue_voice_icon,
        requireAllFeatures = true,
        features = listOf(
            AudioADPCMFeature.NAME,
            AudioADPCMSyncFeature.NAME,
        )
    ),
    BlueVoiceOpus(
        displayName = "BlueVoice Opus",
        group = listOf(DemoGroup.Audio),
        icon = com.st.blue_voice.R.drawable.blue_voice_icon,
        requireAllFeatures = true,
        features = listOf(
            AudioOpusFeature.NAME,
            AudioOpusConfFeature.NAME,
        )
    ),
    BlueVoiceFullDuplex(
        displayName = "BlueVoice FullDuplex",
        group = listOf(DemoGroup.Audio),
        icon = com.st.blue_voice.R.drawable.blue_voice_icon,
        requireAllFeatures = true,
        requireServer = true,
//        features = listOf(
//            AudioOpusFeature.NAME,
//            AudioOpusConfFeature.NAME,
//        )
        couldBeEnabledOutside = true,
        features = emptyList()
    ),
    BlueVoiceFullBand(
        displayName = "BlueVoice FullBand",
        group = listOf(DemoGroup.Audio),
        icon = com.st.blue_voice.R.drawable.blue_voice_icon,
        requireServer = true,
        couldBeEnabledOutside = true,
        features = emptyList()
    ),
    BeamFormingDemo(
        displayName = "BeamForming",
        group = listOf(DemoGroup.Audio),
        icon = com.st.blue_voice.R.drawable.beamforming_icon,
        requireAllFeatures=true,
        features = listOf(AudioOpusFeature.NAME,
            AudioOpusConfFeature.NAME, BeamForming.NAME)
    ),
    BeamFormingDemoADPCM(
        displayName = "BeamForming ADPCM",
        group = listOf(DemoGroup.Audio),
        icon = com.st.blue_voice.R.drawable.beamforming_icon,
        requireAllFeatures=true,
        features = listOf(AudioADPCMFeature.NAME,
            AudioADPCMSyncFeature.NAME, BeamForming.NAME)
    ),
    SourceLocalization(
        displayName = "Source Localization",
        group = listOf(DemoGroup.Audio),
        icon = com.st.source_localization.R.drawable.source_localization_icon,
        features = listOf(DirectionOfArrival.NAME)
    ),
    SpeechToTextDemo(
        displayName = "SpeechToText",
        group = listOf(DemoGroup.Audio, DemoGroup.Cloud),
        icon = com.st.blue_voice.R.drawable.blue_voice_icon,
        requireAllFeatures=true,
        features = listOf(AudioOpusFeature.NAME,
            AudioOpusConfFeature.NAME)
    ),
    SpeechToTextDemoAPDCM(
        displayName = "SpeechToText ADPCM",
        group = listOf(DemoGroup.Audio, DemoGroup.Cloud),
        icon = com.st.blue_voice.R.drawable.blue_voice_icon,
        requireAllFeatures=true,
        features = listOf(AudioADPCMFeature.NAME,
            AudioADPCMSyncFeature.NAME)
    ),
    AudioClassificationDemo(
        displayName = "Audio Classification",
        group = listOf(DemoGroup.Audio, DemoGroup.AI),
        icon = com.st.audio_classification_demo.R.drawable.audio_classification_demo_icon,
        features = listOf(AudioClassification.NAME)
    ),
    ActivityRecognition(
        displayName = "Activity Recognition",
        group = listOf(DemoGroup.AI, DemoGroup.InertialSensors, DemoGroup.Health),
        icon = com.st.activity_recognition.R.drawable.activity_recognition_icon,
        features = listOf(Activity.NAME)
    ),
    MultiNeuralNetwork(
        displayName = "Multi Neural Network",
        group = listOf(DemoGroup.AI, DemoGroup.InertialSensors, DemoGroup.Audio, DemoGroup.Health),
        icon = com.st.multi_neural_network.R.drawable.multi_neural_network_icon,
        features = listOf(Activity.NAME, AudioClassification.NAME),
        requireAllFeatures = true,
    ),
    RegistersMLCDemo(
        displayName = "Machine Learning Core",
        group = listOf(DemoGroup.InertialSensors, DemoGroup.AI),
        icon = com.st.registers_demo.R.drawable.registers_demo_icon,
        features = listOf(RegistersFeature.ML_CORE_NAME)
    ),
    RegistersFMSDemo(
        displayName = "Finite State Machine",
        group = listOf(DemoGroup.InertialSensors, DemoGroup.AI),
        icon = com.st.registers_demo.R.drawable.registers_demo_icon,
        features = listOf(RegistersFeature.FSM_NAME)
    ),
    RegistersSTREDDemo(
        displayName = "STRed-ISPU",
        group = listOf(DemoGroup.InertialSensors, DemoGroup.AI),
        icon = com.st.registers_demo.R.drawable.registers_demo_icon,
        features = listOf(RegistersFeature.STRED_NAME)
    ),
    Flow(
        displayName = "Flow",
        group = listOf(DemoGroup.Control),
        icon = com.st.ui.R.drawable.flow_icon,
        couldBeEnabledOutside = true,
        features = emptyList()
    ),
    EventCounter(
        displayName = "Event Counter",
        group = listOf(DemoGroup.Status),
        icon = com.st.event_counter.R.drawable.event_counter_icon,
        features = listOf(EventCounterFeature.NAME)
    ),
    NavigationGesture(
        displayName = "Navigation Gesture",
        group = listOf(DemoGroup.EnvironmentalSensors,DemoGroup.Control),
        icon = com.st.gesture_navigation.R.drawable.gesture_navigation_icon,
        features = listOf(GestureNavigationFeature.NAME)
    ),
    NfcWriting(
        displayName = "NFC Writing",
        group = listOf(DemoGroup.Configuration),
        icon = com.st.nfc_writing.R.drawable.connectivity_nfc,
        features = listOf(JsonNFC.NAME)
    ),
    BinaryContentDemo(
        displayName = "Binary Content",
        group = listOf(DemoGroup.BinaryContent),
        icon = com.st.binary_content.R.drawable.binary_content_icon,
        features = listOf(BinaryContent.NAME, PnPL.NAME),
        requireAllFeatures = true
    ),
    PianoDemo(
        displayName = "Piano",
        group = listOf(DemoGroup.Audio),
        icon = com.st.piano.R.drawable.piano_icon,
        features = listOf(Piano.NAME)
    ),
    PedometerDemo(
        displayName = "Pedometer",
        group = listOf(DemoGroup.InertialSensors),
        icon = com.st.pedometer.R.drawable.pedometer_icon,
        features = listOf(Pedometer.NAME)
    ),
    Level(
        displayName = "Level",
        group = listOf(DemoGroup.InertialSensors),
        icon = com.st.level.R.drawable.level_icon,
        features = listOf(EulerAngle.NAME)
    ),
    Compass(
        displayName = "Compass",
        group = listOf(DemoGroup.InertialSensors),
        icon = com.st.compass.R.drawable.compass_icon,
        features = listOf(CompassFeature.NAME)
    ),
    SensorFusion(
        displayName = "MEMS Sensor Fusion",
        icon = com.st.sensor_fusion.R.drawable.sensor_fusion_icon,
        group = listOf(DemoGroup.InertialSensors),
        features = listOf(MemsSensorFusion.NAME, MemsSensorFusionCompat.NAME)
    ),
    MemsGestureDemo(
        displayName = "Mems Gesture",
        group = listOf(DemoGroup.InertialSensors),
        icon = com.st.mems_gesture.R.drawable.mems_gesture_icon,
        features = listOf(MemsGesture.NAME)
    ),
    MotionAlgorithms(
        displayName = "Motion Algorithms",
        group = listOf(DemoGroup.InertialSensors),
        icon = com.st.motion_algorithms.R.drawable.motion_algorithms_icon,
        features = listOf(MotionAlgorithm.NAME)
    ),
    CarryPositionDemo(
        displayName = "Carry Position",
        group = listOf(DemoGroup.InertialSensors),
        icon = com.st.carry_position.R.drawable.carry_position_icon,
        features = listOf(CarryPosition.NAME)
    ),
    MotionIntensityDemo(
        displayName = "Motion Intensity",
        group = listOf(DemoGroup.InertialSensors),
        icon = com.st.motion_intensity.R.drawable.motion_intensity_icon,
        features = listOf(MotionIntensity.NAME)
    ),
    FitnessActivity(
        displayName = "Fitness Activity",
        group = listOf(DemoGroup.InertialSensors, DemoGroup.Health),
        icon = com.st.fitness.R.drawable.fitness_activity_icon,
        features = listOf(FitnessActivityFeature.NAME)
    ),
    AccelerationEventDemo(
        displayName = "Acc Event",
        group = listOf(DemoGroup.InertialSensors),
        icon = com.st.acceleration_event.R.drawable.acceleration_event_icon,
        features = listOf(AccelerationEvent.NAME)
    ),
    Gnss(
        displayName = "GNSS",
        group = listOf(DemoGroup.EnvironmentalSensors),
        icon = com.st.gnss.R.drawable.gnss_icon_demo,
        features = listOf(GNSS.NAME)
    ),
    ColorAmbientLightDemo(
        displayName = "Color Ambient Light",
        group = listOf(DemoGroup.EnvironmentalSensors),
        icon = com.st.color_ambient_light.R.drawable.color_ambient_light_icon,
        features = listOf(ColorAmbientLight.NAME)
    ),
    TofObjectsDetection(
        displayName = "ToF Objects Detection",
        group = listOf(DemoGroup.EnvironmentalSensors),
        icon = com.st.tof_objects_detection.R.drawable.tof_objects_detection_icon,
        features = listOf(ToFMultiObject.NAME)
    ),
    ProximityGestureRecognition(
        displayName = "Proximity Gesture",
        group = listOf(DemoGroup.EnvironmentalSensors),
        icon = com.st.proximity_gesture_recognition.R.drawable.proximity_gesture_recognition_icon,
        features = listOf(ProximityGesture.NAME)
    ),
    ElectricChargeVariation(
        displayName = "Electric Charge Variation",
        group = listOf(DemoGroup.EnvironmentalSensors),
        icon = com.st.electric_charge_variation.R.drawable.electric_charge_variation_icon,
        features = listOf(QVAR.NAME)
    ),
    TextualMonitor(
        displayName = "Textual Monitor",
        group = listOf(DemoGroup.Debug),
        icon = com.st.textual_monitor.R.drawable.textual_monitor_icon,
        couldBeEnabledOutside = true,
        features = listOf()
    ),
    Cloud(
        displayName = "Cloud Logging",
        group = listOf(DemoGroup.Cloud),
        icon = com.st.ui.R.drawable.demo_cloud,
        couldBeEnabledOutside = true,
        features = emptyList()
    ),
    NodeStatus(
        displayName = "Rssi & Battery",
        group = listOf(DemoGroup.Status),
        icon = com.st.node_status.R.drawable.node_status_icon,
        features = listOf(Battery.NAME)
    ),
    // *** NEW_DEMO_TEMPLATE ANCHOR1 ***\
    SdLoggingDemo(
        displayName = "SD Logging",
        group = listOf(DemoGroup.DataLog),
        icon = com.st.ui.R.drawable.multiple_log_icon,
        features = listOf(SDLoggingFeature.NAME)
    ),
    CoSensorDemo(
        displayName = "CO Sensor",
        group = listOf(DemoGroup.EnvironmentalSensors),
        icon = com.st.ui.R.drawable.co_sensor_icon,
        features = listOf(COSensor.NAME)
    ),
    AiLoggingDemo(
        displayName = "AI Data Log",
        group = listOf(DemoGroup.DataLog,DemoGroup.AI),
        icon =  com.st.ui.R.drawable.multiple_log_icon,
        features = listOf(AiLogging.NAME)
    );

    fun navigateToPnplSettings(navController: NavController?, nodeId: String) {

        val direction =
            DemoShowcaseNavGraphDirections.globalActionToPnplFragment(
                nodeId = nodeId,
                demoName = displayName.lowercase().replace(" ", "_")
            )

        navController?.navigate(directions = direction)
    }

    fun navigateTo(navController: NavController, nodeId: String) {

        val direction = when (this) {
            Flow -> DemoListFragmentDirections.actionDemoListToWorkingInProgressFragment(nodeId)
            Cloud -> DemoListFragmentDirections.actionDemoListToWorkingInProgressFragment(nodeId)
            BeamFormingDemo -> DemoListFragmentDirections.actionDemoListToBeamFormingFragment(
                nodeId
            )

            BeamFormingDemoADPCM -> DemoListFragmentDirections.actionDemoListToBeamFormingFragment(
                nodeId
            )

            Environmental -> DemoListFragmentDirections.actionDemoListToEnvironmentalDemoFragment(
                nodeId
            )

            Level -> DemoListFragmentDirections.actionDemoListToLevelDemoFragment(nodeId)
            FitnessActivity -> DemoListFragmentDirections.actionDemoListToFitnessActivityDemoFragment(
                nodeId
            )

            Compass -> DemoListFragmentDirections.actionDemoListToCompassDemoGraph(nodeId)
            HighSpeedDataLog2 -> DemoListFragmentDirections.actionDemoListToHighSpeedDataLogFragment(
                nodeId
            )

            BlueVoiceADPCM -> DemoListFragmentDirections.actionDemoListToBlueVoiceFragment(
                nodeId
            )

            BlueVoiceOpus -> DemoListFragmentDirections.actionDemoListToBlueVoiceFragment(
                nodeId
            )

            BlueVoiceFullDuplex -> DemoListFragmentDirections.actionDemoListToBlueVoiceFullDuplexFragment(
                nodeId
            )

            NavigationGesture -> DemoListFragmentDirections.actionDemoListToGestureNavigationDemoFragment(
                nodeId
            )

            NEAIAnomalyDetection -> DemoListFragmentDirections.actionDemoListToNeaiAnomalyDetectionFragment(
                nodeId
            )

            NEAIClassification -> DemoListFragmentDirections.actionDemoListToNeaiClassificationFragment(
                nodeId
            )

            EventCounter -> DemoListFragmentDirections.actionDemoListToEventCounterFragment(
                nodeId
            )

            PianoDemo -> DemoListFragmentDirections.actionDemoListToPianoFragment(
                nodeId
            )

            Pnpl -> DemoListFragmentDirections.actionDemoListToPnplFragment(nodeId)
            Plot -> DemoListFragmentDirections.actionDemoListToPlotFragment(nodeId)
            NfcWriting -> DemoListFragmentDirections.actionDemoListToNfcWritingFragment(nodeId)
            BinaryContentDemo -> DemoListFragmentDirections.actionDemoListToBinaryContentFragment(
                nodeId
            )

            ExtConfig -> DemoListFragmentDirections.actionDemoListToExtConfigDemoGraph(nodeId)
            TofObjectsDetection -> DemoListFragmentDirections.actionDemoListToTofObjectsDetectionFragment(
                nodeId
            )

            ColorAmbientLightDemo -> DemoListFragmentDirections.actionDemoListToColorAmbientLightFragment(
                nodeId
            )

            Gnss -> DemoListFragmentDirections.actionDemoListToGnssFragment(nodeId)
            ElectricChargeVariation -> DemoListFragmentDirections.actionDemoListToElectricChargeVariationFragment(
                nodeId
            )

            MotionIntensityDemo -> DemoListFragmentDirections.actionDemoListToMotionIntensityFragment(
                nodeId
            )

            ActivityRecognition -> DemoListFragmentDirections.actionDemoListToActivityRecognitionFragment(
                nodeId
            )

            CarryPositionDemo -> DemoListFragmentDirections.actionDemoListToCarryPositionFragment(
                nodeId
            )

            MemsGestureDemo -> DemoListFragmentDirections.actionDemoListToMemsGestureFragment(
                nodeId
            )

            MotionAlgorithms -> DemoListFragmentDirections.actionDemoListToMotionAlgorithmsFragment(
                nodeId
            )

            PedometerDemo -> DemoListFragmentDirections.actionDemoListToPedometerFragment(nodeId)
            ProximityGestureRecognition ->
                DemoListFragmentDirections.actionDemoListToProximityGestureRecognitionFragment(
                    nodeId
                )

            SwitchDemo -> DemoListFragmentDirections.actionDemoListToSwitchDemoFragment(nodeId)
            RegistersFMSDemo -> DemoListFragmentDirections.actionDemoListToRegistersDemoFragment(
                nodeId,
                "FSM"
            )

            RegistersMLCDemo -> DemoListFragmentDirections.actionDemoListToRegistersDemoFragment(
                nodeId,
                "MLC"
            )

            RegistersSTREDDemo -> DemoListFragmentDirections.actionDemoListToRegistersDemoFragment(
                nodeId,
                "STRED"
            )

            AccelerationEventDemo -> DemoListFragmentDirections.actionDemoListToAccelerationEventFragment(
                nodeId
            )

            SourceLocalization -> DemoListFragmentDirections.actionDemoListToSourceLocalizationFragment(
                nodeId
            )

            AudioClassificationDemo -> DemoListFragmentDirections.actionDemoListToAudioClassificationDemoFragment(
                nodeId
            )

            LedControl -> DemoListFragmentDirections.actionDemoListToLedControlFragment(nodeId)
            NodeStatus -> DemoListFragmentDirections.actionDemoListToNodeStatusFragment(nodeId)
            TextualMonitor -> DemoListFragmentDirections.actionDemoListToTextualMonitorFragment(
                nodeId
            )

            HeartRateDemo -> DemoListFragmentDirections.actionDemoListToHeartRateDemoFragment(nodeId)
            SensorFusion -> DemoListFragmentDirections.actionDemoListToSensorFusionFragment(nodeId)
            PredictedMaintenance -> DemoListFragmentDirections.actionDemoListToPredictedMaintenanceFragment(
                nodeId
            )

            FftAmplitude -> DemoListFragmentDirections.actionDemoListToFftAmplitudeFragment(nodeId)
            MultiNeuralNetwork -> DemoListFragmentDirections.actionDemoListToMultiNeuralNetworkFragment(
                nodeId
            )

            HighSpeedDataLog1 -> DemoListFragmentDirections.actionDemoListToLegacyDemoFragment(
                nodeId
            )

            SdLoggingDemo -> DemoListFragmentDirections.actionDemoListToLegacyDemoFragment(
                nodeId
            )

            CoSensorDemo -> DemoListFragmentDirections.actionDemoListToLegacyDemoFragment(
                nodeId
            )

            AiLoggingDemo -> DemoListFragmentDirections.actionDemoListToLegacyDemoFragment(
                nodeId
            )

            SpeechToTextDemo -> DemoListFragmentDirections.actionDemoListToLegacyDemoFragment(
                nodeId
            )

            SpeechToTextDemoAPDCM -> DemoListFragmentDirections.actionDemoListToLegacyDemoFragment(
                nodeId
            )
            // *** NEW_DEMO_TEMPLATE ANCHOR2 ***
            BlueVoiceFullBand -> DemoListFragmentDirections.actionDemoListToLegacyDemoFragment(
                nodeId
            )
        }

        direction.let { navController.navigate(directions = it) }
    }

    companion object {
        fun buildDemoList(
            blueManager: BlueManager,
            audioService: AudioService,
            nodeId: String
        ): List<Demo> = values().filter {
            if (it.couldBeEnabledOutside) {
                false
            } else {
                if (it.featuresNotAllowed == null) {
                    if (it.requireAllFeatures) {
                        blueManager.allFeatures(nodeId, it.features)
                    } else {
                        if (it.features.isEmpty()) {
                            true
                        } else {
                            blueManager.anyFeatures(nodeId, it.features)
                        }
                    }
                } else {
                    if (it.requireAllFeatures) {
                        (blueManager.allFeatures(nodeId, it.features) &&
                                !blueManager.anyFeatures(nodeId, it.featuresNotAllowed))
                    } else {
                        (blueManager.anyFeatures(nodeId, it.features) && !blueManager.anyFeatures(
                            nodeId, it.featuresNotAllowed
                        ))
                    }
                }
            }
        }.filter {
            if (it.requireServer) {
                audioService.isServerEnable(nodeId)
            } else {
                true
            }
        }
    }
}