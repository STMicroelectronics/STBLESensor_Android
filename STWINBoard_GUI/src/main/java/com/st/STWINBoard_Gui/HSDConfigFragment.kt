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
package com.st.STWINBoard_Gui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.Sensor
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceParser
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node
import com.st.STWINBoard_Gui.Utils.SensorViewAdapter
import com.st.STWINBoard_Gui.Utils.SensorViewData
import com.st.clab.stwin.gui.R

/**
 *
 */
open class HSDConfigFragment : Fragment() {
    private lateinit var viewModel : HSDConfigViewModel
    private lateinit var loadMLCViewModel : HSDLoadUCFViewModel

    private val mSensorsAdapter = SensorViewAdapter(
            object : SensorViewAdapter.SensorInteractionCallback {
                override fun onSensorCollapsed(selected: SensorViewData) {
                    viewModel.collapseSensor(selected)
                }

                override fun onSensorExpanded(selected: SensorViewData) {
                    viewModel.expandSensor(selected)
                }
            },
            onSubSensorODRChange = {sensor, subSensor, newOdrValue ->
                viewModel.changeODRValue(sensor,subSensor,newOdrValue)
            },
            onSubSensorFullScaleChange = {sensor, subSensor, newFSValue ->
                viewModel.changeFullScale(sensor,subSensor,newFSValue)
            },
            onSubSensorSampleChange = {sensor, subSensor, newSampleValue ->
                viewModel.changeSampleForTimeStamp(sensor,subSensor,newSampleValue)
            },
            onSubSubSensorEnableStatusChange = {sensor, subSensor, newState, paramsLocked ->
                viewModel.changeEnableState(sensor,subSensor, newState, paramsLocked)
            },
            onSubSensorOpenMLCConf = {sensor, subSensor ->
                loadMLCViewModel.openLoadUCF(sensor,subSensor)
                requestMLCConfigFile()
            },
            onUCFStatusChange = {sensor, subSensor, newStatus ->
                viewModel.changeEnableState(sensor,subSensor,newStatus,false)
            })


    private fun requestConfigurationFile() {
        val chooserFile = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json"))
            type = PICKFILE_REQUEST_TYPE
        }
        val chooserTitle = getString(R.string.hsdl_configFileChooserTitle)
        startActivityForResult(Intent.createChooser(chooserFile, chooserTitle), PICKFILE_REQUEST_CODE)
    }

    private fun requestMLCConfigFile() {
        val chooserFile = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/*", "text/*"))
            type = PICKFILE_UCF_REQUEST_TYPE
        }
        val chooserTitle = getString(R.string.stwin_ucfFileChooserTitle)
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
                        displayErrorMessage(IOConfError.InvalidFile)
                    } else {
                        loadMLCViewModel.loadUCFFromFile(fileUri, requireContext().contentResolver)
                        viewModel.changeMLCLockedParams(true)
                    }
                }
            }
            CREATE_FILE_REQUEST_CODE -> {
                val fileUri = data?.data
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.storeConfigToFile(fileUri,requireContext().contentResolver)
                }
            }
            SAVE_SETTINGS_REQUEST_CODE->{
                if (resultCode == Activity.RESULT_OK) {
                    val settings = HSDConfigSaveDialogFragment.extractSaveSettings(data) ?: return
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_stwin_config, container, false)

        root.findViewById<View>(R.id.stWinConf_loadButton).setOnClickListener {
            requestConfigurationFile()
        }

        root.findViewById<View>(R.id.stWinConf_saveButton).setOnClickListener {
            showSaveDialog()
        }

        val recyclerView = root.findViewById<RecyclerView>(R.id.stWinConf_sensorsList)
        recyclerView.adapter = mSensorsAdapter

        mConfView = root.findViewById(R.id.stWinConf_confViewLayout)
        mLoadingView = root.findViewById(R.id.stWinConf_progressLayout)
        mLoadingText = root.findViewById(R.id.stWinConf_progressText)

        val viewSaveButton = root.findViewById<View>(R.id.stWinConf_loadSaveButtonGroup)
        viewSaveButton.visibility = if(showSaveButton){
            View.VISIBLE
        }else{
            View.GONE
        }

        viewModel = ViewModelProvider(requireActivity()).get(HSDConfigViewModel::class.java)
        loadMLCViewModel = ViewModelProvider(requireActivity()).get(HSDLoadUCFViewModel::class.java)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if(error!=null)
                displayErrorMessage(error)
        })
        viewModel.sensorsConfiguration.observe(viewLifecycleOwner, Observer {
            mSensorsAdapter.submitList(it)
        })

        viewModel.savedConfuguration.observe(viewLifecycleOwner, Observer { conf ->
            conf ?: return@Observer
            sendConfigCompleteEvent(conf)
        })
        viewModel.requestFileLocation.observe(viewLifecycleOwner, Observer { askFile ->
            if(askFile){
                requestFileCreation()
            }
        })

        viewModel.fwErrorInfo.observe(viewLifecycleOwner, Observer { hasObsoleteFW ->
            showFWErrorMessage(hasObsoleteFW)
        })

        viewModel.isConfigLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            showConfigLoadingView(isLoading)
        })

        viewModel.isLogging.observe(viewLifecycleOwner, Observer { isLogging ->
            showIsLoggingView(isLogging)
        })

        loadMLCViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            showMLCLoadingView(isLoading)
        })
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

    private fun showMLCLoadingView(isLoading:Boolean){
        showProgressView(isLoading,R.string.stwin_loading_ucf)
    }

    private fun showIsLoggingView(isLogging:Boolean){
        showProgressView(isLogging,R.string.stwin_logging)
    }

    private fun showFWErrorMessage(fwErrorInfo:FWErrorInfo){
        val builder = AlertDialog.Builder(requireContext())
        with(builder)
        {
            setTitle(fwErrorInfo.title)
            setIcon(R.drawable.ic_warning_24dp)
            setMessage("Firmware detected: [" + fwErrorInfo.currFW + "].\n" +
                    "Latest available Firmware version is: [" + fwErrorInfo.targetFW + "].\n\n" +
                    "You can download it here: " + fwErrorInfo.targetFWUrl,)
            setPositiveButton("OK"){_,_->}
            show()
        }
    }

    private fun showConfigLoadingView(isLoading:Boolean){
        showProgressView(isLoading,R.string.stwin_loading_conf)
    }

    private fun displayErrorMessage(error: IOConfError) {
        Snackbar.make(requireView(),error.toStringRes,Snackbar.LENGTH_SHORT)
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
    }

    fun disableNeedNotification(node: Node) {
        viewModel.disableNotificationFromNode(node)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            viewModel.checkIsLogging()
        }
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
                showSaveDialog()
                true
            }

            R.id.menu_hsdl_loadConf -> {
                requestConfigurationFile()
                true
            }
            R.id.menu_hsdl_changeAlias -> {
                val node = getNode()
                if(node!=null) {
                    val aliasSettings = BoardAliasConfDialogFragment.newInstance(node)
                    aliasSettings.show(childFragmentManager, ALIAS_CONFIG_FRAGMENT_TAG)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSaveDialog() {
        val dialog = HSDConfigSaveDialogFragment()
        dialog.setTargetFragment(null, SAVE_SETTINGS_REQUEST_CODE)
        dialog.show(childFragmentManager,"saveDialog")
    }


    private fun sendConfigCompleteEvent(newConf:List<Sensor>){
        val newConfStr = DeviceParser.toJsonStr(newConf)
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
        private val ALIAS_CONFIG_FRAGMENT_TAG = HSDConfigFragment::class.java.name + ".ALIAS_CONFIG_FRAGMENT"
        private const val PICKFILE_REQUEST_CODE = 7777
        private const val PICKFILE_REQUEST_TYPE = "application/json"
        private const val PICKFILE_UCF_REQUEST_CODE = 7778
        private const val PICKFILE_UCF_REQUEST_TYPE = "application/ucf"
        private const val DEFAULT_CONFI_NAME = "STWIN_conf.json"

        private val NODE_TAG_EXTRA = HSDConfigFragment::class.java.name + ".NODE_TAG_EXTRA"
        private val SHOW_SAVE_BUTTON_EXTRA = HSDConfigFragment::class.java.name + ".SHOW_SAVE_BUTTON_EXTRA"
        private val DISABLE_NOTIFICATION_ON_STOP = HSDConfigFragment::class.java.name + ".DISABLE_NOTIFICATION_ON_STOP"

        fun newInstance(node: Node,showSaveButton:Boolean = false,disableNotificationOnStop:Boolean = true): Fragment {
            return HSDConfigFragment().apply {
                arguments = Bundle().apply {
                    putString(NODE_TAG_EXTRA,node.tag)
                    putBoolean(SHOW_SAVE_BUTTON_EXTRA,showSaveButton)
                    putBoolean(DISABLE_NOTIFICATION_ON_STOP,disableNotificationOnStop)
                }
            }
        }

        private const val CREATE_FILE_REQUEST_CODE = 1
        private const val SAVE_SETTINGS_REQUEST_CODE = 2

        private val ACTION_CONFIG_COMPLETE = HSDConfigFragment::class.java.name + ".ACTION_CONFIG_COMPLETE"
        private val ACTION_CONFIG_EXTRA = HSDConfigFragment::class.java.name + ".ACTION_CONFIG_EXTRA"
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

    private val IOConfError.toStringRes:Int
        get() = when(this){
            IOConfError.InvalidFile -> R.string.hsdl_error_invalidFile
            IOConfError.FileNotFound -> R.string.hsdl_error_fileNotFound
            IOConfError.ImpossibleReadFile -> R.string.hsdl_error_readError
            IOConfError.ImpossibleWriteFile -> R.string.hsdl_error_writeError
            IOConfError.ImpossibleCreateFile -> R.string.hsdl_error_createError
        }

}
