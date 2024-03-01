package com.st.cloud_mqtt.network


import android.util.Log
import com.st.cloud_mqtt.CloudMqttViewModel
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence


class CloudMqttV3Connection {
    companion object {
        private var instance: CloudMqttV3Connection? = null

        private val TAG = CloudMqttV3Connection::class.java.name

        fun provideCloudMqttConnection(): CloudMqttV3Connection {
            if (instance == null) {
                instance = CloudMqttV3Connection()
            }
            return instance as CloudMqttV3Connection
        }
    }

    private var mqttClient: MqttClient? = null

    fun closeDeviceConnection() : String? {
        mqttClient?.let {
            try {
                mqttClient!!.disconnect()
                mqttClient= null
                return "Device Connection Closed"
            } catch (e: MqttException) {
                e.printStackTrace()
                return "Close Device Connection failure: $e"
            }
        }
        return null
    }

    fun publish(topic:      String,
                msg:        String,
                qos:        Int                 = 1,
                retained:   Boolean             = false): String? {
        mqttClient?.let {
            try {
                val message = MqttMessage()
                message.payload = msg.toByteArray()
                message.qos = qos
                message.isRetained = retained
                mqttClient!!.publish(topic, message)
                return null
            } catch (e: MqttException) {
                e.printStackTrace()
                Log.d(TAG, "Publish failure: $e")
                return "Publish failure: $e"
            }
        }
        return null
    }

    fun createConnection(viewModel: CloudMqttViewModel): String? {
        if (viewModel.cloudMqttServerConfig.value != null) {
            val brokerUrl = if(viewModel.cloudMqttServerConfig.value!!.isSSL) {
                "ssl://"+viewModel.cloudMqttServerConfig.value!!.hostUrl+":"+viewModel.cloudMqttServerConfig.value!!.hostPort.toString()
            } else {
                "tcp://"+viewModel.cloudMqttServerConfig.value!!.hostUrl+":"+viewModel.cloudMqttServerConfig.value!!.hostPort.toString()
            }

            Log.i(TAG,"brokerUrl =[$brokerUrl]")

            val persistence  = MemoryPersistence()
            mqttClient = MqttClient(brokerUrl,  viewModel.cloudMqttServerConfig.value!!.deviceId,persistence)
            if (mqttClient != null) {
                val connOpts = MqttConnectOptions()
                connOpts.isCleanSession = true

                if (viewModel.cloudMqttServerConfig.value!!.userName.isNotBlank())
                    connOpts.userName = viewModel.cloudMqttServerConfig.value!!.userName

                if (viewModel.cloudMqttServerConfig.value!!.userPassWd.isNotBlank())
                    connOpts.password =
                        viewModel.cloudMqttServerConfig.value!!.userPassWd.toCharArray()

                try {
                    mqttClient!!.connect(connOpts)

                    viewModel.markDeviceAsConnected()

                    Log.d(TAG, "Device Connection Done")
                    return "Device Connection Done"

                } catch (e: MqttException) {
                    e.printStackTrace()
                    Log.d(TAG, "Connection failure: $e")
                    viewModel.setIsLoading(false)
                    return "Connection failure: $e"
                }
            }
        }
        return null
    }
}