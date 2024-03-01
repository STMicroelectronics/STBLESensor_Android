/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.node_status

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.battery.BatteryInfo
import com.st.blue_sdk.features.battery.BatteryStatus
import com.st.blue_sdk.models.Node
import com.st.core.ARG_NODE_ID
import com.st.node_status.databinding.NodeStatusFragmentBinding
import com.st.ui.utils.getBlueStBoardImages
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NodeStatusFragment : Fragment() {

    private val viewModel: NodeStatusViewModel by viewModels()
    private lateinit var binding: NodeStatusFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mRssiText: TextView
    private lateinit var mBatteryStatusText: TextView
    private lateinit var mBatteryPercentageText: TextView
    private lateinit var mBatteryVoltageText: TextView
    private lateinit var mBatteryIcon: ImageView
    private lateinit var mBatteryCardView: CardView

    private val BATTERY_CHARGING_IMAGES = intArrayOf(
        R.drawable.battery_00c,
        R.drawable.battery_20c,
        R.drawable.battery_40c,
        R.drawable.battery_60c,
        R.drawable.battery_80c,
        R.drawable.battery_100c
    )

    private val BATTERY_DISCHARGE_IMAGES = intArrayOf(
        R.drawable.battery_00,
        R.drawable.battery_20,
        R.drawable.battery_40,
        R.drawable.battery_60,
        R.drawable.battery_80,
        R.drawable.battery_100
    )
    private lateinit var mBatteryCurrentText: TextView

    private var mBatteryCapacity = 0

    private lateinit var mRemainingTime: TextView

    private lateinit var mNodeName: TextView
    private lateinit var mNodeAddress: TextView
    private lateinit var mNodeIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = NodeStatusFragmentBinding.inflate(inflater, container, false)

        mRssiText = binding.statusRssiText
        mBatteryPercentageText =  binding.statusBatteryPercentageText
        mBatteryStatusText = binding.statusBatteryStatusText
        mBatteryVoltageText = binding.statusBatteryVoltageText
        mBatteryCardView = binding.statusBatteryCard
        mBatteryCardView.visibility = View.GONE
        mBatteryCurrentText = binding.statusBatteryCurrentText
        mBatteryIcon = binding.statusBatteryImage
        mRemainingTime = binding.statusBatteryRemainingTimeText

        mNodeAddress = binding.statusBoardAddress
        mNodeName = binding.statusBoardName
        mNodeIcon = binding.statusBoardTypeIcon

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val node = viewModel.getNode(nodeId)

        node?.let{setUpNodeInfo(node)}

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.batteryData.collect {
                    updateBatteryView(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rssiData.collect {
                    if(it!=null) {
                        mRssiText.text = getString(R.string.nodeStatus_rssi_format, it.rssi)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.batteryCapacity.collect {
                    mBatteryCapacity = it
                }
            }
        }

        viewModel.hasFeatureFlag.observe(viewLifecycleOwner, Observer { newValue ->
            //the board has the Battery Feature
            if(newValue!=null) {
                if (newValue) {
                    mBatteryCardView.visibility = View.VISIBLE
                    viewModel.readBatteryCapacity(nodeId)
                    //viewModel.readStdConsumedCurrent(nodeId)
                } else {
                    mBatteryVoltageText.setText(R.string.nodeStatus_battery_notFound)
                }
            }
        })
    }

    private fun updateBatteryView(it: BatteryInfo) {
        val status = it.status.value
        val percentage = it.percentage.value
        val voltage = it.voltage.value
        val current = it.current.value

        val batteryIcon = getBatteryIcon(percentage,status)
        val icon = ContextCompat.getDrawable(requireContext(), batteryIcon)

        val batteryStatus = "Status: $status"
        val batteryPercentage = getString(R.string.nodeStatus_battery_percentage, percentage, it.percentage.unit)
        val batteryVoltage =  getString(R.string.nodeStatus_battery_voltage, voltage, it.voltage.unit)
        val batteryCurrent = if(current.isNaN()  or (current==0f)) {
            getString(R.string.nodeStatus_battery_current_unknown)
        } else {
            getString(R.string.nodeStatus_battery_current, current, it.current.unit)
        }

        val remainingBattery = mBatteryCapacity * (percentage / 100.0f)
        val remainingTime: Float = getRemainingTimeMinutes(remainingBattery, current)

        val remainingTimeStr = if(current.isNaN()  or (current==0f)) {
            ""
        } else {
            getString(R.string.nodeStatus_battery_remainingTime, remainingTime)
        }

        if (status != BatteryStatus.Unknown) {
            mBatteryStatusText.visibility = View.VISIBLE
            mBatteryStatusText.text = batteryStatus
        } else {
            mBatteryStatusText.visibility = View.INVISIBLE
        }

        if (percentage.isNaN() or (percentage == 0f)) {
            mBatteryPercentageText.visibility = View.INVISIBLE
        } else {
            mBatteryPercentageText.visibility = View.VISIBLE
            mBatteryPercentageText.text = batteryPercentage
        }

        mBatteryIcon.setImageDrawable(icon)

        if (voltage.isNaN() or (voltage==0f)) {
            mBatteryVoltageText.visibility = View.INVISIBLE
        } else {
            mBatteryVoltageText.visibility = View.VISIBLE
            mBatteryVoltageText.text = batteryVoltage
        }

        if (current.isNaN() or (current==0f)) {
            mBatteryCurrentText.visibility = View.INVISIBLE
        } else {
            mBatteryCurrentText.visibility = View.VISIBLE
            mBatteryCurrentText.text = batteryCurrent
        }

        if (displayRemainingTime(status)) {
            mRemainingTime.visibility = View.VISIBLE
            mRemainingTime.text = remainingTimeStr
        } else {
            mRemainingTime.visibility = View.INVISIBLE
        }
    }

    @SuppressLint("MissingPermission")
    private fun setUpNodeInfo(node: Node) {
        mNodeName.text = node.advertiseInfo?.getName() ?: node.friendlyName
        mNodeAddress.text = node.advertiseInfo?.getAddress() ?: ""
        mNodeIcon.setImageResource(getBlueStBoardImages(node.boardType.name))
    }

    private fun getRemainingTimeMinutes(batteryCapacity: Float, current: Float): Float {
        return if (current < 0)
            batteryCapacity / -current * 60
        else
            Float.NaN
    }

    private fun getIconIndex(percentage: Float, nIcons: Int): Int {
        val iconIndex = percentage.toInt() * nIcons / 100
        return MathUtils.clamp(iconIndex, 0, nIcons - 1)
    }

    @DrawableRes
    private fun getBatteryIcon(percentage: Float, status: BatteryStatus): Int {
        val index: Int
        return when (status) {
            BatteryStatus.LowBattery , BatteryStatus.Discharging, BatteryStatus.PluggedNotCharging -> {
                index = getIconIndex(percentage, BATTERY_DISCHARGE_IMAGES.size)
                BATTERY_DISCHARGE_IMAGES[index]
            }
            BatteryStatus.Charging -> {
                index = getIconIndex(percentage, BATTERY_CHARGING_IMAGES.size)
                BATTERY_CHARGING_IMAGES[index]
            }
            BatteryStatus.Unknown, BatteryStatus.Error -> R.drawable.battery_missing
        }
    }

    private fun displayRemainingTime(status: BatteryStatus): Boolean {
        return status !== BatteryStatus.Charging
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
