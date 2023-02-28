package com.st.blesensor.cloud.stazure

import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.st.BlueSTSDK.Node
import com.st.blesensor.cloud.CloudIotClientConfigurationFactory
import com.st.blesensor.cloud.CloudIotClientConnectionFactory
import java.lang.IllegalArgumentException

class STAzureDashboardConfigFactory  : CloudIotClientConfigurationFactory {


    override fun attachParameterConfiguration(fm:FragmentManager, root: ViewGroup, uid : String?, fw_version: String?) {
        //check if a fragment is already attach, and remove it to attach the new one
        val configFragment = fm.findFragmentByTag(CONFIG_FRAGMENT_TAG) as? STAzureDashboardConfigFragment

        if (configFragment == null) {
            val newFragment = STAzureDashboardConfigFragment()
            val transaction = fm.beginTransaction()
            transaction.add(root.id, newFragment, CONFIG_FRAGMENT_TAG)
            transaction.commitNow()
        }
    }

    override fun detachParameterConfiguration(fm: FragmentManager, root: ViewGroup) {
        val configFragment = fm.findFragmentByTag(CONFIG_FRAGMENT_TAG)
        if(configFragment!=null)
            fm.beginTransaction().remove(configFragment).commit()
    }

    override fun loadDefaultParameters(fm: FragmentManager,n: Node?) {
        if(n == null)
            return
        val configFragment = fm.findFragmentByTag(CONFIG_FRAGMENT_TAG) as? STAzureDashboardConfigFragment
        configFragment?.setDataForNode(n)

    }

    @Throws(IllegalAccessException::class)
    override fun getConnectionFactory(fm: FragmentManager): CloudIotClientConnectionFactory {
        val configFragment = fm.findFragmentByTag(CONFIG_FRAGMENT_TAG) as? STAzureDashboardConfigFragment
        val cs = configFragment?.getDeviceConnectionString()
                ?: throw IllegalArgumentException("Invalid connection string")
        return STAzureDashboardConnectionFactory(cs)
    }

    override fun getName(): String = FACTORY_NAME

    companion object {
        private const val FACTORY_NAME = "Azure IoT - ST Web Dashboard"
        private val CONFIG_FRAGMENT_TAG = STAzureDashboardConfigFragment::class.java.canonicalName!!
    }

}