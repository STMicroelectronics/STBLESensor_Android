package com.st.BlueMS.demos.PnPL

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.st.BlueMS.R
import com.st.BlueSTSDK.Node

class PnPLSettingsDialogFragment(val node: Node?,val cont_names: List<String>?=null, val collapsed:Boolean=true) : androidx.fragment.app.DialogFragment() {
    private lateinit var buttonOk: Button
    //private lateinit var mProgressLayoutBar: LinearLayout

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
                for (c in content.sub_cont_list!!) {
                    if (c.cont_enum_pos != null) {
                        fieldMap[c.cont_name] = c.cont_enum_pos!!
                    } else {
                        fieldMap[c.cont_name] = c.cont_info!!
                    }
                }
                if (content.request_name != null) {
                    viewModel.sendPnPLCommandCmd(
                        component.comp_name,
                        content.cont_name,
                        content.request_name!!,
                        fieldMap
                    )
                } else {
                    viewModel.sendPnPLCommandCmd(component.comp_name, content.cont_name, fieldMap)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogWithOutTitleFullScreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.pnpl_settings_dialog_fragment, container, false)

        //Find ProgressBar
//        mProgressLayoutBar = root.findViewById(R.id.pnpl_settings_dialog_fragment_progress)
//        mProgressLayoutBar.visibility = View.VISIBLE

        //Find Button
        buttonOk = root.findViewById(R.id.pnpl_settings_dialog_fragment_positive)
        buttonOk.setOnClickListener { closeDialog() }

        //Get the ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(PnPLConfigViewModel::class.java)
        viewModel.context = context

        //Set the Recycler View
        recyclerViewPnPLComponents = root.findViewById(R.id.pnpl_settings_dialog_fragment_recycler)
        recyclerViewPnPLComponents.adapter = adapterPnPLComponents

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.compList.observe(viewLifecycleOwner, Observer {
//            if(it!=null) {
//                mProgressLayoutBar.visibility = View.GONE
                adapterPnPLComponents.updatePnPLCompList(it)
//            }
        })

        if(node?.dtdlModel!=null) {
            viewModel.parseDeviceModel(node.dtdlModel,cont_names, collapsed)
        }

        if(node?.dtdlModel!=null) {
            viewModel.enableNotificationFromNode(node)
            viewModel.sendPnPLGetDeviceStatus()
        }
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

    private fun closeDialog() {
        if(node!=null) {
            viewModel.disableNotificationFromNode(node)
        }
        this.dismiss()
    }
}