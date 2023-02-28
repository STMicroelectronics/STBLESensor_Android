package com.st.BlueMS.demos


import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureNEAIClassClassification
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.WifSettings
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation

@DemoDescriptionAnnotation(
    name = "NEAI Classification",
    iconRes = R.drawable.ic_neai_logo,
    demoCategory = ["AI"],
    requireAll = [FeatureNEAIClassClassification::class]
)
class NEAIClassClassificationFragment : BaseDemoFragment() {

    private var mFeature: FeatureNEAIClassClassification? = null
    private var gifIsRunning = false

    private var mCurrentPhase: FeatureNEAIClassClassification.PhaseType = FeatureNEAIClassClassification.PhaseType.IDLE

    private var mNeaiLogo: ImageView? = null
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

    private lateinit var mResourceBusyTv : TextView

    private lateinit var mStartClassification: MaterialButton
    private lateinit var mStopClassification: MaterialButton

    //private lateinit var mScaleUpAnim: Animation


    private val featureListener = Feature.FeatureListener { _, sample ->
        val mode = FeatureNEAIClassClassification.getModeValue(sample)
        val phase = FeatureNEAIClassClassification.getPhaseValue(sample)
        val state = FeatureNEAIClassClassification.getStateValue(sample)
        val classNum = FeatureNEAIClassClassification.getClassNumber(sample)
        val mostProbClass = FeatureNEAIClassClassification.getMostProbableClass(sample)

        val classProb = IntArray(classNum)

        for (i in 0 until classNum) {
            classProb[i] = FeatureNEAIClassClassification.getClassProbability(sample, i)
        }

        mCurrentPhase = phase

        updateGui {
            setTitleStatePhase(mode, state, phase)
            setMostProbClass(mostProbClass)
            setOneClassOutlier(classProb)
            setClassesDetails(classProb, mostProbClass)
        }
    }

    private fun setClassesDetails(classProb: IntArray, mostProbClass: Int) {
        for (elem in classProb.indices) {
            if (classProb[elem] != FeatureNEAIClassClassification.CLASS_PROB_ESCAPE_CODE) {
                mClassesArrayLinearLayouts[elem].visibility = View.VISIBLE
                val string = "CL ${elem + 1} (${classProb[elem]} %):"
                mClassesArrayTextViews[elem].text = string
                if (elem == (mostProbClass - 1)) {
                    mClassesArrayTextViews[elem].setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            com.st.BlueSTSDK.gui.R.color.fabColorPrimaryDark
                        )
                    )
                } else {
                    mClassesArrayTextViews[elem].setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            com.st.BlueSTSDK.gui.R.color.labelPlotContrast
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

    private fun setMostProbClass(mostProbClass: Int) {
        val textString: String =
            if (mostProbClass != 0) {
                "$mostProbClass"
            } else {
                "Unknown"
            }
        mMostProbableClassNumberTv.text = textString
//        if(!mShowAllClassesSwitch.isChecked) {
//            mMostProbableClassNumberTv.startAnimation(mScaleUpAnim)
//        }
    }

    private fun setTitleStatePhase(
        mode: FeatureNEAIClassClassification.ModeType,
        state: FeatureNEAIClassClassification.StateType,
        phase: FeatureNEAIClassClassification.PhaseType
    ) {
        if (phase == FeatureNEAIClassClassification.PhaseType.NULL) {
            mPhaseTv.text = "---"
        } else {
            mPhaseTv.text = phase.name.replace('_', ' ')

            if (phase == FeatureNEAIClassClassification.PhaseType.IDLE) {
                mShowAllClassesSwitch.isEnabled = false
                mShowAllClassesSwitch.visibility = View.GONE
                mStartClassification.isEnabled = true
                mStopClassification.isEnabled = false
                if(mode == FeatureNEAIClassClassification.ModeType.ONE_CLASS) {
                    mClassOutlier.visibility = View.GONE
                    mClassOutlierResult.visibility = View.GONE
                }
                mClassOutlierResult.text = "--"
                mResourceBusyTv.visibility=  View.GONE
                hideClassesDetail()
                stopAnimatingLogo()
            } else if (phase == FeatureNEAIClassClassification.PhaseType.CLASSIFICATION) {
                mShowAllClassesSwitch.isEnabled = true
                mShowAllClassesSwitch.visibility = View.VISIBLE
                mStartClassification.isEnabled = false
                mStopClassification.isEnabled = true
                if(mode == FeatureNEAIClassClassification.ModeType.ONE_CLASS) {
                    mClassOutlier.visibility = View.VISIBLE
                    mClassOutlierResult.visibility = View.VISIBLE
                }
                mResourceBusyTv.visibility=  View.GONE
                updateClassesDetail()
                startAnimatingLogo()
            } else if  (phase == FeatureNEAIClassClassification.PhaseType.BUSY) {
                mShowAllClassesSwitch.isEnabled = false
                mShowAllClassesSwitch.visibility = View.GONE
                //mStartClassification.isEnabled = false
                mStartClassification.isEnabled = true
                mStopClassification.isEnabled = false
                if(mode == FeatureNEAIClassClassification.ModeType.ONE_CLASS) {
                    mClassOutlier.visibility = View.GONE
                    mClassOutlierResult.visibility = View.GONE
                }
                mClassOutlierResult.text = "--"
                //mResourceBusyTv.visibility=  View.VISIBLE
                hideClassesDetail()
                stopAnimatingLogo()
            }
        }

        if (state == FeatureNEAIClassClassification.StateType.NULL) {
            mStateTv.text = "---"
        } else {
            mStateTv.text = state.name.replace('_', ' ')
        }

        val stringTitle =
            when (mode) {
                FeatureNEAIClassClassification.ModeType.ONE_CLASS -> {
                    mLinearLayoutNClasses.visibility = View.GONE
                    "1-Class"
                }
                FeatureNEAIClassClassification.ModeType.N_CLASS -> {
                    mLinearLayoutNClasses.visibility = View.VISIBLE
                    "N-Class"
                }
                else -> {
                    "Something Wrong"
                }
            }
        nClassesTitle.text = stringTitle

    }


    private fun setOneClassOutlier(classProb: IntArray) {
        if (classProb.size == 1) {
            //1Class
            val stringOneClass =
                if (classProb[0] == 0) {
                    "No"
                } else {
                    if (classProb[0] != FeatureNEAIClassClassification.CLASS_PROB_ESCAPE_CODE) {
                        "Yes"
                    } else {
                        "--"
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
            mNeaiLogo?.setImageResource(R.drawable.ic_neai_logo)
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

    private fun expandOrHideSections(rootView: View) {
        /** NEAI Commands Expand ImageButton */
        rootView.findViewById<ImageButton>(R.id.neai_commands_expand_button_classification)
            .setOnClickListener {
                val buttonView =
                    rootView.findViewById<LinearLayout>(R.id.neai_commands_layout_classification)
                if (buttonView.visibility == View.VISIBLE) {
                    rootView.findViewById<ImageButton>(R.id.neai_commands_expand_button_classification)
                        .setImageResource(R.drawable.ic_arrow_down)
                    buttonView.visibility = View.GONE
                } else {
                    rootView.findViewById<ImageButton>(R.id.neai_commands_expand_button_classification)
                        .setImageResource(R.drawable.ic_arrow_up)
                    buttonView.visibility = View.VISIBLE
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView =
            inflater.inflate(R.layout.fragment_demo_neai_classification, container, false)

        expandOrHideSections(rootView)

        mNeaiLogo = rootView.findViewById(R.id.iv_neai_logo_classification)
        mPhaseTv = rootView.findViewById(R.id.tv_phase_classification)
        mStateTv = rootView.findViewById(R.id.tv_state_classification)

        mMostProbableClassTv = rootView.findViewById(R.id.neai_most_probale_class_classification)
        mMostProbableClassNumberTv =
            rootView.findViewById(R.id.neai_most_probale_class_number_classification)

        nClassesTitle = rootView.findViewById(R.id.neai_title_classification)
        mClassOutlier = rootView.findViewById(R.id.neai_class_outlier_textview)
        mClassOutlierResult = rootView.findViewById(R.id.neai_class_outlier_result_textview)

        //Load the animations
        //mScaleUpAnim = AnimationUtils.loadAnimation(activity,R.anim.scale_up)

        mLinearLayoutNClasses =
            rootView.findViewById(R.id.neai_linearlayout_nclasses_classification)

        //Find all the Classes' LinearLayout
        var linearLayout: LinearLayout =
            rootView.findViewById(R.id.neai_class_1_linearlayout_classification)
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = rootView.findViewById(R.id.neai_class_2_linearlayout_classification)
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = rootView.findViewById(R.id.neai_class_3_linearlayout_classification)
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = rootView.findViewById(R.id.neai_class_4_linearlayout_classification)
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = rootView.findViewById(R.id.neai_class_5_linearlayout_classification)
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = rootView.findViewById(R.id.neai_class_6_linearlayout_classification)
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = rootView.findViewById(R.id.neai_class_7_linearlayout_classification)
        mClassesArrayLinearLayouts.add(linearLayout)
        linearLayout = rootView.findViewById(R.id.neai_class_8_linearlayout_classification)
        mClassesArrayLinearLayouts.add(linearLayout)

        //Find all the Classes' TextView
        var textView: TextView = rootView.findViewById(R.id.neai_class_1_textview_classification)
        mClassesArrayTextViews.add(textView)
        textView = rootView.findViewById(R.id.neai_class_2_textview_classification)
        mClassesArrayTextViews.add(textView)
        textView = rootView.findViewById(R.id.neai_class_3_textview_classification)
        mClassesArrayTextViews.add(textView)
        textView = rootView.findViewById(R.id.neai_class_4_textview_classification)
        mClassesArrayTextViews.add(textView)
        textView = rootView.findViewById(R.id.neai_class_5_textview_classification)
        mClassesArrayTextViews.add(textView)
        textView = rootView.findViewById(R.id.neai_class_6_textview_classification)
        mClassesArrayTextViews.add(textView)
        textView = rootView.findViewById(R.id.neai_class_7_textview_classification)
        mClassesArrayTextViews.add(textView)
        textView = rootView.findViewById(R.id.neai_class_8_textview_classification)
        mClassesArrayTextViews.add(textView)

        //Find all the Classes' ProgressBar
        var progressBar: ProgressBar =
            rootView.findViewById(R.id.neai_class_1_progressbar_classification)
        mClassesArrayProgressBars.add(progressBar)
        progressBar = rootView.findViewById(R.id.neai_class_2_progressbar_classification)
        mClassesArrayProgressBars.add(progressBar)
        progressBar = rootView.findViewById(R.id.neai_class_3_progressbar_classification)
        mClassesArrayProgressBars.add(progressBar)
        progressBar = rootView.findViewById(R.id.neai_class_4_progressbar_classification)
        mClassesArrayProgressBars.add(progressBar)
        progressBar = rootView.findViewById(R.id.neai_class_5_progressbar_classification)
        mClassesArrayProgressBars.add(progressBar)
        progressBar = rootView.findViewById(R.id.neai_class_6_progressbar_classification)
        mClassesArrayProgressBars.add(progressBar)
        progressBar = rootView.findViewById(R.id.neai_class_7_progressbar_classification)
        mClassesArrayProgressBars.add(progressBar)
        progressBar = rootView.findViewById(R.id.neai_class_8_progressbar_classification)
        mClassesArrayProgressBars.add(progressBar)


        mShowAllClassesSwitch = rootView.findViewById(R.id.show_all_classes_cb_classification)

        nClassesDetails = rootView.findViewById(R.id.neai_classes_linearlayout_classification)
        nClassesTv = rootView.findViewById(R.id.neai_classes_probability_textview)


        mShowAllClassesSwitch.setOnClickListener {
            updateClassesDetail()
        }

        mResourceBusyTv = rootView.findViewById(R.id.neai_resource_busy_tx)

        mStartClassification = rootView.findViewById(R.id.start_button_classification)
        mStopClassification = rootView.findViewById(R.id.stop_button_classification)

        mStartClassification.setOnClickListener {
            if(mCurrentPhase!=FeatureNEAIClassClassification.PhaseType.BUSY) {
                //If the phase is not Busy... run the Start Classification Command
                mFeature?.writeStartClassificationCommand()
                Toast.makeText(
                    context,
                    "Start Classification",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //if the phase is == Busy... open dialog for asking what to do..
                askIfForceStartCommand()
            }
        }

        mStopClassification.setOnClickListener {
            mFeature?.writeStopClassificationCommand()
            Toast.makeText(
                context,
                "Stop Classification",
                Toast.LENGTH_SHORT
            ).show()
        }

        return rootView
    }


    private fun askIfForceStartCommand() {

        val dialog = AlertDialog.Builder(context)
            .setTitle("WARNING!")
            .setMessage("Resources are busy with another process. Do you want to stop it and start NEAI-Classification anyway?")
            .setPositiveButton("Start"){ dialog, _ ->
                mFeature?.writeStartClassificationCommand()
                dialog.dismiss()
                Toast.makeText(
                    context,
                    "Start Classification",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    override fun enableNeededNotification(node: Node) {
        mFeature = node.getFeature(FeatureNEAIClassClassification::class.java)
        mFeature?.apply {
            addFeatureListener(featureListener)
            enableNotification()
        }
    }

    override fun disableNeedNotification(node: Node) {
        node.getFeature(FeatureNEAIClassClassification::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }
        mFeature = null
    }

}