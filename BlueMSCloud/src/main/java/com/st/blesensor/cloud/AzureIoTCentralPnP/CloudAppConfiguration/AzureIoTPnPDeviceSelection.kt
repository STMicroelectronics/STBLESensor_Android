package com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfigured
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel.CloudTemplatesList
import com.st.blesensor.cloud.AzureIoTCentralPnP.Network.AzureIoTDeviceService
import com.st.blesensor.cloud.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.lang.Exception
import java.net.URLEncoder
import com.google.android.material.snackbar.Snackbar
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.AzureCloudDevice
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.CloudDeviceCredentials
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.CloudDevicesList


class AzureIoTPnPDeviceSelection : Fragment(), AzureIoTPnPDeviceAdapter.OnItemClickListener {

    private lateinit var mViewModelGlobal: AzureIoTCentralPnPCloudAppConfigViewModel
    private lateinit var mViewModelFragment: AzureIoTPnPDeviceSelectionViewModel
    private lateinit var mTextView: TextView
    private lateinit var mFloatingButton: FloatingActionButton
    private lateinit var mButtonNegative: Button

    private var mSelCloudApp: CloudAppConfigured? = null

    private lateinit var mAzureIoTService: AzureIoTDeviceService

    private lateinit var mRecyclerView: RecyclerView
    private val adapterCloudDevice = AzureIoTPnPDeviceAdapter(this)

    private lateinit var mProgressLayoutBar: LinearLayout

    var mCloudDevices: CloudDevicesList? = null
    var mCloudTemplates: CloudTemplatesList? = null

    var mCloudDevice: AzureCloudDevice? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Log.d("IoTPnP", "AzureIoTPnpDeviceSelection: onCreateView")

        val rootView = inflater.inflate(
            R.layout.cloud_config_azure_iot_central_pnp_dev_selection,
            container,
            false
        )

        mTextView =
            rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_select_device_textview)

        mFloatingButton =
            rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_device_button)

        mFloatingButton.setOnClickListener {
            showDialogForAddingNewDevice()
        }

        mViewModelGlobal =
            ViewModelProvider(requireActivity()).get(AzureIoTCentralPnPCloudAppConfigViewModel::class.java)

        mViewModelFragment = ViewModelProvider(requireActivity()).get(AzureIoTPnPDeviceSelectionViewModel::class.java)

        mButtonNegative =
            rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_select_device_negative)
        mButtonNegative.setOnClickListener {
            //Remove the current Selected Device...
            mCloudDevice = null
            mViewModelGlobal.setSelectedDevice(mCloudDevice)
            if (mSelCloudApp != null) {
                //For Avoiding to add a Replication of the Configured Cloud App
                mSelCloudApp!!.configurationDone = false
                mViewModelGlobal.setSelectedCloudApp(mSelCloudApp!!)
            }
            //Go To App Selection page
            mViewModelGlobal.goToCloudAppSelection()
        }

        mRecyclerView =
            rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_select_device_recyclerview)
        mRecyclerView.adapter = adapterCloudDevice
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mSwapToDelete.attachToRecyclerView(mRecyclerView)

        mProgressLayoutBar =
            rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_select_device_progress)

        return rootView
    }


    private val mSwapToDelete = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {
                return true // true if moved, false otherwise
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                AlertDialog.Builder(viewHolder.itemView.context)
                    .setMessage("Are you sure to delete it?")
                    .setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
                        val position = viewHolder.adapterPosition
                        if (mCloudDevices?.list?.get(position) != null) {
                            var error: String;
                            mProgressLayoutBar.visibility = View.VISIBLE
                            mViewModelFragment.receivedAvailableDevicesList()
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val result =
                                        mAzureIoTService.deleteDevice(
                                            mCloudDevices?.list?.get(
                                                position
                                            )!!.id
                                        )
                                    error = when (result.code()) {
                                        204 -> {
                                            "Success"
                                        }
                                        else -> {
                                            "Error deleting Device: Result code=${result.code()}"
                                        }
                                    }
                                } catch (e: HttpException) {
                                    error = "Error deleting Device\n${e.message()}"
                                }
                                mViewModelFragment.setHTTPDeviceOperationResponse(error)

                                val list = readDevicesFromCloudApp()

                                mViewModelFragment.setAvailableDevicesList(list)
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog: DialogInterface, _: Int ->
                        // User cancelled the dialog,
                        // so we will refresh the adapter to prevent hiding the item from UI
                        if(mCloudDevices!=null) {
                            adapterCloudDevice.updateAzureDeviceList(mCloudDevices!!.list, mcuId = mViewModelGlobal.getMcuId())
                        }
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        })

    private fun showDialogForAddingNewDevice() {
        //Open the Dialog for adding the new Device
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        val v = inflater.inflate(R.layout.cloud_config_azure_iot_central_pnp_add_new_device, null)

        val deviceModels = mCloudTemplates?.list?.map { it.displayName }?.toList()

        val spinner =
            v.findViewById<Spinner>(R.id.cloud_config_azure_iot_central_pnp_add_new_device_spinner)

        val dataAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item, deviceModels!!
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val deviceIDTextView =
            v.findViewById<TextView>(R.id.cloud_config_azure_iot_central_pnp_add_new_device_ID)
        val deviceNameTextView =
            v.findViewById<TextView>(R.id.cloud_config_azure_iot_central_pnp_add_new_device_name)

        val mcu_id = mViewModelGlobal.getMcuId()
        //Log.d("IoTPnP","read mcuid=$mcu_id")
        if (mcu_id != null) {
            deviceIDTextView.text = mcu_id
        }

        deviceIDTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Check if it's empty
                val textInput: String = deviceIDTextView.text.toString()
                if (textInput.isEmpty()) {
                    deviceIDTextView.error = "Field can't be empty";
                } else {
                    deviceIDTextView.error = null
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val textInput: String = deviceIDTextView.text.toString()
                if (textInput.isEmpty()) {
                    deviceIDTextView.error = "Field can't be empty";
                } else {
                    if(textInput.matches("^[0-9a-zA-Z]*$".toRegex())) {
                        deviceIDTextView.error = null
                    } else {
                        deviceIDTextView.error = "Only Numbers and Letters"
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        var adapterPosition = 0
        spinner.adapter = dataAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                adapterPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapterPosition = 0
            }
        }


        builder.setView(v)

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        builder.setPositiveButton("Add") { dialog: DialogInterface, _: Int ->

            mProgressLayoutBar.visibility = View.VISIBLE

            val template = mCloudTemplates?.list?.get(adapterPosition)

            if ((template != null)  &&  (deviceIDTextView.error==null)) {

                val newDevice = AzureCloudDevice(
                    id = deviceIDTextView.text.toString(),
                    template = mCloudTemplates?.list?.get(adapterPosition)!!.id,
                    displayName = deviceNameTextView.text.toString()
                )

                val bodyReq: RequestBody = Gson().toJson(newDevice).toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                mViewModelFragment.receivedAvailableDevicesList()
                CoroutineScope(Dispatchers.IO).launch {
                    var error: String
                    try {
                        val result = mAzureIoTService.addDevice(
                            URLEncoder.encode(newDevice.id, "utf-8"),
                            bodyReq
                        )
                        when (result.code()) {
                            200 -> {
                                error="Success"
                            }
                            else -> {
                                error = "Error Adding Device: Result code=${result.code()}"
                            }
                        }

                        mViewModelFragment.setHTTPDeviceOperationResponse(error)

                        val list = readDevicesFromCloudApp()

                        mViewModelFragment.setAvailableDevicesList(list)
                    } catch (e: HttpException) {
                        error = "Error Adding Device\n${e.message()}"
                        mViewModelFragment.setHTTPDeviceOperationResponse(error)
                    }
                }
            }
            dialog.dismiss()

        }
        builder.create()
        builder.show()
        if (mSelCloudApp != null) {
            if (mSelCloudApp!!.mcu_id != null) {
                deviceIDTextView.text = mSelCloudApp!!.mcu_id
            }
        }
    }

    private suspend fun readDevicesFromCloudApp(): CloudDevicesList? {
        var devices: CloudDevicesList? = null
        try {
            devices = mAzureIoTService.getDevices()
            Log.d("IoTPnP", "readDevicesFromCloudApp ${devices.list.size}")
        } catch (e: Exception) {
            val error ="readDevicesFromCloudApp Error: " + e.localizedMessage
            Log.e(this::javaClass.name, error)
            mViewModelFragment.setHTTPDeviceOperationResponse(error)
            e.printStackTrace()
        }
        return devices
    }

    private suspend fun readTemplatesFromCloudApp(): CloudTemplatesList? {
        var templates: CloudTemplatesList? = null
        try {
            templates = mAzureIoTService.getTemplates()
            Log.d("IoTPnP", "readTemplatesFromCloudApp ${templates.list.size}")
        } catch (e: Exception) {
            val error = "readTemplatesFromCloudApp Error: " + e.localizedMessage
            Log.e(this::javaClass.name, error)
            mViewModelFragment.setHTTPDeviceOperationResponse(error)
            e.printStackTrace()
        }
        return templates
    }

    private fun readDeviceCredentialsFromCloudApp() {
        if (mCloudDevice != null) {
            var credentials: CloudDeviceCredentials?
            CoroutineScope(Dispatchers.IO).launch {
                var error: String
                try {
                    val result = mAzureIoTService.getDeviceCredentials(
                        URLEncoder.encode(mCloudDevice!!.id, "utf-8")
                    )
                    when (result.code()) {
                        200 -> {
                            error="Success"
                            val body = result.body()
                            if (body != null) {
                                val data = body.string()
                                credentials = Gson().fromJson(
                                    data,
                                    CloudDeviceCredentials::class.java
                                )
//                                if(credentials!=null) {
//                                    Log.d("IoTPnP", "readDeviceCredentialsFromCloudApp= ${GsonBuilder().setPrettyPrinting().create().toJson(credentials)}")
//                                }
                                mViewModelFragment.setDeviceCredentials(credentials)
                            }
                        }
                        else -> {
                            error="Error Reading Device Credentials: Result code=${result.code()}"
                        }
                    }
                } catch (e: HttpException) {
                    error = "Error Reading Device Credentials\n${e.message()}"
                }
                mViewModelFragment.setHTTPDeviceOperationResponse(error)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Log.d("IoTPnP", "AzureIoTPnpDeviceSelection: onViewCreated")

        mProgressLayoutBar.visibility = View.VISIBLE

        mSelCloudApp = mViewModelGlobal.getSelectedCloudApp()
        if (mSelCloudApp != null) {
            if (mSelCloudApp!!.authorizationKey != null) {
                mAzureIoTService = AzureIoTDeviceService.buildInstance(
                    mSelCloudApp!!.cloudApp.url!!,
                    mSelCloudApp!!.authorizationKey!!
                )
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val list = readDevicesFromCloudApp()
            mViewModelFragment.setAvailableDevicesList(list)

            val templates  = readTemplatesFromCloudApp()
            mViewModelFragment.setTemplates(templates)
        }

        mViewModelFragment.mHttpDeviceOperationResponse.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                mProgressLayoutBar.visibility = View.GONE
                if (result != "Success") {
                    //Error
                    snackBarWithConfirmation(result)
                    mViewModelFragment.receivedHTTPDeviceOperationResponse()
                }
            }
        }

        mViewModelFragment.mAvailableDevicesList.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                mCloudDevices = list
                //Just for Debug...
                //Log.d("IoTPnP", GsonBuilder().setPrettyPrinting().create().toJson(mCloudDevices))
                if (mCloudDevices!!.list.isNotEmpty()) {
                    adapterCloudDevice.updateAzureDeviceList(mCloudDevices!!.list, mcuId = mViewModelGlobal.getMcuId())
                    mTextView.text =
                        getString(R.string.cloud_config_azure_iot_central_pnp_config_devices_available)

                    //Enable Disable Floating Button for creating a new device
                    mFloatingButton.isEnabled =
                        mCloudDevices!!.list.find { it.id == mViewModelGlobal.getMcuId() } == null
                } else {
                    mTextView.text =
                        getString(R.string.cloud_config_azure_iot_central_pnp_config_devices_not_available)
                    mFloatingButton.isEnabled = true
                }
            }
        }

        mViewModelFragment.mDeviceCredentials.observe(viewLifecycleOwner) { cred ->
            if (mCloudDevice != null) {
                mCloudDevice!!.credentials = cred

                if (mSelCloudApp != null) {
                    mCloudDevice!!.connectedCloudAppUrl = mSelCloudApp!!.cloudApp.url
                }

                //We must add the Device Template model...
                val template =
                    mCloudTemplates?.list?.firstOrNull { it.id == mCloudDevice!!.template }
                mCloudDevice!!.templateModel = template

                //Save back the configured Cloud Device to ViewModel
                mViewModelGlobal.setSelectedDevice(mCloudDevice)
                mProgressLayoutBar.visibility = View.GONE
                mViewModelGlobal.goToCloudInitialPage()
            }
        }

        mViewModelFragment.mDeviceTemplates.observe(viewLifecycleOwner) { templates ->
            mProgressLayoutBar.visibility = View.GONE
            if (templates != null) {
                //mProgressLayoutBar.visibility = View.GONE
                mCloudTemplates = templates
                //Just for Debug...
                //Log.d("IoTPnP", GsonBuilder().setPrettyPrinting().create().toJson(mCloudTemplates))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModelFragment.mHttpDeviceOperationResponse.removeObservers(viewLifecycleOwner)
        mViewModelFragment.mAvailableDevicesList.removeObservers(viewLifecycleOwner)
        mViewModelFragment.mDeviceCredentials.removeObservers(viewLifecycleOwner)
        mViewModelFragment.mDeviceTemplates.removeObservers(viewLifecycleOwner)
    }

    private fun snackBarWithConfirmation(message: String) {
        view?.let {
            val snack = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
            snack.setAction("OK") { snack.dismiss() }
            snack.show()
        }
    }

    override fun onItemClick(position: Int) {
        if (mCloudDevices != null) {
            mProgressLayoutBar.visibility = View.VISIBLE
            mCloudDevice = mCloudDevices!!.list[position]

            //Retrieve the DeviceCredentials
            readDeviceCredentialsFromCloudApp()

        }
    }
}