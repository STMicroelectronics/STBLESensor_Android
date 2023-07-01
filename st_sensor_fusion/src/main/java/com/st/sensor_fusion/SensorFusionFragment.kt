/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.sensor_fusion

import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.st.blue_sdk.features.proximity.Proximity
import com.st.blue_sdk.features.proximity.ProximityInfo
import com.st.blue_sdk.features.sensor_fusion.Quaternion
import com.st.core.ARG_NODE_ID
import com.st.sensor_fusion.databinding.SensorFusionFragmentBinding
import com.st.sensor_fusion.utility.GLCubeRender
import com.st.sensor_fusion.utility.HidableTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

@AndroidEntryPoint
class SensorFusionFragment : Fragment() {

    //private val viewModel: SensorFusionViewModel by activityViewModels()
    private val viewModel: SensorFusionViewModel by hiltNavGraphViewModels(R.id.sensor_fusion_nav_graph)
    private val navArgs: SensorFusionFragmentArgs by navArgs()
    private lateinit var binding: SensorFusionFragmentBinding
    private lateinit var nodeId: String

    companion object {
        private const val INITIAL_CUBE_SCALE = 0.9f
        private const val MAX_DISTANCE = 200
        private const val SCALE_FACTOR = INITIAL_CUBE_SCALE / MAX_DISTANCE
    }

    /**
     * text that is hide if clicked, that contains the screen refresh rate
     */
    private lateinit var mFrameRateText: HidableTextView

    /**
     * text that is hide if clicked, that contains the number of quaternions that we receive in a
     * second
     */
    private lateinit var mQuaternionRateText: HidableTextView

    /**
     * opengl surface used for paint the cube
     */
    private lateinit var mGlSurface: GLSurfaceView

    /**
     * renderer used for paint the cube
     */
    private lateinit var mGlRenderer: GLCubeRender

    /**
     * button used for reset the cube position
     */
    private lateinit var mResetButton: Button

    /**
     * image used for tell if the system is calibrated or not
     */
    private lateinit var mCalibButton: ImageButton

    /**
     * feature where we read the cube position
     */
    private lateinit var mRootLayout: View

    /**
     * button for enable/disable the proximity feature, it is show only when the feature is
     * present
     */
    private lateinit var mProximityButton: SwitchCompat

    private lateinit var mProximityText: TextView

    /**
     * time of when we receive the first sample
     */
    private var mFistQuaternionTime: Long = -1
    private val mNQuaternion = AtomicLong(0)

    fun resetQuaternionRate() {
        mFistQuaternionTime = -1
        mNQuaternion.set(0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = SensorFusionFragmentBinding.inflate(inflater, container, false)
        mRootLayout = binding.memsSensorfusionRootLayout
        mFrameRateText = binding.memsSensorfusionRenderingRateText
        mQuaternionRateText = binding.memsSensorfusionQuaternionRateText

        mCalibButton = binding.calibrationImage
        mCalibButton.setOnClickListener { startCalibrationClicked() }

        mResetButton = binding.memsSensorfusionResetButton
        mResetButton.setOnClickListener { resetPositionClicked() }

        mProximityButton = binding.memsSensorfusionProximityButton
        mProximityButton.setOnClickListener { proximityButtonClicked() }
        mProximityText = binding.memsSensorfusionProximityText

        mGlSurface = binding.memsSensorfusionGlSurface
        mGlSurface.setEGLContextClientVersion(2)

        mGlRenderer = GLCubeRender(requireActivity(), getBgColor())
        mGlRenderer.setScaleCube(INITIAL_CUBE_SCALE)
        mGlSurface.setRenderer(mGlRenderer)

        return binding.root
    }

    private fun startCalibrationClicked() {
        //for displaying the board like not calibrated ...
        // just for SensorTile.box and SensorTile.box-Pro if they don't have the config BLE Char
        viewModel.calibrationStatus.postValue(false)
        findNavController().navigate(
            SensorFusionFragmentDirections.actionSensorFusionFragmentToCalibrationDialog(navArgs.nodeId)
        )
    }

    private fun resetPositionClicked() {
        findNavController().navigate(
            SensorFusionFragmentDirections.actionSensorFusionFragmentToResetDialog(navArgs.nodeId)
        )
    }

    private fun proximityButtonClicked() {
        if (mProximityButton.isChecked) {
            viewModel.enableProximityNotification(nodeId)
        } else {
            viewModel.disableProximityNotitification(nodeId)
        }
    }

    private fun getBgColor(): Int {
        val a = TypedValue()
        requireActivity().theme.resolveAttribute(android.R.attr.windowBackground, a, true)
        return if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            a.data
        } else {
            // windowBackground is not a color, probably a drawable
            Color.WHITE
        } //if else
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fusionData.collect {
                    updateGui(it)
                }
            }
        }

        viewModel.calibrationStatus.observe(viewLifecycleOwner) { calibrationStatus ->
            mCalibButton.setImageResource(
                if (calibrationStatus)
                    R.drawable.compass_calibration_calibrated
                else
                    R.drawable.compass_calibration_uncalibrated
            )
        }

        viewModel.resetCube.observe(viewLifecycleOwner) { reset ->
            if (reset) {
                mGlRenderer.resetCube()
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.freeFall.collect {
                    if (it) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.st_sensor_fusion_free_fall_detected),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.proximity.collect {
                    updateCubeDistance(it)
                }
            }
        }
    }

    private fun updateCubeDistance(proximity: Int) {
        val proximityStr: String =
            if (proximity == ProximityInfo.OUT_OF_RANGE_VALUE) {
                mGlRenderer.setScaleCube(INITIAL_CUBE_SCALE)
                getString(R.string.st_sensor_fusion_proximity_out_of_range)
            } else {
                mGlRenderer.setScaleCube(min(proximity, MAX_DISTANCE) * SCALE_FACTOR)
                getString(R.string.st_sensor_fusion_proximity_format, proximity)
            }
        mProximityText.text = proximityStr
    }

    private fun updateGui(it: Quaternion) {
        if (mFistQuaternionTime < 0) mFistQuaternionTime = System.currentTimeMillis()
        //+1 for avoid division by 0 the first time that we initialize mFistQuaternionTime
        //+1 for avoid division by 0 the first time that we initialize mFistQuaternionTime
        val averageQuaternionRate = mNQuaternion.incrementAndGet() * 1000 /
                (System.currentTimeMillis() - mFistQuaternionTime + 1)
        //update the cube rotation
        mGlRenderer.setRotation(it.qi, it.qj, it.qk, it.qs)
        mFrameRateText.text =
            resources.getString(R.string.memsSensorFusion_frameRate, mGlRenderer.getRenderingRate())
        mQuaternionRateText.text =
            resources.getString(R.string.memsSensorFusion_quaternionRate, averageQuaternionRate)
    }

    override fun onResume() {
        super.onResume()
        resetQuaternionRate()
        viewModel.startDemo(nodeId = nodeId)

        //Check if the node have Proximity Feature
        if (viewModel.nodeHaveProximityFeature()) {
            mProximityButton.visibility = View.VISIBLE
            mProximityText.visibility = View.VISIBLE
            mProximityButton.isChecked = true
            viewModel.enableProximityNotification(nodeId)
        } else {
            mProximityButton.visibility = View.INVISIBLE
            mProximityText.visibility = View.GONE
            mProximityButton.isChecked = false
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
