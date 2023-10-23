/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.led_control

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.external.stm32.P2PConfiguration
import com.st.blue_sdk.features.external.stm32.switch_status.SwitchInfo
import com.st.blue_sdk.models.Boards
import com.st.core.ARG_NODE_ID
import com.st.led_control.databinding.LedControlFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class LedControlFragment : Fragment() {

    private val viewModel: LedControlViewModel by viewModels()
    private lateinit var binding: LedControlFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mDeviceName: TextView
    private lateinit var mInstructionText: TextView
    private lateinit var mLedImage: ImageView

    private var mLedStatus: Boolean = false
    var mCurrentDevice: P2PConfiguration.DeviceId? = null


    private lateinit var mAlarmViewGroup: Group
    private lateinit var mLedViewGroup: Group
    private lateinit var mAlarmText: TextView
    private lateinit var mAlarmImage: ImageView

    private lateinit var mRssiText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = LedControlFragmentBinding.inflate(inflater, container, false)

        mDeviceName = binding.stm32wbSingleTitleText
        mInstructionText = binding.stm32wbSingleInstruction
        mAlarmViewGroup = binding.stm32wbSingleAlarmView
        mLedImage = binding.stm32wbSingleLedImage
        mLedViewGroup = binding.stm32wbSingleLedView

        mAlarmText = binding.stm32wbSingleAlarmText
        mAlarmImage = binding.stm32wbSingleAlarmImage

        mAlarmText.text = resources.getString(R.string.stm32wb_single_alarm_caption)
        mAlarmImage.setColorFilter( ContextCompat.getColor(requireContext(), com.st.ui.R.color.colorGrey))

        mLedImage.setOnClickListener {
            if(mCurrentDevice!=null) {
                mLedStatus = mLedStatus==false
                changeLedStatusImage(mLedStatus)
                viewModel.writeSwitchCommand(nodeId, mCurrentDevice!!,mLedStatus)
            }
        }

        mRssiText = binding.stm32wbSingleRssiText

        return binding.root
    }

    private fun changeLedStatusImage(newState: Boolean) {
        when(newState) {
            true -> mLedImage.setImageResource(R.drawable.stm32wb_led_on)
            false -> mLedImage.setImageResource(R.drawable.stm32wb_led_off)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.switchData.collect {
                    updateLedControlView(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rssiData.collect {
                    if(it!=null) {
                        mRssiText.text = getString(R.string.stm32wb_rssiFormat, it.rssi)
                    }
                }
            }
        }

    }

    private fun Date.timeToString(): String {
        val timeFormat: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeStr = timeFormat.format(this)
        return String.format(Locale.getDefault(), "%s", timeStr)
    }

    private fun updateLedControlView(it: SwitchInfo) {
        if (mCurrentDevice == null) {
            //first time
            val device = P2PConfiguration.getDeviceIdById(it.deviceId.value)
            if (device != null) {
                showDeviceDetected(device)
            }
        }

        val eventDate = Date().timeToString()
        val isSelected = if(it.isSwitchPressed.value) {
            1
        } else {
            0
        }
        mAlarmText.text = getString(R.string.stm32wb_single_eventFormat, eventDate, isSelected)
        animateAlarmColor()
    }

    private fun animateAlarmColor() {
        val initialColor = ContextCompat.getColor(requireContext(), com.st.ui.R.color.colorAccent)
        val finalColor = ContextCompat.getColor(requireContext(), com.st.ui.R.color.colorGrey)
        val duration = resources.getInteger(R.integer.stm32wb_single_alarmBlinkDuration)
        val colorAnimation: ValueAnimator =
            ValueAnimator.ofObject(ArgbEvaluator(), initialColor, finalColor)
        colorAnimation.duration = duration.toLong()
        colorAnimation.addUpdateListener { animator: ValueAnimator ->
            mAlarmImage.setColorFilter(
                animator.animatedValue as Int
            )
        }
        colorAnimation.start()
    }

    private fun showDeviceDetected(device: P2PConfiguration.DeviceId) {
        mCurrentDevice = device
        mDeviceName.text = getString(R.string.stm32wb_deviceNameFormat, device.id)
        mLedViewGroup.visibility = View.VISIBLE
        mInstructionText.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)

        val node = viewModel.getNode(nodeId)

        if(node!=null) {
            mCurrentDevice = node.advertiseInfo?.let {
                val deviceId = it.getDeviceId().toInt()
                val sdkVersion = it.getProtocolVersion().toInt()
                P2PConfiguration.getDeviceIdByBoardId(deviceId,sdkVersion)
            }
//                node.advertiseInfo?.getDeviceId()
//                    ?.let { P2PConfiguration.getDeviceIdByBoardId(it.toInt()) }
        }

        if (mCurrentDevice != null) {
            showDeviceDetected(mCurrentDevice!!)
        }

        if (node != null) {
            if (node.familyType == Boards.Family.WBA_FAMILY) { // there is no notion of P2PServer 1, P2PServer 2, etc. for WBA
                showDeviceDetected(P2PConfiguration.DeviceId.Device1)
            }

            if(node.rssi!=null) {
                mRssiText.text = getString(R.string.stm32wb_rssiFormat, node.rssi!!.rssi)
            }
        }

        changeLedStatusImage(mLedStatus)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
