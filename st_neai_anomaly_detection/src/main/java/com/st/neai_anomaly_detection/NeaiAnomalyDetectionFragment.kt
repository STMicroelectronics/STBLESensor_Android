/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.neai_anomaly_detection

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.st.blue_sdk.features.extended.neai_anomaly_detection.NeaiAnomalyDetectionInfo
import com.st.blue_sdk.features.extended.neai_anomaly_detection.PhaseType
import com.st.blue_sdk.features.extended.neai_anomaly_detection.StateType
import com.st.blue_sdk.features.extended.neai_anomaly_detection.StatusType
import com.st.core.ARG_NODE_ID
import com.st.neai_anomaly_detection.databinding.NeaiAnomalyDetectionFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NeaiAnomalyDetectionFragment : Fragment() {

    private val viewModel: NeaiAnomalyDetectionViewModel by viewModels()

    private lateinit var binding: NeaiAnomalyDetectionFragmentBinding

    private lateinit var nodeId: String

    private var generalPhaseStatus = PhaseType.Idle
    private var expandedNeaiCommands = true
    private var expandedNeaiLibrary = false

    private var gifIsRunning = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NeaiAnomalyDetectionFragmentBinding.inflate(inflater, container, false)
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        expandOrHideSections()
        handleStartStopButton()
        handleResetKnowledgeButton()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.anomalyDetectionData.collect {
                    updateNeaiAnomalyDetectionView(it)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
    }

    private fun updateNeaiAnomalyDetectionView(neaiAnomalyInfo: NeaiAnomalyDetectionInfo) {
        val phase = neaiAnomalyInfo.phase.value
        val state = neaiAnomalyInfo.state.value
        val phaseProgress = neaiAnomalyInfo.phaseProgress.value.toInt()
        val status = neaiAnomalyInfo.status.value
        val similarity = neaiAnomalyInfo.similarity.value.toInt()

        setPhaseUI(phase)
        setStateUI(state)
        setProgressUI(phaseProgress)
        setStatusUI(status)
        setSimilarityUI(similarity)
    }

    private fun setPhaseUI(phase: PhaseType) {
        if (phase == PhaseType.Null) {
            binding.tvPhase.text = resources.getString(R.string.st_neaiAnomalyDetection_text_noValue)
        } else {
            binding.tvPhase.text = when(phase) {
                PhaseType.Idle -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_phase_idle)
                PhaseType.Learning -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_phase_learning)
                PhaseType.Detection -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_phase_detection)
                PhaseType.Idle_Trained -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_phase_idleTrained)
                PhaseType.Busy -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_phase_busy)
                else -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_phase_null)
            }
            updateUiBasedOnPhase(phase)
        }
    }


    private fun setStateUI(state: StateType) {
        if (state == StateType.Null) {
            binding.tvState.text = resources.getString(R.string.st_neaiAnomalyDetection_text_noValue)
        } else {
            binding.tvState.text = when(state) {
                StateType.Ok -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_state_ok)
                StateType.Init_Not_Called -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_state_initNotCalled)
                StateType.Board_Error -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_state_boardError)
                StateType.Knowledge_Error -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_state_knowledgeError)
                StateType.Not_Enough_Learning -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_state_notEnoughLearning)
                StateType.Minimal_Learning_done -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_state_minimalLearningDone)
                StateType.Unknown_Error -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_state_unknownError)
                else -> resources.getString(R.string.st_neaiAnomalyDetection_aiengine_state_null)
            }
        }
    }

    private fun setProgressUI(phaseProgress: Int) {
        if (phaseProgress == 255) {
            binding.tvProgress.text = resources.getString(R.string.st_neaiAnomalyDetection_text_noValue)
            binding.pbProgrss.visibility = View.GONE
        } else {
            val tvProgressString = "$phaseProgress%"
            binding.tvProgress.text = tvProgressString
            binding.pbProgrss.visibility = View.VISIBLE
            binding.pbProgrss.setProgress(phaseProgress, true)
        }
    }

    private fun setStatusUI(status: StatusType) {
        when (status) {
            StatusType.Null -> {
                binding.tvStatus.text = resources.getString(R.string.st_neaiAnomalyDetection_text_noValue)
            }

            StatusType.Anomaly -> {
                binding.tvStatus.text = resources.getString(R.string.st_neaiAnomalyDetection_results_status_anomaly)
                binding.neaiSignalStatusImageView.setImageResource(R.drawable.predictive_status_warnings)
            }

            StatusType.Normal -> {
                binding.tvStatus.text = resources.getString(R.string.st_neaiAnomalyDetection_results_status_normal)
                binding.neaiSignalStatusImageView.setImageResource(R.drawable.predictive_status_good)
            }
        }
    }

    private fun setSimilarityUI(similarity: Int) {
        if (similarity == 255) {
            binding.tvSimilarity.text = resources.getString(R.string.st_neaiAnomalyDetection_text_noValue)
        } else {
            binding.tvSimilarity.text = similarity.toString()
        }
    }


    private fun expandOrHideSections() {

        /** Neai Commands Expand ImageButton */
        binding.neaiCommandsExpandButton.setOnClickListener {
            expandedNeaiCommands = if (expandedNeaiCommands) {
                binding.neaiCommandsExpandButton.setImageResource(R.drawable.ic_arrow_down)
                binding.neaiCommandsLayout.visibility = View.GONE
                false
            } else {
                binding.neaiCommandsExpandButton.setImageResource(R.drawable.ic_arrow_up)
                binding.neaiCommandsLayout.visibility = View.VISIBLE
                true
            }
        }

        /** Neai Library Expand ImageButton */
        binding.neaiLibraryExpandButton.setOnClickListener {
            expandedNeaiLibrary = if (expandedNeaiLibrary) {
                binding.neaiLibraryExpandButton.setImageResource(R.drawable.ic_arrow_down)
                binding.neaiLibraryLayout.visibility = View.GONE
                false
            } else {
                binding.neaiLibraryExpandButton.setImageResource(R.drawable.ic_arrow_up)
                binding.neaiLibraryLayout.visibility = View.VISIBLE
                true
            }
        }
    }

    private fun handleResetKnowledgeButton() {
        binding.resetKnowledgeButton.setOnClickListener {
            viewModel.writeResetLearningCommand(nodeId)
            Toast.makeText(context, "Reset DONE.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleStartStopButton() {
        binding.startStopButton.setOnClickListener {
            if (generalPhaseStatus != PhaseType.Busy) {
                if (generalPhaseStatus == PhaseType.Learning ||
                    generalPhaseStatus == PhaseType.Detection
                ) {
                    viewModel.writeStopCommand(nodeId)
                } else {
                    if (binding.learningDetectingCb.isChecked) {
                        viewModel.writeDetectionCommand(nodeId)
                    } else {
                        viewModel.writeLearningCommand(nodeId)
                    }
                }
            } else {
                //if the phase is == Busy... open dialog for asking what to do..
                askIfForceStartCommand()
            }
        }
    }

    private fun askIfForceStartCommand() {

        val dialog = AlertDialog.Builder(context)
            .setTitle("WARNING!")
            .setMessage("Resources are busy with another process. Do you want to stop it and start NEAI-Anomaly Detection anyway?")
            .setPositiveButton("Start") { dialog, _ ->
                if (binding.learningDetectingCb.isChecked) {
                    viewModel.writeDetectionCommand(nodeId)
                } else {
                    viewModel.writeLearningCommand(nodeId)
                }
                dialog.dismiss()

                if (binding.learningDetectingCb.isChecked) {
                    Toast.makeText(
                        context,
                        "Start Detection",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Start Learning",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun startAnimatingLogo() {
        if (!gifIsRunning) {
            loadGif(R.drawable.neai_logo_white)
            gifIsRunning = true
        }
    }

    private fun loadGif(neaiGif: Int) {
        binding.ivNeaiLogo.let {
            Glide.with(this)
                .asGif()
                .load(neaiGif)
                .into(it)
        }
    }

    private fun stopAnimatingLogo() {
        if (gifIsRunning) {
            binding.ivNeaiLogo.setImageResource(R.drawable.neai_logo)
            gifIsRunning = false
        }
    }

    private fun updateUiBasedOnPhase(phase: PhaseType) {
        generalPhaseStatus = phase

        if (phase == PhaseType.Idle) {
            stopAnimatingLogo()
            binding.startStopButton.text = resources.getString(R.string.st_neaiAnomalyDetection_action_start)
            binding.startStopButton.isEnabled = true
            binding.resetKnowledgeButton.isEnabled = false
            binding.resetKnowledgeButton.alpha = 0.4F
            binding.learningDetectingCb.isChecked = false
            binding.learningDetectingCb.isEnabled = true
            binding.neaiSignalStatusImageView.visibility = View.INVISIBLE
            binding.resourceBusyTx.visibility = View.GONE
        } else if (phase == PhaseType.Idle_Trained) {
            stopAnimatingLogo()
            binding.startStopButton.text = resources.getString(R.string.st_neaiAnomalyDetection_action_start)
            binding.startStopButton.isEnabled = true
            binding.resetKnowledgeButton.isEnabled = true
            binding.resetKnowledgeButton.alpha = 1.0F
            binding.learningDetectingCb.isChecked = true
            binding.learningDetectingCb.isEnabled = true
            binding.neaiSignalStatusImageView.visibility = View.INVISIBLE
            binding.resourceBusyTx.visibility = View.GONE
        } else if (phase == PhaseType.Learning) {
            startAnimatingLogo()
            binding.startStopButton.text = resources.getString(R.string.st_neaiAnomalyDetection_action_stop)
            binding.startStopButton.isEnabled = true
            binding.resetKnowledgeButton.isEnabled = false
            binding.resetKnowledgeButton.alpha = 0.4F
            binding.learningDetectingCb.isChecked = false
            binding.learningDetectingCb.isEnabled = true
            binding.neaiSignalStatusImageView.visibility = View.INVISIBLE
            binding.resourceBusyTx.visibility = View.GONE
        } else if (phase == PhaseType.Detection) {
            startAnimatingLogo()
            binding.startStopButton.text = resources.getString(R.string.st_neaiAnomalyDetection_action_stop)
            binding.startStopButton.isEnabled = true
            binding.resetKnowledgeButton.isEnabled = false
            binding.resetKnowledgeButton.alpha = 0.4F
            binding.learningDetectingCb.isChecked = true
            binding.learningDetectingCb.isEnabled = true
            binding.neaiSignalStatusImageView.visibility = View.VISIBLE
            binding.resourceBusyTx.visibility = View.GONE
        } else if (phase == PhaseType.Busy) {
            stopAnimatingLogo()
            binding.startStopButton.text = resources.getString(R.string.st_neaiAnomalyDetection_action_start)
            binding.startStopButton.isEnabled = true
            binding.resetKnowledgeButton.isEnabled = false
            binding.resetKnowledgeButton.alpha = 0.4F
            binding.learningDetectingCb.isEnabled = true
            binding.neaiSignalStatusImageView.visibility = View.INVISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
