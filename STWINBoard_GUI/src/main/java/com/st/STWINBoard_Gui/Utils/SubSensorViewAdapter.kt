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

import android.graphics.Color
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.*
import com.st.clab.stwin.gui.R

class SubSensorViewAdapter(
        private val sensor: Sensor,
        private val onSubSensorEnableStatusChange: OnSubSensorEnableStatusChange,
        private val onSubSensorODRChange: OnSubSensorODRChange,
        private val onSubSensorFullScaleChange: OnSubSensorFullScaleChange,
        private val onSubSensorSampleChange: OnSubSensorSampleChange,
        private val onSubSensorOpenMLCConf: OnSubSensorOpenMLCConf,
        private val onUCFStatusChange: OnUCFStatusChange
        ) : RecyclerView.Adapter<SubSensorViewAdapter.ViewHolder>() {

    //SubParam List
    private val mSubSensorList: List<SubSensorDescriptor> = sensor.sensorDescriptor.subSensorDescriptors
    private val mSubStatusList: List<SubSensorStatus> = sensor.sensorStatus.subSensorStatusList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_sub_sensor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subSensorDescriptor = mSubSensorList[position]
        val subSensorStatus = mSubStatusList[position]
        holder.bind(subSensorDescriptor,subSensorStatus)
    }

    override fun getItemCount(): Int {
        return mSubSensorList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mIcon: ImageView = itemView.findViewById(R.id.subSensor_icon)
        private val mName: TextView = itemView.findViewById(R.id.subSensor_name)

        private val mEnabledSwitch:Switch = itemView.findViewById(R.id.subSensor_enable)

        private val mOdrViews:View = itemView.findViewById(R.id.subSensor_odrViews)
        private val mOdrSelector:Spinner = itemView.findViewById(R.id.subSensor_odrSelector)

        private val mFsViews:View = itemView.findViewById(R.id.subSensor_fullScaleViews)
        private val mFsSelector:Spinner = itemView.findViewById(R.id.subSensor_fsSelector)
        private val mFsUnit:TextView = itemView.findViewById(R.id.subSensor_fsUnit)

        private val mSampleTSValue:TextInputEditText = itemView.findViewById(R.id.subSensor_sampleTSValue)
        private val mSampleTSLayout:TextInputLayout = itemView.findViewById(R.id.subSensor_sampleTSLayout)

        private val mUCFLoadedChip:Chip = itemView.findViewById(R.id.ucf_load_status_check)
        private val mMLCLoadButton:Button = itemView.findViewById(R.id.subSensor_MLCLoadButton)

        private var mSubSensor:SubSensorDescriptor? = null
        private var mSubSensorStatus:SubSensorStatus? = null

        private val onCheckedChangeListener  = object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                val subSensor = mSubSensor ?: return
                var paramsLocked = sensor.sensorStatus.paramsLocked
                if (mSubSensor!!.sensorType == SensorType.MLC || mSubSensor!!.sensorType == SensorType.STREDL){
                    if(isChecked) {
                        if (mSubSensorStatus!!.ucfLoaded) {
                            paramsLocked = true
                        }
                    } else {
                        paramsLocked = false
                    }
                }
                displaySensorConfigurationViews(isChecked, paramsLocked, subSensor.sensorType)
                onSubSensorEnableStatusChange(sensor,subSensor,isChecked, paramsLocked)
            }
        }

        private val onUCFStatusChangeListener  = object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                val subSensor = mSubSensor ?: return
                onUCFStatusChange(sensor,subSensor,isChecked)
            }
        }

        init {

            mOdrSelector.onUserSelectedItemListener = OnUserSelectedListener(object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) { }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedValue = parent?.getItemAtPosition(position) as Double
                    val subSensor = mSubSensor ?: return
                    onSubSensorODRChange(sensor,subSensor,selectedValue)
                }
            })

            mFsSelector.onUserSelectedItemListener = OnUserSelectedListener(object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) { }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedValue = parent?.getItemAtPosition(position) as Double
                    val subSensor = mSubSensor ?: return
                    onSubSensorFullScaleChange(sensor,subSensor,selectedValue)
                    Log.d("SubSensor","FS onItemChange $selectedValue")
                }
            })

            mSampleTSValue.setOnEditorActionListener { v: TextView, actionId: Int, _: KeyEvent? ->
                val subSensor = mSubSensor ?: return@setOnEditorActionListener false
                val newValue = v.text.toString().toIntOrNull() ?: return@setOnEditorActionListener false
                when (actionId) {
                    EditorInfo.IME_ACTION_SEND,
                        EditorInfo.IME_ACTION_NEXT,
                    EditorInfo.IME_ACTION_DONE-> {
                        onSubSensorSampleChange(sensor,subSensor,newValue)
                        //v.clearFocus()
                    }
                }
                false
            }

            /* Add also on .xml android:focusableInTouchMode="false" */
//            mSampleTSValue.setOnClickListener {
//                val layout = LinearLayout(it.context)
//                layout.layoutDirection
//                val inputTextView = TextInputEditText(it.context)
//                inputTextView.inputType = InputType.TYPE_CLASS_NUMBER
//                inputTextView.text = (it as TextView).text as Editable?
//
//                val layoutParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
//                layoutParams.setMargins(64, 8, 8, 8)
//                layout.addView(inputTextView,layoutParams)
//
//                val builder = AlertDialog.Builder(it.context)
//                builder.setView(layout)
//                builder.setTitle(mName.text)
//                builder.setPositiveButton(
//                    "Send"
//                ) { dialog, _ ->
//                    val newValue = inputTextView.text.toString().toIntOrNull()
//                    it.text = inputTextView.text
//                    if ((mSubSensor != null) && (newValue!=null)) {
//                        onSubSensorSampleChange(sensor, mSubSensor!!, newValue)
//                    }
//                    dialog.dismiss()
//                }
//                builder.setNegativeButton(
//                    "Cancel"
//                ) { dialog, _ ->
//                    dialog.dismiss()
//                }
//                val alert = builder.create()
//                alert.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
//                alert.show()
//                inputTextView.requestFocus()
//            }

            mMLCLoadButton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    val subSensor = mSubSensor ?: return
                    onSubSensorOpenMLCConf(sensor, subSensor)
                }
            })

        }

        fun bind(subSensor:SubSensorDescriptor, status: SubSensorStatus){
            mSubSensor = subSensor
            mSubSensorStatus = status
            setSensorData(subSensor.sensorType)
            setEnableState(status.isActive,subSensor.sensorType,sensor.sensorStatus.paramsLocked)
            setUCFStatus(status.ucfLoaded,subSensor.sensorType)
            setOdr(subSensor.odr, status.odr, status.isActive, subSensor.sensorType, sensor.sensorStatus.paramsLocked)
            setFullScale(subSensor.fs,status.fs, status.isActive, subSensor.sensorType, sensor.sensorStatus.paramsLocked)
            setFullScaleUnit(subSensor.unit)
            setSample(subSensor.samplesPerTs,status.samplesPerTs)
        }

        private fun setFullScaleUnit(unit: String?) {
            if (unit!= null)
                mFsUnit.text = mFsUnit.context.getString(R.string.subSensor_fullScaleUnitFormat, unit)
            else {
                mFsUnit.text = ""
            }
        }

        private fun setOdr(odrValues: List<Double>?, currentValue: Double?, isActive:Boolean, sensorType:SensorType, lockParams:Boolean) {
            if(odrValues == null || odrValues.isEmpty()) {
                mOdrSelector.visibility = View.GONE
                return
            } else {
                if(!lockParams && (sensorType != SensorType.MLC && sensorType != SensorType.STREDL) && isActive) {
                    mOdrViews.visibility = View.VISIBLE
                } else {
                    mOdrViews.visibility = View.GONE
                }
            }

            val selectedIndex = if(currentValue !=null) {
                val index = odrValues.indexOf(currentValue)
                if(index >0) index else 0
            }else{
                0
            }

            val spinnerAdapter = ArrayAdapter(mOdrSelector.context,
                    android.R.layout.simple_spinner_item, odrValues).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            mOdrSelector.adapter = spinnerAdapter
            mOdrSelector.setSelection(selectedIndex)
        }

        private fun setFullScale(fsValues: List<Double>?, currentValue: Double?, isActive:Boolean, sensorType:SensorType, lockParams:Boolean) {
            if(fsValues == null || fsValues.isEmpty()) {
                mFsSelector.visibility = View.GONE;
                return
            } else {
                if(!lockParams && (sensorType != SensorType.MLC && sensorType != SensorType.STREDL) && isActive) {
                    mFsViews.visibility = View.VISIBLE
                } else {
                    mFsViews.visibility = View.GONE
                }
            }

            val selectedIndex = if(currentValue !=null) {
                val index = fsValues.indexOf(currentValue)
                if(index >0) index else 0
            }else{
                0
            }

            val spinnerAdapter = ArrayAdapter(mFsSelector.context,
                    android.R.layout.simple_spinner_item, fsValues).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            mFsSelector.adapter = spinnerAdapter
            mFsSelector.setSelection(selectedIndex)
        }

        private fun setEnableState(newState:Boolean,sensorType: SensorType, lockParams:Boolean){
            mEnabledSwitch.setOnCheckedChangeListener(null)
            mEnabledSwitch.isChecked = newState
            displaySensorConfigurationViews(newState, lockParams, sensorType)
            mEnabledSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
        }

        private fun setUCFStatus(newState:Boolean,sensorType: SensorType){
            if (sensorType == SensorType.MLC || sensorType == SensorType.STREDL) {
                mUCFLoadedChip.setOnCheckedChangeListener(null)
                mUCFLoadedChip.isChecked = newState
                if (newState){
                    mUCFLoadedChip.setText(R.string.ucf_file_loaded)
                    mUCFLoadedChip.setTextColor(Color.parseColor("#FFFFFF"))
                    mUCFLoadedChip.setChipIconResource(R.drawable.ic_done)
                    mMLCLoadButton.setText(R.string.subSensor_change_mlc)
                } else {
                    mUCFLoadedChip.setText(R.string.ucf_file_not_loaded)
                    mUCFLoadedChip.setTextColor(Color.parseColor("#E6007E"))
                    mUCFLoadedChip.setChipIconResource(R.drawable.ic_invalid)
                    mMLCLoadButton.setText(R.string.subSensor_load_mlc)
                }
                mUCFLoadedChip.setOnCheckedChangeListener(onUCFStatusChangeListener)
                mEnabledSwitch.isEnabled = newState
            }
        }

        private fun displaySensorConfigurationViews(showIt: Boolean, lockParams: Boolean, sensorType: SensorType){
            if(showIt){
                if(sensorType == SensorType.MLC || sensorType == SensorType.STREDL) {
                    mMLCLoadButton.visibility= View.VISIBLE
                    mUCFLoadedChip.visibility= View.VISIBLE
                    mFsViews.visibility = View.GONE
                    mOdrViews.visibility = View.GONE
                } else if(sensorType == SensorType.CLASS) {
                    mMLCLoadButton.visibility= View.GONE
                    mUCFLoadedChip.visibility= View.GONE
                    mFsViews.visibility = View.GONE
                    mOdrViews.visibility = View.GONE
                } else {
                    if (lockParams){
                        mMLCLoadButton.visibility= View.GONE
                        mUCFLoadedChip.visibility= View.GONE
                        mFsViews.visibility = View.GONE
                        mOdrViews.visibility = View.GONE
                    } else {
                        mMLCLoadButton.visibility= View.GONE
                        mUCFLoadedChip.visibility= View.GONE
                        mFsViews.visibility = View.VISIBLE
                        mOdrViews.visibility = View.VISIBLE
                    }
                }
            }else{
                if(sensorType == SensorType.MLC || sensorType == SensorType.STREDL) {
                    mMLCLoadButton.visibility = View.VISIBLE
                    mUCFLoadedChip.visibility = View.VISIBLE
                    mFsViews.visibility = View.GONE
                    mOdrViews.visibility = View.GONE
                }else{
                    mMLCLoadButton.visibility = View.GONE
                    mUCFLoadedChip.visibility = View.GONE
                    mFsViews.visibility = View.GONE
                    mOdrViews.visibility = View.GONE
                }
            }
        }

        private fun setSample(settings:SamplesPerTs,currentValue: Int?){
            val inputChecker = CheckIntNumberRange(mSampleTSLayout, R.string.subSensor_sampleErrorFromat, settings.min,
                    settings.max)
            mSampleTSValue.addTextChangedListener(inputChecker)
            val value = currentValue ?: settings.min
            mSampleTSValue.setText(value.toString())
        }

        private fun setSensorData(sensorType: SensorType) {
            mIcon.setImageResource(sensorType.imageResource)
            mName.setText(sensorType.nameResource)
        }
    }
}