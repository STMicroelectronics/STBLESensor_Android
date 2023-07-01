/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.textual_monitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.st.blue_sdk.board_catalog.models.BleCharacteristic
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.features.Feature
import com.st.core.ARG_NODE_ID
import dagger.hilt.android.AndroidEntryPoint
import com.st.textual_monitor.databinding.TextualMonitorFragmentBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TextualMonitorFragment : Fragment() {

    private val viewModel: TextualMonitorViewModel by viewModels()
    private lateinit var binding: TextualMonitorFragmentBinding
    private lateinit var nodeId: String

    private lateinit var startStopButton: MaterialButton
    private lateinit var spinner: Spinner
    private var adapterSpinnerPosition = 0

    private lateinit var featureList: List<GenericTextualFeature?>

    private lateinit var featureData: TextView
    private lateinit var scrollView: ScrollView

    private lateinit var scrollViewSerialConsole: ScrollView
    private lateinit var textViewSerialConsole: TextView
    private lateinit var buttonViewSerialConsole: Button

    private var serialConsoleIsRunning = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = TextualMonitorFragmentBinding.inflate(inflater, container, false)

        startStopButton = binding.genericTextualDemoButton
        startStopButton.setOnClickListener { startStop() }

        spinner = binding.genericTextualDemoSpinner

        featureData = binding.genericTextualDemoText
        scrollView = binding.genericTextualDemoScrollview

        scrollViewSerialConsole = binding.textualDemoSerialConsoleScrollview
        textViewSerialConsole = binding.textualDemoSerialConsoleText
        buttonViewSerialConsole = binding.genericTextualDemoButtonSerialConsole

        buttonViewSerialConsole.setOnClickListener { startStopSerialConsole(nodeId) }

        return binding.root
    }

    private fun startStopSerialConsole(nodeId: String) {
        if(serialConsoleIsRunning) {
            serialConsoleIsRunning = false
            scrollViewSerialConsole.visibility = View.GONE
            viewModel.stopReceiveDebugMessage()
            textViewSerialConsole.text=""
        } else {
            serialConsoleIsRunning = true
            scrollViewSerialConsole.visibility = View.VISIBLE
            viewModel.startReceiveDebugMessage(nodeId)
        }
    }

    private fun startStop() {
        val featureSelected = featureList[adapterSpinnerPosition]
        featureSelected?.let {
            if (viewModel.feature != null) {
                //the feature is already notifying
                viewModel.stopDemo(nodeId)
                startStopButton.setIconResource(R.drawable.ic_play_arrow)
            } else {
                val initString = "${it.description}:\n\n"
                featureData.text = initString

                //set the current feature
                viewModel.setSelectedFeature(it.feature, it.bleCharDesc)

                //Start the notification
                viewModel.startDemo(nodeId)

                startStopButton.setIconResource(R.drawable.ic_stop)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //retrieve the Fw model
        val fwModel = viewModel.getNodeFwModel(nodeId)

        //retrieve the List of Features
        val features = viewModel.getNodeFeatures(nodeId).filter { it.isDataNotifyFeature }

        if (features.isNotEmpty()) {
            featureList = features.map {
                retrieveBleCharDescription(it, fwModel)
            }

            val dataAdapter = ArrayAdapter(requireActivity(),
                android.R.layout.simple_spinner_item, featureList.map { it?.name }).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            adapterSpinnerPosition = 0

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    adapterSpinnerPosition = position
                    changeFeature()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    adapterSpinnerPosition = 0
                }
            }

            spinner.adapter = dataAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataFeature.collect {
                    featureData.append(it)
                    scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.debugMessages.collect {
                        if (!it.isNullOrEmpty()) {
                            val string = textViewSerialConsole.text.toString() + it
                            textViewSerialConsole.text = string
                            scrollViewSerialConsole.post { scrollViewSerialConsole.fullScroll(View.FOCUS_DOWN) }
                        }
                }
            }
        }
    }

    //retrieve the Feature Description from Fw Model if the feature is a General Purpose
    private fun retrieveBleCharDescription(
        it: Feature<*>,
        fwModel: BoardFirmware?
    ): GenericTextualFeature? {
        var bleCharDesc: BleCharacteristic? = null
        val uuid = it.uuid
        if (it.type == Feature.Type.GENERAL_PURPOSE) {
            bleCharDesc = fwModel?.characteristics?.firstOrNull { it.uuid == uuid.toString() }
        }

        val name: String = if (bleCharDesc == null) {
            it.name
        } else {
            "GP " + bleCharDesc.name
        }
        val desc: String = bleCharDesc?.name ?: it.name
        return GenericTextualFeature(name, desc, it, bleCharDesc)
    }

    fun changeFeature() {
        val featureSelected = featureList[adapterSpinnerPosition]
        //Check if the PrevSelected feature is already notifying something or not
        val prevFeature = viewModel.feature
        if ((prevFeature != null) && (featureSelected != null)) {
            //Disable the notification of previous selected Feature
            viewModel.stopDemo(nodeId = nodeId)
            startStopButton.setIconResource(R.drawable.ic_play_arrow)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
