package com.st.blesensor.cloud.AzureIoTCentralPnP

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoFragment
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration.AzureIoTPnPCloudConfigActivity
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.AzureCloudDevice
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration.AzureIoTPnPDeviceAdapter
import com.st.blesensor.cloud.R
import java.lang.reflect.Type

class AzureIoTCentralPnPRegDeviceFragment(val mcu_id: String?, val fw_version: String?): Fragment(),
    AzureIoTPnPDeviceAdapter.OnItemClickListener {

    private lateinit var mNode: Node
    private var cloudDevices = ArrayList<AzureCloudDevice>()
    private var gson = Gson()

    private lateinit var mFloatingButton: FloatingActionButton
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mTextView: TextView

    private var selectedCloudDevice:AzureCloudDevice?=null

    private var adapterCloudDevice = AzureIoTPnPDeviceAdapter(this)

    private val mSwapToDelete = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return true // true if moved, false otherwise
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                AlertDialog.Builder(viewHolder.itemView.context).setMessage("Are you sure to delete it?")
                    .setPositiveButton("Yes"){ dialog: DialogInterface, _: Int ->
                        val position = viewHolder.adapterPosition
                        cloudDevices.remove(cloudDevices[position])
                        adapterCloudDevice.updateAzureDeviceList(cloudDevices, node = mNode,true)
                        if(cloudDevices.isEmpty()) {
                            mTextView.text = requireContext().getString(R.string.cloud_config_azure_iot_central_pnp_conf_device_no_dev_textview)
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("No"){ dialog: DialogInterface, _: Int ->
                        // User cancelled the dialog,
                        // so we will refresh the adapter to prevent hiding the item from UI
                        adapterCloudDevice.updateAzureDeviceList(cloudDevices, node = mNode,true)
                        dialog.dismiss() }
                    .create()
                    .show()
            }
        })

    private val intentLauncherConfigDevice =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            Log.d("IoTPnP","AzureIoTCentralPnPRegDeviceFragment: Received intentLauncherConfigDevice Results")
            if (result.resultCode == Activity.RESULT_OK) {
                val serialized = result.data?.getStringExtra("configDevice")
                if(serialized!=null) {
                    val deviceConfigured = gson.fromJson(serialized, AzureCloudDevice::class.java)

                    if(deviceConfigured!=null) {
                        // we save the mac address of the current node
                        deviceConfigured.macAdd = mNode.tag
                        Log.d("IoTPnP","AzureIoTCentralPnPRegDeviceFragment: Received Results configDevice ${GsonBuilder().setPrettyPrinting().create().toJson(deviceConfigured)}")
//                        //Search if we need to update one device
//                        val equalDevice = cloudDevices.find { CheckEqualDevice(it,deviceConfigured) }
//                        if(equalDevice!=null) {
//                            cloudDevices.remove(equalDevice)
//                        }
                        cloudDevices.add(deviceConfigured)

                    }
                }
            }
        }

//    private fun CheckEqualDevice(old:AzureCloudDevice, new: AzureCloudDevice): Boolean {
//        var retValue = true
//        if(old.macAdd!=new.macAdd) {
//            //Same BLE Mac Address
//            retValue = false
//        } else {
//            //Same Cloud Device
//            if(old.credentials?.symmetricKey?.primaryKey != new.credentials?.symmetricKey?.primaryKey) {
//                retValue = false
//            }
//        }
//        return retValue
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Log.d("IoTPnP","AzureIoTCentralPnPRegDeviceFragment: onCreateView")
        val rootView = inflater.inflate(R.layout.cloud_azure_iot_central_pnp_conf_device, container, false)
        mFloatingButton= rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_conf_device_action_button)
        mRecyclerView = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_conf_device_recyclerview)
        mTextView = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_conf_device_textview)
        mRecyclerView.adapter = adapterCloudDevice
        mSwapToDelete.attachToRecyclerView(mRecyclerView)
        mRecyclerView.layoutManager = LinearLayoutManager(context)


        mFloatingButton.setOnClickListener{
            val intent = Intent(requireContext(), AzureIoTPnPCloudConfigActivity::class.java)
            intent.putExtra(AzureIoTPnPCloudConfigActivity.AZ_CLOUD_CONF_NODE_TAG_ARG, mNode.tag)
            intent.putExtra(AzureIoTPnPCloudConfigActivity.AZ_CLOUD_CONF_MCU_ID_TAG_ARG,mcu_id)
            intent.putExtra(AzureIoTPnPCloudConfigActivity.AZ_CLOUD_CONF_MCU_FW_VERSION_TAG_ARG,fw_version)
            intentLauncherConfigDevice.launch(intent)
        }

        return rootView
    }

    override fun onDestroy() {
        //Log.d("IoTPnP","AzureIoTCentralPnPRegDeviceFragment: onDestroy")
        val pref = requireContext().getSharedPreferences(DemoFragment.SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = pref.edit()

        if(cloudDevices.isNotEmpty()) {
            val serialized = gson.toJson(cloudDevices)
            editor.putString(CLOUD_DEV_SAVED,serialized)
        } else {
            editor.putString(CLOUD_DEV_SAVED,null)
        }
        editor.apply()
        super.onDestroy()
    }

    fun loadConfiguredDevicesForNode(n: Node) {
        //Set the Current Node
        Log.d("IoTPnP","AzureIoTCentralPnPRegDeviceFragment: loadConfiguredDevicesForNode")
        mNode = n

        val pref = requireContext().getSharedPreferences(DemoFragment.SHARED_PREFS, Context.MODE_PRIVATE)
        var serialized = pref.getString(CLOUD_DEV_SAVED,null)
        if(serialized!=null) {
            val type: Type =
                object : TypeToken<List<AzureCloudDevice?>?>() {}.type
            cloudDevices = gson.fromJson(serialized, type)

            if (cloudDevices.isNotEmpty()) {
                adapterCloudDevice.updateAzureDeviceList(cloudDevices,node =mNode,true)
                Log.d("IoTPnP","AzureIoTCentralPnPRegDeviceFragment: loadConfiguredDevicesForNode loaded ${cloudDevices.size} devices")
                mTextView.text = requireContext().getString(R.string.cloud_config_azure_iot_central_pnp_conf_device_yes_dev_textview)
            } else {
                mTextView.text = requireContext().getString(R.string.cloud_config_azure_iot_central_pnp_conf_device_no_dev_textview)
            }
        } else {
            mTextView.text = requireContext().getString(R.string.cloud_config_azure_iot_central_pnp_conf_device_no_dev_textview)
        }
    }

    companion object {
        val CLOUD_DEV_SAVED_FRAGMENT_TAG = AzureIoTCentralPnPRegDeviceFragment::class.java.name + ".CLOUD_DEV_SAVED_FRAGMENT_TAG"
        val CLOUD_DEV_SAVED = AzureIoTCentralPnPRegDeviceFragment::class.java.name + ".CLOUD_DEV_SAVED"
    }

    override fun onItemClick(position: Int) {
        selectedCloudDevice = cloudDevices[position]
        mTextView.text = requireContext().getString(R.string.cloud_config_azure_iot_central_pnp_conf_device_selected_dev_textview)
    }

    fun getSelectedDevice() = selectedCloudDevice

//    private fun snackBarWithConfirmation(message: String) {
//        view?.let {
//            val snack = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
//            snack.setAction("OK") { snack.dismiss() }
//            snack.show()
//        }
//    }
}