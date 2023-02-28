package com.st.BlueMS.demos.ExtConfig

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.st.BlueMS.R
import com.st.BlueSTSDK.Node
import com.st.STWINBoard_Gui.IOConfError
import com.st.STWINBoard_Gui.Utils.SensorViewData

class SensorConfigDialogFragment(val node: Node?) : androidx.fragment.app.DialogFragment() {
    private lateinit var button_ok: Button
    private lateinit var viewModel: ExtConfigurationViewModel
    private lateinit var loadMLCViewModel: ExtConfigurationMLCConfigViewModel
    private lateinit var mProgressLayoutBar: LinearLayout

    private val mSensorsAdapter = ExtConfigurationSensorViewAdapter(
            object : ExtConfigurationSensorViewAdapter.SensorInteractionCallback {
                override fun onSensorCollapsed(selected: SensorViewData) {
                    viewModel.collapseSensor(selected)
                }

                override fun onSensorExpanded(selected: SensorViewData) {
                    viewModel.expandSensor(selected)
                }
            },
            onSubSensorODRChange = { sensor, subSensor, newOdrValue ->
                viewModel.changeODRValue(sensor,subSensor,newOdrValue)
            },
            onSubSensorFullScaleChange = { sensor, subSensor, newFSValue ->
                viewModel.changeFullScale(sensor,subSensor,newFSValue)
            },
            onSubSensorSampleChange = { sensor, subSensor, newSampleValue ->
                viewModel.changeSampleForTimeStamp(sensor,subSensor,newSampleValue)
            },
            onSubSubSensorEnableStatusChange = { sensor, subSensor, newState, paramsLocked ->
                viewModel.changeEnableState(sensor,subSensor, newState, paramsLocked)
            },
            onSubSensorOpenMLCConf = { sensor, subSensor ->
                loadMLCViewModel.openLoadMLCConf(sensor,subSensor)
                requestMLCConfigFile()
            },
            onUCFStatusChange = { sensor, subSensor, newStatus ->
                viewModel.changeEnableState(sensor,subSensor,newStatus,false)
            })

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        loadMLCViewModel.loadUCFFromFile(node,uri, requireContext().contentResolver)
    }

    private fun requestMLCConfigFile() {
        getContent.launch("application/*")
    }

    private val IOConfError.toStringRes:Int
        get() = when(this){
            IOConfError.InvalidFile -> com.st.clab.stwin.gui.R.string.hsdl_error_invalidFile
            IOConfError.FileNotFound -> com.st.clab.stwin.gui.R.string.hsdl_error_fileNotFound
            IOConfError.ImpossibleReadFile -> com.st.clab.stwin.gui.R.string.hsdl_error_readError
            IOConfError.ImpossibleWriteFile -> com.st.clab.stwin.gui.R.string.hsdl_error_writeError
            IOConfError.ImpossibleCreateFile -> com.st.clab.stwin.gui.R.string.hsdl_error_createError
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogWithOutTitleFullScreen);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.extconf_sensor_config, container, false)

        //Find ProgressBar
        mProgressLayoutBar = root.findViewById(R.id.extconfig_sensor_progress)

        //Find Button
        button_ok = root.findViewById(R.id.extconfig_sensor_positive)
        button_ok.setOnClickListener { closeDialog() }


        //Find RecyclerView
        val recyclerView: RecyclerView = root.findViewById(R.id.extconfig_sensor_config)
        recyclerView.adapter = mSensorsAdapter

        //Find the ViewModels
        viewModel = ViewModelProvider(requireActivity()).get(ExtConfigurationViewModel::class.java)
        loadMLCViewModel = ViewModelProvider(requireActivity()).get(ExtConfigurationMLCConfigViewModel::class.java)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.readSensors()
        mProgressLayoutBar.visibility= View.VISIBLE
        viewModel.readSensorsConfig_answer.observe(viewLifecycleOwner, Observer {
            if(it!=null) {
                if (it.isNotEmpty()) {
                    mProgressLayoutBar.visibility = View.GONE
                }
                mSensorsAdapter.submitList(it)
            }
        })

        loadMLCViewModel.mcLoaded.observe(viewLifecycleOwner, Observer {
            if(it==true) {
                viewModel.changeMLCLockedParams(true)
                loadMLCViewModel.mcLoadedServed()
            }
        })

        loadMLCViewModel.error.observe(viewLifecycleOwner, Observer {
            if(it!=null) {
                Snackbar.make(requireView(),it.toStringRes,Snackbar.LENGTH_SHORT).show()
            }
        })

    }

    private fun closeDialog() {
        viewModel.setSensorsDone()
        viewModel.readSensorsConfig_answer.removeObservers(viewLifecycleOwner)
        loadMLCViewModel.mcLoaded.removeObservers(viewLifecycleOwner)
        this.dismiss()
    }
}