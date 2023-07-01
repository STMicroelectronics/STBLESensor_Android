/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.acceleration_event

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.acceleration_event.databinding.AccelerationEventFragmentBinding
import com.st.acceleration_event.event_view.MultipleEventView
import com.st.acceleration_event.event_view.SingleEventView
import com.st.blue_sdk.features.acceleration_event.AccelerationEventInfo
import com.st.blue_sdk.features.acceleration_event.AccelerationType
import com.st.blue_sdk.features.acceleration_event.DetectableEventType
import com.st.blue_sdk.models.Boards
import com.st.core.ARG_NODE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccelerationEventFragment : Fragment(){

    private val viewModel: AccelerationEventViewModel by viewModels()
    private lateinit var binding: AccelerationEventFragmentBinding
    private lateinit var nodeId: String
    private lateinit var nodeType: Boards.Model

    private var mCurrentEvent = DetectableEventType.None
    private lateinit var mSingleEventView: SingleEventView
    private lateinit var mMultipleEventView: MultipleEventView

    private lateinit var mEventSelector: Spinner
    private lateinit var mDetectableEventArrayAdapter: ArrayAdapter<DetectableEventType>

    /**
     * list of possible events supported by the nucleo board
     */
    private val nucleoSupportedEvents = arrayOf(
        DetectableEventType.None,
        DetectableEventType.Multiple,
        DetectableEventType.Orientation,
        DetectableEventType.DoubleTap,
        DetectableEventType.FreeFall,
        DetectableEventType.Pedometer,
        DetectableEventType.SingleTap,
        DetectableEventType.Tilt,
        DetectableEventType.WakeUp
    )

    private val sensorTileBoxSupportedEvents = arrayOf(
        DetectableEventType.None,
        DetectableEventType.Orientation,
        DetectableEventType.DoubleTap,
        DetectableEventType.FreeFall,
        DetectableEventType.SingleTap,
        DetectableEventType.Tilt,
        DetectableEventType.WakeUp
    )

    private val stwinSupportedEvents = arrayOf(
        DetectableEventType.None,
        DetectableEventType.Orientation,
        DetectableEventType.DoubleTap,
        DetectableEventType.FreeFall,
        DetectableEventType.SingleTap,
        DetectableEventType.Tilt,
        DetectableEventType.WakeUp
    )

    private val idB008SupportedEvents = arrayOf(
        DetectableEventType.None,
        DetectableEventType.FreeFall,
        DetectableEventType.SingleTap,
        DetectableEventType.WakeUp,
        DetectableEventType.Tilt,
        DetectableEventType.Pedometer
    )

    private val bcn002v1SupportedEvents = arrayOf(
        DetectableEventType.None,
        DetectableEventType.WakeUp,
        DetectableEventType.SingleTap,
        DetectableEventType.Pedometer,
        DetectableEventType.Tilt,
        DetectableEventType.FreeFall
    )

    private val proteusSupportedEvents = arrayOf(
        DetectableEventType.None,
        DetectableEventType.WakeUp
    )

    /**
     * get the list of supported event by the node
     * @param type type of node that we are using
     * @return supported events
     */
    fun getDetectableEvent(nodeType: Boards.Model): Array<DetectableEventType> {
        return when (nodeType) {
            Boards.Model.STEVAL_IDB008VX -> idB008SupportedEvents
            Boards.Model.STEVAL_BCN002V1 -> bcn002v1SupportedEvents
            Boards.Model.SENSOR_TILE_BOX -> sensorTileBoxSupportedEvents
            Boards.Model.STEVAL_STWINKIT1 -> stwinSupportedEvents
            Boards.Model.STEVAL_STWINKT1B -> stwinSupportedEvents
            Boards.Model.SENSOR_TILE_BOX_PRO -> sensorTileBoxSupportedEvents
            Boards.Model.SENSOR_TILE_BOX_PROB -> sensorTileBoxSupportedEvents
            Boards.Model.STWIN_BOX -> stwinSupportedEvents
            Boards.Model.STWIN_BOXB -> stwinSupportedEvents
            Boards.Model.PROTEUS -> proteusSupportedEvents
            Boards.Model.SENSOR_TILE -> nucleoSupportedEvents
            Boards.Model.BLUE_COIN -> nucleoSupportedEvents
            Boards.Model.WB_BOARD -> nucleoSupportedEvents
            Boards.Model.WBA_BOARD -> nucleoSupportedEvents
            Boards.Model.NUCLEO -> nucleoSupportedEvents
            Boards.Model.NUCLEO_F401RE -> nucleoSupportedEvents
            Boards.Model.NUCLEO_L476RG -> nucleoSupportedEvents
            Boards.Model.NUCLEO_L053R8 -> nucleoSupportedEvents
            Boards.Model.NUCLEO_F446RE -> nucleoSupportedEvents
            else -> arrayOf(DetectableEventType.None)
        }
    }

    private fun getDefaultEvent(nodeType: Boards.Model): DetectableEventType {
        return when (nodeType) {
            Boards.Model.STEVAL_WESU1 -> DetectableEventType.Multiple
            Boards.Model.SENSOR_TILE -> DetectableEventType.Orientation
            Boards.Model.BLUE_COIN -> DetectableEventType.Orientation
            Boards.Model.STEVAL_IDB008VX -> DetectableEventType.FreeFall
            Boards.Model.STEVAL_BCN002V1 -> DetectableEventType.Orientation
            Boards.Model.SENSOR_TILE_BOX -> DetectableEventType.Orientation
            Boards.Model.STEVAL_STWINKIT1 -> DetectableEventType.Orientation
            Boards.Model.STEVAL_STWINKT1B -> DetectableEventType.Orientation
            Boards.Model.SENSOR_TILE_BOX_PRO -> DetectableEventType.Orientation
            Boards.Model.SENSOR_TILE_BOX_PROB -> DetectableEventType.Orientation
            Boards.Model.STWIN_BOX -> DetectableEventType.Orientation
            Boards.Model.STWIN_BOXB -> DetectableEventType.Orientation
            Boards.Model.PROTEUS -> DetectableEventType.WakeUp
            Boards.Model.WB_BOARD -> DetectableEventType.Orientation
            Boards.Model.WBA_BOARD -> DetectableEventType.Orientation
            Boards.Model.NUCLEO -> DetectableEventType.Orientation
            Boards.Model.NUCLEO_F401RE -> DetectableEventType.Orientation
            Boards.Model.NUCLEO_L476RG -> DetectableEventType.Orientation
            Boards.Model.NUCLEO_L053R8 -> DetectableEventType.Orientation
            Boards.Model.NUCLEO_F446RE -> DetectableEventType.Orientation
            else -> DetectableEventType.None
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = AccelerationEventFragmentBinding.inflate(inflater, container, false)

        mSingleEventView = binding.accEventSingleEventView
        mMultipleEventView = binding.accEventMultipleEventView

        mEventSelector = binding.selectEventType

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nodeType = viewModel.getBoardType(nodeId)

        if (mCurrentEvent == DetectableEventType.None) {
            mCurrentEvent = getDefaultEvent(nodeType)
        }

        initializeEventSelector()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.accEventData.collect {
                    updateAccEventView(it)
                }
            }
        }

    }

    private fun initializeEventSelector() {
        mDetectableEventArrayAdapter = ArrayAdapter(
            requireActivity(),
            R.layout.simple_spinner_item, getDetectableEvent(nodeType)
        ).apply {
            setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        }

        mEventSelector.adapter = mDetectableEventArrayAdapter
        mEventSelector.setSelection(mDetectableEventArrayAdapter.getPosition(mCurrentEvent))
        mEventSelector.isEnabled = true

        mEventSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, selectedIndex: Int, p3: Long) {
                val selectedEvent = mDetectableEventArrayAdapter.getItem(selectedIndex)
                if (selectedEvent == DetectableEventType.Multiple) {
                    mSingleEventView.visibility = View.GONE
                    mMultipleEventView.visibility = View.VISIBLE
                } else {
                    mSingleEventView.visibility = View.VISIBLE
                    mMultipleEventView.visibility = View.GONE
                }

                if (selectedEvent != null) {
                    enableEvent(selectedEvent)
                }
            }
        }
    }

    private fun enableEvent(selectedEvent: DetectableEventType) {
        if (mSingleEventView.visibility == View.VISIBLE)
            mSingleEventView.enableEvent(selectedEvent)
        if (mMultipleEventView.visibility == View.VISIBLE)
            mMultipleEventView.enableEvent(selectedEvent)

        //disable the current one
        viewModel.setDetectableEventCommand(nodeId, mCurrentEvent, false)
        //enable the new one
        viewModel.setDetectableEventCommand(nodeId, selectedEvent, true)
        mCurrentEvent = selectedEvent
    }

    private fun displayEvent(
        selectedEvent: DetectableEventType,
        selectedEvents: List<AccelerationType>,
        steps: Int
    ) {
        if (mSingleEventView.visibility == View.VISIBLE)
            mSingleEventView.displayEvent(selectedEvents, steps)
        if (mMultipleEventView.visibility == View.VISIBLE)
            mMultipleEventView.displayEvent(
                selectedEvents,
                steps
            )
    }

    private fun updateAccEventView(it: AccelerationEventInfo) {
        val events = it.accEvent.map { event -> event.value }
        val steps: Int = if (it.numSteps.value != null) {
            it.numSteps.value!!.toInt()
        } else {
            0
        }
        displayEvent(mCurrentEvent, events, steps)
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
