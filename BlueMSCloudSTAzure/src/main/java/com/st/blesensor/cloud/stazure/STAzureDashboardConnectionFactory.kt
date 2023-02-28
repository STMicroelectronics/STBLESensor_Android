package com.st.blesensor.cloud.stazure

import android.content.Context
import android.net.Uri
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionConsole
import com.st.blesensor.cloud.CloudIotClientConnectionFactory
import com.st.blesensor.cloud.CloudIotClientConnectionFactory.CloutIotClient
import com.st.blesensor.cloud.stazure.iotHub.STAzureFeatureListener
import com.st.blesensor.cloud.stazure.iotHub.STAzureIotHubConnectionClient

internal class STAzureDashboardConnectionFactory( private val connectionString: String) : CloudIotClientConnectionFactory {
    override fun createClient(ctx: Context): CloudIotClientConnectionFactory.CloutIotClient? {
        return STAzureIotHubConnectionClient(connectionString)
    }

    override fun connect(ctx: Context, client: CloudIotClientConnectionFactory.CloutIotClient, connectionListener: CloudIotClientConnectionFactory.ConnectionListener): Boolean {
        val azureClient = client as? STAzureIotHubConnectionClient
                ?: return false
        azureClient.connect(connectionListener)
        return true
    }

    override fun getFeatureListener(broker: CloudIotClientConnectionFactory.CloutIotClient, minUpdateIntervalMs: Long): Feature.FeatureListener? {
       val azureClient = broker as? STAzureIotHubConnectionClient
               ?: return null
       return  azureClient.createFeatureListener(minUpdateIntervalMs)
    }

    override fun disconnect(client: CloudIotClientConnectionFactory.CloutIotClient) {
        val azureClient = client as? STAzureIotHubConnectionClient
                ?: return
        azureClient.disconnect()
    }

    override fun destroy(client: CloudIotClientConnectionFactory.CloutIotClient) {
    }

    override fun isConnected(client: CloudIotClientConnectionFactory.CloutIotClient): Boolean {
        val azureClient = client as? STAzureIotHubConnectionClient ?: return false
        return azureClient.isConnected
    }

    override fun enableCloudFwUpgrade(node: Node, cloudConnection: CloudIotClientConnectionFactory.CloutIotClient, callback: CloudIotClientConnectionFactory.FwUpgradeAvailableCallback): Boolean {
        val client = cloudConnection as? STAzureIotHubConnectionClient
                ?: return false

        val fwConsole = FwVersionConsole.getFwVersionConsole(node) ?: // no upgrade fw supported
        return false
        client.enableCloudFwUpgrade(callback)
        fwConsole.setLicenseConsoleListener { console, _, version ->
            //remove the listener from the console
            console.setLicenseConsoleListener(null)
            val boardVersion = version as? FwVersionBoard ?: return@setLicenseConsoleListener
            client.reportFwInfo(boardVersion)
        }
        //read the current fw version
        fwConsole.readVersion(FirmwareType.BOARD_FW)
        client.enableCloudFwUpgrade(callback)
        return true
    }


    override fun getDataPage(): Uri? = DASHBOARD_PAGE


    override fun supportFeature(f: Feature): Boolean  = STAzureFeatureListener.isSupportedFeature(f)


    companion object{
        private val DASHBOARD_PAGE = Uri.parse("https://stm32ode.azurewebsites.net")
    }
}
