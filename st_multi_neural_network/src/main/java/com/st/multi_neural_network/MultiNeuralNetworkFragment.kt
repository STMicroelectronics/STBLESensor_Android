/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.multi_neural_network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.st.blue_sdk.features.activity.ActivityInfo
import com.st.blue_sdk.features.activity.ActivityType
import com.st.blue_sdk.features.extended.audio_classification.AudioClassType
import com.st.blue_sdk.features.extended.audio_classification.AudioClassificationInfo
import com.st.core.ARG_NODE_ID
import dagger.hilt.android.AndroidEntryPoint
import com.st.multi_neural_network.databinding.MultiNeuralNetworkFragmentBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MultiNeuralNetworkFragment : Fragment() {

    companion object{
        private val DATE_FORMAT = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    }

    private val viewModel: MultiNeuralNetworkViewModel by viewModels()
    private lateinit var binding: MultiNeuralNetworkFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mHumanActivityView:View
    private lateinit var mHumanActivityImage: ImageView
    private lateinit var mHumanActivityDesc: TextView

    private lateinit var mAudioSceneView:View
    private lateinit var mAudioSceneImage: ImageView
    private lateinit var mAudioSceneStatusText: TextView
    private lateinit var mAudioSceneDesc: TextView

    private lateinit var mComboView:View
    private lateinit var mComboImage: ImageView
    private lateinit var mComboDesc: TextView

    private lateinit var mAlgoSelectorGroup:View
    private lateinit var mAlgoSelector: Spinner

    private var mActivityAlgo: Short = -1
    private lateinit var mActivityState: ActivityType
    private var mAudioAlgo: Short = -1
    private lateinit var mAudioState: AudioClassType


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = MultiNeuralNetworkFragmentBinding.inflate(inflater, container, false)

        mAlgoSelector =  binding.multiNNAlgoSelectSpinner

        mHumanActivityImage = binding.multiNNHumanActivityImage
        mHumanActivityView = binding.multiNNHumanActivityCardView
        mHumanActivityDesc = binding.multiNNHumanActivityDesc

        mAudioSceneImage = binding.multiNNAudioSceneImage
        mAudioSceneView = binding.multiNNAudioSceneCardView
        mAudioSceneDesc = binding.multiNNAudioSceneImageDesc
        mAudioSceneStatusText = binding.multiNNAudioSceneStatusText

        mComboView = binding.multiNNComboCardView
        mComboImage = binding.multiNNComboImage
        mComboDesc = binding.multiNNComboDesc

        mAlgoSelectorGroup = binding.multiNNAlgoSelectGroup
        mAlgoSelector = binding.multiNNAlgoSelectSpinner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ActivityRecognition
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityInfo.collect {
                    updateActivityUI(it)
                    updateComboUI(it,null)
                }
            }
        }

        //Audio Classification
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.audioClassificationInfo.collect {
                    updateAudioUI(it)
                    updateComboUI(null,it)
                }
            }
        }

        //Update Algorithm Selector Spinner
        viewModel.algorithmsList.observe(viewLifecycleOwner){ algorithmsList ->
            if(algorithmsList == null) {
                return@observe
            }//else

            //Make the Algorithms selector visible
            mAlgoSelectorGroup.visibility = View.VISIBLE

            //Fill the Spinner

            val names = algorithmsList.map { it.name }.toTypedArray()
            mAlgoSelector.setSelection(0,false)
            mAlgoSelector.adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item,names).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            mAlgoSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) { }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selected = algorithmsList[position]
                    viewModel.selectAlgorithm(nodeId,selected)
                }
            }
        }

    }

    private fun updateComboUI( activity: ActivityInfo?,audio: AudioClassificationInfo?) {
        if(audio!=null) {
            mAudioState = audio.classification.value
            mAudioAlgo = audio.algorithm.value
        }
        if(activity!=null) {
            mActivityState = activity.activity.value
            mActivityAlgo = activity.algorithm.value
        }

        if((mAudioAlgo.toInt()==1) && (mActivityAlgo.toInt()==4)) {
            mComboView.visibility = View.VISIBLE
            if((mActivityState!=ActivityType.AdultInCar) && (mAudioState== AudioClassType.BabyIsCrying)) {
                val dateString = DATE_FORMAT.format(Date())
                val activityString = getString(R.string.multiNN_warning)
                mComboDesc.text=String.format("%s: %s",dateString,activityString)
                mComboImage.setImageResource(R.drawable.ic_error)
            } else {
                val dateString = DATE_FORMAT.format(Date())
                val activityString = getString(R.string.multiNN_quiet)
                mComboDesc.text=String.format("%s: %s",dateString,activityString)
                mComboImage.setImageResource(R.drawable.ic_warning_light_grey_24dp)
            }
        }
    }


    private fun updateActivityUI(it: ActivityInfo) {
        val activity = it.activity.value
        val algorithm = it.algorithm.value
        val date = it.date.value
        
        //Human Activity Recognition Card
        mHumanActivityView.visibility = View.VISIBLE
        if(algorithm.toInt()==4) {
            if(activity==ActivityType.AdultInCar) {
                val activityString = getString(activity.stringResource)
                mHumanActivityDesc.text=String.format("%s: %s", DATE_FORMAT.format(date),activityString)
                mHumanActivityImage.setImageResource(activity.imageResource)
            } else {
                val activityString = getString(R.string.activityRecognition_adultNotInCar)
                mHumanActivityDesc.text=String.format("%s: %s", DATE_FORMAT.format(date),activityString)
                mHumanActivityImage.setImageResource(R.drawable.activity_adult_not_in_car)
            }
        } else {
            val activityString = getString(activity.stringResource)
            mHumanActivityDesc.text=String.format("%s: %s", DATE_FORMAT.format(date),activityString)
            mHumanActivityImage.setImageResource(activity.imageResource)                  
        }
    }

    private fun updateAudioUI(it: AudioClassificationInfo) {
        val scene = it.classification.value
        val algorithm = it.algorithm.value
        val dateString = DATE_FORMAT.format(Date())
        
        //Audio Classification Card
        if(algorithm.toInt()==1) {
            when (scene) {
                AudioClassType.AscOff -> {
                    mAudioSceneStatusText.setText(R.string.algorithm_paused)
                }
                AudioClassType.AscOn -> {
                    mAudioSceneStatusText.setText(R.string.algorithm_running)
                }
                AudioClassType.BabyIsCrying -> {
                    mAudioSceneView.visibility = View.VISIBLE
                    val sceneString = getString(scene.stringResource)
                    mAudioSceneDesc.text=String.format("%s: %s",dateString,sceneString)
                    mAudioSceneImage.setImageResource(scene.imageResource)
                }
                else -> {
                    mAudioSceneView.visibility = View.VISIBLE
                    val sceneString = getString(R.string.audio_baby_not_crying)
                    mAudioSceneDesc.text=String.format("%s: %s",dateString,sceneString)
                    mAudioSceneImage.setImageResource(R.drawable.audio_scene_babynotcrying)
                }
            }
        } else {
            when (scene) {
                AudioClassType.AscOff -> {
                    mAudioSceneStatusText.setText(R.string.algorithm_paused)
                }

                AudioClassType.AscOn -> {
                    mAudioSceneStatusText.setText(R.string.algorithm_running)
                }
                else -> {
                    mAudioSceneView.visibility = View.VISIBLE
                    val sceneString = getString(scene.stringResource)
                    mAudioSceneDesc.text=String.format("%s: %s",dateString,sceneString)
                    mAudioSceneImage.setImageResource(scene.imageResource)
                }
            }
        }
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

internal val AudioClassType.imageResource:Int
    get() {
        return when(this){
            AudioClassType.Unknown -> R.drawable.audio_scene_unkown
            AudioClassType.Indoor -> R.drawable.audio_scene_inside
            AudioClassType.Outdoor -> R.drawable.audio_scene_outside
            AudioClassType.InVehicle -> R.drawable.audio_scene_invehicle
            AudioClassType.BabyIsCrying -> R.drawable.audio_scene_babycrying
            AudioClassType.AscOff -> R.drawable.ic_pause
            AudioClassType.AscOn ->  R.drawable.ic_play_arrow
            AudioClassType.Error -> R.drawable.ic_error
        }
    }


internal val AudioClassType.stringResource:Int
    get() {
        return when(this){
            AudioClassType.Unknown -> R.string.audio_scene_unknown
            AudioClassType.Indoor -> R.string.audio_scene_indoor
            AudioClassType.Outdoor -> R.string.audio_scene_outdoor
            AudioClassType.InVehicle -> R.string.audio_scene_inVehicle
            AudioClassType.BabyIsCrying ->  R.string.audio_baby_crying
            AudioClassType.AscOff ->  R.string.audio_scene_off
            AudioClassType.AscOn ->  R.string.audio_scene_on
            AudioClassType.Error -> R.string.audio_scene_error
        }
    }

internal val ActivityType.imageResource: Int
    get() {
        return when(this){
            ActivityType.NoActivity -> R.drawable.activity_unkown
            ActivityType.Stationary -> R.drawable.activity_stationary
            ActivityType.Walking ->  R.drawable.activity_walking
            ActivityType.FastWalking -> R.drawable.activity_fastwalking
            ActivityType.Jogging -> R.drawable.activity_jogging
            ActivityType.Biking -> R.drawable.activity_biking
            ActivityType.Driving -> R.drawable.activity_driving
            ActivityType.Stairs -> R.drawable.activity_stairs
            ActivityType.AdultInCar -> R.drawable.activity_adult_in_car
            ActivityType.Error -> R.drawable.activity_unkown
        }
    }

internal val ActivityType.stringResource: Int
    get() {
        return when(this){
            ActivityType.NoActivity -> R.string.activityRecognition_unknownImageDesc
            ActivityType.Stationary -> R.string.activityRecognition_stationaryImageDesc
            ActivityType.Walking ->  R.string.activityRecognition_walkingImageDesc
            ActivityType.FastWalking -> R.string.activityRecognition_fastWalkingImageDesc
            ActivityType.Jogging -> R.string.activityRecognition_joggingImageDesc
            ActivityType.Biking -> R.string.activityRecognition_bikingImageDesc
            ActivityType.Driving -> R.string.activityRecognition_drivingImageDesc
            ActivityType.Stairs -> R.string.activityRecognition_stairsImageDesc
            ActivityType.AdultInCar -> R.string.activityRecognition_adultInCar
            ActivityType.Error -> R.string.activityRecognition_unknownImageDesc
        }
    }
