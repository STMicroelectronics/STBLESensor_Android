package com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration.AzureIoTCentralPnPCloudAppConfigViewModel.Companion.APP_CLOUD_CONF_FRAGMENT_TAG
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration.AzureIoTCentralPnPCloudAppConfigViewModel.Companion.APP_CLOUD_SEL_FRAGMENT_TAG
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration.AzureIoTCentralPnPCloudAppConfigViewModel.Companion.APP_CONF_CLOUD_FRAGMENT_TAG
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration.AzureIoTCentralPnPCloudAppConfigViewModel.Companion.DEVICE_SEL_FRAGMENT_TAG
import com.st.blesensor.cloud.R

class AzureIoTPnPCloudConfigActivity : AppCompatActivity() {

    private lateinit var mCloudAppConfigViewModel: AzureIoTCentralPnPCloudAppConfigViewModel
    private lateinit var mNode: Node
    private var mMcuId: String?=null
    private var mRunningFwVersion: String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Log.d("IoTPnP","AzureIoTPnPCloudConfigActivity: onCreate")

        setContentView(R.layout.cloud_azure_iot_central_pnp_root_view)

        mCloudAppConfigViewModel = ViewModelProvider(this).get(AzureIoTCentralPnPCloudAppConfigViewModel::class.java)

        mNode = Manager.getSharedInstance().getNodeWithTag(intent.getStringExtra(AZ_CLOUD_CONF_NODE_TAG_ARG)!!)!!

        mMcuId = intent.getStringExtra(AZ_CLOUD_CONF_MCU_ID_TAG_ARG)
        mRunningFwVersion = intent.getStringExtra(AZ_CLOUD_CONF_MCU_FW_VERSION_TAG_ARG)

        mCloudAppConfigViewModel.setNode(mNode)
        mCloudAppConfigViewModel.setMcuId(mMcuId)
        mCloudAppConfigViewModel.setRunningFwVersion(mRunningFwVersion)

        mCloudAppConfigViewModel.currentView.observe(this, Observer { destination ->
            changeView(destination)
        })
    }

    /**
     * Move between Fragment
     */
    private fun changeView(destinationCloudAppConfigView: AzureIoTCentralPnPCloudAppConfigViewModel.IoTCentralPnPDestination) {
        Log.d("IoTPnP","AzureIoTPnPCloudConfigActivity: changeView")

        when (destinationCloudAppConfigView) {
            is AzureIoTCentralPnPCloudAppConfigViewModel.IoTCentralPnPDestination.ConfiguredCloudAppSelection -> {
              goToConfiguredCloudAppSelection()
            }
            is AzureIoTCentralPnPCloudAppConfigViewModel.IoTCentralPnPDestination.CloudAppSelection -> {
                goToCloudAppSelection()
            }
            is AzureIoTCentralPnPCloudAppConfigViewModel.IoTCentralPnPDestination.CloudAppConfiguration -> {
                goToCloudAppConfiguration()
            }
            is AzureIoTCentralPnPCloudAppConfigViewModel.IoTCentralPnPDestination.DeviceSelection -> {
                goToDeviceSelection()
            }
            is AzureIoTCentralPnPCloudAppConfigViewModel.IoTCentralPnPDestination.CloudFinalState -> {
                //We need to Send back the Configured Cloud Application
                val data = Intent()
                if(mCloudAppConfigViewModel.getWeHaveOneValidApp()) {
                    val selectedDevice = mCloudAppConfigViewModel.getSelectedDevice()
                    if (selectedDevice!=null) {
                        val serialized = Gson().toJson(selectedDevice)
                        data.putExtra("configDevice", serialized)
                    }
                }
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }


    private fun goToConfiguredCloudAppSelection() {
        val fm = supportFragmentManager
        var fragment = fm.findFragmentByTag(APP_CONF_CLOUD_FRAGMENT_TAG)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, AzureIoTCentralPnPConfiguredAppSelectionFragment::class.java.name)
            supportFragmentManager.beginTransaction()
                .replace(R.id.cloud_azure_iot_central_pnp_rootView, fragment, APP_CONF_CLOUD_FRAGMENT_TAG)
                .commit()
        }
    }

    private fun goToCloudAppSelection() {
        val fm = supportFragmentManager
        var fragment = fm.findFragmentByTag(APP_CLOUD_SEL_FRAGMENT_TAG)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, AzureIoTCentralPnPCloudAppSelection::class.java.name)
            supportFragmentManager.beginTransaction()
                .replace(R.id.cloud_azure_iot_central_pnp_rootView, fragment, APP_CLOUD_SEL_FRAGMENT_TAG)
                .commit()
        }
    }

    private fun goToCloudAppConfiguration() {
        val fm = supportFragmentManager
        var fragment = fm.findFragmentByTag(APP_CLOUD_CONF_FRAGMENT_TAG)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, AzureIoTCentralPnPCloudAppConfig::class.java.name)
            supportFragmentManager.beginTransaction()
                .replace(R.id.cloud_azure_iot_central_pnp_rootView, fragment, APP_CLOUD_CONF_FRAGMENT_TAG)
                .commit()
        }
    }

    private fun goToDeviceSelection() {
        val fm = supportFragmentManager
        var fragment = fm.findFragmentByTag(DEVICE_SEL_FRAGMENT_TAG)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, AzureIoTPnPDeviceSelection::class.java.name)
            supportFragmentManager.beginTransaction()
                .replace(R.id.cloud_azure_iot_central_pnp_rootView, fragment, DEVICE_SEL_FRAGMENT_TAG)
                .commit()
        }
    }

    companion object {
        val AZ_CLOUD_CONF_NODE_TAG_ARG = AzureIoTPnPCloudConfigActivity::class.java.name + ".AZ_CLOUD_CONF_NODE_TAG_ARG"
        val AZ_CLOUD_CONF_MCU_ID_TAG_ARG = AzureIoTPnPCloudConfigActivity::class.java.name + ".AZ_CLOUD_CONF_MCU_ID_TAG_ARG"
        val AZ_CLOUD_CONF_MCU_FW_VERSION_TAG_ARG =  AzureIoTPnPCloudConfigActivity::class.java.name + ".AZ_CLOUD_CONF_MCU_FW_VERSION_TAG_ARG"
    }
}