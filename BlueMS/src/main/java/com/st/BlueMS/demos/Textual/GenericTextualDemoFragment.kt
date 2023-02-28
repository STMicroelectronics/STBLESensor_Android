package com.st.BlueMS.demos.Textual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.BLENodeDefines
import com.st.BlueSTSDK.fwDataBase.db.BleCharacteristic
import com.st.BlueSTSDK.fwDataBase.db.BoardFirmware
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation

@DemoDescriptionAnnotation(
    iconRes = R.drawable.ic_baseline_text_snippet_24,
    name = "Textual Monitor",
    demoCategory = ["Debug"],
    requireAny = true
)
class GenericTextualDemoFragment : BaseDemoFragment() {
    private lateinit var startStopButton: MaterialButton
    private lateinit var spinner: Spinner
    private var adapterSpinnerPosition = 0

    private lateinit var featureData: TextView
    private lateinit var scrollView: ScrollView

    private lateinit var featureList: List<GenericTextualFeature?>
    private var fwDetails: BoardFirmware? = null
    private var mViewModel: GenericTextualViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        val rootView = inflater.inflate(R.layout.fragment_generic_textual_demo, container, false)


        startStopButton = rootView.findViewById(R.id.generic_textual_demo_button)
        startStopButton.setOnClickListener { startStop() }

        spinner = rootView.findViewById(R.id.generic_textual_demo_spinner)

        featureData = rootView.findViewById(R.id.generic_textual_demo_text)
        scrollView = rootView.findViewById(R.id.generic_textual_demo_scrollview)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(requireActivity()).get(GenericTextualViewModel::class.java)

        mViewModel?.sample_data?.observe(viewLifecycleOwner, Observer { newString ->
            if (newString != null) {
                featureData.append(newString)
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
            }
        })

        if (node != null) {

            //take the Firmware DB
//            val firmwareDB = ReadBoardFirmwareDataBase(requireContext())
//            val optBytes = NumberConversion.BigEndian.uint32ToBytes(node!!.advertiseOptionBytes)

            //Read the Firmware Details
            fwDetails = node!!.fwDetails
            val features = node!!.features
            if (features.isNotEmpty()) {
                featureList = features.filter { it.isDataNotifyFeature }
                    .map {
                        retrieveBleCharDescription(it)
                    }

                val dataAdapter = ArrayAdapter(requireActivity(),
                    android.R.layout.simple_spinner_item, featureList.map { it?.name }).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                adapterSpinnerPosition = 0

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        adapterSpinnerPosition = position
                        changeFeature()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        adapterSpinnerPosition = 0
                    }
                }

                spinner.adapter = dataAdapter

            }
        }
    }

    private fun retrieveBleCharDescription(featureSelected: Feature): GenericTextualFeature? {
        var bleCharDesc: BleCharacteristic? = null
        val uuid = node?.getCorrespondingUUID(featureSelected)
        if (BLENodeDefines.FeatureCharacteristics.isGeneralPurposeCharacteristics(uuid)) {
            bleCharDesc = fwDetails?.characteristics?.firstOrNull { it.uuid == uuid.toString() }
        }

        val name: String = if (bleCharDesc == null) {
            //Remove all the part like com.st.BlueSTSDK.Features.FeatureGNSS -> GNSS
            featureSelected.javaClass.name.replace(".*\\.Feature".toRegex(), "")
        } else {
            "GP " + bleCharDesc.name
        }

        val desc: String = bleCharDesc?.name ?: featureSelected.name
        return GenericTextualFeature(name, desc, featureSelected, bleCharDesc)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel?.sample_data?.removeObservers(viewLifecycleOwner)
    }

    fun changeFeature() {
        val featureSelected = featureList[adapterSpinnerPosition]
        //Check if the PrevSelected feature is already notifying something or not
        val prevFeature = mViewModel?.getSelectedFeature()
        if ((prevFeature != null) && (featureSelected != null)) {
            if (node?.isEnableNotification(prevFeature) == true) {
                //Disable the notification of previous selected Feature
                mViewModel?.apply {
                    clearLastSampleData()
                    disableFeatureNotification()
                }
                startStopButton.setIconResource(R.drawable.ic_play_arrow)
            }
        }
    }

    fun startStop() {
        val featureSelected = featureList[adapterSpinnerPosition]

        if (featureSelected != null) {
            mViewModel?.setSelectedFeature(featureSelected.feature, featureSelected.bleCharDesc)

            //Check if the Selected feature is already notifying something or not
            if (node?.isEnableNotification(featureSelected.feature) == true) {
                mViewModel?.apply {
                    clearLastSampleData()
                    disableFeatureNotification()
                }
                startStopButton.setIconResource(R.drawable.ic_play_arrow)
            } else {
                //Clear the TextView
                val initString = "${featureSelected.description}:\n\n"
                featureData.text = initString
                //Stop the notification
                mViewModel?.enableFeatureNotification()
                startStopButton.setIconResource(R.drawable.ic_stop)
            }
        }
    }

    override fun enableNeededNotification(node: Node) {
    }

    override fun disableNeedNotification(node: Node) {
    }
}