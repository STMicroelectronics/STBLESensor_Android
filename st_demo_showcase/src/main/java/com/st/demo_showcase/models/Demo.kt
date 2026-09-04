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
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
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
import com.st.blue_sdk.features.extended.asset_tracking_event.AssetTrackingEvent
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
import com.st.blue_sdk.features.extended.medical_signal.MedicalSignal16BitFeature
import com.st.blue_sdk.features.extended.medical_signal.MedicalSignal24BitFeature
import com.st.blue_sdk.features.extended.motion_algorithm.MotionAlgorithm
import com.st.blue_sdk.features.extended.piano.Piano
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.predictive.PredictiveAccelerationStatus
import com.st.blue_sdk.features.extended.predictive.PredictiveFrequencyStatus
import com.st.blue_sdk.features.extended.predictive.PredictiveSpeedStatus
import com.st.blue_sdk.features.extended.raw_controlled.RawControlled
import com.st.blue_sdk.features.extended.registers_feature.RegistersFeature
import com.st.blue_sdk.features.extended.robotics_movement.RoboticsMovement
import com.st.blue_sdk.features.extended.scene_description.SceneDescription
import com.st.blue_sdk.features.extended.tof_multi_object.ToFMultiObject
import com.st.blue_sdk.features.external.std.HeartRate
import com.st.blue_sdk.features.external.stm32.led_and_reboot.ControlLedAndReboot
import com.st.blue_sdk.features.external.stm32.switch_status.SwitchStatus
import com.st.blue_sdk.features.fft.FFTAmplitudeFeature
import com.st.blue_sdk.features.humidity.Humidity
import com.st.blue_sdk.features.logging.sd.SDLoggingFeature
import com.st.blue_sdk.features.mems_gesture.MemsGesture
import com.st.blue_sdk.features.motion_intensity.MotionIntensity
import com.st.blue_sdk.features.ota.stm32wb.OTAControl
import com.st.blue_sdk.features.ota.stm32wb.OTAReboot
import com.st.blue_sdk.features.pedometer.Pedometer
import com.st.blue_sdk.features.pressure.Pressure
import com.st.blue_sdk.features.proximity_gesture.ProximityGesture
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusion
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusionCompat
import com.st.blue_sdk.features.switchfeature.SwitchFeature
import com.st.blue_sdk.features.temperature.Temperature
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.services.audio.AudioService
import com.st.demo_showcase.R
import com.st.demo_showcase.ui.demo_list.BeamFormingNavKey
import com.st.demo_showcase.ui.demo_list.CompassNavKey
import com.st.demo_showcase.ui.demo_list.EnvironmentalNavKey
import com.st.demo_showcase.ui.demo_list.FitnessActivityNavKey
import com.st.plot.utils.PLOTTABLE_FEATURE
import com.st.blue_sdk.features.compass.Compass as CompassFeature
import com.st.blue_sdk.features.event_counter.EventCounter as EventCounterFeature
import com.st.blue_sdk.features.extended.fitness_activity.FitnessActivity as FitnessActivityFeature
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigation as GestureNavigationFeature
import com.st.blue_sdk.features.extended.neai_anomaly_detection.NeaiAnomalyDetection as NeaiAnomalyDetectionFeature
import com.st.blue_sdk.features.extended.neai_class_classification.NeaiClassClassification as NeaiClassClassificationFeature
import com.st.blue_sdk.features.extended.neai_extrapolation.NeaiExtrapolation as NeaiExtrapolationFeature
import com.st.external_app.model.ExternalAppType
import com.st.external_app.ExternalAppAIoTCraft
import com.st.external_app.ExternalAppRobotics

import com.st.demo_showcase.ui.demo_list.*
import com.st.demo_showcase.ui.demo_show_case.DemoShowCasePnplSettingsNavKey
import com.st.registers_demo.common.RegistersDemoType

const val LOG_SETTINGS = "Log Settings"

val DEFAULT_MENU_ACTIONS = listOf(
    LOG_SETTINGS
)

@Keep
enum class Demo(
    @param:DrawableRes val icon: Int,
    var displayName: String,
    val group: List<DemoGroup> = emptyList(),
    val features: List<String>,
    val featuresNotAllowed: List<String>? = null,
    val requireAllFeatures: Boolean = false,
    val requireServer: Boolean = false,
    val couldBeEnabledOutside: Boolean = false,
    val isBoardTypeDependent: Boolean = false,
    val boardTypesAllowed: List<Boards.Model> = listOf(),
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
        features = PLOTTABLE_FEATURE
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
        features = listOf(NeaiClassClassificationFeature.NAME),
    ),
    NEAIExtrapolation(
        displayName = "NEAI Extrapolation",
        group = listOf(DemoGroup.AI),
        icon = com.st.neai_extrapolation.R.drawable.neai_icon,
        features = listOf(NeaiExtrapolationFeature.NAME)
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
        features = listOf(HSDataLogConfig.NAME, PnPL.NAME),
        settings = listOf(),
        isBoardTypeDependent = true,
        boardTypesAllowed = Boards.Model.entries.filter { it != Boards.Model.PROTEUS }
    ),
    SimpleHighSpeedDataLog2(
        displayName = "HighSpeed DataLog2",
        group = listOf(DemoGroup.AI, DemoGroup.DataLog),
        icon = com.st.high_speed_data_log.R.drawable.high_speed_data_log_icon,
        requireAllFeatures = true,
        features = listOf(HSDataLogConfig.NAME, PnPL.NAME),
        settings = listOf(),
        isBoardTypeDependent = true,
        boardTypesAllowed = listOf(Boards.Model.PROTEUS)
    ),
    AIoTCraftHighSpeedDataLog2(
        displayName = "HighSpeed DataLog2",
        group = listOf(DemoGroup.AI, DemoGroup.DataLog),
        icon = com.st.high_speed_data_log.R.drawable.high_speed_data_log_icon,
        requireAllFeatures = true,
        features = listOf(HSDataLogConfig.NAME, PnPL.NAME),
        settings = listOf(),
        isBoardTypeDependent = true,
        boardTypesAllowed = listOf() //In this way it will be not visible per any board
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
            AudioADPCMSyncFeature.NAME
        )
    ),
    BlueVoiceOpus(
        displayName = "BlueVoice Opus",
        group = listOf(DemoGroup.Audio),
        icon = com.st.blue_voice.R.drawable.blue_voice_icon,
        requireAllFeatures = true,
        features = listOf(
            AudioOpusFeature.NAME,
            AudioOpusConfFeature.NAME
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
        requireAllFeatures = true,
        features = listOf(
            AudioOpusFeature.NAME,
            AudioOpusConfFeature.NAME, BeamForming.NAME
        )
    ),
    BeamFormingDemoADPCM(
        displayName = "BeamForming ADPCM",
        group = listOf(DemoGroup.Audio),
        icon = com.st.blue_voice.R.drawable.beamforming_icon,
        requireAllFeatures = true,
        features = listOf(
            AudioADPCMFeature.NAME,
            AudioADPCMSyncFeature.NAME, BeamForming.NAME
        )
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
        requireAllFeatures = true,
        features = listOf(
            AudioOpusFeature.NAME,
            AudioOpusConfFeature.NAME
        )
    ),
    SpeechToTextDemoAPDCM(
        displayName = "SpeechToText ADPCM",
        group = listOf(DemoGroup.Audio, DemoGroup.Cloud),
        icon = com.st.blue_voice.R.drawable.blue_voice_icon,
        requireAllFeatures = true,
        features = listOf(
            AudioADPCMFeature.NAME,
            AudioADPCMSyncFeature.NAME
        )
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
        requireAllFeatures = true
    ),
    RegistersMLCDemo(
        displayName = "Machine Learning Core",
        group = listOf(DemoGroup.InertialSensors, DemoGroup.AI),
        icon = com.st.registers_demo.R.drawable.registers_demo_icon,
        features = listOf(RegistersFeature.ML_CORE_NAME)
    ),
    RegistersFSMDemo(
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
        icon = com.st.flow_demo.R.drawable.flow_icon,
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
        group = listOf(DemoGroup.EnvironmentalSensors, DemoGroup.Control),
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
    TextualMonitor(
        displayName = "Textual Monitor",
        group = listOf(DemoGroup.Debug),
        icon = com.st.textual_monitor.R.drawable.textual_monitor_icon,
        couldBeEnabledOutside = true,
        features = listOf()
    ),
    NodeStatus(
        displayName = "Rssi & Battery",
        group = listOf(DemoGroup.Status),
        icon = com.st.node_status.R.drawable.node_status_icon,
        features = listOf(Battery.NAME)
    ),
    RawPnpl(
        displayName = "Raw PnPL Controlled",
        icon = com.st.raw_pnpl.R.drawable.ic_rule,
        features = listOf(PnPL.NAME, RawControlled.NAME),
        requireAllFeatures = true,
        group = listOf(DemoGroup.Control)
    ),
    SmartMotorControl(
        displayName = "Smart Motor Control",
        icon = com.st.smart_motor_control.R.drawable.smart_motor_control_icon,
        features = listOf(PnPL.NAME, RawControlled.NAME),
        requireAllFeatures = true,
        group = listOf(DemoGroup.Control, DemoGroup.DataLog),
        settings = listOf()
    ),
    WbsOtaFUOTA(
        displayName = "FUOTA",
        icon = R.drawable.wbs_ota_config,
        group = listOf(DemoGroup.Fota),
        features = listOf(OTAControl.NAME, OTAReboot.NAME)
    ),
    CloudAzureIotCentral(
        displayName = "Cloud Azure IoT Central",
        group = listOf(DemoGroup.Cloud),
        couldBeEnabledOutside = true,
        icon = com.st.cloud_azure_iot_central.R.drawable.cloud_azure_iot_central_icon,
        features = emptyList()
    ),
    CloudMqtt(
        displayName = "Cloud MQTT",
        group = listOf(DemoGroup.Cloud),
        icon = com.st.cloud_mqtt.R.drawable.cloud_mqtt_icon,
        couldBeEnabledOutside = true,
        features = emptyList()
    ),
    MedicalSignal(
        displayName = "Medical Signals",
        icon = com.st.medical_signal.R.drawable.medical_signal_icon,
        features = listOf(MedicalSignal16BitFeature.NAME, MedicalSignal24BitFeature.NAME),
        group = listOf(DemoGroup.Health, DemoGroup.Graphs)
    ),
    ExternalAppLinkToAIoTCraft(
        displayName = ExternalAppAIoTCraft.appTitle,
        icon = ExternalAppAIoTCraft.appIcon,
        couldBeEnabledOutside = true,
        group = listOf(DemoGroup.AI, DemoGroup.DataLog),
        features = emptyList()
    ),
    ExternalAppLinkToRobotics(
        displayName = ExternalAppRobotics.appTitle,
        icon = ExternalAppRobotics.appIcon,
        couldBeEnabledOutside = false,
        group = listOf(DemoGroup.AI, DemoGroup.DataLog, DemoGroup.EnvironmentalSensors,DemoGroup.Control),
        features = listOf(RoboticsMovement.NAME,SceneDescription.NAME)
    ),
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
        group = listOf(DemoGroup.DataLog, DemoGroup.AI),
        icon = com.st.ui.R.drawable.multiple_log_icon,
        features = listOf(AiLogging.NAME)
    ),
    AssetTrackingEventDemo(
        displayName = "Asset Tracking Event",
        group = listOf(DemoGroup.InertialSensors),
        icon = com.st.asset_tracking_event.R.drawable.asset_tracking_event_icon,
        features = listOf(AssetTrackingEvent.NAME)
    ),
    HeadBoneConduction(
        displayName = "Bone Conduction",
        group = listOf(DemoGroup.Audio, DemoGroup.AI, DemoGroup.InertialSensors),
        icon = com.st.head_bone_conduction.R.drawable.head_bone_conduction_icon,
        couldBeEnabledOutside = true,
        features = emptyList()
    );

    fun navigateToPnplSettings(backState: NavBackStack<NavKey>, nodeId: String) {

        val direction = DemoShowCasePnplSettingsNavKey(nodeId = nodeId,
            demoName =  displayName.lowercase().replace(" ", "_"),
            currentDemo = this)

        backState.add(direction)
    }


    fun navigateTo(backState: NavBackStack<NavKey>, nodeId: String, isExpert: Boolean = false) {
        val direction = when (this) {
            Flow -> FlowNavKey(nodeId)
            BeamFormingDemo -> BeamFormingNavKey(nodeId)
            BeamFormingDemoADPCM -> BeamFormingNavKey(nodeId)
            Environmental -> EnvironmentalNavKey(nodeId)
            Level -> LevelNavKey(nodeId)
            FitnessActivity -> FitnessActivityNavKey(nodeId)
            Compass -> CompassNavKey(nodeId)
            HighSpeedDataLog2 -> HighSpeedDataLogNavKey(nodeId)
            SimpleHighSpeedDataLog2 -> SimpleHighSpeedDataLog2NavKey(nodeId)
            AIoTCraftHighSpeedDataLog2 -> AIoTCraftHighSpeedDataLog2NavKey(nodeId)
            BlueVoiceADPCM -> BlueVoiceOpusNavKey(nodeId)
            BlueVoiceOpus -> BlueVoiceOpusNavKey(nodeId)
            BlueVoiceFullDuplex -> BlueVoiceFullDuplexNavKey(nodeId)
            NavigationGesture -> NavigationGestureNavKey(nodeId)
            NEAIAnomalyDetection -> NEAIAnomalyDetectionNavKey(nodeId)
            NEAIClassification -> NEAIClassificationNavKey(nodeId)
            NEAIExtrapolation -> NEAIExtrapolationNavKey(nodeId)
            EventCounter -> EventCounterNavKey(nodeId)
            PianoDemo -> PianoNavKey(nodeId)
            Pnpl -> PnplNavKey(nodeId)
            Plot -> PlotNavKey(nodeId, isExpert)
            NfcWriting -> NfcWritingNavKey(nodeId)
            BinaryContentDemo -> BinaryContentNavKey(nodeId)
            ExtConfig -> ExtConfigNavKey(nodeId)
            TofObjectsDetection -> TofObjectsDetectionNavKey(nodeId)
            ColorAmbientLightDemo -> ColorAmbientLightNavKey(nodeId)
            Gnss -> GnssNavKey(nodeId)
            MotionIntensityDemo -> MotionIntensityNavKey(nodeId)
            ActivityRecognition -> ActivityRecognitionNavKey(nodeId)
            CarryPositionDemo -> CarryPositionNavKey(nodeId)
            MemsGestureDemo -> MemsGestureNavKey(nodeId)
            MotionAlgorithms -> MotionAlgorithmsNavKey(nodeId)
            PedometerDemo -> PedometerNavKey(nodeId)
            ProximityGestureRecognition -> ProximityGestureRecognitionNavKey(nodeId)
            SwitchDemo -> SwitchNavKey(nodeId)
            RegistersFSMDemo -> RegistersNavKey(
                nodeId = nodeId,
                demoType = RegistersDemoType.FSM
            )
            RegistersMLCDemo -> RegistersNavKey(
                nodeId = nodeId,
                demoType = RegistersDemoType.MLC
            )
            RegistersSTREDDemo -> RegistersNavKey(
                nodeId = nodeId,
                demoType = RegistersDemoType.STRED
            )
            AccelerationEventDemo -> AccelerationEventNavKey(nodeId)
            SourceLocalization -> SourceLocalizationNavKey(nodeId)
            AudioClassificationDemo -> AudioClassificationNavKey(nodeId)
            LedControl -> LedControlNavKey(nodeId)
            NodeStatus -> NodeStatusNavKey(nodeId)
            TextualMonitor -> TextualMonitorNavKey(nodeId)
            HeartRateDemo -> HeartRateNavKey(nodeId)
            SensorFusion -> SensorFusionNavKey(nodeId)
            PredictedMaintenance -> PredictedMaintenanceNavKey(nodeId)
            FftAmplitude -> FftAmplitudeNavKey(nodeId)
            MultiNeuralNetwork -> MultiNeuralNetworkNavKey(nodeId)
            HighSpeedDataLog1 -> ExternalAppNavKey(
                nodeId = nodeId,
                externalAppType = ExternalAppType.BLESENSORCLASSIC
            )
            SdLoggingDemo -> ExternalAppNavKey(
                nodeId = nodeId,
                externalAppType = ExternalAppType.BLESENSORCLASSIC
            )
            CoSensorDemo -> ExternalAppNavKey(
                nodeId = nodeId,
                externalAppType = ExternalAppType.BLESENSORCLASSIC
            )
            AiLoggingDemo -> ExternalAppNavKey(
                nodeId = nodeId,
                externalAppType = ExternalAppType.BLESENSORCLASSIC
            )
            AssetTrackingEventDemo -> AssetTrackingEventNavKey(nodeId)
            SpeechToTextDemo -> ExternalAppNavKey(
                nodeId = nodeId,
                externalAppType = ExternalAppType.BLESENSORCLASSIC
            )
            SpeechToTextDemoAPDCM -> ExternalAppNavKey(
                nodeId = nodeId,
                externalAppType = ExternalAppType.BLESENSORCLASSIC
            )
            RawPnpl -> RawPnplNavKey(nodeId)
            SmartMotorControl -> SmartMotorControlNavKey(nodeId)
            CloudAzureIotCentral -> CloudAzureIotCentralNavKey(nodeId)
            CloudMqtt -> CloudMqttNavKey(nodeId)
            MedicalSignal -> MedicalSignalNavKey(nodeId)
            BlueVoiceFullBand -> ExternalAppNavKey(
                nodeId = nodeId,
                externalAppType = ExternalAppType.BLESENSORCLASSIC
            )
            WbsOtaFUOTA -> WbsOtaFUOTA(nodeId)
            ExternalAppLinkToAIoTCraft -> ExternalAppNavKey(
                nodeId = nodeId,
                externalAppType = ExternalAppType.AIOTCRAFT
            )
            ExternalAppLinkToRobotics -> ExternalAppNavKey(
                nodeId = nodeId,
                externalAppType = ExternalAppType.ROBOTICS
            )
            HeadBoneConduction -> HeadBoneConductionNavKey(nodeId)
        }

        backState.add(direction)
    }

    companion object {

        private val map = entries.associateBy { it.displayName }
        infix fun from(name: String) = map[name]

        fun buildDemoList(
            blueManager: BlueManager,
            audioService: AudioService,
            nodeId: String
        ): List<Demo> = entries.filter {
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