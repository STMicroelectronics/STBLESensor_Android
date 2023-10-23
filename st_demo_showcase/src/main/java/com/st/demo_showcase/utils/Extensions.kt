/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.utils

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AreaChart
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.NavController
import com.st.demo_showcase.DemoShowcaseNavGraphDirections
import com.st.demo_showcase.models.Demo
import com.st.demo_showcase.models.LOG_SETTINGS
import com.st.plot.PlotFragmentDirections
import com.st.plot.utils.PLOT_SETTINGS
import com.st.ui.composables.ActionItem

fun List<String>?.toActions(
    demo: Demo?,
    navController: NavController?,
    nodeId: String
): List<ActionItem> {
    if (this == null) return emptyList()
    return this.map {
        ActionItem(
            label = it,
            imageVector = when (it) {
                LOG_SETTINGS -> Icons.Default.Settings
                PLOT_SETTINGS -> Icons.Default.AreaChart
                else -> null
            },
            action = {
                val direction = when (demo) {
                    Demo.Plot ->
                        when (it) {
                            PLOT_SETTINGS -> PlotFragmentDirections.actionDemoPlotToPlotSettingsFragment()
                            LOG_SETTINGS -> DemoShowcaseNavGraphDirections.globalActionToLogSettings()
                            else -> null
                        }

                    else -> when (it) {
                        LOG_SETTINGS -> DemoShowcaseNavGraphDirections.globalActionToLogSettings()
                        else -> null
                    }
                }
                direction?.let { dir ->
                    navController?.navigate(dir)
                }
            }
        )
    }
}

fun Demo.isLoginRequired(): Boolean = when (this) {
//    Demo.Flow -> true
//    Demo.Cloud -> true
//    Demo.ExtConfig -> true
    else -> false
}

fun Demo.isExpertRequired(): Boolean = when (this) {
    //Demo.Flow -> true
    Demo.ExtConfig -> true
    Demo.Pnpl -> true
    Demo.TextualMonitor -> true
    else -> false
}

fun Demo.isPnPLMandatory(): Boolean = when (this) {
    Demo.Pnpl -> true
    Demo.HighSpeedDataLog2 -> true
    Demo.BinaryContentDemo -> true
    else -> false
}

@Suppress("UNUSED_PARAMETER")
fun Demo.getDescription(context: Context): String =
    when (this) {
        Demo.Flow -> "Deploy one application to the board"
        Demo.Cloud -> "Connect the board to different cloud providers"
        Demo.Environmental -> "Display available temperature, pressure, humidity and Lux sensors values"
        Demo.Level -> "Show Level and Pitch&Roll"
        Demo.FitnessActivity -> "Display recognized fitness activity and counter reps"
        Demo.Compass -> "Display a magnetic compass direction"
        Demo.HighSpeedDataLog2 -> "High speed sensors data log configuration, control and tagging"
        Demo.BlueVoiceADPCM -> "\"Bluevoice\" ADPCM audio bluetooth streaming"
        Demo.BlueVoiceOpus -> "\"Bluevoice\" audio bluetooth streaming using advanced OPUS compression algorithm"
        Demo.BlueVoiceFullDuplex -> "\"BlueVoice\" audio bluetooth streaming in a full-duplex configuration"
        Demo.NavigationGesture -> "Recognition of gesture navigation using sensor"
        Demo.NEAIAnomalyDetection -> "AI library (generated using NanoEdgeAIStudio) for predictive maintenance solution"
        Demo.NEAIClassification -> "AI library (generated using NanoEdgeAIStudio) for classification"
        Demo.EventCounter -> "Display the counter that will be increased at each event detected by board"
        Demo.PianoDemo -> "Display a Piano keyboard for playing audio notes on the board"
        Demo.Pnpl -> "Board Control and Configuration using PnP-Like messages defined by a DTDL-Model"
        Demo.Plot -> "Display the sensors' value on a configurable plot"
        Demo.NfcWriting -> "Write NDEF records (Text/Wi-Fi/Business Card and URL) to the board"
        Demo.BinaryContentDemo -> "Receive or Send to the board a Binary content"
        Demo.ExtConfig -> "Advance board extended configuration trough json-like messages"
        Demo.TofObjectsDetection -> "Multi objects' distance and presence detection using Time-of-Flight (ToF) sensor"
        Demo.ColorAmbientLightDemo -> "Display illuminance, intensity UV radiation and correlated color temperature"
        Demo.Gnss -> "Display GNSS coordinates (Latitude, Longitude and Altitude) and satellites' signal information"
        Demo.ElectricChargeVariation -> "Display Raw data coming from electric charge variation (QVAR) sensor"
        Demo.MotionIntensityDemo -> "Display the level of motion intensity measured by the board"
        Demo.ActivityRecognition -> "Display the activity recognized using different algorithms that could be enabled on the board"
        Demo.CarryPositionDemo -> "Display the board carry position recognized"
        Demo.MemsGestureDemo -> "Recognition of the gesture performed by the user with the board"
        Demo.MotionAlgorithms -> "Recognition of different human stances with different algorithms"
        Demo.PedometerDemo -> "Calculate number of steps and its frequency"
        Demo.ProximityGestureRecognition -> "Gesture recognition (tap/swap) using Time-of-Flight (ToF) sensor"
        Demo.SwitchDemo -> "Switch on/off the LED placed on the board"
        Demo.RegistersFMSDemo -> "Display the registers output for the Finite State Machine core present on advance accelerometer"
        Demo.RegistersMLCDemo -> "Display the registers output for the Machine Learning core present on advance accelerometer"
        Demo.RegistersSTREDDemo -> "Display the registers output for the  STRed-ISPU core"
        Demo.AccelerationEventDemo -> "Detect different acceleration event types"
        Demo.SourceLocalization -> "Real time source localization algorithm using the signals acquired from multiple board's microphones"
        Demo.AudioClassificationDemo -> "Real time audio scene classification using the signal acquired from board's microphone"
        Demo.LedControl -> "Switch on/off the LED placed on the board and display RSSI value and the alarm received from the board"
        Demo.NodeStatus -> "Display board RSSI and Battery information if available"
        Demo.TextualMonitor -> "Show in a textual way the values received and parsed from any bluetooth characteristics"
        Demo.HeartRateDemo -> "Display the Heart Rate Bluetooth standard profile"
        Demo.SensorFusion -> "6-axis or 9-axis Sensor Fusion demo"
        Demo.PredictedMaintenance -> "Display sensor data values acquired and processed with dedicated predictive maintenance algorithm"
        Demo.FftAmplitude -> "Display in a graphical way the FFT Amplitude values received from the board"
        Demo.MultiNeuralNetwork -> "Display advanced applications such as human activity recognition or acoustic scene classification on the basis of output generated by multi neural networks"
        Demo.HighSpeedDataLog1 -> "High speed sensors data log configuration, control and tagging"
        Demo.SdLoggingDemo -> "Configure and control a simple sensors data log"
        Demo.CoSensorDemo -> "Display electrochemical toxic gas level though CO Sensor"
        Demo.AiLoggingDemo -> "Configure, control and tag a simple sensors data log"
        Demo.SpeechToTextDemo -> "Speech to Text conversion from \"Bluevoice\" audio bluetooth streaming"
        Demo.BeamFormingDemo -> "Combine signals from multiple omnidirectional microphones to synthesize a virtual microphone that captures sound from a specific direction"
        Demo.BeamFormingDemoADPCM -> "Combine signals from multiple omnidirectional microphones to synthesize a virtual microphone that captures sound from a specific direction"
        Demo.BlueVoiceFullBand -> "\"BlueVoice\" audio bluetooth streaming Music"
        Demo.SpeechToTextDemoAPDCM -> "Speech to Text conversion from \"Bluevoice\" audio bluetooth streaming"
    }