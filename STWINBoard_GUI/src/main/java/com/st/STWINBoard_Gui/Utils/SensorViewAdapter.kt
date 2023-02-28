/*
 * Copyright (c) 2020  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 * STMicroelectronics company nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 * in a directory whose title begins with st_images may only be used for internal purposes and
 * shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 * icons, pictures, logos and other images that are provided with the source code in a directory
 * whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.STWINBoard_Gui.Utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.Sensor
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SensorType
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SubSensorDescriptor
import com.st.clab.stwin.gui.R


typealias OnSubSensorEnableStatusChange = (sensor:Sensor,subSensor: SubSensorDescriptor, newState:Boolean, paramsLocked:Boolean)->Unit
typealias OnSubSensorODRChange = (sensor:Sensor,subSensor: SubSensorDescriptor, newOdrValue:Double)->Unit
typealias OnSubSensorFullScaleChange = (sensor:Sensor,subSensor: SubSensorDescriptor, newFSValue:Double)->Unit
typealias OnSubSensorSampleChange = (sensor:Sensor,subSensor: SubSensorDescriptor, newSampleValue:Int)->Unit
typealias OnSubSensorOpenMLCConf = (sensor:Sensor,subSensor: SubSensorDescriptor)->Unit
typealias OnUCFStatusChange = (sensor:Sensor,subSensor: SubSensorDescriptor, newStatus:Boolean)->Unit

internal class SensorViewAdapter(
        private val mCallback: SensorInteractionCallback,
        private val onSubSubSensorEnableStatusChange: OnSubSensorEnableStatusChange,
        private val onSubSensorODRChange: OnSubSensorODRChange,
        private val onSubSensorFullScaleChange: OnSubSensorFullScaleChange,
        private val onSubSensorSampleChange: OnSubSensorSampleChange,
        private val onSubSensorOpenMLCConf: OnSubSensorOpenMLCConf,
        private val onUCFStatusChange: OnUCFStatusChange) :
        ListAdapter<SensorViewData,SensorViewAdapter.ViewHolder>(SensorDiffCallback()) {

    interface SensorInteractionCallback {
        fun onSensorCollapsed(selected: SensorViewData)
        fun onSensorExpanded(selected: SensorViewData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_sensor, parent, false)
        return ViewHolder(mCallback, view)
    }

    private fun getMLCSensorID(subSensorDescriptorList:List<SubSensorDescriptor>):Int {
        for (ssd in subSensorDescriptorList){
            if (ssd.sensorType == SensorType.MLC){
                return ssd.id
            }
        }
        return -1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val s = getItem(position)
        holder.bind(s)
        holder.mSensorName.text = s.sensor.name
        holder.mSensorId.text = s.sensor.id.toString()

        val mlcId = getMLCSensorID(s.sensor.sensorDescriptor.subSensorDescriptors)
        if(mlcId != -1){
            s.sensor.sensorStatus.paramsLocked = s.sensor.sensorStatus.subSensorStatusList[mlcId].isActive && s.sensor.sensorStatus.subSensorStatusList[mlcId].ucfLoaded
        }

        if(s.isCollapsed){
            holder.mSubSensorListView.visibility = View.GONE
            holder.mSensorArrowBtn.setBackgroundResource(R.drawable.ic_expand_view)
            holder.mSubSensorPreview.visibility = View.VISIBLE
        }else{
            holder.mSubSensorListView.visibility = View.VISIBLE
            holder.mSensorArrowBtn.setBackgroundResource(R.drawable.ic_collaps_view)
            holder.mSubSensorPreview.visibility = View.GONE
        }

        val subSensorPreviewAdapter = SubSensorPreviewViewAdapter(
                s.sensor,
                onSubSubSensorEnableStatusChange,
                onSubSensorOpenMLCConf
        )
        holder.mSubSensorPreview.adapter = subSensorPreviewAdapter

        val subSensorParamsAdapter = SubSensorViewAdapter(
                s.sensor,
                onSubSubSensorEnableStatusChange,
                onSubSensorODRChange,
                onSubSensorFullScaleChange,
                onSubSensorSampleChange,
                onSubSensorOpenMLCConf,
                onUCFStatusChange)

        holder.mSubSensorListView.adapter = subSensorParamsAdapter
    }

    inner class ViewHolder(mCallback: SensorInteractionCallback, itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var currentData: SensorViewData? = null

        val mSensorName: TextView = itemView.findViewById(R.id.sensorItem_nameLabel)
        val mSensorArrowBtn: ImageView = itemView.findViewById(R.id.sensorItem_expandImage)
        val mSensorId: TextView = itemView.findViewById(R.id.sensorItem_idLabel)
        val mSubSensorPreview: RecyclerView = itemView.findViewById(R.id.sensorItem_subSensorPreview)
        val mSubSensorListView: RecyclerView = itemView.findViewById(R.id.sensorItem_subSensorList)

        init {

            itemView.findViewById<View>(R.id.sensorItem_headerLayout).setOnClickListener {
                val sensor = currentData ?: return@setOnClickListener
                if(sensor.isCollapsed){
                    mCallback.onSensorExpanded(sensor)
                } else {
                    mCallback.onSensorCollapsed(sensor)
                }
            }
        }

        fun bind(sensor: SensorViewData) {
            currentData = sensor
        }
    }
}

private class SensorDiffCallback : DiffUtil.ItemCallback<SensorViewData>(){

    override fun areItemsTheSame(oldItem: SensorViewData, newItem: SensorViewData): Boolean {
        val checkParamLock = oldItem.sensor.sensorStatus.paramsLocked == newItem.sensor.sensorStatus.paramsLocked
        return checkParamLock && oldItem.sensor.sensorStatus == newItem.sensor.sensorStatus
    }

    override fun areContentsTheSame(oldItem: SensorViewData, newItem: SensorViewData): Boolean {
        return oldItem == newItem
    }
}

