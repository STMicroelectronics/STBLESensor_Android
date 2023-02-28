package com.st.BlueMS.demos.PnPL

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Features.PnPL.FeaturePnPL
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation

@DemoDescriptionAnnotation(
    name = "Fw Control", iconRes = R.drawable.ic_ext_config_icon,
    requireAll = [FeaturePnPL::class],
    demoCategory = ["Control"],
    requireDTDLLoaded=true,
    requireChangeName=true)
class PnPLConfigurationFragment : BaseDemoFragment() {

    private val PICKFILE_REQUEST_CODE = 7777
    private lateinit var viewModel: PnPLConfigViewModel

    private val adapterPnPLComponents = PnPLComponentAdapter(
        object : PnPLComponentAdapter.ComponentInteractionCallback {
            override fun onComponentCollapsed(selected: PnPLComponentViewData) {
                viewModel.collapseComponent(selected)
            }

            override fun onComponentExpanded(selected: PnPLComponentViewData) {
                viewModel.expandComponent(selected)
            }
        },
        contChangedListener = { component, content, value ->
            Log.i("PnPLConfFragment", "component: $component, content: $content,value: $value")
            viewModel.sendPnPLSetProperty(component.comp_name, content.cont_name, value)
        },
        subContChangedListener = { component, content, subContent, value ->
            Log.i("PnPLConfFragment", "component: $component, content: $content,value: $value")
            viewModel.sendPnPLSetProperty(
                component.comp_name,
                content.cont_name,
                subContent.cont_name,
                value
            )
        },
        commandSentListener = { component, content, command_list ->
            Log.i(
                "PnPLConfFragment",
                "component: $component, content: $content,value: $command_list"
            )
            val fieldMap = emptyMap<String, Any>().toMutableMap()
            if (content.sub_cont_list != null) {
                var validCommand=true
                for (c in content.sub_cont_list!!) {
                    if(c.cont_name!=null) {
                        if (c.cont_enum_pos != null) {
                            fieldMap[c.cont_name] = c.cont_enum_pos!!
                        } else {
                            if (c.cont_info != null) {
                                fieldMap[c.cont_name] = c.cont_info!!
                            } else {
                                validCommand = false
                                    Toast.makeText(
                                        context,
                                        "Missing value",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                        }
                    } else {
                        validCommand=false
                        Toast.makeText(context, "Missing value", Toast.LENGTH_SHORT).show()
                    }
                }
                if(validCommand) {
                    if (content.request_name != null) {
                        viewModel.sendPnPLCommandCmd(
                            component.comp_name,
                            content.cont_name,
                            content.request_name!!,
                            fieldMap
                        )
                    } else {
                        viewModel.sendPnPLCommandCmd(
                            component.comp_name,
                            content.cont_name,
                            fieldMap
                        )
                    }
                }
            } else {
                viewModel.sendPnPLCommandCmd(component.comp_name, content.cont_name)
            }
        },
        loadfileListener = { component, content ->
            Log.i("PnPLConfFragment", "component: $component, content: $content")
            viewModel.setComponentToLoadFile(
                component.comp_name,
                content.cont_name,
                content.request_name!!
            )
            requestOpenFile()
        })

    private lateinit var recyclerViewPnPLComponents: RecyclerView

//    var myDownloadId: Long = -1
//    private val broadCastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(contxt: Context?, intent: Intent?) {
//            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
//            if (id == myDownloadId) {
//                val manager =
//                    context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                val uri = manager.getUriForDownloadedFile(myDownloadId)
//
//                if(uri!=null) {
//                    val inputStream = context!!.contentResolver.openInputStream(uri)
//
//                    if (inputStream != null) {
//                        val strData = inputStream.readBytes().toString(Charsets.UTF_8)
//                        viewModel.parseDeviceModel(strData)
//                    }
//                }
//            }
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        val rootView = inflater.inflate(R.layout.fragment_generic_pnpl, container, false)

        //Get the ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(PnPLConfigViewModel::class.java)
        viewModel.context = context;

        //Set the Recycler View
        recyclerViewPnPLComponents = rootView.findViewById(R.id.PnPLComponentsRecycler)
        recyclerViewPnPLComponents.adapter = adapterPnPLComponents

        return rootView
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewModel.compList.observe(viewLifecycleOwner, Observer {
//            adapterPnPLComponents.updatePnPLCompList(it)
//        })
//
////        if (node?.fwDetails?.dtmi != null) {
////            val uri_dtmi =
////                "https://raw.githubusercontent.com/SW-Platforms/appconfig/release/" +
////                        node!!.fwDetails.dtmi!!.replace(':','/').
////                        replace(';', '-') + ".expanded.json"
////            val request = DownloadManager.Request(Uri.parse(uri_dtmi))
////                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
////                .setTitle(uri_dtmi.substringAfterLast("/"))
////                .setAllowedOverMetered(true)
////            val dm = context?.let {
////                ContextCompat.getSystemService(
////                    it,
////                    DownloadManager::class.java
////                )
////            }
////            if (dm != null) {
////                myDownloadId = dm.enqueue(request)
////            }
////        }
////
////        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
////        requireActivity().registerReceiver(broadCastReceiver, filter)
//
//        if(node?.dtdlModel!=null) {
//            viewModel.parseDeviceModel(node!!.dtdlModel)
//        }
//
//    }

    override fun onResume() {
        super.onResume()

        viewModel.compList.observe(viewLifecycleOwner, Observer {
            adapterPnPLComponents.updatePnPLCompList(it)
        })

        if(node?.dtdlModel!=null) {
            viewModel.parseDeviceModel(node!!.dtdlModel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        requireActivity().unregisterReceiver(broadCastReceiver)
    }

    private fun requestOpenFile() {
        val chooserFile = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/*", "text/*"))
            type = "*/*"
        }
        val chooserTitle = getString(com.st.clab.stwin.gui.R.string.stwin_ucfFileChooserTitle)
        startActivityForResult(
            Intent.createChooser(chooserFile, chooserTitle),
            PICKFILE_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICKFILE_REQUEST_CODE -> {
                var type = ""
                val fileUri = data?.data?.also { uri ->
                    type = context?.contentResolver?.getType(uri).toString()
                }
                if (resultCode == Activity.RESULT_OK) {
                    if (type != "application/octet-stream") { //NOTE filter other known MIME types (it is not exhaustive)
                        displayErrorMessage("Invalid File")
                    } else {
                        viewModel.sendFileToSelectedComponent(
                            fileUri,
                            requireContext().contentResolver
                        )
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun displayErrorMessage(error: String) {
        Snackbar.make(requireView(), error, Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun enableNeededNotification(node: Node) {
        viewModel.enableNotificationFromNode(node)
        viewModel.sendPnPLGetDeviceStatus()
    }

    override fun disableNeedNotification(node: Node) {
        viewModel.disableNotificationFromNode(node)
    }
}