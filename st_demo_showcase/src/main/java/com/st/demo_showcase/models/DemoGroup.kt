/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.models

enum class DemoGroup(
    val displayName: String
) {
    Control("Control"),
    BinaryContent("Binary Content"),
    EnvironmentalSensors("Environmental Sensors"),
    Configuration("Configuration"),
    AI("AI"),
    InertialSensors("Inertial Sensors"),
    Debug("Debug"),
    Health("Health"),
    Log("Log"),
    DataLog("Data Log"),
    Audio("Audio"),
    Cloud("Cloud"),
    PredictiveMaintenance("Predictive Maintenance"),
    Graphs("Graphs"),
    Status("Status")
}
