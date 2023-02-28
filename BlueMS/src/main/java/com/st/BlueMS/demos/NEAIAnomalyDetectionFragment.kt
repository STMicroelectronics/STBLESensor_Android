package com.st.BlueMS.demos

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureNEAIAnomalyDetection
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation

@DemoDescriptionAnnotation(
    name = "NEAI Anomaly Detection",
    iconRes = R.drawable.ic_neai_logo,
    demoCategory = ["AI"],
    requireAll = [FeatureNEAIAnomalyDetection::class]
)
class NEAIAnomalyDetectionFragment : BaseDemoFragment() {

    private var generalPhaseStatus: FeatureNEAIAnomalyDetection.PhaseType =
        FeatureNEAIAnomalyDetection.PhaseType.IDLE

    private var mNeaiLogo: ImageView? = null
    private var mPhaseTv: TextView? = null
    private var mStateTv: TextView? = null
    private var mProgressTv: TextView? = null
    private var mProgressPb: ProgressBar? = null
    private var mStatusTv: TextView? = null
    private var mSimilarityTv: TextView? = null
    private var mLearningDetectingSwitch: SwitchCompat? = null
    private var mSignalStatusImage: ImageView? = null

    private var mResourceBusyTv: TextView? = null

    private var mResetKnowledgeButton: MaterialButton? = null
    private var mStartStopButton: MaterialButton? = null

    private var expandedNeaiCommands = true
    private var expandedNeaiLibrary = false

    private val noDataValue: String = "---"

    private var mFeature: FeatureNEAIAnomalyDetection? = null

    private var gifIsRunning = false

    private var mPhase: FeatureNEAIAnomalyDetection.PhaseType =  FeatureNEAIAnomalyDetection.PhaseType.IDLE

    @RequiresApi(Build.VERSION_CODES.N)
    private val featureListener = Feature.FeatureListener { _, sample ->
        val phase = FeatureNEAIAnomalyDetection.getPhaseValue(sample)
        val state = FeatureNEAIAnomalyDetection.getStateValue(sample)
        val phaseProgress = FeatureNEAIAnomalyDetection.getPhaseProgressValue(sample)
        val status = FeatureNEAIAnomalyDetection.getStatusValue(sample)
        val similarity = FeatureNEAIAnomalyDetection.getSimilarityValue(sample)

        mPhase = phase

        updateGui {
            setPhaseUI(phase)
            setStateUI(state)
            setProgressUI(phaseProgress)
            setStatusUI(status)
            setSimilarityUI(similarity)
            //mNeaiADRawValue?.text = "DEBUG RAW DATA\npayload --> phase:$phase - state:$state - phase_progress:$phaseProgress - status:$status - similarity: $similarity"
        }
    }

    private fun setPhaseUI(phase: FeatureNEAIAnomalyDetection.PhaseType) {
        if (phase == FeatureNEAIAnomalyDetection.PhaseType.NULL) {
            mPhaseTv?.text = noDataValue
        } else {
            mPhaseTv?.text = phase.name.replace('_', ' ')
            updateUiBasedOnPhase(phase)
        }
    }

    private fun setStateUI(state: FeatureNEAIAnomalyDetection.StateType) {
        if (state == FeatureNEAIAnomalyDetection.StateType.NULL) {
            mStateTv?.text = noDataValue
        } else {
            mStateTv?.text = state.name.replace('_', ' ')
        }
    }

    private fun setProgressUI(phaseProgress: Int) {
        if (phaseProgress == -1) {
            mProgressTv?.text = noDataValue
            mProgressPb?.visibility = View.GONE
        } else {
            mProgressTv?.text = "$phaseProgress%"
            mProgressPb?.visibility = View.VISIBLE
            mProgressPb?.setProgress(phaseProgress, true)
        }
    }

    private fun setStatusUI(status: FeatureNEAIAnomalyDetection.StatusType) {
        when (status) {
            FeatureNEAIAnomalyDetection.StatusType.NULL -> {
                mStatusTv?.text = noDataValue
            }
            FeatureNEAIAnomalyDetection.StatusType.ANOMALY -> {
                mStatusTv?.text = status.name.replace('_', ' ')
                mSignalStatusImage?.setImageResource(R.drawable.predictive_status_warnings)
            }
            FeatureNEAIAnomalyDetection.StatusType.NORMAL -> {
                mStatusTv?.text = status.name.replace('_', ' ')
                mSignalStatusImage?.setImageResource(R.drawable.predictive_status_good)
            }
        }
    }

    private fun setSimilarityUI(similarity: Int) {
        if (similarity == -1) {
            mSimilarityTv?.text = noDataValue
        } else {
            mSimilarityTv?.text = similarity.toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* Inflate the layout */
        val rootView =
            inflater.inflate(R.layout.fragment_demo_neai_anomaly_detection, container, false)

        expandOrHideSections(rootView)

        mNeaiLogo = rootView.findViewById(R.id.iv_neai_logo)
        mPhaseTv = rootView.findViewById(R.id.tv_phase)
        mStateTv = rootView.findViewById(R.id.tv_state)
        mProgressTv = rootView.findViewById(R.id.tv_progress)
        mProgressPb = rootView.findViewById(R.id.pb_progrss)
        mStatusTv = rootView.findViewById(R.id.tv_status)
        mSimilarityTv = rootView.findViewById(R.id.tv_similarity)
        mLearningDetectingSwitch = rootView.findViewById(R.id.learning_detecting_cb)
        mSignalStatusImage = rootView.findViewById(R.id.neai_signal_status_image_view)

        mResetKnowledgeButton = rootView.findViewById(R.id.reset_knowledge_button)
        mStartStopButton = rootView.findViewById(R.id.start_stop_button)

        mResourceBusyTv = rootView.findViewById(R.id.resource_busy_tx)

        handleStartStopButton()
        handleResetKnowledgeButton()

        return rootView
    }

    override fun enableNeededNotification(node: Node) {
        mFeature = node.getFeature(FeatureNEAIAnomalyDetection::class.java)
        mFeature?.apply {
            addFeatureListener(featureListener)
            enableNotification()
        }
    }

    override fun disableNeedNotification(node: Node) {
        node.getFeature(FeatureNEAIAnomalyDetection::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }
        mFeature = null
    }

    private fun expandOrHideSections(rootView: View) {

        /** Neai Commands Expand ImageButton */
        rootView.findViewById<ImageButton>(R.id.neai_commands_expand_button).setOnClickListener {
            expandedNeaiCommands = if (expandedNeaiCommands) {
                rootView.findViewById<ImageButton>(R.id.neai_commands_expand_button)
                    .setImageResource(R.drawable.ic_arrow_down)
                rootView.findViewById<LinearLayout>(R.id.neai_commands_layout).visibility =
                    View.GONE
                false
            } else {
                rootView.findViewById<ImageButton>(R.id.neai_commands_expand_button)
                    .setImageResource(R.drawable.ic_arrow_up)
                rootView.findViewById<LinearLayout>(R.id.neai_commands_layout).visibility =
                    View.VISIBLE
                true
            }
        }

        /** Neai Library Expand ImageButton */
        rootView.findViewById<ImageButton>(R.id.neai_library_expand_button).setOnClickListener {
            expandedNeaiLibrary = if (expandedNeaiLibrary) {
                rootView.findViewById<ImageButton>(R.id.neai_library_expand_button)
                    .setImageResource(R.drawable.ic_arrow_down)
                rootView.findViewById<LinearLayout>(R.id.neai_library_layout).visibility = View.GONE
                false
            } else {
                rootView.findViewById<ImageButton>(R.id.neai_library_expand_button)
                    .setImageResource(R.drawable.ic_arrow_up)
                rootView.findViewById<LinearLayout>(R.id.neai_library_layout).visibility =
                    View.VISIBLE
                true
            }
        }
    }

    private fun handleResetKnowledgeButton() {
        mResetKnowledgeButton?.setOnClickListener {
            mFeature?.writeResetKnowledgeCommand()
            Toast.makeText(context, "Reset DONE.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleStartStopButton() {
        mStartStopButton?.setOnClickListener {
            if (mLearningDetectingSwitch != null) {
                if(mPhase!= FeatureNEAIAnomalyDetection.PhaseType.BUSY) {
                    if (generalPhaseStatus == FeatureNEAIAnomalyDetection.PhaseType.LEARNING ||
                        generalPhaseStatus == FeatureNEAIAnomalyDetection.PhaseType.DETECTION
                    ) {
                        mFeature?.writeStopCommand()
                    } else {
                        if (mLearningDetectingSwitch!!.isChecked) {
                            mFeature?.writeDetectionCommand()
                        } else {
                            mFeature?.writeLearningCommand()
                        }
                    }
                } else {
                    //if the phase is == Busy... open dialog for asking what to do..
                    askIfForceStartCommand()
                }
            }
        }
    }

    private fun askIfForceStartCommand() {

        val dialog = AlertDialog.Builder(context)
            .setTitle("WARNING!")
            .setMessage("Resources are busy with another process. Do you want to stop it and start NEAI-Anomaly Detection anyway?")
            .setPositiveButton("Start"){ dialog, _ ->
                if (mLearningDetectingSwitch!!.isChecked) {
                    mFeature?.writeDetectionCommand()
                } else {
                    mFeature?.writeLearningCommand()
                }
                dialog.dismiss()

                if (mLearningDetectingSwitch!!.isChecked) {
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
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
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
            mNeaiLogo?.setImageResource(R.drawable.ic_neai_logo)
            gifIsRunning = false
        }
    }

    private fun updateUiBasedOnPhase(phase: FeatureNEAIAnomalyDetection.PhaseType) {
        generalPhaseStatus = phase

        if (phase == FeatureNEAIAnomalyDetection.PhaseType.IDLE) {
            stopAnimatingLogo()
            mStartStopButton?.text = "Start"
            mStartStopButton?.isEnabled = true
            mResetKnowledgeButton?.isEnabled = false
            mResetKnowledgeButton?.alpha = 0.4F
            mLearningDetectingSwitch?.isChecked = false
            mLearningDetectingSwitch?.isEnabled = true
            mSignalStatusImage?.visibility = View.INVISIBLE
            mResourceBusyTv?.visibility= View.GONE
        } else if (phase == FeatureNEAIAnomalyDetection.PhaseType.IDLE_TRAINED) {
            stopAnimatingLogo()
            mStartStopButton?.text = "Start"
            mStartStopButton?.isEnabled = true
            mResetKnowledgeButton?.isEnabled = true
            mResetKnowledgeButton?.alpha = 1.0F
            mLearningDetectingSwitch?.isChecked = true
            mLearningDetectingSwitch?.isEnabled = true
            mSignalStatusImage?.visibility = View.INVISIBLE
            mResourceBusyTv?.visibility= View.GONE
        } else if (phase == FeatureNEAIAnomalyDetection.PhaseType.LEARNING) {
            startAnimatingLogo()
            mStartStopButton?.text = "Stop"
            mStartStopButton?.isEnabled = true
            mResetKnowledgeButton?.isEnabled = false
            mResetKnowledgeButton?.alpha = 0.4F
            mLearningDetectingSwitch?.isChecked = false
            mLearningDetectingSwitch?.isEnabled = true
            mSignalStatusImage?.visibility = View.INVISIBLE
            mResourceBusyTv?.visibility= View.GONE
        } else if (phase == FeatureNEAIAnomalyDetection.PhaseType.DETECTION) {
            startAnimatingLogo()
            mStartStopButton?.text = "Stop"
            mStartStopButton?.isEnabled = true
            mResetKnowledgeButton?.isEnabled = false
            mResetKnowledgeButton?.alpha = 0.4F
            mLearningDetectingSwitch?.isChecked = true
            mLearningDetectingSwitch?.isEnabled = true
            mSignalStatusImage?.visibility = View.VISIBLE
            mResourceBusyTv?.visibility= View.GONE
        } else if (phase == FeatureNEAIAnomalyDetection.PhaseType.BUSY) {
//            mStartStopButton?.text = "---"
//            mStartStopButton?.isEnabled = false
            stopAnimatingLogo()
            mStartStopButton?.text = "Start"
            mStartStopButton?.isEnabled = true
            mResetKnowledgeButton?.isEnabled = false
            mResetKnowledgeButton?.alpha = 0.4F
            //mLearningDetectingSwitch?.isEnabled = false
            mLearningDetectingSwitch?.isEnabled = true
            mSignalStatusImage?.visibility = View.INVISIBLE
            //mResourceBusyTv?.visibility= View.VISIBLE

        }
    }
}