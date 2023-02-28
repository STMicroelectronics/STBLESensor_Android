package com.st.trilobyte.ui.fragment.flow_builder.functionOption

import android.content.Context
import androidx.annotation.StringRes
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import com.st.trilobyte.R
import com.st.trilobyte.models.Function
import com.st.trilobyte.models.Property
import com.st.trilobyte.models.Sensor

internal class ThresholdPropertiesWidget : FunctionPropertyWidget{

    constructor(context: Context):
            super(context)

    constructor(context: Context, attrs: AttributeSet):
            super(context,attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):
            super(context,attrs,defStyleAttr)

    fun init(propertiy: Property<*>, sensor: Sensor) {
        super.init(propertiy)
        setupDescriptionLabel(sensor.id,mView.findViewById(R.id.property_label))
    }

    private fun setupDescriptionLabel(id: String, label: TextView) {
        val strId = getDescriptionMessageForSensor(id) ?: return
        label.setText(strId)
        label.visibility = View.VISIBLE
    }


    private fun getDescriptionMessageForSensor(id:String):Int?{
        return when(id){
            "S1" -> R.string.thresholdDesc_temperature
            "S2" -> R.string.thresholdDesc_humidity
            "S3" -> R.string.thresholdDesc_pressure
            "S4" -> R.string.thresholdDesc_acceleration
            "S5" -> R.string.thresholdDesc_acceleration
            "S6" -> R.string.thresholdDesc_gyroscope
            "S7" -> R.string.thresholdDesc_acceleration
            "S8" -> R.string.thresholdDesc_magnetometer
            "S10" -> R.string.thresholdDesc_timer
            else -> null
        }
    }

}