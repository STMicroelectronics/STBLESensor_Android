package com.st.BlueMS.demos.PnPL

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueMS.R
import com.st.BlueMS.databinding.PnplComponentElementBinding
import com.st.BlueSTSDK.Features.PnPL.ComponentType
import com.st.BlueSTSDK.Features.PnPL.PnPLComponent
import com.st.BlueSTSDK.Features.PnPL.PnPLContent
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SensorType
import com.st.STWINBoard_Gui.Utils.imageResource
import com.st.STWINBoard_Gui.Utils.nameResource

typealias OnContentChangedListener = (component: PnPLComponent, content: PnPLContent, value:Any)->Unit
typealias OnSubContentChangedListener = (component: PnPLComponent, content: PnPLContent, subContent: PnPLContent, value:Any)->Unit
typealias OnCommandSentListener = (component: PnPLComponent, content: PnPLContent, command_fields: List<PnPLContent>)->Unit
typealias OnLoadFilePressedListener = (component: PnPLComponent, content: PnPLContent)->Unit

class PnPLComponentAdapter(
    private val mCallback: ComponentInteractionCallback,
    private val contChangedListener: OnContentChangedListener,
    private val subContChangedListener: OnSubContentChangedListener,
    private val commandSentListener: OnCommandSentListener,
    private val loadfileListener: OnLoadFilePressedListener)
        : RecyclerView.Adapter<PnPLComponentAdapter.PnPlComponentViewHolder>(){

    interface ComponentInteractionCallback {
        fun onComponentCollapsed(selected: PnPLComponentViewData)
        fun onComponentExpanded(selected: PnPLComponentViewData)
    }

    private var componentList = listOf<PnPLComponentViewData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PnPLComponentAdapter.PnPlComponentViewHolder {
        val binding = PnplComponentElementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PnPlComponentViewHolder(mCallback, binding)
    }

    override fun onBindViewHolder(holder: PnPlComponentViewHolder, position: Int) {
        val currentItem = componentList[position]
        if (currentItem.component.comp_type != null) {
            holder.componentIcon.visibility = View.VISIBLE
            when (currentItem.component.comp_type) {
                ComponentType.SENSOR -> {
                    holder.componentSubNameTextView.visibility = View.VISIBLE
                    when (currentItem.component.comp_name.split("_").takeLast(1)[0]) {
                        "acc" -> {
                            holder.componentNameTextView.setText(SensorType.Accelerometer.nameResource)//"Accelerometer"
                            holder.componentSubNameTextView.text = currentItem.component.comp_display_name.split("_")[0]
                            holder.componentIcon.setImageResource(SensorType.Accelerometer.imageResource)
                        }
                        "mag" -> {
                            holder.componentNameTextView.setText(SensorType.Magnetometer.nameResource)
                            holder.componentSubNameTextView.text = currentItem.component.comp_display_name.split("_")[0]
                            holder.componentIcon.setImageResource(SensorType.Magnetometer.imageResource)
                        }
                        "gyro" -> {
                            holder.componentNameTextView.setText(SensorType.Gyroscope.nameResource)
                            holder.componentSubNameTextView.text = currentItem.component.comp_display_name.split("_")[0]
                            holder.componentIcon.setImageResource(SensorType.Gyroscope.imageResource)
                        }
                        "temp" -> {
                            holder.componentNameTextView.setText(SensorType.Temperature.nameResource)
                            holder.componentSubNameTextView.text = currentItem.component.comp_display_name.split("_")[0]
                            holder.componentIcon.setImageResource(SensorType.Temperature.imageResource)
                        }
                        "hum" -> {
                            holder.componentNameTextView.setText(SensorType.Humidity.nameResource)
                            holder.componentSubNameTextView.text = currentItem.component.comp_display_name.split("_")[0]
                            holder.componentIcon.setImageResource(SensorType.Humidity.imageResource)
                        }
                        "press" -> {
                            holder.componentNameTextView.setText(SensorType.Pressure.nameResource)
                            holder.componentSubNameTextView.text = currentItem.component.comp_display_name.split("_")[0]
                            holder.componentIcon.setImageResource(SensorType.Pressure.imageResource)
                        }
                        "mic" -> {
                            holder.componentNameTextView.setText(SensorType.Microphone.nameResource)
                            holder.componentSubNameTextView.text = currentItem.component.comp_display_name.split("_")[0]
                            holder.componentIcon.setImageResource(SensorType.Microphone.imageResource)
                        }
                        "mlc" -> {
                            holder.componentNameTextView.setText(SensorType.MLC.nameResource)
                            holder.componentSubNameTextView.text = currentItem.component.comp_display_name.split("_")[0]
                            holder.componentIcon.setImageResource(SensorType.MLC.imageResource)
                        }
                        "stredl" -> {
                            holder.componentNameTextView.setText(SensorType.STREDL.nameResource)
                            holder.componentSubNameTextView.text = currentItem.component.comp_display_name.split("_")[0]
                            holder.componentIcon.setImageResource(SensorType.STREDL.imageResource)
                        }
                        "class" -> {
                            holder.componentNameTextView.setText(SensorType.CLASS.nameResource)
                            holder.componentSubNameTextView.text = currentItem.component.comp_display_name.split("_")[0]
                            holder.componentIcon.setImageResource(SensorType.CLASS.imageResource)
                        }
                        else -> {
                            holder.componentNameTextView.text = currentItem.component.comp_display_name
                            holder.componentIcon.setImageResource(SensorType.Unknown.imageResource)
                        }
                    }
                }
                ComponentType.ALGORITHM -> {
                    holder.componentIcon.setImageResource(R.drawable.sensor_type_class)
                    holder.componentNameTextView.text = currentItem.component.comp_display_name
                }
                ComponentType.OTHER -> {
                    holder.componentNameTextView.text = currentItem.component.comp_display_name
                    holder.componentIcon.setImageResource(R.drawable.component_info)
                    holder.componentSubNameTextView.visibility = View.GONE
                }
                else -> {
                    holder.componentNameTextView.text = currentItem.component.comp_display_name
                    holder.componentIcon.setImageResource(R.drawable.component_info)
                    holder.componentSubNameTextView.visibility = View.GONE
                }
            }
        }

        if(currentItem.isCollapsed){
            holder.contentListRecyclerView.visibility = View.GONE
            val enabledContent = currentItem.component.cont_list.find { it.cont_name == "enable" }
            if(enabledContent != null){
                holder.componentEnabled.visibility = View.VISIBLE
                if (enabledContent.cont_info != null) {
                    holder.setComponentEnableState(enabledContent.cont_info as? Boolean ?: false)
                }
            }
            else
            {
                holder.componentEnabled.visibility = View.INVISIBLE
            }
            holder.componentExpandImage.setBackgroundResource(com.st.clab.stwin.gui.R.drawable.ic_expand_view)
        }else{
            holder.contentListRecyclerView.visibility = View.VISIBLE
            val enabledContent = currentItem.component.cont_list.find { it.cont_name == "enable" }
            if(enabledContent != null){
                holder.componentEnabled.visibility = View.GONE
                if (enabledContent.cont_info != null) {
                    holder.setComponentEnableState(enabledContent.cont_info as? Boolean ?: false)
                }
            }
            else
            {
                holder.componentEnabled.visibility = View.GONE
            }
            holder.componentExpandImage.setBackgroundResource(com.st.clab.stwin.gui.R.drawable.ic_collaps_view)
        }

        val contentListAdapter = PnPLContentAdapter(
            currentItem.component,
            currentItem.component.cont_list,
            contChangedListener,
            subContChangedListener,
            commandSentListener,
            loadfileListener
        )
        holder.contentListRecyclerView.adapter = contentListAdapter

        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return componentList.size
    }

    fun getComponentList():List<PnPLComponent>{
        return componentList.map { it.component }
    }

    fun updatePnPLCompList(newList: List<PnPLComponentViewData>) {
        componentList = newList
        //to update the RecyclerView
        notifyDataSetChanged()
    }

    inner class PnPlComponentViewHolder(mCallback: ComponentInteractionCallback, binding: PnplComponentElementBinding) : RecyclerView.ViewHolder(binding.root){
        private var mComponent: PnPLComponentViewData? = null

        private val pnplComponentHeader: ConstraintLayout = binding.pnplComponentHeader
        val componentExpandImage: ImageView = binding.pnplCompExpandImage
        val componentNameTextView: TextView = binding.pnplCompName
        val componentSubNameTextView: TextView = binding.pnplCompSubName
        val componentIcon: ImageView = binding.pnplComponentIcon
        val contentListRecyclerView: RecyclerView = binding.pnplContList
        val componentEnabled: SwitchCompat = binding.pnplCompEnabled

        private val onComponentEnableStatusListener  = object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                val compViewData = mComponent ?: return
                val enabledContent = compViewData.component.cont_list.find { it.cont_name == "enable" }
                if(enabledContent != null){
                    if ((enabledContent.cont_info as? Boolean) != isChecked){
                        contChangedListener(compViewData.component, enabledContent, isChecked)
                    }
                }
            }
        }

        fun setComponentEnableState(newState:Boolean){
            componentEnabled.setOnCheckedChangeListener(null)
            componentEnabled.isChecked = newState
            componentEnabled.setOnCheckedChangeListener(onComponentEnableStatusListener)
        }

        init {
            componentEnabled.setOnCheckedChangeListener(onComponentEnableStatusListener)
            pnplComponentHeader.setOnClickListener ( object : View.OnClickListener {
                override fun onClick(view: View?) {
                    val compViewData = mComponent ?: return
                    if (compViewData.isCollapsed) {
                        mCallback.onComponentExpanded(compViewData)
                        val enabledContent = compViewData.component.cont_list.find { it.cont_name == "enable" }
                        if(enabledContent != null){
                            componentEnabled.visibility = View.GONE
                            componentEnabled.isChecked = enabledContent.cont_info as? Boolean ?: false
                            //componentEnabled.isChecked = enabledContent.cont_info as Boolean
                        }
                    } else {
                        mCallback.onComponentCollapsed(compViewData)
                        val enabledContent = compViewData.component.cont_list.find { it.cont_name == "enable" }
                        if(enabledContent != null){
                            componentEnabled.visibility = View.VISIBLE
                            componentEnabled.isChecked = enabledContent.cont_info as? Boolean ?: false
                            //componentEnabled.isChecked = enabledContent.cont_info as Boolean
                        }
                    }
                }
            })
        }

        fun bind(component: PnPLComponentViewData){
            mComponent = component
        }
    }
}