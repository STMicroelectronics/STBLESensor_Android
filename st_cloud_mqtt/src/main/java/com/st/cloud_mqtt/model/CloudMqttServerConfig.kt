package com.st.cloud_mqtt.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudMqttServerConfig(
    @SerialName("hostUrl")
    var hostUrl: String="",
    @SerialName("hostPort")
    var hostPort: Int,
    @SerialName("userName")
    var userName: String,
    @SerialName("userPassWd")
    var userPassWd: String,
    @SerialName("deviceId")
    var deviceId: String,
    @SerialName("ssl")
    var isSSL: Boolean=false
)
