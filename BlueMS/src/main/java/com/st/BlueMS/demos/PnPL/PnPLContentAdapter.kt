package com.st.BlueMS.demos.PnPL

import android.R
import android.text.Editable
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.st.BlueMS.databinding.PnplContentElementBinding
import com.st.BlueSTSDK.Features.PnPL.*
import com.st.STWINBoard_Gui.Utils.OnUserSelectedListener
import com.st.STWINBoard_Gui.Utils.onUserSelectedItemListener


class PnPLContentAdapter(
    private val component: PnPLComponent,
    private val content_list: List<PnPLContent>,
    private val contChangedListener: OnContentChangedListener,
    private val subContChangedListener: OnSubContentChangedListener,
    private val commandSentListener: OnCommandSentListener,
    private val loadfileListener: OnLoadFilePressedListener
) :
    RecyclerView.Adapter<PnPLContentAdapter.PnPLContentViewHolder>() {

    private var contentList = content_list

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PnPLContentAdapter.PnPLContentViewHolder {
        val binding =
            PnplContentElementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PnPLContentViewHolder(binding)
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onBindViewHolder(holder: PnPLContentAdapter.PnPLContentViewHolder, position: Int) {
        val currentItem = contentList[position]

        holder.contentNameTextView.text = currentItem.cont_display_name

        if (currentItem.cont_type.contains("Property")) {
            holder.commandSendButton.visibility = View.GONE
            if (currentItem.cont_info != null) {
                if (currentItem.cont_schema == "string") {
                    holder.contentValueStringEditText.visibility = View.VISIBLE
                    holder.contentValueStringEditText.text =
                        currentItem.cont_info.toString().toEditable()
                    if (!currentItem.cont_writable!!) holder.contentValueStringEditText.isEnabled =
                        false
                } else if (currentItem.cont_schema == "double") {
                    holder.contentValueDoubleEditText.visibility = View.VISIBLE
                    holder.contentValueDoubleEditText.text =
                        currentItem.cont_info.toString().toEditable()
                    if (!currentItem.cont_writable!!) holder.contentValueDoubleEditText.isEnabled =
                        false
                } else if (currentItem.cont_schema == "integer") {
                    holder.contentValueIntegerEditText.visibility = View.VISIBLE
                    holder.contentValueIntegerEditText.text =
                        currentItem.cont_info.toString().toEditable()
                    if (!currentItem.cont_writable!!) holder.contentValueIntegerEditText.isEnabled =
                        false
                } else if (currentItem.cont_schema == "boolean") {
                    if (currentItem.file_loaded_status == null) {
                        holder.contentValueBooleanSwitch.visibility = View.VISIBLE
                        holder.contentValueBooleanSwitch.isChecked =
                            currentItem.cont_info.toString().toBoolean()
                        if (!currentItem.cont_writable!!) holder.contentValueBooleanSwitch.isEnabled =
                            false
                    } else {
                        holder.contentNameTextView.visibility = View.GONE
                        holder.commandLoadFileChip.visibility = View.VISIBLE
                        if (currentItem.file_loaded_status == true) {
                            holder.commandLoadFileChip.text = "File loaded"
                        } else {
                            holder.commandLoadFileChip.text = "File not loaded"
                        }
                    }

                } else if (currentItem.cont_schema == "enum_int" || currentItem.cont_schema == "enum_string") { //TODO test enum_string
                    holder.contentValueEnumSpinner.visibility = View.VISIBLE
                    val valueList = mutableListOf<Any>()
                    for (e in currentItem.cont_info as List<*>) {
                        valueList.add((e as PnPLEnumValue).displayName)
                    }
                    val spinnerAdapter = ArrayAdapter(
                        holder.contentValueEnumSpinner.context,
                        R.layout.simple_spinner_item, valueList
                    ).apply {
                        setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    }
                    holder.contentValueEnumSpinner.adapter = spinnerAdapter
                    val enumPosition = currentItem.cont_enum_pos
                    if (enumPosition != null) {
                        holder.contentValueEnumSpinner.setSelection(enumPosition)
                    }
                    if (!currentItem.cont_writable!!) holder.contentValueEnumSpinner.isEnabled =
                        false
                } else {
                    Log.e(
                        "ERROR",
                        "Content schema not supported for: ${currentItem.cont_display_name}"
                    )
                }
            } else if (currentItem.cont_schema == "object") {
                holder.contentListRecyclerView.visibility = View.VISIBLE
                val subContentListAdapter = PnPLSubContentAdapter(
                    component,
                    currentItem,
                    currentItem.sub_cont_list!!,
                    subContChangedListener
                )
                holder.contentListRecyclerView.adapter = subContentListAdapter
            }
        } else if (currentItem.cont_type.contains("Command")) {
            if (currentItem.cont_name == "load_file") {
                holder.commandLoadFileButton.visibility = View.VISIBLE
            } else {
                holder.commandSendButton.visibility = View.VISIBLE
                if (currentItem.sub_cont_list != null) {
                    holder.contentListRecyclerView.visibility = View.VISIBLE
                    val commandFieldsListAdapter = PnPLCommandAdapter(
                        component,
                        currentItem,
                        currentItem.sub_cont_list!!,
                        commandSentListener,
                        holder.commandSendButton
                    )
                    holder.contentListRecyclerView.adapter = commandFieldsListAdapter
                } else {
                    if (currentItem.cont_info != null) {
                        holder.contentValueEnumSpinner.visibility = View.VISIBLE
                        val valueList = mutableListOf<Any>()
                        for (e in currentItem.cont_info as List<*>) {
                            valueList.add((e as PnPLEnumValue).displayName)
                        }
                        val spinnerAdapter = ArrayAdapter(
                            holder.contentValueEnumSpinner.context,
                            R.layout.simple_spinner_item, valueList
                        ).apply {
                            setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                        }
                        holder.contentValueEnumSpinner.adapter = spinnerAdapter
                        val enumPosition = currentItem.cont_enum_pos
                        if (enumPosition != null) {
                            holder.contentValueEnumSpinner.setSelection(enumPosition)
                        }
                    }
                }
            }
        } else if (currentItem.cont_type.contains("Telemetry")) {
            Log.e("ERROR", "Telemetry Components are not yet supported")
        }

        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    inner class PnPLContentViewHolder(binding: PnplContentElementBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var mContent: PnPLContent? = null
        val contentNameTextView: TextView = binding.pnplContName
        val contentValueStringEditText: TextInputEditText =
            binding.pnplContValueString //String LineEdit
        val contentValueDoubleEditText: TextInputEditText =
            binding.pnplContValueDouble //Double(Float) LineEdit with Double Validator
        val contentValueIntegerEditText: TextInputEditText =
            binding.pnplContValueInteger //Integer LineEdit with Integer Validator
        val contentValueBooleanSwitch: SwitchCompat = binding.pnplContValueBoolean //Boolean Switch
        val contentValueEnumSpinner: Spinner = binding.pnplContValueEnum //Enum Spinner
        val contentListRecyclerView: RecyclerView = binding.pnplSubContList //for Object Properties
        val commandSendButton: Button = binding.pnplSendCommand //Button to send Command
        val commandLoadFileButton: Button = binding.pnplCommLoadFile //Button to load a file to send
        val commandLoadFileChip: Chip =
            binding.pnplCommFileLoadCheck //Button to load a file to send


        private val onCommandSentListener = object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val content = mContent ?: return
                commandSentListener(component, content, listOf())
            }
        }

        private val onContentBooleanChangeListener =
            object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                    val content = mContent ?: return
                    contChangedListener(component, content, isChecked)
                }
            }

        private val onCommandLoadFileClickedListener = object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val content = mContent ?: return
                loadfileListener(component, content)
            }
        }

        init {
            contentValueBooleanSwitch.setOnCheckedChangeListener(onContentBooleanChangeListener)
            commandSendButton.setOnClickListener(onCommandSentListener)
            commandLoadFileButton.setOnClickListener(onCommandLoadFileClickedListener)

            contentValueStringEditText.setOnEditorActionListener { v: TextView, actionId: Int, _: KeyEvent? ->
                val content = mContent ?: return@setOnEditorActionListener false
                val newValue = v.text.toString()
                when (actionId) {
                    EditorInfo.IME_ACTION_SEND,
                    EditorInfo.IME_ACTION_NEXT,
                    EditorInfo.IME_ACTION_DONE -> {
                        contChangedListener(component, content, newValue)
                        //v.clearFocus()
                    }
                }
                false
            }

            contentValueDoubleEditText.setOnEditorActionListener { v: TextView, actionId: Int, _: KeyEvent? ->
                val content = mContent ?: return@setOnEditorActionListener false
                val newValue =
                    v.text.toString().toFloatOrNull() ?: return@setOnEditorActionListener false
                when (actionId) {
                    EditorInfo.IME_ACTION_SEND,
                    EditorInfo.IME_ACTION_NEXT,
                    EditorInfo.IME_ACTION_DONE -> {
                        contChangedListener(component, content, newValue.toInt())
                        //v.clearFocus()
                    }
                }
                false
            }

            contentValueIntegerEditText.setOnEditorActionListener { v: TextView, actionId: Int, _: KeyEvent? ->
                val content = mContent ?: return@setOnEditorActionListener false
                val newValue =
                    v.text.toString().toIntOrNull() ?: return@setOnEditorActionListener false
                when (actionId) {
                    EditorInfo.IME_ACTION_SEND,
                    EditorInfo.IME_ACTION_NEXT,
                    EditorInfo.IME_ACTION_DONE -> {
                        contChangedListener(component, content, newValue)
                        //v.clearFocus()
                    }
                }
                false
            }


            contentValueEnumSpinner.onUserSelectedItemListener =
                OnUserSelectedListener(object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val content = mContent ?: return
                        val enumList = (content.cont_info as List<*>)
                        contChangedListener(
                            component,
                            content,
                            (enumList[position] as PnPLEnumValue).enumValue
                        )
                    }
                })
        }

        fun bind(content: PnPLContent) {
            mContent = content
        }
    }
}