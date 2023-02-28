package com.st.blesensor.cloud.stazure.registration

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.st.blesensor.cloud.stazure.ConfigurationViewModel
import com.st.blesensor.cloud.stazure.R

internal class RegisterDeviceFragment: DialogFragment(){

    private lateinit var viewModel:ConfigurationViewModel

    private lateinit var mGroupNameLayout: TextInputLayout
    private lateinit var mGroupPasswordLayout: TextInputLayout
    private lateinit var mGroupNameText: TextView
    private lateinit var mGroupPassword: TextView
    private lateinit var mProgressView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //the factory is not needed since it it instantiate into the STAzureDashboardConfigFragment
        viewModel = ViewModelProvider(requireActivity())
                .get(ConfigurationViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle(R.string.stAzure_registerDeviceTitle)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_register_device, container, false)
        root.findViewById<View>(R.id.stAzure_registerDeviceButton).setOnClickListener {
            registerDevice()
        }

        mGroupNameLayout = root.findViewById(R.id.stAzure_groupNameLayout)
        mGroupPasswordLayout = root.findViewById(R.id.stAzure_gropuPasswordLayout)

        mGroupNameText = root.findViewById(R.id.stAzure_groupNameText)
        mGroupPassword = root.findViewById(R.id.stAzure_passwordText)

        mProgressView = root.findViewById<ProgressBar>(R.id.stAzure_registrationProgress)

        viewModel.registrationStatus.observe(viewLifecycleOwner, Observer { status ->
            manageReistrationStatus(status)
        })

        root.findViewById<View>(R.id.stAzure_createGroupButton).setOnClickListener {
            requireActivity()
                    .startActivity(Intent(Intent.ACTION_VIEW, CREATE_GROUP_URI))
        }

        return root
    }


    private fun manageReistrationStatus(status:ConfigurationViewModel.RegistrationStatus? ){
        if(status == null){
            return
        }
        when(status){
            ConfigurationViewModel.RegistrationStatus.UNKNOWN -> {}
            ConfigurationViewModel.RegistrationStatus.DEVICE_INVALID_PARAM -> dismiss()
            ConfigurationViewModel.RegistrationStatus.LOGGING -> {
                mProgressView.visibility = View.VISIBLE
            }
            ConfigurationViewModel.RegistrationStatus.LOGIN_INVALID_NAME -> {
                mGroupNameLayout.error = getString(R.string.stAzure_invalidGroupName)
                mGroupPasswordLayout.error = null
                mProgressView.visibility = View.GONE
            }
            ConfigurationViewModel.RegistrationStatus.LOGIN_INVALID_PASSWORD -> {
                mGroupNameLayout.error = null
                mGroupPasswordLayout.error = getString(R.string.stAzure_invalidGropuPassword)
                mProgressView.visibility = View.GONE
            }
            ConfigurationViewModel.RegistrationStatus.LOGIN_FORBIDDEN -> {
                mGroupNameLayout.error = getString(R.string.stAzure_accessForbbidden)
                mGroupPasswordLayout.error = getString(R.string.stAzure_accessForbbidden)
                mProgressView.visibility = View.GONE
            }
            ConfigurationViewModel.RegistrationStatus.ADDING_DEVICE -> {
                mGroupNameLayout.error = null
                mGroupPasswordLayout.error = null
            }
            ConfigurationViewModel.RegistrationStatus.SUCCESS ->{
                dismiss()
            }
        }
    }

    private fun registerDevice(){
        viewModel.registerDeviceForUser(mGroupNameText.text ,mGroupPassword.text)
    }

    companion object{
        private val CREATE_GROUP_URI = Uri.parse("https://stm32ode.azurewebsites.net//#/signup")
    }

}