/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.neai_classification

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.st.blue_sdk.features.extended.neai_class_classification.*
import com.st.core.ARG_NODE_ID
import com.st.neai_classification.databinding.NeaiClassificationFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NeaiClassificationFragment : Fragment() {

    private val viewModel: NeaiClassificationViewModel by viewModels()

    private lateinit var binding: NeaiClassificationFragmentBinding

    private lateinit var nodeId: String

    private var gifIsRunning = false

    private var mNeaiLogo: ImageView? = null

    private var mCurrentPhase = PhaseType.Idle
    private lateinit var mPhaseTv: TextView
    private lateinit var mStateTv: TextView

    private lateinit var mMostProbableClassTv: TextView
    private lateinit var mMostProbableClassNumberTv: TextView
    private lateinit var nClassesDetails: LinearLayout
    private lateinit var nClassesTv: TextView
    private lateinit var nClassesTitle: TextView
    private lateinit var mClassOutlier: TextView
    private lateinit var mClassOutlierResult: TextView
    private lateinit var mLinearLayoutNClasses: LinearLayout

    private var mClassesArrayLinearLayouts = ArrayList<LinearLayout>()
    private var mClassesArrayProgressBars = ArrayList<ProgressBar>()
    private var mClassesArrayTextViews = ArrayList<TextView>()

    private lateinit var mShowAllClassesSwitch: SwitchCompat

    private lateinit var mResourceBusyTv: TextView

    private lateinit var mStartClassification: MaterialButton
    private lateinit var mStopClassification: MaterialButton


    private fun setClassesDetails(classProb: IntArray, mostProbClass: Short?) {
        val mostProb = mostProbClass ?: 0
        for (elem in classProb.indices) {
            if (classProb[elem] != NeaiClassClassification.CLASS_PROB_ESCAPE_CODE) {
                mClassesArrayLinearLayouts[elem].visibility = View.VISIBLE
                val string = "CL ${elem + 1} (${classProb[elem]} %):"
                mClassesArrayTextViews[elem].text = string
                if (elem == (mostProb - 1)) {
                    mClassesArrayTextViews[elem].setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            com.st.ui.R.color.fabColorPrimaryDark
                        )
                    )
                } else {
                    mClassesArrayTextViews[elem].setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            com.st.ui.R.color.labelPlotContrast
                        )
                    )
                }
                mClassesArrayProgressBars[elem].setProgress(classProb[elem], false)
            } else {
                mClassesArrayLinearLayouts[elem].visibility = View.GONE
            }
        }

        for (elem in classProb.size until mClassesArrayLinearLayouts.size) {
            mClassesArrayLinearLayouts[elem].visibility = View.GONE
        }
    }

    private fun setMostProbClass(mostProbClass: Short?) {
        val mostProb = (mostProbClass ?: 0).toInt()
        val textString: String =
            if (mostProb != 0) {
                "$mostProbClass"
            } else {
                resources.getString(R.string.st_neaiClassification_text_unknown)
            }
        mMostProbableClassNumberTv.text = textString
//        if(!mShowAllClassesSwitch.isChecked) {
//            mMostProbableClassNumberTv.startAnimation(mScaleUpAnim)
//        }
    }

    private fun setTitleStatePhase(
        mode: ModeType,
        state: StateType?,
        phase: PhaseType
    ) {
        if (phase == PhaseType.Null) {
            mPhaseTv.text = resources.getString(R.string.st_neaiClassification_text_noValue)
        } else {
            mPhaseTv.text = when(phase) {
                PhaseType.Idle -> resources.getString(R.string.st_neaiClassification_phase_idle)
                PhaseType.Classification -> resources.getString(R.string.st_neaiClassification_phase_classification)
                PhaseType.Busy -> resources.getString(R.string.st_neaiClassification_phase_busy)
                PhaseType.Null -> resources.getString(R.string.st_neaiClassification_phase_null)
            }

            if (phase == PhaseType.Idle) {
                mShowAllClassesSwitch.isEnabled = false
                mShowAllClassesSwitch.visibility = View.GONE
                mStartClassification.isEnabled = true
                mStopClassification.isEnabled = false
                if (mode == ModeType.One_Class) {
                    mClassOutlier.visibility = View.GONE
                    mClassOutlierResult.visibility = View.GONE
                }
                mClassOutlierResult.text = resources.getString(R.string.st_neaiClassification_text_noValue)
                mResourceBusyTv.visibility = View.GONE
                hideClassesDetail()
                stopAnimatingLogo()
            } else if (phase == PhaseType.Classification) {
                mShowAllClassesSwitch.isEnabled = true
                mShowAllClassesSwitch.visibility = View.VISIBLE
                mStartClassification.isEnabled = false
                mStopClassification.isEnabled = true
                if (mode == ModeType.One_Class) {
                    mClassOutlier.visibility = View.VISIBLE
                    mClassOutlierResult.visibility = View.VISIBLE
                }
                mResourceBusyTv.visibility = View.GONE
                updateClassesDetail()
                startAnimatingLogo()
            } else if (phase == PhaseType.Busy) {
                mShowAllClassesSwitch.isEnabled = false
                mShowAllClassesSwitch.visibility = View.GONE
                mStartClassification.isEnabled = true
                mStopClassification.isEnabled = false
                if (mode == ModeType.One_Class) {
                    mClassOutlier.visibility = View.GONE
                    mClassOutlierResult.visibility = View.GONE
                }
                mClassOutlierResult.text = resources.getString(R.string.st_neaiClassification_text_noValue)
                //mResourceBusyTv.visibility = View.VISIBLE
                hideClassesDetail()
                stopAnimatingLogo()
            }
        }

        if (state != null) {
            if (state == StateType.Null) {
                mStateTv.text = resources.getString(R.string.st_neaiClassification_text_noValue)
            } else {
                mStateTv.text = when(state) {
                    StateType.Ok -> resources.getString(R.string.st_neaiClassification_state_ok)
                    StateType.Init_Not_Called -> resources.getString(R.string.st_neaiClassification_state_initNotCalled)
                    StateType.Board_Error -> resources.getString(R.string.st_neaiClassification_state_boardError)
                    StateType.Knowledge_Error -> resources.getString(R.string.st_neaiClassification_state_knowledgeError)
                    StateType.Not_Enough_Learning -> resources.getString(R.string.st_neaiClassification_state_notEnoughLearning)
                    StateType.Minimal_Learning_done -> resources.getString(R.string.st_neaiClassification_state_minimalLearningDone)
                    StateType.Unknown_Error -> resources.getString(R.string.st_neaiClassification_state_unknownError)
                    StateType.Null -> resources.getString(R.string.st_neaiClassification_state_null)
                }
            }
        } else {
            mStateTv.text = resources.getString(R.string.st_neaiClassification_text_noValue)
        }

        val stringTitle =
            when (mode) {
                ModeType.One_Class -> {
                    mLinearLayoutNClasses.visibility = View.GONE
                    resources.getString(R.string.st_neaiClassification_title_oneClass)
                }

                ModeType.N_Class -> {
                    mLinearLayoutNClasses.visibility = View.VISIBLE
                    resources.getString(R.string.st_neaiClassification_title_nClass)
                }

                else -> {
                    resources.getString(R.string.st_neaiClassification_title_wrongClass)
                }
            }
        nClassesTitle.text = stringTitle

    }

    private fun setOneClassOutlier(classProb: IntArray) {
        if (classProb.size == 1) {
            //1Class
            val stringOneClass =
                if (classProb[0] == 0) {
                    resources.getString(R.string.st_neaiClassification_outlier_no)
                } else {
                    if (classProb[0] != NeaiClassClassification.CLASS_PROB_ESCAPE_CODE) {
                        resources.getString(R.string.st_neaiClassification_outlier_yes)
                    } else {
                        resources.getString(R.string.st_neaiClassification_text_noValue)
                    }
                }
            mClassOutlierResult.text = stringOneClass
        }
    }

    private fun startAnimatingLogo() {
        if (!gifIsRunning) {
            when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    loadGif(R.drawable.neai_logo_dark)
                }

                Configuration.UI_MODE_NIGHT_NO -> {
                    loadGif(R.drawable.neai_logo_white)
                }
            }
            gifIsRunning = true
        }
    }

    private fun loadGif(neaiGif: Int) {
        mNeaiLogo?.let {
            Glide.with(this)
                .asGif()
                .load(neaiGif)
                .into(it)
        }
    }

    private fun stopAnimatingLogo() {
        if (gifIsRunning) {
            mNeaiLogo?.setImageResource(R.drawable.neai_icon)
            gifIsRunning = false
        }
    }


    private fun updateClassesDetail() {
        if (mShowAllClassesSwitch.isChecked) {
            mMostProbableClassNumberTv.visibility = View.GONE
            mMostProbableClassTv.visibility = View.GONE
            nClassesTv.visibility = View.VISIBLE
            nClassesDetails.visibility = View.VISIBLE
        } else {
            nClassesDetails.visibility = View.GONE
            nClassesTv.visibility = View.GONE
            mMostProbableClassTv.visibility = View.VISIBLE
            mMostProbableClassNumberTv.visibility = View.VISIBLE
        }
    }

    private fun hideClassesDetail() {
        nClassesDetails.visibility = View.GONE
        nClassesTv.visibility = View.GONE
        mMostProbableClassTv.visibility = View.GONE
        mMostProbableClassNumberTv.visibility = View.GONE
    }

    private fun expandOrHideSections() {
        /** NEAI Commands Expand ImageButton */
        binding.neaiCommandsExpandButtonClassification.setOnClickListener {
            if (binding.neaiCommandsLayoutClassification.visibility == View.VISIBLE) {
                binding.neaiCommandsExpandButtonClassification.setImageResource(R.drawable.ic_arrow_down)
                binding.neaiCommandsLayoutClassification.visibility = View.GONE
            } else {
                binding.neaiCommandsExpandButtonClassification.setImageResource(R.drawable.ic_arrow_up)
                binding.neaiCommandsLayoutClassification.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NeaiClassificationFragmentBinding.inflate(inflater, container, false)
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        expandOrHideSections()

        mNeaiLogo = binding.ivNeaiLogoClassification
        mPhaseTv = binding.tvPhaseClassification
        mStateTv = binding.tvStateClassification

        mMostProbableClassTv = binding.neaiMostProbaleClassClassification
        mMostProbableClassNumberTv = binding.neaiMostProbaleClassNumberClassification

        nClassesTitle = binding.neaiTitleClassification
        mClassOutlier = binding.neaiClassOutlierTextview
        mClassOutlierResult = binding.neaiClassOutlierResultTextview

        //Load the animations
        //mScaleUpAnim = AnimationUtils.loadAnimation(activity,R.anim.scale_up)

        mLinearLayoutNClasses = binding.neaiLinearlayoutNclassesClassification

        //Find all the Classes' LinearLayout
        var linearLayout: LinearLayout = binding.neaiClass1LinearlayoutClassification
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = binding.neaiClass2LinearlayoutClassification
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = binding.neaiClass3LinearlayoutClassification
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = binding.neaiClass4LinearlayoutClassification
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = binding.neaiClass5LinearlayoutClassification
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = binding.neaiClass6LinearlayoutClassification
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = binding.neaiClass7LinearlayoutClassification
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = binding.neaiClass8LinearlayoutClassification
        mClassesArrayLinearLayouts.add(linearLayout)

        //Find all the Classes' TextView
        var textView: TextView = binding.neaiClass1TextviewClassification
        mClassesArrayTextViews.add(textView)
        textView = binding.neaiClass2TextviewClassification
        mClassesArrayTextViews.add(textView)
        textView = binding.neaiClass3TextviewClassification
        mClassesArrayTextViews.add(textView)
        textView = binding.neaiClass4TextviewClassification
        mClassesArrayTextViews.add(textView)
        textView = binding.neaiClass5TextviewClassification
        mClassesArrayTextViews.add(textView)
        textView = binding.neaiClass6TextviewClassification
        mClassesArrayTextViews.add(textView)
        textView = binding.neaiClass7TextviewClassification
        mClassesArrayTextViews.add(textView)
        textView = binding.neaiClass8TextviewClassification
        mClassesArrayTextViews.add(textView)

        //Find all the Classes' ProgressBar
        var progressBar: ProgressBar = binding.neaiClass1ProgressbarClassification
        mClassesArrayProgressBars.add(progressBar)
        progressBar = binding.neaiClass2ProgressbarClassification
        mClassesArrayProgressBars.add(progressBar)
        progressBar = binding.neaiClass3ProgressbarClassification
        mClassesArrayProgressBars.add(progressBar)
        progressBar = binding.neaiClass4ProgressbarClassification
        mClassesArrayProgressBars.add(progressBar)
        progressBar = binding.neaiClass5ProgressbarClassification
        mClassesArrayProgressBars.add(progressBar)
        progressBar = binding.neaiClass6ProgressbarClassification
        mClassesArrayProgressBars.add(progressBar)
        progressBar = binding.neaiClass7ProgressbarClassification
        mClassesArrayProgressBars.add(progressBar)
        progressBar = binding.neaiClass8ProgressbarClassification
        mClassesArrayProgressBars.add(progressBar)


        mShowAllClassesSwitch = binding.showAllClassesCbClassification

        nClassesDetails = binding.neaiClassesLinearlayoutClassification
        nClassesTv = binding.neaiClassesProbabilityTextview

        mShowAllClassesSwitch.setOnClickListener {
            updateClassesDetail()
        }

        mResourceBusyTv = binding.neaiResourceBusyTx

        mStartClassification = binding.startButtonClassification
        mStopClassification = binding.stopButtonClassification

        mStartClassification.setOnClickListener {
            if (mCurrentPhase != PhaseType.Busy) {
                viewModel.writeStartClassificationCommand(nodeId)
                it.isEnabled = false
                mStopClassification.isEnabled = true
                Toast.makeText(
                    context,
                    resources.getString(R.string.st_neaiClassification_text_startClassificationMessage),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //if the phase is == Busy... open dialog for asking what to do..
                askIfForceStartCommand()
            }
        }

        mStopClassification.setOnClickListener {
            viewModel.writeStopClassificationCommand(nodeId)
            it.isEnabled = false
            mStartClassification.isEnabled = true
            Toast.makeText(
                context,
                resources.getString(R.string.st_neaiClassification_text_stopClassificationMessage),
                Toast.LENGTH_SHORT
            ).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.classificationData.collect {
                    updateNeaiClassificationView(it)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
    }

    private fun updateNeaiClassificationView(neaiClassificationInfo: NeaiClassClassificationInfo) {
        val mode = neaiClassificationInfo.mode.value
        val phase = neaiClassificationInfo.phase.value
        val state = neaiClassificationInfo.state?.value
        val classNum = neaiClassificationInfo.classNum?.value
        val mostProbClass = neaiClassificationInfo.classMajorProb?.value

        mCurrentPhase = phase

        if (classNum != null) {
            val classProb = IntArray(classNum.toInt())

            for (i in 0 until classNum) {
                classProb[i] = neaiClassificationInfo.classProb?.get(i)?.value?.toInt()!!
            }

            setOneClassOutlier(classProb)
            setClassesDetails(classProb, mostProbClass)
        }


        setTitleStatePhase(mode, state, phase)
        setMostProbClass(mostProbClass)
    }

    private fun askIfForceStartCommand() {

        val dialog = AlertDialog.Builder(context)
            .setTitle(resources.getString(R.string.st_neaiClassification_alert_title))
            .setMessage(resources.getString(R.string.st_neaiClassification_alert_message))
            .setPositiveButton(resources.getString(R.string.st_neaiClassification_alert_startBtnLabel)) { dialog, _ ->
                viewModel.writeStartClassificationCommand(nodeId)
                dialog.dismiss()
                Toast.makeText(
                    context,
                    resources.getString(R.string.st_neaiClassification_text_startClassificationMessage),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(resources.getString(R.string.st_neaiClassification_alert_cancelBtnLabel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
