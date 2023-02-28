/*
 * Copyright (c) 2020  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 * STMicroelectronics company nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 * in a directory whose title begins with st_images may only be used for internal purposes and
 * shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 * icons, pictures, logos and other images that are provided with the source code in a directory
 * whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.BlueMS.demos.HighSpeedDatalog2.config

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.st.BlueMS.R
import com.st.BlueMS.demos.PnPL.PnPLComponentAdapter
import com.st.BlueMS.demos.PnPL.PnPLComponentViewData
import com.st.BlueSTSDK.Features.PnPL.PnPLComponent
import com.st.BlueSTSDK.Features.PnPL.PnPLParser
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node

/**
 *
 */
open class HSD2ConfigFragment : Fragment() {
    private lateinit var viewModel : HSD2ConfigViewModel
    private lateinit var loadMLCViewModel : HSD2LoadUCFViewModel

    private val mSensorsAdapter = PnPLComponentAdapter(
        object : PnPLComponentAdapter.ComponentInteractionCallback {
            override fun onComponentCollapsed(selected: PnPLComponentViewData) {
                viewModel.collapseSensor(selected)
            }

            override fun onComponentExpanded(selected: PnPLComponentViewData) {
                viewModel.expandSensor(selected)
            }
        },
        contChangedListener = { component, content, value ->
            Log.i("TEST", "component: $component, content: $content,value: $value")
            viewModel.sendPnPLSetProperty(component.comp_name, content.cont_name, value)
            if(component.comp_name == "ism330dhcx_acc" || component.comp_name == "ism330dhcx_gyro"){
                viewModel.sendPnPLSetProperty("ism330dhcx_mlc", "enable", false)
                viewModel.sendPnPLGetComponentStatus("ism330dhcx_mlc")
            }
        },
        subContChangedListener = { component, content, subContent, value ->
            Log.i("TEST", "component: $component, content: $content,value: $value")
            viewModel.sendPnPLSetProperty(component.comp_name, content.cont_name, subContent.cont_name, value)
        },
        commandSentListener = { component, content, command_list ->
            Log.i("TEST", "component: $component, content: $content,value: $command_list")
            var fieldMap = emptyMap<String,Any>().toMutableMap()
            for (c in content.sub_cont_list!!){
                if (c.cont_enum_pos != null) {
                    fieldMap[c.cont_name] = c.cont_enum_pos!!
                } else {
                    fieldMap[c.cont_name] = c.cont_info!!
                }
            }
            content.request_name?.let {
                viewModel.sendPnPLCommandCmd(component.comp_name, content.cont_name, it, fieldMap)
            }
        },
        loadfileListener = { component, content ->
            Log.i("TEST", "component: $component, content: $content")
            loadMLCViewModel.openLoadUCF(component.comp_name, content.cont_name)
            requestMLCConfigFile()
        })

    private fun requestConfigurationFile() {
        val chooserFile = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            //putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json"))
            type = PICKFILE_REQUEST_TYPE
        }
        val chooserTitle = getString(com.st.clab.stwin.gui.R.string.hsdl_configFileChooserTitle)
        startActivityForResult(Intent.createChooser(chooserFile, chooserTitle), PICKFILE_REQUEST_CODE)
    }

    private fun requestMLCConfigFile() {
        val chooserFile = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/*", "text/*"))
            type = PICKFILE_UCF_REQUEST_TYPE
        }
        val chooserTitle = getString(com.st.clab.stwin.gui.R.string.stwin_ucfFileChooserTitle)
        startActivityForResult( Intent.createChooser(chooserFile, chooserTitle), PICKFILE_UCF_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICKFILE_REQUEST_CODE -> {
                val fileUri = data?.data
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.loadConfigFromFile(fileUri,requireContext().contentResolver)
                }
            }
            PICKFILE_UCF_REQUEST_CODE -> {
                var type = ""
                val fileUri = data?.data?.also {
                    uri -> type = context?.contentResolver?.getType(uri).toString()
                }
                if (resultCode == Activity.RESULT_OK) {
                    if(type != "application/octet-stream") { //NOTE filter other known MIME types (it is not exhaustive)
                        displayErrorMessage("Invalid File")
                    } else {
                        loadMLCViewModel.loadUCFFromFile(fileUri, requireContext().contentResolver)
                        //viewModel.changeMLCLockedParams(true)
                        viewModel.updateMLCUCFLoadedStatus(true)
                    }
                }
                viewModel.updateMLCUCFLoadedStatus(false)
            }
            CREATE_FILE_REQUEST_CODE -> {
                val fileUri = data?.data
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.storeConfigToFile(fileUri,requireContext().contentResolver)
                }
            }
            SAVE_SETTINGS_REQUEST_CODE->{
                if (resultCode == Activity.RESULT_OK) {
                    val settings = HSD2SaveDialogFragment.extractSaveSettings(data) ?: return
                    viewModel.saveConfiguration(settings)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private lateinit var mConfView:View
    private lateinit var mLoadingView:View
    private lateinit var mLoadingText:TextView
    private lateinit var recyclerViewPnPLComponents: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_hsdatalog2_config, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(HSD2ConfigViewModel::class.java)
        viewModel.context = context
        loadMLCViewModel = ViewModelProvider(requireActivity()).get(HSD2LoadUCFViewModel::class.java)
        loadMLCViewModel.context = context

        mConfView = root.findViewById(R.id.pnpl_confViewLayout)
        mLoadingView = root.findViewById(R.id.pnpl_progressLayout)
        mLoadingText = root.findViewById(R.id.pnpl_progressText)

        //Set the Recycler View
        recyclerViewPnPLComponents = root.findViewById(R.id.pnpl_sensorList)
        recyclerViewPnPLComponents.adapter = mSensorsAdapter

        root.findViewById<View>(R.id.pnpl_loadButton).setOnClickListener {
            requestConfigurationFile()
        }

        root.findViewById<View>(R.id.pnpl_saveButton).setOnClickListener {
            showSaveDialog(viewModel.isSDCardMounted())
        }

        val viewSaveButton = root.findViewById<View>(R.id.pnpl_loadSaveButtonGroup)
        viewSaveButton.visibility = if(showSaveButton){
            View.VISIBLE
        }else{
            View.GONE
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if(error!=null)
                displayErrorMessage(error)
        })*/

        viewModel.savedConfiguration.observe(viewLifecycleOwner, Observer { conf ->
            conf ?: return@Observer
            sendConfigCompleteEvent(conf)
        })

        viewModel.requestFileLocation.observe(viewLifecycleOwner, Observer { askFile ->
            if(askFile){
                requestFileCreation()
            }
        })

        loadMLCViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            showMLCLoadingView(isLoading)
        })

        viewModel.isLogging.observe(viewLifecycleOwner, Observer { isLogging ->
            showIsLoggingView(isLogging)
        })

        viewModel.isConfigLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            showConfigLoadingView(isLoading)
        })

        viewModel.sensorCompList.observe(viewLifecycleOwner, Observer {
            mSensorsAdapter.updatePnPLCompList(it)
        })

        val node = getNode()

        if(node?.dtdlModel!=null) {
            viewModel.parseDeviceModel(node!!.dtdlModel)
        }
    }

    private fun showProgressView(showIt:Boolean, @StringRes desc:Int){
        if(showIt){
            mLoadingView.visibility = View.VISIBLE
            mConfView.visibility=View.GONE
            mLoadingText.setText(desc)
        }else{
            mLoadingView.visibility = View.GONE
            mConfView.visibility=View.VISIBLE
        }
    }

    fun showIsLoggingView(isLogging:Boolean){
        showProgressView(isLogging,R.string.stwin_logging)
    }

    private fun showMLCLoadingView(isLoading:Boolean){
        showProgressView(isLoading,R.string.stwin_loading_ucf)
    }

    private fun showConfigLoadingView(isLoading:Boolean){
        showProgressView(isLoading,R.string.stwin_loading_conf)
    }

    private fun updateMLCUCFChip(isMLCUCFLoaded:Boolean){}

    private fun displayErrorMessage(error: String) {
        Snackbar.make(requireView(),error,Snackbar.LENGTH_SHORT)
                .show()
    }

    private fun getNode():Node?{
        return arguments?.getString(NODE_TAG_EXTRA)?.let {
            Manager.getSharedInstance().getNodeWithTag(it)
        }
    }

    override fun onStart() {
        super.onStart()
        val node = getNode()
        if(node!=null){
            enableNeededNotification(node)
        }
    }

    fun enableNeededNotification(node: Node) {
        viewModel.enableNotificationFromNode(node)
        loadMLCViewModel.attachTo(node)
        //TODO HERE!
        //viewModel.sendPnPLGetDeviceStatus()
    }

    fun disableNeedNotification(node: Node) {
        viewModel.disableNotificationFromNode(node)
    }

    override fun onStop() {
        super.onStop()
        val node = getNode()
        if(node!=null && disableNotificationOnStop){
            disableNeedNotification(node)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.findViewById<RecyclerView>(R.id.stWinConf_sensorsList)
            ?.adapter=null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_stwin_hs_datalog,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_hsdl_saveConf -> {
                showSaveDialog(viewModel.isSDCardMounted())
                true
            }

            R.id.menu_hsdl_loadConf -> {
                requestConfigurationFile()
                true
            }
            R.id.menu_hsdl_changeAlias -> {
                val node = getNode()
                if(node!=null) {
                    val aliasSettings = HSD2BoardAliasDialogFragment.newInstance(node)
                    aliasSettings.show(childFragmentManager, ALIAS_CONFIG_FRAGMENT_TAG)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSaveDialog(isSDMounted: Boolean) {
        val dialog = HSD2SaveDialogFragment(isSDMounted)
        dialog.setTargetFragment(null, SAVE_SETTINGS_REQUEST_CODE)
        dialog.show(childFragmentManager,"saveDialog")
    }

    private fun sendConfigCompleteEvent(newConf:List<PnPLComponent>){
        val newConfStr = PnPLParser.getJsonFromComponents(newConf)
        LocalBroadcastManager.getInstance(requireContext())
            .sendBroadcast(buildConfigCompletedEvent(newConfStr))
    }

    private fun requestFileCreation(){
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = PICKFILE_REQUEST_TYPE
            putExtra(Intent.EXTRA_TITLE, DEFAULT_CONFI_NAME)
        }
        viewModel.requestFileLocation.value = false
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }

    private val showSaveButton:Boolean
        get() =  arguments?.getBoolean(SHOW_SAVE_BUTTON_EXTRA,false) ?: false

    private val disableNotificationOnStop:Boolean
        get() = arguments?.getBoolean(DISABLE_NOTIFICATION_ON_STOP,true) ?: true

    companion object {
        private val ALIAS_CONFIG_FRAGMENT_TAG = HSD2ConfigFragment::class.java.name + ".ALIAS_CONFIG_FRAGMENT"
        private const val PICKFILE_REQUEST_CODE = 7777
        private const val PICKFILE_REQUEST_TYPE = "*/*"//"application/json"
        private const val PICKFILE_UCF_REQUEST_CODE = 7778
        private const val PICKFILE_UCF_REQUEST_TYPE = "application/ucf"
        private const val DEFAULT_CONFI_NAME = "STWINBox_conf.json"

        private val NODE_TAG_EXTRA = HSD2ConfigFragment::class.java.name + ".NODE_TAG_EXTRA"
        private val SHOW_SAVE_BUTTON_EXTRA = HSD2ConfigFragment::class.java.name + ".SHOW_SAVE_BUTTON_EXTRA"
        private val DISABLE_NOTIFICATION_ON_STOP = HSD2ConfigFragment::class.java.name + ".DISABLE_NOTIFICATION_ON_STOP"

        fun newInstance(node: Node,showSaveButton:Boolean = false,disableNotificationOnStop:Boolean = true): Fragment {
            return HSD2ConfigFragment().apply {
                arguments = Bundle().apply {
                    putString(NODE_TAG_EXTRA,node.tag)
                    putBoolean(SHOW_SAVE_BUTTON_EXTRA,showSaveButton)
                    putBoolean(DISABLE_NOTIFICATION_ON_STOP,disableNotificationOnStop)
                }
            }
        }

        private const val CREATE_FILE_REQUEST_CODE = 1
        private const val SAVE_SETTINGS_REQUEST_CODE = 2

        private val ACTION_CONFIG_COMPLETE = HSD2ConfigFragment::class.java.name + ".ACTION_CONFIG_COMPLETE"
        private val ACTION_CONFIG_EXTRA = HSD2ConfigFragment::class.java.name + ".ACTION_CONFIG_EXTRA"
        fun buildConfigCompleteIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(ACTION_CONFIG_COMPLETE)
            }
        }

        fun extractSavedConfig(intent: Intent?):String?{
            return if(intent?.action == ACTION_CONFIG_COMPLETE){
                intent.getStringExtra(ACTION_CONFIG_EXTRA)
            }else{
                null
            }
        }

        internal fun buildConfigCompletedEvent(newConfig: String?):Intent{
            return Intent(ACTION_CONFIG_COMPLETE).apply {
                if(newConfig!=null)
                    putExtra(ACTION_CONFIG_EXTRA, newConfig)
            }
        }

    }

    /*private val IOConfError.toStringRes:Int
        get() = when(this){
            IOConfError.InvalidFile -> R.string.hsdl_error_invalidFile
            IOConfError.FileNotFound -> R.string.hsdl_error_fileNotFound
            IOConfError.ImpossibleReadFile -> R.string.hsdl_error_readError
            IOConfError.ImpossibleWriteFile -> R.string.hsdl_error_writeError
            IOConfError.ImpossibleCreateFile -> R.string.hsdl_error_createError
        }*/

}
