package com.st.BlueMS.demos.ColorAmbientLight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Features.FeatureColorAmbientLight
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation

@DemoDescriptionAnnotation(name = "Color Ambient Light", iconRes = R.drawable.ic_ambient_light,
        requireAll = [FeatureColorAmbientLight::class],
       demoCategory = ["Environmental Sensors"],)
class ColorAmbientLightFragment : BaseDemoFragment() {

    private lateinit var viewModel : ColorAmbientLightViewModel

    private lateinit var textViewLux : TextView
    private lateinit var textViewCCT: TextView
    private lateinit var textViewUVIndex: TextView

    private lateinit var progressBarLux : ProgressBar
    private lateinit var progressBarCCT: ProgressBar
    private lateinit var progressBarUVIndex: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Inflate the layout
        val rootView = inflater.inflate(R.layout.fragment_color_ambient_light, container, false)

        textViewLux = rootView.findViewById(R.id.textViewLux)
        textViewCCT = rootView.findViewById(R.id.textViewCCT)
        textViewUVIndex = rootView.findViewById(R.id.textViewUVIndex)

        progressBarLux = rootView.findViewById(R.id.progressBarLux)
        progressBarCCT = rootView.findViewById(R.id.progressBarCCT)
        progressBarUVIndex = rootView.findViewById(R.id.progressBarUVIndex)

        // Get the ViewModel
        viewModel = ViewModelProvider(this).get(ColorAmbientLightViewModel::class.java)

        return rootView
    }

    fun attachToLuxValue() {
        val minValue = viewModel.getMinValueLux()
        val maxValue = viewModel.getMaxValueLux()

        viewModel.lux_value.observe(viewLifecycleOwner, Observer { newValue ->
            // Find %
            if(newValue!=null) {
                val percentage = ((newValue - minValue) * 100) / (maxValue - minValue)
                // Update UI
                progressBarLux.progress = percentage
                textViewLux.text = "$newValue Lux"
            }
        })
    }

    fun attachToCCTValue() {
        val minValue = viewModel.getMinValueCCT()
        val maxValue = viewModel.getMaxValueCCT()

        viewModel.cct_value.observe(viewLifecycleOwner, Observer { newValue ->
            if(newValue!=null) {
                // Find %
                val percentage = ((newValue - minValue) * 100) / (maxValue - minValue)
                // Update UI
                progressBarCCT.progress = percentage
                textViewCCT.text = "$newValue CCT"
            }
        })
    }

    fun attachToUVIndexValue() {
        val minValue = viewModel.getMinValueUVIndex()
        val maxValue = viewModel.getMaxValueUVIndex()
        viewModel.uv_index.observe(viewLifecycleOwner, Observer { newValue ->
            if(newValue!=null) {
                // Find %
                val percentage = ((newValue - minValue) * 100) / (maxValue - minValue)
                // Update UI
                progressBarUVIndex.progress = percentage
                textViewUVIndex.text = "$newValue UV Index"
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachToLuxValue()
        attachToCCTValue()
        attachToUVIndexValue()
    }

    override fun enableNeededNotification(node: Node) {
        viewModel.enableNotification(node)
    }

    override fun disableNeedNotification(node: Node) {
        viewModel.disableNotification(node)
    }

}

