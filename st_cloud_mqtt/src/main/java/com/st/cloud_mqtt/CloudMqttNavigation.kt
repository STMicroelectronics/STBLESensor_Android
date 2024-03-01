package com.st.cloud_mqtt

interface CloudMqttNavigation {
    val route: String
}

object CloudMqttNavigationApplicationConfiguration: CloudMqttNavigation {
    override val route: String
        get() = "cloudApplicationConfiguration"
}

object CloudMqttNavigationDeviceConnection: CloudMqttNavigation {
    override val route: String
        get() = "cloudDeviceConnection"
}