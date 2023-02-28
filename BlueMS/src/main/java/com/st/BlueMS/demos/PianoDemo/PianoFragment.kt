package com.st.BlueMS.demos.PianoDemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Features.FeaturePiano
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.DemosActivity
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation

@DemoDescriptionAnnotation(name = "Piano",
    iconRes = R.drawable.ic_baseline_music_note_24,
    demoCategory = ["Audio"],
    requireAll = [FeaturePiano::class])
class PianoFragment : BaseDemoFragment() {

    private var mFeature: FeaturePiano? = null
    private var mPianoView: PianoView? = null

    private val mKeyByte = byteArrayOf(
        1 , //NOTE_C1
        2 , // NOTE_CS1
        3 , // NOTE_D1
        4 , // NOTE_DS1
        5 , // NOTE_E1
        6 , // NOTE_F1
        7 , // NOTE_FS1
        8 , // NOTE_G1
        9 , // NOTE_GS1
        10, // NOTE_A1
        11, // NOTE_AS1
        12, // NOTE_B1
        13, // NOTE_C2
        14, // NOTE_CS2
        15, // NOTE_D2
        16, // NOTE_DS2
        17, // NOTE_E2
        18, // NOTE_F2
        19, // NOTE_FS2
        20, // NOTE_G2
        21, // NOTE_GS2
        22, // NOTE_A2
        23, // NOTE_AS2
        24 // NOTE_B2
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        val rootView = inflater.inflate(R.layout.fragment_piano_demo, container, false)
        mPianoView = rootView.findViewById(R.id.piano_demo_pianoview)
        return rootView
    }

    override fun enableNeededNotification(node: Node) {
        mFeature = node.getFeature(FeaturePiano::class.java)
        mPianoView?.SetFeatureAndKeyMap(mFeature,mKeyByte)

        (activity as DemosActivity).enableUserInput(false)
        mFeature?.apply {
            enableNotification()
        }
    }

    override fun disableNeedNotification(node: Node) {
        (activity as DemosActivity).enableUserInput(true)
        node.getFeature(FeaturePiano::class.java)?.apply {
            disableNotification()
        }
        mFeature = null
    }

}