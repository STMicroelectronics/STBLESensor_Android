package com.st.blesensor.cloud.stazure

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.st.BlueSTSDK.Node
import com.st.blesensor.cloud.stazure.registration.RegisterDeviceFragment
import com.st.blesensor.cloud.stazure.storage.RegisteredDeviceDatabase

internal class STAzureDashboardConfigFragment : Fragment(R.layout.fragment_stazure_config){

    private var node:Node? =null
    private lateinit var deviceId: TextView
    private lateinit var deviceStatus:TextView
    private lateinit var friendlyName: TextInputEditText
    private lateinit var friendlyNameLayout: TextInputLayout
    private lateinit var registerButton:MaterialButton
    private lateinit var viewModel: ConfigurationViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerDeviceDao = RegisteredDeviceDatabase.getDatabase(requireContext()).registeredDevices()
        val configFactory = ConfigurationViewModel.Factory(registerDeviceDao)
        viewModel = ViewModelProvider(requireActivity(),configFactory)
                .get(ConfigurationViewModel::class.java)
        deviceStatus = view.findViewById(R.id.stAzure_deviceStatus)
        deviceId = view.findViewById(R.id.stAzure_deviceIdLabel)
        friendlyName = view.findViewById(R.id.stAzure_friendyNameText)
        registerButton = view.findViewById(R.id.stAzure_registerButton)
        friendlyNameLayout = view.findViewById(R.id.stAzure_deviceNameLayout)


        registerButton.setOnClickListener {
            moveToRegisterDevice()
        }


        viewModel.deviceId.observe(viewLifecycleOwner, Observer {
            deviceId.text = it
        })
        viewModel.friendlyName.observe(viewLifecycleOwner, Observer {
            friendlyName.setText(it)
            friendlyNameLayout.error = null
        })

        viewModel.showEmptyNameError.observe(viewLifecycleOwner, Observer {
            friendlyNameLayout.error = getString(R.string.stAzure_invalidEmptyName)
        })

        viewModel.connectionString.observe(viewLifecycleOwner, Observer {cs ->
            if(cs == null){
                registerButton.visibility = View.VISIBLE
                deviceStatus.setText(R.string.stAzure_unkownDevice)
            }else{
                registerButton.visibility = View.GONE
                deviceStatus.setText(R.string.stAzure_kownDevice)
            }
        })

        viewModel.registrationStatus.observe(viewLifecycleOwner, Observer { status ->
            if(status == null) {
                return@Observer
            }
            if(status == ConfigurationViewModel.RegistrationStatus.DEVICE_INVALID_PARAM){
                friendlyNameLayout.error = getString(R.string.stAzure_registrationFailInvalidParam)
            }else{
                friendlyNameLayout.error = null
            }
        })

    }

    override fun onStart() {
        super.onStart()
        node?.let {
            viewModel.loadDataForNode(it)
        }
    }

    private fun moveToRegisterDevice() {
        viewModel.setFriendlyName(friendlyName.text.toString())
        val registerDeviceDialog = RegisterDeviceFragment()
        registerDeviceDialog.show(childFragmentManager,REGISTER_DIALOG_TAG)
    }


    fun setDataForNode(node: Node){
        //could be called before the frament is created, we store the node and call the loadData
        //when we create the viewmodel
        this.node = node
        if(::viewModel.isInitialized) {
            viewModel.loadDataForNode(node)
        }
    }

    fun getDeviceConnectionString():String?{
        return  viewModel.connectionString.value
    }

    companion object{
        val  REGISTER_DIALOG_TAG = STAzureDashboardConfigFragment::class.java.name+".REGISTER_DIALOG_TAG"
    }

}