package com.st.BlueMS.demos.PnPL

import android.R
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueMS.databinding.PnplContentElementBinding
import com.st.BlueSTSDK.Features.PnPL.PnPLComponent
import com.st.BlueSTSDK.Features.PnPL.PnPLContent
import com.st.BlueSTSDK.Features.PnPL.PnPLEnumValue


class MinMaxFilter : InputFilter {
    private var mIntMin: Int
    private var mIntMax: Int

    constructor(minValue: Int, maxValue: Int) {
        mIntMin = minValue
        mIntMax = maxValue
    }

    constructor(minValue: String, maxValue: String) {
        mIntMin = minValue.toInt()
        mIntMax = maxValue.toInt()
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toInt()
            if (isInRange(mIntMin, mIntMax, input)) return null
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}

class PnPLSubContentAdapter(
    private val component: PnPLComponent,
    private val content: PnPLContent,
    private val sub_content_list: List<PnPLContent>,
    private val subContChangedListener: OnSubContentChangedListener) :
    RecyclerView.Adapter<PnPLSubContentAdapter.PnPLSubContentViewHolder>() {

    private var subContentList = sub_content_list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PnPLSubContentAdapter.PnPLSubContentViewHolder {
        val binding = PnplContentElementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PnPLSubContentViewHolder(binding)
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onBindViewHolder(holder: PnPLSubContentAdapter.PnPLSubContentViewHolder, position: Int) {
        val currentItem = subContentList[position]

        holder.subContentNameTextView.text = currentItem.cont_display_name

        if (currentItem.cont_info != null) {
            if(currentItem.cont_schema == "string"){
                holder.subContentValueStringEditText.visibility = View.VISIBLE
                holder.subContentValueStringEditText.text = currentItem.cont_info.toString().toEditable()
                if(!currentItem.cont_writable!!) holder.subContentValueStringEditText.isEnabled = false
            } else if (currentItem.cont_schema == "double"){
                holder.subContentValueDoubleEditText.visibility = View.VISIBLE
                holder.subContentValueDoubleEditText.text = currentItem.cont_info.toString().toEditable()
                val minValueField = subContentList.find { it.cont_name == "min" }
                val maxValueField = subContentList.find { it.cont_name == "max" }
                if(minValueField != null && maxValueField != null){
                    holder.subContentValueDoubleEditText.filters = arrayOf<InputFilter>(MinMaxFilter(minValueField.cont_info.toString(), maxValueField.cont_info.toString()))
                }
                if(!currentItem.cont_writable!!) holder.subContentValueDoubleEditText.isEnabled = false
            } else if (currentItem.cont_schema == "integer"){
                holder.subContentValueIntegerEditText.visibility = View.VISIBLE
                holder.subContentValueIntegerEditText.text = currentItem.cont_info.toString().toEditable()
                val minValueField = subContentList.find { it.cont_name == "min" }
                val maxValueField = subContentList.find { it.cont_name == "max" }
                if(minValueField != null && maxValueField != null){
                    holder.subContentValueDoubleEditText.filters = arrayOf<InputFilter>(MinMaxFilter(minValueField.cont_info.toString(), maxValueField.cont_info.toString()))
                }
                if(!currentItem.cont_writable!!) holder.subContentValueIntegerEditText.isEnabled = false
            } else if (currentItem.cont_schema == "boolean"){
                holder.subContentValueBooleanSwitch.visibility = View.VISIBLE
                holder.subContentValueBooleanSwitch.isChecked = currentItem.cont_info.toString().toBoolean()
                if(!currentItem.cont_writable!!) holder.subContentValueBooleanSwitch.isEnabled = false
            } else if (currentItem.cont_schema == "enum_int" || currentItem.cont_schema == "enum_string"){ //TODO test enum_string
                holder.subContentValueEnumSpinner.visibility = View.VISIBLE
                val valueList = mutableListOf<Any>()
                for (e in currentItem.cont_info as List<*>){
                    valueList.add((e as PnPLEnumValue).displayName)
                }
                val spinnerAdapter = ArrayAdapter(holder.subContentValueEnumSpinner.context,
                    R.layout.simple_spinner_item, valueList).apply {
                    setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                }
                holder.subContentValueEnumSpinner.adapter = spinnerAdapter
                val enumPosition = currentItem.cont_enum_pos
                if(enumPosition != null) {
                    holder.subContentValueEnumSpinner.setSelection(enumPosition)
                }
                if (!currentItem.cont_writable!!) holder.subContentValueEnumSpinner.isEnabled = false
            }
            else {
                Log.e("ERROR","Content schema not supported for: ${currentItem.cont_display_name}")
            }
        }
        else {
            if(currentItem.cont_schema == "string"){
                holder.subContentValueStringEditText.visibility = View.VISIBLE
                if(!currentItem.cont_writable!!) holder.subContentValueStringEditText.isEnabled = false
                holder.subContentValueStringEditText.imeOptions = EditorInfo.IME_ACTION_SEND;
            } else if (currentItem.cont_schema == "double"){
                holder.subContentValueDoubleEditText.visibility = View.VISIBLE
                if(!currentItem.cont_writable!!) holder.subContentValueDoubleEditText.isEnabled = false
            } else if (currentItem.cont_schema == "integer"){
                holder.subContentValueIntegerEditText.visibility = View.VISIBLE
                if(!currentItem.cont_writable!!) holder.subContentValueIntegerEditText.isEnabled = false
            } else if (currentItem.cont_schema == "boolean"){
                holder.subContentValueBooleanSwitch.visibility = View.VISIBLE
                if(!currentItem.cont_writable!!) holder.subContentValueBooleanSwitch.isEnabled = false
            } else if (currentItem.cont_schema == "enum_int" || currentItem.cont_schema == "enum_string"){ //TODO test enum_string
                holder.subContentValueEnumSpinner.visibility = View.VISIBLE
                val valueList = mutableListOf<Any>()
                for (e in currentItem.cont_info as List<*>){
                    valueList.add((e as PnPLEnumValue).displayName)
                }
                val spinnerAdapter = ArrayAdapter(
                    holder.subContentValueEnumSpinner.context,
                    R.layout.simple_spinner_item, valueList
                ).apply {
                    setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                }
                holder.subContentValueEnumSpinner.adapter = spinnerAdapter
                if(!currentItem.cont_writable!!) holder.subContentValueEnumSpinner.isEnabled = false
            }
            else {
                Log.e("ERROR","Content schema not supported for: ${currentItem.cont_display_name}")
            }
        }
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return subContentList.size
    }

    inner class PnPLSubContentViewHolder(binding: PnplContentElementBinding) : RecyclerView.ViewHolder(binding.root){
        private var mSubContent: PnPLContent? = null
        val subContentNameTextView: TextView = binding.pnplContName
        val subContentValueStringEditText: EditText = binding.pnplContValueString //String LineEdit
        val subContentValueDoubleEditText: EditText = binding.pnplContValueDouble //Double(Float) LineEdit with Double Validator
        val subContentValueIntegerEditText: EditText = binding.pnplContValueInteger //Integer LineEdit with Integer Validator
        val subContentValueBooleanSwitch: SwitchCompat = binding.pnplContValueBoolean //Boolean Switch
        val subContentValueEnumSpinner: Spinner = binding.pnplContValueEnum //Enum Spinner

        private val onContentBooleanChangeListener  = object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                val subContent = mSubContent ?: return
                subContChangedListener(component, content, subContent, isChecked)
            }
        }

        private val onContentStringChangeListener  = object : View.OnFocusChangeListener{
            override fun onFocusChange(textView: View?, hasFocus: Boolean) {
                if (textView!!.isEnabled) {
                    if (!hasFocus) {
                        val subContent = mSubContent ?: return
                        val value = (textView as EditText).text.toString()
                        if (value.isNotEmpty()) {
                            subContChangedListener(component, content, subContent, value)
                        }
                    }
                }
            }
        }

        private val onContentDoubleChangeListener  = object : View.OnFocusChangeListener{
            override fun onFocusChange(textView: View?, hasFocus: Boolean) {
                if (textView!!.isEnabled) {
                    if (!hasFocus) {
                        val subContent = mSubContent ?: return
                        val value = (textView as EditText).text.toString()
                        if (value.isNotEmpty()) {
                            subContChangedListener(component, content, subContent, value.toDouble())
                        }
                    }
                }
            }
        }

        private val onContentIntegerChangeListener  = object : View.OnFocusChangeListener{
            override fun onFocusChange(textView: View?, hasFocus: Boolean) {
                if (textView!!.isEnabled) {
                    if (!hasFocus) {
                        val subContent = mSubContent ?: return
                        val value = (textView as EditText).text.toString()
                        if (value.isNotEmpty()) {
                            subContChangedListener(component, content, subContent, value.toInt())
                        }
                    }
                }
            }
        }

        private val onContentEnumChangeListener  = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val subContent = mSubContent ?: return
                val enumList = (content.cont_info as List <*>)
                subContentList.find { subContent.cont_name == it.cont_name }?.cont_enum_pos = position
                subContChangedListener(component, content, subContent, (enumList[position] as PnPLEnumValue).enumValue)
            }
        }

        init {
            subContentValueBooleanSwitch.setOnCheckedChangeListener(onContentBooleanChangeListener)
            subContentValueStringEditText.onFocusChangeListener = onContentStringChangeListener
            subContentValueDoubleEditText.onFocusChangeListener = onContentDoubleChangeListener
            subContentValueIntegerEditText.onFocusChangeListener = onContentIntegerChangeListener
            subContentValueEnumSpinner.onItemSelectedListener = onContentEnumChangeListener
        }

        fun bind(subContent: PnPLContent){
            mSubContent = subContent
        }
    }

}