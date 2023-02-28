package com.st.blesensor.cloud

import android.content.Context
import android.net.Uri
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node

class FakeClientConnection: CloudIotClientConnectionFactory {
    class FakeClient : CloudIotClientConnectionFactory.CloutIotClient {

    }
    private var client = FakeClient()

    override fun createClient(ctx: Context): CloudIotClientConnectionFactory.CloutIotClient? {
        return client
    }

    override fun connect(
        ctx: Context,
        client: CloudIotClientConnectionFactory.CloutIotClient,
        connectionListener: CloudIotClientConnectionFactory.ConnectionListener
    ): Boolean {
        return false
    }

    override fun getFeatureListener(
        broker: CloudIotClientConnectionFactory.CloutIotClient,
        minUpdateIntervalMs: Long
    ): Feature.FeatureListener? {
      return null
    }

    override fun disconnect(client: CloudIotClientConnectionFactory.CloutIotClient) {
    }

    override fun destroy(client: CloudIotClientConnectionFactory.CloutIotClient) {
    }

    override fun isConnected(client: CloudIotClientConnectionFactory.CloutIotClient): Boolean {
        return false
    }

    override fun getDataPage(): Uri? {
        return null
    }


    override fun supportFeature(f: Feature): Boolean {
       return false
    }

    override fun enableCloudFwUpgrade(
        node: Node,
        cloudConnection: CloudIotClientConnectionFactory.CloutIotClient,
        callback: CloudIotClientConnectionFactory.FwUpgradeAvailableCallback
    ): Boolean {
        return false
    }
}