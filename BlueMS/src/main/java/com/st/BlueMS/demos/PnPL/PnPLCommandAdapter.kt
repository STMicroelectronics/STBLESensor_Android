package com.st.BlueMS.demos.PnPL

import android.R
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueMS.databinding.PnplContentElementBinding
import com.st.BlueSTSDK.Features.PnPL.PnPLComponent
import com.st.BlueSTSDK.Features.PnPL.PnPLContent
import com.st.BlueSTSDK.Features.PnPL.PnPLEnumValue

class PnPLCommandAdapter(
    private val component: PnPLComponent,
    private val content: PnPLContent,
    private val command_fields: List<PnPLContent>,
    private val commandSentListener: OnCommandSentListener,
    private val command_send_button: Button) :
    RecyclerView.Adapter<PnPLCommandAdapter.PnPLCommandViewHolder>() {

    private var commandFieldList = command_fields

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PnPLCommandAdapter.PnPLCommandViewHolder {
        val binding = PnplContentElementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PnPLCommandViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PnPLCommandAdapter.PnPLCommandViewHolder, position: Int) {
        val currentItem = commandFieldList[position]

        holder.subContentNameTextView.text = currentItem.cont_display_name

        if(currentItem.cont_schema == "string"){
            holder.subContentValueStringEditText.visibility = View.VISIBLE
        } else if (currentItem.cont_schema == "double"){
            holder.subContentValueDoubleEditText.visibility = View.VISIBLE
        } else if (currentItem.cont_schema == "integer"){
            holder.subContentValueIntegerEditText.visibility = View.VISIBLE
        } else if (currentItem.cont_schema == "boolean"){
            holder.subContentValueBooleanSwitch.visibility = View.VISIBLE
        } else if (currentItem.cont_schema == "enum_int" || currentItem.cont_schema == "enum_string"){
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
        }
        else {
            Log.e("ERROR","Content schema not supported for: ${currentItem.cont_display_name}")
        }
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return commandFieldList.size
    }

    inner class PnPLCommandViewHolder(binding: PnplContentElementBinding) : RecyclerView.ViewHolder(binding.root){
        private var mCommandField: PnPLContent? = null
        val subContentNameTextView: TextView = binding.pnplContName
        val subContentValueStringEditText: EditText = binding.pnplContValueString //String LineEdit
        val subContentValueDoubleEditText: EditText = binding.pnplContValueDouble //Double(Float) LineEdit with Double Validator
        val subContentValueIntegerEditText: EditText = binding.pnplContValueInteger //Integer LineEdit with Integer Validator
        val subContentValueBooleanSwitch: SwitchCompat = binding.pnplContValueBoolean //Boolean Switch
        val subContentValueEnumSpinner: Spinner = binding.pnplContValueEnum //Enum Spinner

        private val onCommandSentListener  = object : View.OnClickListener{
            override fun onClick(p0: View?) {
                commandSentListener(component, content, commandFieldList)
            }
        }

        private val onContentBooleanChangeListener  = object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                val commandField = mCommandField ?: return
                commandFieldList.find { commandField.cont_name == it.cont_name }?.cont_info = isChecked
            }
        }

        private val onContentStringChangeListener  = (object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val commandField = mCommandField ?: return
                val value = p0.toString()
                if (value.isNotEmpty()) {
                    commandFieldList.find { commandField.cont_name == it.cont_name }?.cont_info = value
                } else {
                    commandFieldList.find { commandField.cont_name == it.cont_name }?.cont_info = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        private val onContentDoubleChangeListener  = (object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val commandField = mCommandField ?: return
                val value = p0.toString()
                if (value.isNotEmpty()) {
                    commandFieldList.find { commandField.cont_name == it.cont_name }?.cont_info = value.toFloat()
                } else {
                    commandFieldList.find { commandField.cont_name == it.cont_name }?.cont_info = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        private val onContentIntegerChangeListener  = (object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val commandField = mCommandField ?: return
                val value = p0.toString()
                if (value.isNotEmpty()) {
                    commandFieldList.find { commandField.cont_name == it.cont_name }?.cont_info = value.toInt()
                } else {
                    commandFieldList.find { commandField.cont_name == it.cont_name }?.cont_info = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        private val onContentEnumChangeListener  = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val commandField = mCommandField ?: return
                commandFieldList.find { commandField.cont_name == it.cont_name }?.cont_enum_pos = position
            }
        }

        init {
            command_send_button.setOnClickListener(onCommandSentListener)
            subContentValueBooleanSwitch.setOnCheckedChangeListener(onContentBooleanChangeListener)
            subContentValueStringEditText.addTextChangedListener(onContentStringChangeListener)
            subContentValueDoubleEditText.addTextChangedListener(onContentDoubleChangeListener)
            subContentValueIntegerEditText.addTextChangedListener(onContentIntegerChangeListener)
            subContentValueEnumSpinner.onItemSelectedListener = onContentEnumChangeListener
        }

        fun bind(subContent: PnPLContent){
            mCommandField = subContent
        }
    }
}