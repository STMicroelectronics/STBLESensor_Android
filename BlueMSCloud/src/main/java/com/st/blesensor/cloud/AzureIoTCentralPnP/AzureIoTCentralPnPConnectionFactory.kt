package com.st.blesensor.cloud.AzureIoTCentralPnP

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.TextView
import com.github.lucadruda.iotc.device.exceptions.IoTCentralException
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.blesensor.cloud.AzureIoTCentralPnP.AzurePnPHelperFunction.AzurePnpInterface
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.AzureCloudDevice
import com.st.blesensor.cloud.CloudIotClientConnectionFactory
import com.st.blesensor.cloud.CloudIotClientConnectionFactory.CloutIotClient

class AzureIoTCentralPnPConnectionFactory(var selectedDevice: AzureCloudDevice?) :
    CloudIotClientConnectionFactory {

    private val mClient = AzureIoTCentralPnPConnection()

    private var mContext: Context?=null

    override fun createClient(ctx: Context): CloutIotClient {
        Log.d("IoTPnP", "AzureIoTCentralPnPConnectionFactory: createClient")

        mClient.currentDevice = selectedDevice

        if (selectedDevice != null) {
            mClient.mAzureCom = AzurePnpInterface(selectedDevice)
        }

        return mClient
    }

    private var mFeatureListener: AzureIoTCentralPnPFeatureListener?=null

    private var mFeatureNameTextView: TextView?=null
    private var mFeatureDetailTextView: TextView?=null

    override fun connect(
        ctx: Context,
        client: CloutIotClient,
        connectionListener: CloudIotClientConnectionFactory.ConnectionListener
    ): Boolean {
        Log.d("IoTPnP", "AzureIoTCentralPnPConnectionFactory: connect")
        mClient.mBackgroundThread.submit {
            try {
                mClient.mAzureCom?.initializeAndProvisionDevice()
                mClient.isConnected = true
                mContext = ctx
                connectionListener.onSuccess()
            } catch (e: IoTCentralException) {
                e.printStackTrace()
                connectionListener.onFailure(e)
            }
        }
        return true
    }

    override fun showSendingData() = true

    override fun setTextViewsForDataSample(name: TextView, detail: TextView) {
        mFeatureNameTextView = name
        mFeatureDetailTextView = detail
    }

    override fun getFeatureListener(
        broker: CloutIotClient,
        minUpdateIntervalMs: Long
    ): Feature.FeatureListener? {
        Log.d("IoTPnP", "AzureIoTCentralPnPConnectionFactory: getFeatureListener")
        mFeatureListener = AzureIoTCentralPnPFeatureListener(mClient, minUpdateIntervalMs,mContext)
        //Retrieve the Device supported Feature
        if((mFeatureListener!=null) && (mClient.currentDevice!=null)) {
            mFeatureListener!!.setCapabilityModel(mClient.currentDevice!!)
            mFeatureListener!!.setTextViewsForDataSample(mFeatureNameTextView,mFeatureDetailTextView)
        }
        return mFeatureListener
    }

    override fun disconnect(client: CloutIotClient) {
        Log.d("IoTPnP", "AzureIoTCentralPnPConnectionFactory: disconnect")
        mClient.mBackgroundThread.submit {
            try {
                mClient.mAzureCom?.closeConnection()
            } catch (e: IoTCentralException) {
                e.printStackTrace()
            }
        }
        mClient.isConnected=false
    }

    override fun destroy(client: CloutIotClient) {
        Log.d("IoTPnP", "AzureIoTCentralPnPConnectionFactory: destroy ")
        mClient.mBackgroundThread.shutdown();
    }

    override fun isConnected(client: CloutIotClient): Boolean {
        Log.d("IoTPnP", "AzureIoTCentralPnPConnectionFactory: isConnected")
        return mClient.isConnected
    }

    override fun getDataPage(): Uri? {
        Log.d("IoTPnP", "AzureIoTCentralPnPConnectionFactory: getDataPage Not Present")
        return null
    }


    override fun supportFeature(f: Feature): Boolean {
        return mFeatureListener?.isSupportFeature(f) ?: false
    }

    override fun enableCloudFwUpgrade(
        node: Node,
        cloudConnection: CloutIotClient,
        callback: CloudIotClientConnectionFactory.FwUpgradeAvailableCallback
    ): Boolean {
        Log.d("IoTPnP", "AzureIoTCentralPnPConnectionFactory: enableCloudFwUpgrade")
        return false
    }
}