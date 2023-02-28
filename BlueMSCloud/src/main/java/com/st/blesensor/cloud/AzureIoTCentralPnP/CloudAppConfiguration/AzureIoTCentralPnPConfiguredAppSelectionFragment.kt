package com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.st.blesensor.cloud.R
import com.st.BlueSTSDK.gui.demos.DemoFragment.SHARED_PREFS

import androidx.recyclerview.widget.ItemTouchHelper
import com.google.gson.reflect.TypeToken
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfigured
import java.lang.reflect.Type


class AzureIoTCentralPnPConfiguredAppSelectionFragment: Fragment(), AzureIoTCentralPnPCloudAdapter.OnItemClickListener {

    private lateinit var mFloatingButton: FloatingActionButton
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mTextView: TextView
    private lateinit var mCancelButton: Button

    private var cloudAppConfigDone = ArrayList<CloudAppConfigured>()

    private lateinit var mCloudAppConfigViewModel: AzureIoTCentralPnPCloudAppConfigViewModel

    private val adapterCloudApplication = AzureIoTCentralPnPCloudAdapter(this)

    private var gson = Gson()

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
                        cloudAppConfigDone.remove(cloudAppConfigDone[position])
                        adapterCloudApplication.updateCloudAppList(cloudAppConfigDone)
                        if(cloudAppConfigDone.isEmpty()) {
                            mTextView.text = requireContext().getString(R.string.cloud_config_azure_iot_central_pnp_no_app_textview)
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("No"){ dialog: DialogInterface, _: Int ->
                        // User cancelled the dialog,
                        // so we will refresh the adapter to prevent hiding the item from UI
                        adapterCloudApplication.updateCloudAppList(cloudAppConfigDone)
                        dialog.dismiss() }
                    .create()
                    .show()
            }
        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Log.d("IoTPnP","AzureIoTCentralPnPConfiguredAppSelectionFragment: onCreateView")
        val rootView = inflater.inflate(R.layout.cloud_config_azure_iot_central_pnp, container, false)

        mCloudAppConfigViewModel = ViewModelProvider(requireActivity()).get(AzureIoTCentralPnPCloudAppConfigViewModel::class.java)

        mFloatingButton= rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_action_button)
        mRecyclerView = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_recyclerview)
        mTextView = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_textview)
        mRecyclerView.adapter = adapterCloudApplication
        mSwapToDelete.attachToRecyclerView(mRecyclerView)
        mRecyclerView.layoutManager = LinearLayoutManager(context)

        loadConfiguredCloudAppsForNode()

        //For Configuring a New Application
        mFloatingButton.setOnClickListener{
            mCloudAppConfigViewModel.goToCloudAppSelection()
        }
        mCancelButton = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_negative)
        mCancelButton.setOnClickListener {
            mCloudAppConfigViewModel.goToCloudInitialPage()
            mCloudAppConfigViewModel.setSelectedDevice(null)
        }

        return rootView
    }

    override fun onDestroy() {
        //Log.d("IoTPnP","AzureIoTCentralPnPConfiguredAppSelectionFragment: onDestroy")
        val pref = requireContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = pref.edit()
        if(cloudAppConfigDone.isNotEmpty()) {
            val serialized = gson.toJson(cloudAppConfigDone)
            editor.putString(CLOUD_APP_SAVED, serialized)
        } else {
            editor.putString(CLOUD_APP_SAVED, null)
        }
        editor.apply()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //check if we have a new application to add
        val newConfigApplication = mCloudAppConfigViewModel.getSelectedCloudApp()
        if(newConfigApplication!=null) {
            if (newConfigApplication.configurationDone) {
                cloudAppConfigDone.add(newConfigApplication)
                adapterCloudApplication.updateCloudAppList(cloudAppConfigDone)
            }
        }

        if(cloudAppConfigDone.isEmpty()) {
            mTextView.text = requireContext().getString(R.string.cloud_config_azure_iot_central_pnp_no_app_textview)
        } else {
            mTextView.text = requireContext().getString(R.string.cloud_config_azure_iot_central_pnp_yes_app_textview)
        }
    }

    override fun onItemClick(position: Int) {
        val selectedCloudApp = cloudAppConfigDone[position]
        mCloudAppConfigViewModel.setSelectedCloudApp(selectedCloudApp)
        mCloudAppConfigViewModel.goToDeviceSelection()
    }

    fun loadConfiguredCloudAppsForNode() {
        //Set the Current Node
        Log.d("IoTPnP","AzureIoTCentralPnPConfiguredAppSelectionFragment: loadConfiguredCloudAppsForNode")

        val pref = requireContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val serialized = pref.getString(CLOUD_APP_SAVED,null)
        if(serialized!=null) {
            val type: Type =
                object : TypeToken<List<CloudAppConfigured?>?>() {}.type
            cloudAppConfigDone = gson.fromJson(serialized, type)
            if (cloudAppConfigDone.isNotEmpty()) {
                adapterCloudApplication.updateCloudAppList(cloudAppConfigDone)
                mTextView.text = requireContext().getString(R.string.cloud_config_azure_iot_central_pnp_yes_app_textview)
            } else {
                mTextView.text = requireContext().getString(R.string.cloud_config_azure_iot_central_pnp_no_app_textview)
            }
        } else {
            mTextView.text = requireContext().getString(R.string.cloud_config_azure_iot_central_pnp_no_app_textview)
        }

    }

    companion object {
        val CLOUD_APP_SAVED = AzureIoTCentralPnPConfiguredAppSelectionFragment::class.java.name + ".CLOUD_APP_SAVED"
    }
}