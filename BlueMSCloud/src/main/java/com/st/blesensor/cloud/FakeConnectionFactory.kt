package com.st.blesensor.cloud

import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.st.BlueSTSDK.Node

class FakeConnectionFactory : CloudIotClientConfigurationFactory {
    override fun attachParameterConfiguration(
        fm: FragmentManager,
        root: ViewGroup,
        id_mcu: String?,
        fw_version: String?
    ) {

    }

    override fun detachParameterConfiguration(fm: FragmentManager, root: ViewGroup) {
    }

    override fun loadDefaultParameters(fm: FragmentManager, n: Node?) {
    }

    override fun getName(): String {
        return "-------------"
    }

    override fun getConnectionFactory(fm: FragmentManager): CloudIotClientConnectionFactory {
        return FakeClientConnection()
    }
}