package com.st.BlueMS.demos.ExtConfig

import android.R.layout
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Features.ExtConfiguration.CustomCommand
import com.st.BlueSTSDK.Features.ExtConfiguration.FeatureExtConfiguration
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.WifSettings
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation


@DemoDescriptionAnnotation(name = "Board Configuration", iconRes = R.drawable.ic_ext_config_icon,
        requareAll = [FeatureExtConfiguration::class],
        includeOnSettingsGroup = true)
class ExtConfigurationFragment : BaseDemoFragment(), CustomCommandAdapter.OnItemClickListener {

    private lateinit var viewModel: ExtConfigurationViewModel

    private lateinit var mTextReadCert: TextView
    private lateinit var mTextReadUid: TextView
    private lateinit var mTextReadVFw: TextView
    private lateinit var mTextReadVInfo: TextView
    private lateinit var mTextReadVHelp: TextView
    private lateinit var mTextReadVPowerStatus: TextView
    private lateinit var mTextChangePin: TextView
    private lateinit var mTextClearDB: TextView
    private lateinit var mTextDFU: TextView
    private lateinit var mTextPowerOff: TextView
    private lateinit var mTextSetCert: TextView
    private lateinit var mTextSetName: TextView
    private lateinit var mTextSetTime: TextView
    private lateinit var mTextSetDate: TextView
    private lateinit var mTextSetWiFi: TextView
    private lateinit var mTextCustomCommands: TextView
    private lateinit var mProgressLayoutBar: LinearLayout

    private lateinit var mTextCardInfo: TextView
    private lateinit var mTextCardSecurity: TextView
    private lateinit var mTextCardControl: TextView
    private lateinit var mTextCardSetting: TextView

    private lateinit var mCardCustomCommands: CardView
    private val adapterCustomCommand = CustomCommandAdapter(this)
    private lateinit var recyclerViewCustomCommand: RecyclerView
    private lateinit var  mTextCardCustom : TextView

    private lateinit var mLinearLayoutInfo: LinearLayout
    private lateinit var mLinearLayoutSecurity: LinearLayout
    private lateinit var mLinearLayoutControl: LinearLayout
    private lateinit var mLinearLayoutSetting: LinearLayout

    private var showMcuIdDialog = true;

    private var customCommand = listOf<CustomCommand>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Inflate the layout
        val rootView = inflater.inflate(R.layout.fragment_extended_settings, container, false)

        // Get the ViewModel
        viewModel = ViewModelProvider(this).get(ExtConfigurationViewModel::class.java)

        //Find the ProgressBar
        mProgressLayoutBar = rootView.findViewById(R.id.ExSettProgress)

        // find all the LinearLayout
        mLinearLayoutInfo = rootView.findViewById(R.id.ExtSettInfoLinearLayout)
        mLinearLayoutSecurity = rootView.findViewById(R.id.ExtSettSecurityLinearLayout)
        mLinearLayoutControl = rootView.findViewById(R.id.ExtSettControlLinearLayout)
        mLinearLayoutSetting = rootView.findViewById(R.id.ExtSettSettingLinearLayout)

        // Find all TextView
        mTextCardInfo = rootView.findViewById(R.id.ExtSettInfoText)
        mTextCardSecurity = rootView.findViewById(R.id.ExtSettSecurityText)
        mTextCardControl = rootView.findViewById(R.id.ExtSettControlText)
        mTextCardSetting = rootView.findViewById(R.id.ExtSettSettingText)

        mTextReadUid = rootView.findViewById(R.id.ExtSettReadUidText)
        mTextReadVFw = rootView.findViewById(R.id.ExtSettReadVFwText)
        mTextReadVInfo = rootView.findViewById(R.id.ExtSettReadVInfoText)
        mTextReadVHelp = rootView.findViewById(R.id.ExtSettReadVHelpText)
        mTextReadVPowerStatus = rootView.findViewById(R.id.ExtSettReadVPowerStatusText)

        mTextReadCert = rootView.findViewById(R.id.ExtSettReadCertText)
        mTextSetCert = rootView.findViewById(R.id.ExtSettSetCertText)
        mTextChangePin = rootView.findViewById(R.id.ExtSettChangePinText)
        mTextClearDB = rootView.findViewById(R.id.ExtSettClearDBText)

        mTextDFU = rootView.findViewById(R.id.ExtSettDFUText)
        mTextPowerOff = rootView.findViewById(R.id.ExtSettPowerOffText)

        mTextCustomCommands = rootView.findViewById(R.id.ExtSettCustomCommands)
        mTextSetName = rootView.findViewById(R.id.ExtSettSetNameText)
        mTextSetTime = rootView.findViewById(R.id.ExtSettSetTimeText)
        mTextSetDate = rootView.findViewById(R.id.ExtSettSetDateText)
        mTextSetWiFi = rootView.findViewById(R.id.ExtSettSetWiFiText)

        //Set the Callbacks for setOnClick listener
        mTextCardInfo.setOnClickListener { controlCard(mLinearLayoutInfo) }
        mTextCardSecurity.setOnClickListener { controlCard(mLinearLayoutSecurity) }
        mTextCardControl.setOnClickListener { controlCard(mLinearLayoutControl) }
        mTextCardSetting.setOnClickListener { controlCard(mLinearLayoutSetting) }

        mTextReadUid.setOnClickListener { readUid() }
        mTextReadVFw.setOnClickListener { readvFw() }
        mTextReadVInfo.setOnClickListener { readInfo() }
        mTextReadVHelp.setOnClickListener { readHelp() }
        mTextReadVPowerStatus.setOnClickListener { readPowStatus() }

        mTextReadCert.setOnClickListener { readCert() }
        mTextChangePin.setOnClickListener { setPIN() }
        mTextClearDB.setOnClickListener { clearDB() }

        mTextDFU.setOnClickListener { setDFU() }
        mTextPowerOff.setOnClickListener { powerOff() }

        mTextSetName.setOnClickListener { setName() }
        mTextSetTime.setOnClickListener { setTime() }
        mTextSetDate.setOnClickListener { setDate() }
        mTextSetWiFi.setOnClickListener { setWifi() }
        mTextCustomCommands.setOnClickListener { readCustomCommands() }

        //Set the CustomCommands section
        mCardCustomCommands = rootView.findViewById(R.id.ExtSettCustomCommandsCard)
        mTextCardCustom     = rootView.findViewById(R.id.ExtSettCustomCommandsText)
        //Set the Recycler View
        recyclerViewCustomCommand = rootView.findViewById(R.id.ExtSettCustomCommandsRecycler)
        recyclerViewCustomCommand.adapter = adapterCustomCommand
        recyclerViewCustomCommand.layoutManager = LinearLayoutManager(context)
        //recyclerViewCustomCommand.setHasFixedSize(true)
        mTextCardCustom.setOnClickListener {
            if (recyclerViewCustomCommand.visibility == View.VISIBLE) {
                recyclerViewCustomCommand.visibility = View.GONE
            } else {
                recyclerViewCustomCommand.visibility = View.VISIBLE
            }}

        return rootView
    }

    private fun controlCard(layout: LinearLayout) {
        if (layout.visibility == View.VISIBLE) {
            layout.visibility = View.GONE
        } else {
            layout.visibility = View.VISIBLE
        }
    }

    private fun attachCommandCommandList() {
        viewModel.commandlist_answer.observe(viewLifecycleOwner, Observer { newString ->
            if (newString != null) {
                mTextReadUid.isEnabled = newString.contains(FeatureExtConfiguration.READ_UID)
                //mTextReadUid.isEnabled = false
                if (mTextReadUid.isEnabled == false) {
                    node?.let { viewModel.setMCUID(node!!.tag.replace(":", "")) }
                } else {
                    if (viewModel.getMCUID() == null) {
                        showMcuIdDialog = false
                        //Ask to the board the STM32 Unique ID
                        readUid();
                    }
                }

                viewModel.commandListReceived()

                mTextReadVFw.isEnabled = newString.contains(FeatureExtConfiguration.READ_VERSION_FW)
                mTextReadVInfo.isEnabled = newString.contains(FeatureExtConfiguration.READ_INFO)
                mTextReadVHelp.isEnabled = newString.contains(FeatureExtConfiguration.READ_HELP)
                mTextReadVPowerStatus.isEnabled = newString.contains(FeatureExtConfiguration.READ_POWER_STATUS)

                mTextChangePin.isEnabled = newString.contains(FeatureExtConfiguration.CHANGE_PIN)
                mTextClearDB.isEnabled = newString.contains(FeatureExtConfiguration.CLEAR_DB)
//                mTextReadCert.isEnabled = newString.contains(FeatureExtConfiguration.READ_CERTIFICATE)
//                mTextSetCert.isEnabled = newString.contains(FeatureExtConfiguration.SET_CERTIFICATE)

                mTextDFU.isEnabled = newString.contains(FeatureExtConfiguration.SET_DFU)
                mTextPowerOff.isEnabled = newString.contains(FeatureExtConfiguration.POWER_OFF)

                mTextSetName.isEnabled = newString.contains(FeatureExtConfiguration.SET_NAME)
                mTextSetTime.isEnabled = newString.contains(FeatureExtConfiguration.SET_TIME)
                mTextSetDate.isEnabled = newString.contains(FeatureExtConfiguration.SET_DATE)
                mTextSetWiFi.isEnabled = newString.contains(FeatureExtConfiguration.SET_WIFI)
                mTextCustomCommands.isEnabled = newString.contains(FeatureExtConfiguration.READ_CUSTOM_COMMANDS)

                //Enable each Card that contains at least a valid command
                if (!((mTextReadUid.isEnabled) || (mTextReadVFw.isEnabled) || (mTextReadVInfo.isEnabled) || (mTextReadVHelp.isEnabled) || (mTextReadVPowerStatus.isEnabled))) {
                    mTextCardInfo.isEnabled = false
                    mLinearLayoutInfo.visibility = View.GONE
                }

                if (!((mTextChangePin.isEnabled) || (mTextClearDB.isEnabled) || (mTextReadCert.isEnabled) || (mTextSetCert.isEnabled))) {
                    mTextCardSecurity.isEnabled = false
                    mLinearLayoutSecurity.visibility = View.GONE
                }

                if (!((mTextDFU.isEnabled) || (mTextPowerOff.isEnabled))) {
                    mTextCardControl.isEnabled = false
                    mLinearLayoutControl.visibility = View.GONE
                }

                if (!((mTextSetName.isEnabled) || (mTextSetTime.isEnabled) || (mTextSetDate.isEnabled) || (mTextSetWiFi.isEnabled) || (mTextCustomCommands.isEnabled))) {
                    mTextCardSetting.isEnabled = false
                    mLinearLayoutSetting.visibility = View.GONE
                }

                val certificate = viewModel.getRetrivedCertiticate()
                certificate?.let { viewModel.setCert(certificate) }
                viewModel.setRetrivedCertificate(null)
            }
        })
    }

    private fun attachCommandCustomCommandList() {
        viewModel.customcommandlist_answer.observe(viewLifecycleOwner, Observer { newList ->
            if (newList != null) {
                customCommand = newList
                mProgressLayoutBar.visibility = View.GONE
                //viewModel.customcommandlist_answer.removeObservers(viewLifecycleOwner)
                adapterCustomCommand.updateCustomCommandList(newList)
                //viewModel.customCommandListReceived()
            }
        })
    }

    private fun attachCommandHelp() {
        viewModel.help_answer.observe(viewLifecycleOwner, Observer { newString ->
            if (newString != null) {
                mProgressLayoutBar.visibility = View.GONE
                val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                alertDialog.setTitle("Help")
                alertDialog.setMessage(newString)
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
                viewModel.helpReceived()
            }
        })
    }

    private fun attachCommandInfo() {
        viewModel.info_answer.observe(viewLifecycleOwner, Observer { newString ->
            if (newString != null) {
                mProgressLayoutBar.visibility = View.GONE
                val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                alertDialog.setTitle("Info")
                alertDialog.setMessage(newString)
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
                viewModel.infoReceived()
            }
        })
    }

    private fun attachCommandUID() {
        viewModel.uid_answer.observe(viewLifecycleOwner, Observer { newString ->
            if (newString != null) {
                if (showMcuIdDialog) {
                    val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                    alertDialog.setTitle("STM32 UID")
                    alertDialog.setMessage(newString)
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alertDialog.show()
                } else {
                    showMcuIdDialog = true
                }
                viewModel.setMCUID(newString)
                viewModel.uidReceived()
            }
        })
    }

    private fun attachCommandPowerStatus() {
        viewModel.powerstatus_answer.observe(viewLifecycleOwner, Observer { newString ->
            if (newString != null) {
                val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                alertDialog.setTitle("Power Status")
                alertDialog.setMessage(newString)
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
                viewModel.powerstatusReceived()
            }
        })
    }

    private fun attachCommandVersionFW() {
        viewModel.versionfw_answer.observe(viewLifecycleOwner, Observer { newString ->
            if (newString != null) {
                val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                alertDialog.setTitle("Version FW")
                alertDialog.setMessage(newString)
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
                viewModel.versionfwReceived()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachCommandCommandList()
        attachCommandPowerStatus()
        attachCommandHelp()
        attachCommandVersionFW()
        attachCommandUID()
        attachCommandInfo()
        attachCommandCustomCommandList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.commandlist_answer.removeObservers(viewLifecycleOwner)
        viewModel.certificate_answer.removeObservers(viewLifecycleOwner)
        viewModel.versionfw_answer.removeObservers(viewLifecycleOwner)
        viewModel.powerstatus_answer.removeObservers(viewLifecycleOwner)
        viewModel.uid_answer.removeObservers(viewLifecycleOwner)
        viewModel.info_answer.removeObservers(viewLifecycleOwner)
        viewModel.help_answer.removeObservers(viewLifecycleOwner)
        viewModel.customcommandlist_answer.removeObservers(viewLifecycleOwner)
    }

    override fun enableNeededNotification(node: Node) {

        showIntroductionMessage("In this section it's possible to configure the Firmware running on the board", context);

        viewModel.enableNotification(node)
    }

    override fun disableNeedNotification(node: Node) {
        viewModel.disableNotification(node)
    }

    private fun readPowStatus() {
        viewModel.readPowStatus()
    }

    private fun readHelp() {
        mProgressLayoutBar.visibility = View.VISIBLE
        viewModel.readHelp()
    }

    private fun readvFw() {
        viewModel.readvFw()
    }

    private fun readUid() {
        viewModel.readUid()
    }

    private fun readCert() {
        mProgressLayoutBar.visibility = View.VISIBLE
        viewModel.readCert()
    }

    private fun readInfo() {
        mProgressLayoutBar.visibility = View.VISIBLE
        viewModel.readInfo()
    }

    private fun readCustomCommands() {
        //attachCommandCustomCommandList()
        mCardCustomCommands.visibility = View.VISIBLE
        mProgressLayoutBar.visibility = View.VISIBLE
        viewModel.readCustomCommands()
    }

    private fun setWifi() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

        // Get the layout inflater

        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        val v = inflater.inflate(R.layout.wifi_credentials, null)

        val securityTypeList = listOf("OPEN", "WEP", "WPA", "WPA2", "WPA/WPA2")
        val securitySpinner = v.findViewById<Spinner>(R.id.wifi_security)
        val dataAdapter = ArrayAdapter(requireActivity(), layout.simple_spinner_item, securityTypeList)
        val ssidTextView = v.findViewById<TextView>(R.id.wifi_ssid)
        val passwdTextView = v.findViewById<TextView>(R.id.wifi_password)
        var adapterPosition = 0;
        securitySpinner.adapter = dataAdapter

        securitySpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                adapterPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapterPosition = 0
            }
        }


        builder.setView(v)

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        builder.setPositiveButton("Send to Node") { dialog: DialogInterface, _: Int ->
            val wifiConf = WifSettings(enable = true, ssid = ssidTextView.text.toString(), password = passwdTextView.text.toString(), securityType = dataAdapter.getItem(adapterPosition).toString())
            viewModel.setWiFi(wifiConf)
            dialog.dismiss()
        }
        builder.create()
        builder.show()
    }

    private fun setName() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        builder.setTitle("Set the Board Name")

        val customLayout = layoutInflater.inflate(R.layout.set_input_extconfig, null);
        builder.setView(customLayout);
        val editText: EditText = customLayout.findViewById(R.id.extconfig_text_input)
        val inputTex: TextInputLayout = customLayout.findViewById(R.id.extconfig_text_input_layout)

        //Set the Default Name
        editText.hint = node?.name

        // Specify the type of input expected;
        editText.inputType = InputType.TYPE_CLASS_TEXT
        inputTex.counterMaxLength = 7
        editText.filters = arrayOf<InputFilter>(LengthFilter(7))

        builder.setPositiveButton("OK") { _: DialogInterface, _: Int -> viewModel.setName(editText.text.toString()) }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        builder.show()
    }

    private fun setPIN() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        builder.setTitle("Change the PIN")

        val customLayout = layoutInflater.inflate(R.layout.set_input_extconfig, null);
        builder.setView(customLayout);
        val editText: EditText = customLayout.findViewById(R.id.extconfig_text_input)
        val inputTex: TextInputLayout = customLayout.findViewById(R.id.extconfig_text_input_layout)

        //Set the Default Name
        editText.hint = 123456.toString()

        // Specify the type of input expected;
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        inputTex.counterMaxLength = 6
        editText.filters = arrayOf<InputFilter>(LengthFilter(6))

        // Set up the buttons
        builder.setPositiveButton("OK") { _: DialogInterface, _: Int -> viewModel.setPIN(editText.text.toString().toInt()) }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        builder.show()
    }

    private fun clearDB() {
        viewModel.clearDB()
        Toast.makeText(requireActivity(), "Security Database Cleared", Toast.LENGTH_SHORT).show()
    }

    private fun setDFU() {
        viewModel.setDFU()
        Toast.makeText(requireActivity(), "The Board will reboot on DFU Mode", Toast.LENGTH_SHORT).show()
    }

    private fun powerOff() {
        viewModel.powerOff()
        Toast.makeText(requireActivity(), "The Board will be switched off", Toast.LENGTH_SHORT).show()
    }

    private fun setTime() {
        viewModel.setTime()
        Toast.makeText(requireActivity(), "Time set", Toast.LENGTH_SHORT).show()
    }

    private fun setDate() {
        viewModel.setDate()
        Toast.makeText(requireActivity(), "Data set", Toast.LENGTH_SHORT).show()
    }

    /* for the Custom Command Adapter */
    override fun onItemClick(position: Int) {
        when (customCommand[position].type) {
            "String" -> {
                customCommandDialogString(position)
            }
            "Integer" -> {
                customCommandDialogInteger(position)
            }
            "Boolean" -> {
                customCommandDialogBoolean(position)
            }
            "Void" -> {
                //Send Directly the Command without creating the Dialog
                viewModel.sendCustomCommandVoid(customCommand[position].name)
            }
        }
    }

    private fun customCommandDialogInteger(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

        if(customCommand[position].description!=null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null);
        builder.setView(customLayout);
        val inputTex: TextInputLayout = customLayout.findViewById(R.id.custom_command_number)
        inputTex.visibility=View.VISIBLE
        val editText = inputTex.editText
        val helpText: TextView = customLayout.findViewById(R.id.custom_command_help)

        // Retrieve Max and Minimum allowed value
        val min = customCommand[position].min
        val max = customCommand[position].max

        val helpString = "$min <= Valid Value <= $max"
        helpText.text = helpString

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        // Check the input
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Check if it's empty
                val textInput: String = editText.text.toString()
                if (textInput.isEmpty()) {
                    editText.error = "Field can't be empty";
                } else {
                    editText.error = null
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val textInput: String = editText.text.toString()
                if (textInput.isEmpty()) {
                    editText.error = "Field can't be empty";
                    buttonPositive.isEnabled = false
                    buttonPositive.visibility = View.INVISIBLE
                } else {
                    if ((textInput[0] != '-') || (textInput.length > 1)) {
                        when {
                            Integer.parseInt(textInput) < min -> {
                                editText.error = "Field must be > $min";
                                buttonPositive.isEnabled = false
                                buttonPositive.visibility = View.INVISIBLE
                            }
                            Integer.parseInt(textInput) > max -> {
                                editText.error = "Field must be < $max ";
                                buttonPositive.isEnabled = false
                                buttonPositive.visibility = View.INVISIBLE
                            }
                            else -> {
                                editText.error = null
                                buttonPositive.isEnabled = true
                                buttonPositive.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        val dialog = builder.create();
        dialog.show();

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            if(editText!=null) {
                viewModel.sendCustomCommandInteger(customCommand[position].name, Integer.parseInt(editText.text.toString()))
                dialog.dismiss()
            }
        }
    }

    private fun customCommandDialogString(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

        if(customCommand[position].description!=null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null);
        builder.setView(customLayout);

        val inputText: TextInputLayout = customLayout.findViewById(R.id.custom_command_string)
        inputText.visibility=View.VISIBLE
        val helpText: TextView = customLayout.findViewById(R.id.custom_command_help)
        val editText = inputText.editText

        // Retrieve Max and Minimum allowed value
        val min = customCommand[position].min

        val max = customCommand[position].max

        inputText.counterMaxLength = max

        val helpString = "$min chars <= Valid Entry <= $max chars"
        helpText.text = helpString

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        //if the custom command allows a command with a string of 0 char dimension
        if(min==0) {
            buttonPositive.isEnabled= true
            buttonPositive.visibility= View.VISIBLE
        }

        // Check the input
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Check if it's empty
                val textInput: String = editText.text.toString().trim()
                if (textInput.isEmpty() && (min != 0)) {
                    editText.error = "Field can't be empty";
                } else {
                    editText.error = null
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val textInput: String = editText.text.toString().trim()
                if (textInput.isEmpty() && (min != 0)) {
                    editText.error = "Field can't be empty";
                    buttonPositive.isEnabled = false
                    buttonPositive.visibility = View.INVISIBLE
                } else if (textInput.length < min) {
                    buttonPositive.isEnabled = false
                    buttonPositive.visibility = View.INVISIBLE
                    editText.error = "Field must be at least $min chars";
                } else if (textInput.length > max) {
                    editText.error = "Field must be less than $max chars";
                    buttonPositive.isEnabled = false
                    buttonPositive.visibility = View.INVISIBLE
                } else {
                    editText.error = null
                    buttonPositive.isEnabled = true
                    buttonPositive.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        val dialog = builder.create();
        dialog.show();

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            viewModel.sendCustomCommandString(customCommand[position].name, editText?.text.toString())
            dialog.dismiss()
        }
    }

    private fun customCommandDialogBoolean(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

        if(customCommand[position].description!=null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null);
        builder.setView(customLayout);
        val inputSwitch: SwitchCompat = customLayout.findViewById(R.id.custom_command_boolean)
        inputSwitch.visibility = View.VISIBLE

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        buttonPositive.isEnabled=true;
        buttonPositive.visibility= View.VISIBLE

        val dialog = builder.create();
        dialog.show();

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            viewModel.sendCustomCommandString(customCommand[position].name, inputSwitch.isChecked.toString())
            dialog.dismiss()
        }
    }
}