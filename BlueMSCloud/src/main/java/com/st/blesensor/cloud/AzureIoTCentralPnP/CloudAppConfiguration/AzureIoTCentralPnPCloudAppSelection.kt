package com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.fwDataBase.ReadBoardFirmwareDataBase
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfigured
import com.st.blesensor.cloud.R

class AzureIoTCentralPnPCloudAppSelection : Fragment(), AzureIoTCentralPnPCloudAdapter.OnItemClickListener {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mButtonNegative: Button

    private val adapterCloudApplication = AzureIoTCentralPnPCloudAdapter(this)
    private var mSelectedCloudApp: CloudAppConfigured?=null

    private lateinit var mCloudAppConfigViewModel: AzureIoTCentralPnPCloudAppConfigViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //Log.d("IoTPnP","AzureIoTCentralPnPCloudAppSelection: onCreateView")

        val rootView = inflater.inflate(R.layout.cloud_config_azure_iot_central_pnp_cloud_app_selection, container, false)

        mCloudAppConfigViewModel = ViewModelProvider(requireActivity()).get(AzureIoTCentralPnPCloudAppConfigViewModel::class.java)

        mRecyclerView = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_select_app_recyclerview)
        mRecyclerView.adapter = adapterCloudApplication
        mRecyclerView.layoutManager = LinearLayoutManager(context)

        mButtonNegative = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_select_app_negative)
        mButtonNegative.setOnClickListener {
            //We need to send back the Configured Cloud app Fragment
            mCloudAppConfigViewModel.goToConfiguredCloudApplication()
        }

        //Load the available cloud Application from Fw db
        loadCloudAppsForNode(mCloudAppConfigViewModel.getNode())


        return rootView
    }


    private fun loadCloudAppsForNode(n: Node) {
        if(n.protocolVersion==2.toShort()) {
            //For SDK V2 we retrieve the Cloud App looking Board id and Fw id
            if (n.fwDetails != null) {
                mCloudAppConfigViewModel.setListCloudApps(n.fwDetails.cloud_apps.map { CloudAppConfigured(it) })
            }
        } else {
            //For SDK V1 we have a Default Cloud App for some boards present on catalog (remove trailing \r or \n)
            val runningFwVersion = mCloudAppConfigViewModel.getRunningFwVersion()?.replace("\n", "")?.replace("\r", "")
            val fwDatabase = ReadBoardFirmwareDataBase(requireContext())
            if(runningFwVersion!=null) {
                val fwDetails =
                    fwDatabase.getFwDetailsNodeSDKV1(n.typeId.toInt(), runningFwVersion)
                if(fwDetails!=null) {
                    mCloudAppConfigViewModel.setListCloudApps(fwDetails.cloud_apps.map { CloudAppConfigured(it) })
                }
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Log.d("IoTPnP","AzureIoTCentralPnPCloudAppSelection: onViewCreated "+mCloudAppConfigViewModel.getListCloudApps().size)
        adapterCloudApplication.updateCloudAppList(mCloudAppConfigViewModel.getListCloudApps())
    }

    override fun onItemClick(position: Int) {
        //Log.d("IoTPnP","AzureIoTCentralPnPCloudAppSelection: onItemClick position=$position")
        mSelectedCloudApp = mCloudAppConfigViewModel.getListCloudApps()[position]
        if(mSelectedCloudApp!=null) {
                mCloudAppConfigViewModel.setSelectedCloudApp(mSelectedCloudApp!!)
                mCloudAppConfigViewModel.goToCloudAppConfiguration()
        }
    }
}