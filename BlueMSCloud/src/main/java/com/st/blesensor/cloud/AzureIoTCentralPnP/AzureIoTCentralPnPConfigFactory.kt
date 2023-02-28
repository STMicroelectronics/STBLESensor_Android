package com.st.blesensor.cloud.AzureIoTCentralPnP

import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.st.BlueSTSDK.Node
import com.st.blesensor.cloud.AzureIoTCentralPnP.AzureIoTCentralPnPRegDeviceFragment.Companion.CLOUD_DEV_SAVED_FRAGMENT_TAG
import com.st.blesensor.cloud.CloudIotClientConfigurationFactory
import com.st.blesensor.cloud.CloudIotClientConnectionFactory

class AzureIoTCentralPnPConfigFactory : CloudIotClientConfigurationFactory {
    private val CLOUD_NAME = "Azure IoT Central PnP"

    override fun attachParameterConfiguration(
        fm: FragmentManager,
        root: ViewGroup,
        id_mcu: String?,
       fw_version: String?
    ) {
        //check if a fragment is already attached, and remove it to attach the new one
        val mConfigFragment =
            fm.findFragmentByTag(CLOUD_DEV_SAVED_FRAGMENT_TAG) as AzureIoTCentralPnPRegDeviceFragment?

        Log.d("IoTPnP", "AzureIoTCentralPnPConfigFactory: attachParameterConfiguration")

        if (mConfigFragment == null) {
            val transaction = fm.beginTransaction()
            val newFragment = AzureIoTCentralPnPRegDeviceFragment(id_mcu,fw_version)
            transaction.add(root.id, newFragment, CLOUD_DEV_SAVED_FRAGMENT_TAG)
            transaction.commitNow()
        }
    }

    override fun detachParameterConfiguration(fm: FragmentManager, root: ViewGroup) {
        val configFragment = fm.findFragmentByTag(CLOUD_DEV_SAVED_FRAGMENT_TAG)
        Log.d("IoTPnP", "AzureIoTCentralPnPConfigFactory: detachParameterConfiguration")
        if (configFragment != null) {
            fm.beginTransaction().remove(configFragment).commit()
        }
    }

    override fun loadDefaultParameters(fm: FragmentManager, n: Node?) {
        val mConfigFragment =
            fm.findFragmentByTag(CLOUD_DEV_SAVED_FRAGMENT_TAG) as AzureIoTCentralPnPRegDeviceFragment?
        Log.d("IoTPnP", "AzureIoTCentralPnPConfigFactory: loadDefaultParameters")

        if (mConfigFragment != null && n != null) {
            mConfigFragment.loadConfiguredDevicesForNode(n)
        }
    }

    override fun getName(): String {
        return CLOUD_NAME
    }

    override fun getConnectionFactory(fm: FragmentManager): CloudIotClientConnectionFactory {
        Log.d("IoTPnP", "AzureIoTCentralPnPConfigFactory: getConnectionFactory")
        val mConfigFragment = fm.findFragmentByTag(CLOUD_DEV_SAVED_FRAGMENT_TAG) as AzureIoTCentralPnPRegDeviceFragment?
        val selectedDevice = mConfigFragment?.getSelectedDevice()
        return AzureIoTCentralPnPConnectionFactory(selectedDevice)
    }

}