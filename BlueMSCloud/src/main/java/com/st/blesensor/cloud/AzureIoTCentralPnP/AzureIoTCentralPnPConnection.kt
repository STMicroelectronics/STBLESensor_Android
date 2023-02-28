package com.st.blesensor.cloud.AzureIoTCentralPnP

import com.st.blesensor.cloud.AzureIoTCentralPnP.AzurePnPHelperFunction.AzurePnpInterface
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.AzureCloudDevice
import com.st.blesensor.cloud.CloudIotClientConnectionFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AzureIoTCentralPnPConnection : CloudIotClientConnectionFactory.CloutIotClient {
    var isConnected = false
    var mAzureCom: AzurePnpInterface? = null
    var currentDevice: AzureCloudDevice? = null
    val mBackgroundThread: ExecutorService = Executors.newFixedThreadPool(1)
}