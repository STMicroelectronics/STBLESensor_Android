/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.neai_extrapolation

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material.Snackbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.st.blue_sdk.features.extended.neai_extrapolation.model.NeaiExtrapolationData
import com.st.blue_sdk.features.extended.neai_extrapolation.model.PhaseType
import com.st.blue_sdk.features.extended.neai_extrapolation.model.StateType
import com.st.core.ARG_NODE_ID
import dagger.hilt.android.AndroidEntryPoint
import com.st.neai_extrapolation.databinding.NeaiExtrapolationFragmentBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NeaiExtrapolationFragment : Fragment() {

    private val viewModel: NeaiExtrapolationViewModel by viewModels()
    private lateinit var binding: NeaiExtrapolationFragmentBinding
    private lateinit var nodeId: String

    private var gifIsRunning = false
    private var expandedNeaiCommands = true
    private var expandedNeaiLibrary = false

    private var generalPhaseStatus = PhaseType.Idle
    private var stub = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = NeaiExtrapolationFragmentBinding.inflate(inflater, container, false)

        expandOrHideSections()
        handleStartStopButtons()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.neaiStubButton.setOnClickListener {
            showDialogForRemovingDemoMode()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.extrapolationData.collect {
                    it.extrapolation?.let { extrapolation ->
                        updateNeaiExtractionView(extrapolation)
                    }
                }
            }
        }
    }

    private fun updateNeaiExtractionView(extrapolation: NeaiExtrapolationData) {
        val phase = extrapolation.phase
        val state = extrapolation.state
        val target = extrapolation.target
        val unit = extrapolation.unit
        val stub = extrapolation.stub

        setPhaseUI(phase)
        setStateUI(state)
        setTargetUI(target, unit)
        setStubUI(stub)
    }

    private fun setPhaseUI(phase: PhaseType) {
        if (phase == PhaseType.Null) {
            binding.tvPhase.text = resources.getString(R.string.st_neai_extrapolation_text_noValue)
        } else {
            binding.tvPhase.text = when (phase) {
                PhaseType.Idle -> resources.getString(R.string.st_neai_extrapolation_aiengine_phase_idle)
                PhaseType.Extrapolation -> resources.getString(R.string.st_neai_extrapolation_aiengine_phase_extrapolation)
                PhaseType.Busy -> resources.getString(R.string.st_neai_extrapolation_aiengine_phase_busy)
                else -> resources.getString(R.string.st_neai_extrapolation_aiengine_phase_null)
            }
            updateUiBasedOnPhase(phase)
        }
    }

    private fun updateUiBasedOnPhase(phase: PhaseType) {
        generalPhaseStatus = phase

        when (phase) {
            PhaseType.Idle -> {
                stopAnimatingLogo()
                binding.startButton.isEnabled = true
                binding.stopButton.isEnabled = false
                binding.resourceBusyTx.visibility = View.GONE
            }

            PhaseType.Extrapolation -> {
                startAnimatingLogo()
                binding.startButton.isEnabled = false
                binding.stopButton.isEnabled = true
                binding.resourceBusyTx.visibility = View.GONE
            }

            PhaseType.Busy -> {
                stopAnimatingLogo()
                binding.startButton.isEnabled = true
                binding.stopButton.isEnabled = false
                binding.resourceBusyTx.visibility = View.VISIBLE
            }

            PhaseType.Null -> {}
        }
    }

    private fun setStateUI(state: StateType?) {
        if (state != null) {
            if (state == StateType.Null) {
                binding.tvState.text =
                    resources.getString(R.string.st_neai_extrapolation_text_noValue)
            } else {
                binding.tvState.text = when (state) {
                    StateType.Ok -> resources.getString(R.string.st_neai_extrapolation_aiengine_state_ok)
                    StateType.Init_Not_Called -> resources.getString(R.string.st_neai_extrapolation_aiengine_state_initNotCalled)
                    StateType.Board_Error -> resources.getString(R.string.st_neai_extrapolation_aiengine_state_boardError)
                    StateType.Knowledge_Error -> resources.getString(R.string.st_neai_extrapolation_aiengine_state_knowledgeError)
                    StateType.Not_Enough_Learning -> resources.getString(R.string.st_neai_extrapolation_aiengine_state_notEnoughLearning)
                    StateType.Minimal_Learning_done -> resources.getString(R.string.st_neai_extrapolation_aiengine_state_minimalLearningDone)
                    StateType.Unknown_Error -> resources.getString(R.string.st_neai_extrapolation_aiengine_state_unknownError)
                    else -> resources.getString(R.string.st_neai_extrapolation_aiengine_state_null)
                }
            }
        } else {
            binding.tvState.text =
                resources.getString(R.string.st_neai_extrapolation_text_noValue)
        }
    }

    private fun setTargetUI(target: Float?, unit: String?) {
        val text = if (unit != null) {
            "${target ?: ""} [$unit]"
        } else {
            target?.toString() ?: ""
        }
        binding.tvTarget.text = text
    }

    private fun setStubUI(stub: Boolean) {
        this.stub = stub
        when (stub) {
            true -> binding.neaiStubButton.visibility = View.VISIBLE
            false -> binding.neaiStubButton.visibility = View.GONE
        }
    }

    private fun handleStartStopButtons() {
        binding.startButton.setOnClickListener {
            when (generalPhaseStatus) {
                PhaseType.Idle -> viewModel.writeStartCommand(nodeId)
                PhaseType.Extrapolation -> {}
                PhaseType.Busy -> askIfForceStartCommand()
                PhaseType.Null -> {}
            }
        }

        binding.stopButton.setOnClickListener {
            when (generalPhaseStatus) {
                PhaseType.Idle -> {}
                PhaseType.Extrapolation -> viewModel.writeStopCommand(nodeId)
                PhaseType.Busy -> {}
                PhaseType.Null -> {}
            }
        }
    }


    private fun askIfForceStartCommand() {
        val dialog = AlertDialog.Builder(context)
            .setTitle("WARNING!")
            .setMessage("Resources are busy with another process. Do you want to stop it and start NEAI-Extrapolation anyway?")
            .setPositiveButton("Start") { dialog, _ ->
                viewModel.writeStartCommand(nodeId)
                dialog.dismiss()

                Toast.makeText(
                    context,
                    "Start Extrapolation",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }


    private fun showDialogForRemovingDemoMode() {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Demo Mode")
            .setMessage("This is a demo extrapolation library, its results have no sense. To easily develop your own real AI libraries, use the free ST tool:\n NanoEdge AI Studio.")
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
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
                binding.neaiStubButton.visibility = View.GONE
                false
            } else {
                binding.neaiLibraryExpandButton.setImageResource(R.drawable.ic_arrow_up)
                binding.neaiStubButton.visibility = View.VISIBLE
                true
            }
        }
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
            binding.ivNeaiLogo.setImageResource(R.drawable.neai_icon)
            gifIsRunning = false
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
