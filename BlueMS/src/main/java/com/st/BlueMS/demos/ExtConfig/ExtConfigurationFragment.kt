package com.st.BlueMS.demos.ExtConfig

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.*
import android.text.InputFilter.LengthFilter
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Features.ExtConfiguration.CustomCommand
import com.st.BlueSTSDK.Features.ExtConfiguration.FeatureExtConfiguration
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.WifSettings
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.fwDataBase.ReadBoardFirmwareDataBase
import com.st.BlueSTSDK.fwDataBase.db.BoardFirmware
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeActivity
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate


@DemoDescriptionAnnotation(
    name = "Board Configuration", iconRes = R.drawable.ic_ext_config_icon,
    requireAll = [FeatureExtConfiguration::class],
    demoCategory = ["Configuration"]
)
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
    private lateinit var mTextReadBanksStatus: TextView
    private lateinit var mTextBanksSwap: TextView
    private lateinit var mTextSetCert: TextView
    private lateinit var mTextSetName: TextView
    private lateinit var mTextSetTime: TextView
    private lateinit var mTextSetDate: TextView
    private lateinit var mTextSetWiFi: TextView
    private lateinit var mTextSetSensors: TextView
    private lateinit var mTextCustomCommands: TextView
    private lateinit var mProgressLayoutBar: LinearLayout

    private lateinit var mHorLayTextCardInfo: LinearLayout
    private lateinit var mHorLayTextCardSecurity: LinearLayout
    private lateinit var mHorLayTextCardControl: LinearLayout
    private lateinit var mHorLayTextCardSetting: LinearLayout

    private lateinit var mCardCustomCommands: CardView
    private val adapterCustomCommand = CustomCommandAdapter(this)
    private lateinit var recyclerViewCustomCommand: RecyclerView
    private lateinit var mHorLayTextCardCustom: LinearLayout

    private lateinit var mLinearLayoutInfo: LinearLayout
    private lateinit var mLinearLayoutSecurity: LinearLayout
    private lateinit var mLinearLayoutControl: LinearLayout
    private lateinit var mLinearLayoutSetting: LinearLayout

    private var showMcuIdDialog = true

    private var customCommand = listOf<CustomCommand>()

    var myDownloadId: Long = -1
    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == myDownloadId) {
                val manager =
                    context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val uri = manager.getUriForDownloadedFile(myDownloadId)
                val intent = FwUpgradeActivity.getStartIntent(context, node, false, uri)
                startActivity(intent)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        val rootView = inflater.inflate(R.layout.fragment_extended_settings, container, false)

        // Get the ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(ExtConfigurationViewModel::class.java)
        viewModel.context = context

        //Find the ProgressBar
        mProgressLayoutBar = rootView.findViewById(R.id.ExSettProgress)

        // find all the LinearLayout
        mLinearLayoutInfo = rootView.findViewById(R.id.ExtSettInfoLinearLayout)
        mLinearLayoutSecurity = rootView.findViewById(R.id.ExtSettSecurityLinearLayout)
        mLinearLayoutControl = rootView.findViewById(R.id.ExtSettControlLinearLayout)
        mLinearLayoutSetting = rootView.findViewById(R.id.ExtSettSettingLinearLayout)

        // Find all LinearLayout that contain the Card Names
        mHorLayTextCardInfo = rootView.findViewById(R.id.ExtSettInfoHorLayText)
        mHorLayTextCardSecurity = rootView.findViewById(R.id.ExtSettSecurityHorLayText)
        mHorLayTextCardControl = rootView.findViewById(R.id.ExtSettControlHorLayText)
        mHorLayTextCardSetting = rootView.findViewById(R.id.ExtSettSettingHorLayText)

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
        mTextReadBanksStatus = rootView.findViewById(R.id.ExtSettReadBanksStatusText)
        mTextBanksSwap = rootView.findViewById(R.id.ExtSettBanksSwapText)

        mTextCustomCommands = rootView.findViewById(R.id.ExtSettCustomCommands)
        mTextSetName = rootView.findViewById(R.id.ExtSettSetNameText)
        mTextSetTime = rootView.findViewById(R.id.ExtSettSetTimeText)
        mTextSetDate = rootView.findViewById(R.id.ExtSettSetDateText)
        mTextSetWiFi = rootView.findViewById(R.id.ExtSettSetWiFiText)
        mTextSetSensors = rootView.findViewById(R.id.ExtSettSetSensorsText)

        val mImageInfo:ImageView = rootView.findViewById(R.id.ExtSettInfoCollaps)
        val mImageSecurity: ImageView = rootView.findViewById(R.id.ExtSettSecurityCollaps)
        val mImageControl: ImageView = rootView.findViewById(R.id.ExtSettControlCollaps)
        val mImageSetting: ImageView = rootView.findViewById(R.id.ExtSettSettingCollaps)

        //Set the Callbacks for setOnClick listener
        mHorLayTextCardInfo.setOnClickListener { controlCard(mLinearLayoutInfo,mImageInfo) }
        mHorLayTextCardSecurity.setOnClickListener { controlCard(mLinearLayoutSecurity,mImageSecurity) }
        mHorLayTextCardControl.setOnClickListener { controlCard(mLinearLayoutControl,mImageControl) }
        mHorLayTextCardSetting.setOnClickListener { controlCard(mLinearLayoutSetting,mImageSetting) }

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
        mTextReadBanksStatus.setOnClickListener { readBanksStatus() }
        mTextBanksSwap.setOnClickListener { banksSwap() }

        mTextSetName.setOnClickListener { setName() }
        mTextSetTime.setOnClickListener { setTime() }
        mTextSetDate.setOnClickListener { setDate() }
        mTextSetWiFi.setOnClickListener { setWifi() }
        mTextSetSensors.setOnClickListener { readSensors() }
        mTextCustomCommands.setOnClickListener { readCustomCommands() }

        //Set the CustomCommands section
        mCardCustomCommands = rootView.findViewById(R.id.ExtSettCustomCommandsCard)
        mHorLayTextCardCustom = rootView.findViewById(R.id.ExtSettCustomCommandsHorLayText)
        //Set the Recycler View
        recyclerViewCustomCommand = rootView.findViewById(R.id.ExtSettCustomCommandsRecycler)
        recyclerViewCustomCommand.adapter = adapterCustomCommand
        recyclerViewCustomCommand.layoutManager = LinearLayoutManager(context)
        //recyclerViewCustomCommand.setHasFixedSize(true)

        val mImageCustom: ImageView = rootView.findViewById(R.id.ExtSettCustomCommandsCollaps)
        mHorLayTextCardCustom.setOnClickListener {
            if (recyclerViewCustomCommand.visibility == View.VISIBLE) {
                recyclerViewCustomCommand.visibility = View.GONE
                mImageCustom.setImageResource(R.drawable.ic_expand_view)
            } else {
                recyclerViewCustomCommand.visibility = View.VISIBLE
                mImageCustom.setImageResource(R.drawable.ic_collaps_view)
            }
        }
        return rootView
    }

    private fun controlCard(layout: LinearLayout, image: ImageView) {
        if (layout.visibility == View.VISIBLE) {
            layout.visibility = View.GONE
            image.setImageResource(R.drawable.ic_expand_view)
        } else {
            layout.visibility = View.VISIBLE
            image.setImageResource(R.drawable.ic_collaps_view)
        }
    }

    private fun attachCommandCommandList() {
        viewModel.commandlist_answer.observe(viewLifecycleOwner, Observer { newString ->
            if (newString != null) {
                mTextReadUid.isEnabled = newString.contains(FeatureExtConfiguration.READ_UID)
                //mTextReadUid.isEnabled = false
                if (mTextReadUid.isEnabled == false) {
                    node?.let { viewModel.setMcuId(node!!.tag.replace(":", "")) }
                } else {
                    if (viewModel.getMcuId() == null) {
                        showMcuIdDialog = false
                        //Ask to the board the STM32 Unique ID
                        readUid()
                    }
                }

                viewModel.commandListReceived()

                enableStandardCommand(
                    mTextReadVFw,
                    newString.contains(FeatureExtConfiguration.READ_VERSION_FW)
                )

                enableStandardCommand(
                    mTextReadUid,
                    newString.contains(FeatureExtConfiguration.READ_UID)
                )

                enableStandardCommand(
                    mTextReadVInfo,
                    newString.contains(FeatureExtConfiguration.READ_INFO)
                )
                enableStandardCommand(
                    mTextReadVHelp,
                    newString.contains(FeatureExtConfiguration.READ_HELP)
                )
                enableStandardCommand(
                    mTextReadVPowerStatus,
                    newString.contains(FeatureExtConfiguration.READ_POWER_STATUS)
                )

                enableStandardCommand(
                    mTextChangePin,
                    newString.contains(FeatureExtConfiguration.CHANGE_PIN)
                )
                enableStandardCommand(
                    mTextClearDB,
                    newString.contains(FeatureExtConfiguration.CLEAR_DB)
                )
                enableStandardCommand(
                    mTextReadCert,
                    newString.contains(FeatureExtConfiguration.READ_CERTIFICATE)
                )
                enableStandardCommand(
                    mTextSetCert,
                    newString.contains(FeatureExtConfiguration.SET_CERTIFICATE)
                )

                enableStandardCommand(mTextDFU, newString.contains(FeatureExtConfiguration.SET_DFU))
                enableStandardCommand(
                    mTextPowerOff,
                    newString.contains(FeatureExtConfiguration.POWER_OFF)
                )
                enableStandardCommand(
                    mTextReadBanksStatus,
                    newString.contains(FeatureExtConfiguration.BANKS_STATUS)
                )
                enableStandardCommand(
                    mTextBanksSwap,
                    newString.contains(FeatureExtConfiguration.BANKS_SWAP)
                )

                enableStandardCommand(
                    mTextSetName,
                    newString.contains(FeatureExtConfiguration.SET_NAME)
                )
                enableStandardCommand(
                    mTextSetTime,
                    newString.contains(FeatureExtConfiguration.SET_TIME)
                )
                enableStandardCommand(
                    mTextSetDate,
                    newString.contains(FeatureExtConfiguration.SET_DATE)
                )
                enableStandardCommand(
                    mTextSetWiFi,
                    newString.contains(FeatureExtConfiguration.SET_WIFI)
                )
                enableStandardCommand(
                    mTextSetSensors,
                    newString.contains(FeatureExtConfiguration.READ_SENSORS)
                )
                enableStandardCommand(
                    mTextCustomCommands,
                    newString.contains(FeatureExtConfiguration.READ_CUSTOM_COMMANDS)
                )


                //Enable each Card that contains at least a valid command
                if (!((mTextReadUid.isEnabled) || (mTextReadVFw.isEnabled) || (mTextReadVInfo.isEnabled) || (mTextReadVHelp.isEnabled) || (mTextReadVPowerStatus.isEnabled))) {
                    mHorLayTextCardInfo.isEnabled = false
                    mLinearLayoutInfo.visibility = View.GONE
                }

                if (!((mTextChangePin.isEnabled) || (mTextClearDB.isEnabled) || (mTextReadCert.isEnabled) || (mTextSetCert.isEnabled))) {
                    mHorLayTextCardSecurity.isEnabled = false
                    mLinearLayoutSecurity.visibility = View.GONE
                }

                if (!((mTextDFU.isEnabled) || (mTextPowerOff.isEnabled) || (mTextReadBanksStatus.isEnabled) || (mTextBanksSwap.isEnabled))) {
                    mHorLayTextCardControl.isEnabled = false
                    mLinearLayoutControl.visibility = View.GONE
                }

                if (!((mTextSetName.isEnabled) || (mTextSetTime.isEnabled) || (mTextSetDate.isEnabled) || (mTextSetWiFi.isEnabled) || (mTextCustomCommands.isEnabled) || (mTextSetSensors.isEnabled))) {
                    mHorLayTextCardSetting.isEnabled = false
                    mLinearLayoutSetting.visibility = View.GONE
                }

                val certificate = viewModel.getRetrievedCertiticate()
                certificate?.let { viewModel.setCert(certificate) }
                viewModel.setRetrievedCertificate(null)
            }
        })
    }

    private fun enableStandardCommand(textView: TextView, isPresent: Boolean) {
        textView.isEnabled = isPresent
        if (isPresent) {
            textView.visibility = View.VISIBLE
        }
    }

    private fun attachCommandCustomCommandList() {
        viewModel.customcommandlist_answer.observe(viewLifecycleOwner, Observer { newList ->
            if (newList != null) {
                customCommand = newList
                mProgressLayoutBar.visibility = View.GONE
                adapterCustomCommand.updateCustomCommandList(newList)
                viewModel.customCommandReceived()
            }
        })
    }

    private fun attachFwUri() {
        viewModel.fw_uri_path.observe(viewLifecycleOwner, Observer { fileName ->
            if (fileName != null) {
                viewModel.fwUriPathReceived()
                val request = DownloadManager.Request(Uri.parse(fileName))
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setTitle(fileName.substringAfterLast("/"))
                    .setAllowedOverMetered(true)
                val dm = context?.let { getSystemService(it, DownloadManager::class.java) }
                if (dm != null) {
                    myDownloadId = dm.enqueue(request)
                }
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

    private fun attachCommandReadBanksStatus() {
        viewModel.read_banks_status_answer.observe(viewLifecycleOwner, Observer { banksStatus ->
            if (banksStatus != null) {
                mProgressLayoutBar.visibility = View.GONE
                viewModel.readBanksStatusReceived()

                val builder = androidx.appcompat.app.AlertDialog.Builder(
                    requireActivity(),
                    R.style.DialogWithOutTitleFullScreen
                )

                val customLayout = layoutInflater.inflate(R.layout.flash_banks_status, null)
                builder.setView(customLayout)

                builder.setTitle("Flash Memory Banks Status")

                val currentMemoryBankTextView =
                    customLayout.findViewById<TextView>(R.id.flash_banks_current_memory_bank)

                val fwBank1TextView =
                    customLayout.findViewById<TextView>(R.id.flash_banks_status_fw_bank1)
                val fwBank2TextView =
                    customLayout.findViewById<TextView>(R.id.flash_banks_status_fw_bank2)
                val wBank2AvailableTextView =
                    customLayout.findViewById<TextView>(R.id.flash_banks_status_fw_bank2_available)

                val banksSwapButton =
                    customLayout.findViewById<Button>(R.id.flash_banks_status_swap_button)

                val textUpdateChangeLogTitle =
                    customLayout.findViewById<TextView>(R.id.flash_banks_status_update_Available_text)

                val textChangeLogTitle =
                    customLayout.findViewById<TextView>(R.id.flash_banks_status_update_text_changelog_title)
                val textChangeLog =
                    customLayout.findViewById<TextView>(R.id.flash_banks_status_update_text_changelog)

                val spinner = customLayout.findViewById<Spinner>(R.id.flash_banks_status_spinner)

                val textSelectOneFw =
                    customLayout.findViewById<TextView>(R.id.flash_banks_status_select_one_firmware_text)

                val textSelectOneFwDescriptionTitle =
                    customLayout.findViewById<TextView>(R.id.flash_banks_status_select_one_firmware_text_description_title)

                val textSelectOneFwDescription =
                    customLayout.findViewById<TextView>(R.id.flash_banks_status_select_one_firmware_text_description)
                val buttonPositive =
                    customLayout.findViewById<Button>(R.id.flash_banks_status_command_positive)
                val buttonNegative =
                    customLayout.findViewById<Button>(R.id.flash_banks_status_command_negative)

                val firmwareDB = context?.let { ReadBoardFirmwareDataBase(it) }
                val fwUpgradeModel: BoardFirmware?

                var adapterSpinnerPosition = 0

                currentMemoryBankTextView.text = "Current Bank [${banksStatus.currentBank}]"

                val listFwUpdate: List<BoardFirmware>? =
                    if ((node!!.fwDetails != null) && (firmwareDB != null)) {
                        firmwareDB.getAllFwForFwName(
                            node!!.fwDetails.ble_dev_id,
                            node!!.fwDetails.fw_name
                        )?.filter { it.fota.fw_url != null }
                    } else
                        null

                val fwDetails2 =
                    if (banksStatus.fwId2 != FeatureExtConfiguration.FLASH_FW_ID_NOT_VALID) {
                        firmwareDB?.getFwDetailsNode(node!!.typeId.toInt(), banksStatus.fwId2)
                    } else {
                        null
                    }

                //Search if there are one fw different respect the actual running, with a valid fw url
                var listFwCompatible: List<BoardFirmware>? =
                    if ((node!!.fwDetails != null) && (firmwareDB != null)) {
                        firmwareDB.getListOfFwCompatibleWithBoardId(
                            node!!.fwDetails.ble_dev_id
                        )
                            ?.filter { (it.fw_name != node!!.fwDetails.fw_name) || (it.fw_version != node!!.fwDetails.fw_version) }
                            ?.filter { it.fota.bootloader_type==node!!.fwDetails.fota.bootloader_type}
                            ?.filter { it.fota.fw_url != null }
                    } else
                        null


                if ((listFwCompatible != null) && (fwDetails2 != null)) {
                    //Remove the fw with the same name and version of the current one on the other bank
                    listFwCompatible =
                        listFwCompatible.filter { (it.fw_name != fwDetails2.fw_name) || (it.fw_version != fwDetails2.fw_version) }
                }

                if (listFwUpdate != null) {
                    //fwUpgradeModel = listFwUpdate.maxByOrNull { it.fw_version }
                    //If we are running for example Firmware V1.0.0  and there are like updates V1.1.0 and V1.2.0..
                    // we propose like update the Lower version one
                    fwUpgradeModel =
                        if (fwDetails2 != null) {
                            //Remove the Update if it's already present on other bank
                            listFwUpdate.filter { it.fw_version > node!!.fwDetails.fw_version }
                                .filter { (it.fw_version != fwDetails2.fw_version) && (it.fw_name != fwDetails2.fw_name) }
                                .minByOrNull { it.fw_version }
                        } else {
                            listFwUpdate.filter { it.fw_version > node!!.fwDetails.fw_version }
                                .minByOrNull { it.fw_version }
                        }

                    if (fwUpgradeModel != null) {
                        //if (fwUpgradeModel.fw_version != node!!.fwDetails.fw_version) {
                        textUpdateChangeLogTitle.visibility = View.VISIBLE
                        val fullString =
                            "Update Available:\n  ${fwUpgradeModel.fw_name} V${fwUpgradeModel.fw_version}"
                        textUpdateChangeLogTitle.text = fullString
                        fwUpgradeModel.changelog?.let { it1 ->
                            textChangeLogTitle.visibility = View.VISIBLE
                            textChangeLog.visibility = View.VISIBLE
                            textChangeLog.movementMethod = ScrollingMovementMethod()
                            textChangeLog.text = fwUpgradeModel.changelog
                        }
                        //}
                    }
                } else {
                    fwUpgradeModel = null
                }

                val fwDetails = node!!.fwDetails
                if (fwDetails != null) {
                    // the FwId1 must be != FLASH_FW_ID_NOT_VALID...
                    val fullFw = "${fwDetails.fw_name} V${fwDetails.fw_version}"
                    fwBank1TextView.text = fullFw
                } else {
                    fwBank1TextView.text = "unknown"
                }

                if (!listFwCompatible.isNullOrEmpty()) {
                    spinner.visibility = View.VISIBLE
                    textSelectOneFw.visibility = View.VISIBLE
                    textSelectOneFwDescriptionTitle.visibility = View.VISIBLE
                    textSelectOneFwDescription.visibility = View.VISIBLE
                    textSelectOneFwDescription.movementMethod = ScrollingMovementMethod()
                    buttonPositive.alpha = 1f
                    buttonPositive.isEnabled = true

                    val dataAdapter = ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item,
                        listFwCompatible.map { it.fw_name + " V" + it.fw_version }).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    adapterSpinnerPosition = 0

                    spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View,
                                position: Int,
                                id: Long
                            ) {
                                adapterSpinnerPosition = position
                                listFwCompatible[adapterSpinnerPosition].fw_desc.let { it1 ->
                                    textSelectOneFwDescription.text = it1
                                }
                                val fullString =
                                    "Install\n${listFwCompatible[adapterSpinnerPosition].fw_name} V${listFwCompatible[adapterSpinnerPosition].fw_version}"
                                buttonPositive.text = fullString
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                adapterSpinnerPosition = 0
                            }
                        }

                    spinner.adapter = dataAdapter
                }


                val dialog = builder.create()
                dialog.show()

                if ((fwUpgradeModel != null) && (listFwCompatible != null)) {
                    val updatePositionInList =
                        listFwCompatible.indexOfFirst { (it.fw_name == fwUpgradeModel.fw_name) || (it.fw_version == fwUpgradeModel.fw_version) }
                    spinner.setSelection(updatePositionInList)
                }

                if (fwDetails2 != null) {
                    val fullFw = "${fwDetails2.fw_name} V${fwDetails2.fw_version}"
                    fwBank2TextView.text = fullFw
                    wBank2AvailableTextView.visibility = View.VISIBLE

                    banksSwapButton.isEnabled = true
                    banksSwapButton.visibility = View.VISIBLE
                    banksSwapButton.setOnClickListener {
                        banksSwap()
                        dialog.dismiss()
                    }
                } else {
                    fwBank2TextView.text = "No Firmware Present"
                }

                buttonNegative.setOnClickListener { dialog.dismiss() }

                buttonPositive.setOnClickListener {
                    listFwCompatible?.let {
                        listFwCompatible[adapterSpinnerPosition].let {
                            listFwCompatible[adapterSpinnerPosition].fota.fw_url?.let { it1 ->
                                viewModel.fwUriPath(it1)
                            }
                        }
                    }
                    dialog.dismiss()
                }
            }
        })
    }


    private fun attachCommandError() {
        viewModel.error_answer.observe(viewLifecycleOwner, Observer { newString ->
            if (newString != null) {
                val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                val titleText: String = "ERROR!"

                // Initialize a new foreground color span instance
                val foregroundColorSpan = ForegroundColorSpan(Color.RED)

                // Initialize a new spannable string builder instance
                val ssBuilder = SpannableStringBuilder(titleText)

                // Apply the text color span
                ssBuilder.setSpan(
                    foregroundColorSpan,
                    0,
                    titleText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                alertDialog.setTitle(ssBuilder)
                alertDialog.setMessage(newString)
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
                viewModel.errorReceived()
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
                viewModel.setMcuId(newString)
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
                viewModel.powerStatusReceived()
            }
        })
    }

    private fun attachCommandVersionFw() {
        viewModel.versionfw_answer.observe(viewLifecycleOwner, Observer { newString ->
            if (newString != null) {
                val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                alertDialog.setTitle("Version FW")
                alertDialog.setMessage(newString)

                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
                viewModel.versionFwReceived()
            }
        })
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachCommandCommandList()
        attachCommandPowerStatus()
        attachCommandHelp()
        attachCommandVersionFw()
        attachCommandUID()
        attachCommandInfo()
        attachCommandReadBanksStatus()
        attachCommandError()
        attachCommandCustomCommandList()
        attachFwUri()

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

        requireActivity().registerReceiver(broadCastReceiver, filter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.commandlist_answer.removeObservers(viewLifecycleOwner)
        viewModel.certificate_answer.removeObservers(viewLifecycleOwner)
        viewModel.versionfw_answer.removeObservers(viewLifecycleOwner)
        viewModel.powerstatus_answer.removeObservers(viewLifecycleOwner)
        viewModel.uid_answer.removeObservers(viewLifecycleOwner)
        viewModel.info_answer.removeObservers(viewLifecycleOwner)
        viewModel.error_answer.removeObservers(viewLifecycleOwner)
        viewModel.help_answer.removeObservers(viewLifecycleOwner)
        viewModel.customcommandlist_answer.removeObservers(viewLifecycleOwner)
        viewModel.read_banks_status_answer.removeObservers(viewLifecycleOwner)
        viewModel.fw_uri_path.removeObservers(viewLifecycleOwner)

        requireActivity().unregisterReceiver(broadCastReceiver)
    }

    override fun enableNeededNotification(node: Node) {

        showIntroductionMessage(
            "In this section it's possible to configure the Firmware running on the board",
            context
        )

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
        mCardCustomCommands.visibility = View.VISIBLE
        mProgressLayoutBar.visibility = View.VISIBLE
        viewModel.readCustomCommands()
    }

    private fun readSensors() {
        val sensorConfig = SensorConfigDialogFragment(node)
        sensorConfig.show(parentFragmentManager, CONFIG_SENSORS_DIALOG_TAG)
    }

    private fun setWifi() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        val v = inflater.inflate(R.layout.wifi_credentials, null)

        val securityTypeList = listOf("OPEN", "WEP", "WPA", "WPA2", "WPA/WPA2")
        val securitySpinner = v.findViewById<Spinner>(R.id.wifi_security)

        val dataAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item, securityTypeList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val ssidTextView = v.findViewById<TextView>(R.id.wifi_ssid)
        val passwdTextView = v.findViewById<TextView>(R.id.wifi_password)
        var adapterPosition = 0
        securitySpinner.adapter = dataAdapter

        securitySpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                adapterPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapterPosition = 0
            }
        }


        builder.setView(v)

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        builder.setPositiveButton("Send to Node") { dialog: DialogInterface, _: Int ->
            val wifiConf = WifSettings(
                enable = true,
                ssid = ssidTextView.text.toString(),
                password = passwdTextView.text.toString(),
                securityType = dataAdapter.getItem(adapterPosition).toString()
            )
            viewModel.setWiFi(wifiConf)
            dialog.dismiss()
            snackBarWithConfirmation("Wi-Fi Credential Sent to Board")

        }
        builder.create()
        builder.show()
    }

    private fun setName() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        builder.setTitle("Set the Board Name")

        val customLayout = layoutInflater.inflate(R.layout.set_input_extconfig, null)
        builder.setView(customLayout)
        val editText: EditText = customLayout.findViewById(R.id.extconfig_text_input)
        val inputTex: TextInputLayout = customLayout.findViewById(R.id.extconfig_text_input_layout)

        //Set the Default Name
        editText.hint = node?.name

        // Specify the type of input expected
        editText.inputType = InputType.TYPE_CLASS_TEXT
        inputTex.counterMaxLength = 7
        editText.filters = arrayOf<InputFilter>(LengthFilter(7))

        builder.setPositiveButton("OK") { _: DialogInterface, _: Int ->
            run {
                viewModel.setName(editText.text.toString())
                snackBarWithConfirmation("The Board will change the name after the disconnection")
            }
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        builder.show()
    }

    private fun snackBarWithConfirmation(message: String) {
        view?.let {
            val snack = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
            snack.setAction("OK") { snack.dismiss() }
            snack.show()
        }
    }



    private fun setPIN() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        builder.setTitle("Change the PIN")

        val customLayout = layoutInflater.inflate(R.layout.set_input_extconfig, null)
        builder.setView(customLayout)
        val editText: EditText = customLayout.findViewById(R.id.extconfig_text_input)
        val inputTex: TextInputLayout = customLayout.findViewById(R.id.extconfig_text_input_layout)

        //Set the Default Name
        editText.hint = 123456.toString()

        // Specify the type of input expected
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        inputTex.counterMaxLength = 6
        editText.filters = arrayOf<InputFilter>(LengthFilter(6))

        // Set up the buttons
        builder.setPositiveButton("OK") { _: DialogInterface, _: Int ->
            run {
                viewModel.setPIN(editText.text.toString().toInt())
                snackBarWithConfirmation("The Board will use the new PIN")
            }
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        builder.show()
    }

    private fun clearDB() {
        viewModel.clearDB()
        snackBarWithConfirmation("Board Security Database Cleared")
    }

    private fun setDFU() {
        viewModel.setDFU()
        snackBarWithConfirmation("The Board will Reboot on DFU Mode")
    }

    private fun powerOff() {
        viewModel.powerOff()
        snackBarWithConfirmation("The Board will be switched off")
    }

    private fun banksSwap() {
        viewModel.banksSwap()
        snackBarWithConfirmation("The Board will Reboot at disconnection")
    }

    private fun readBanksStatus() {
        viewModel.readBanksStatus()
    }

    private fun setTime() {
        viewModel.setTime()
        snackBarWithConfirmation("Board Time synchronized")
    }

    private fun setDate() {
        viewModel.setDate()
        snackBarWithConfirmation("Board Date synchronized")
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
            "EnumInteger" -> {
                customCommandDialogEnumInteger(position)
            }
            "EnumString" -> {
                customCommandDialogEnumString(position)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mCardCustomCommands.visibility=View.GONE
    }

    private fun customCommandDialogInteger(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

        if (customCommand[position].description != null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null)
        builder.setView(customLayout)
        val inputTex: TextInputLayout = customLayout.findViewById(R.id.custom_command_number)
        inputTex.visibility = View.VISIBLE
        val editText = inputTex.editText
        val helpText: TextView = customLayout.findViewById(R.id.custom_command_help)

        // Retrieve Max and Minimum allowed value
        val min = customCommand[position].min
        val max = customCommand[position].max

        val helpString = "$min <= Valid Value <= $max"
        helpText.text = helpString

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        if(customCommand[position].default_value!=null) {
            if (editText != null) {
                editText.hint = customCommand[position].default_value.toString()
            }
        }

        // Check the input
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Check if it's empty
                val textInput: String = editText.text.toString()
                if (textInput.isEmpty()) {
                    editText.error = "Field can't be empty"
                } else {
                    editText.error = null
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val textInput: String = editText.text.toString()
                if (textInput.isEmpty()) {
                    editText.error = "Field can't be empty"
                    buttonPositive.isEnabled = false
                    buttonPositive.visibility = View.INVISIBLE
                } else {
                    if ((textInput[0] != '-') || (textInput.length > 1)) {
                        when {
                            Integer.parseInt(textInput) < min -> {
                                editText.error = "Field must be > $min"
                                buttonPositive.isEnabled = false
                                buttonPositive.visibility = View.INVISIBLE
                            }
                            Integer.parseInt(textInput) > max -> {
                                editText.error = "Field must be < $max "
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

        val dialog = builder.create()
        dialog.show()

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            if (editText != null) {
                viewModel.sendCustomCommandInteger(
                    customCommand[position].name,
                    Integer.parseInt(editText.text.toString())
                )
                dialog.dismiss()
            }
        }
    }

    private fun customCommandDialogEnumInteger(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

        if (customCommand[position].description != null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null)
        builder.setView(customLayout)
        val spinner: Spinner = customLayout.findViewById(R.id.custom_command_enum)

        val dataAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item, customCommand[position].integerValues
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        var adapterPosition = 0
        spinner.visibility = View.VISIBLE
        spinner.adapter = dataAdapter

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        buttonPositive.isEnabled = true
        buttonPositive.visibility = View.VISIBLE

        if(customCommand[position].default_value!=null) {
            if(customCommand[position].default_value!!<customCommand[position].integerValues.size) {
                spinner.setSelection(customCommand[position].default_value!!)
            }
        }

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                adapterPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapterPosition = 0
            }
        }

        val dialog = builder.create()
        dialog.show()

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            viewModel.sendCustomCommandInteger(
                customCommand[position].name,
                customCommand[position].integerValues[adapterPosition]
            )
            dialog.dismiss()
        }
    }

    private fun customCommandDialogEnumString(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

        if (customCommand[position].description != null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null)
        builder.setView(customLayout)
        val spinner: Spinner = customLayout.findViewById(R.id.custom_command_enum)
        val dataAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item, customCommand[position].stringValues
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        var adapterPosition = 0
        spinner.visibility = View.VISIBLE
        spinner.adapter = dataAdapter

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        buttonPositive.isEnabled = true
        buttonPositive.visibility = View.VISIBLE

        if(customCommand[position].default_value!=null) {
            if(customCommand[position].default_value!!<customCommand[position].stringValues.size) {
                spinner.setSelection(customCommand[position].default_value!!)
            }
        }

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                adapterPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapterPosition = 0
            }
        }

        val dialog = builder.create()
        dialog.show()

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            viewModel.sendCustomCommandString(
                customCommand[position].name,
                customCommand[position].stringValues[adapterPosition].toString()
            )
            dialog.dismiss()
        }
    }

    private fun customCommandDialogString(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

        if (customCommand[position].description != null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null)
        builder.setView(customLayout)

        val inputText: TextInputLayout = customLayout.findViewById(R.id.custom_command_string)
        inputText.visibility = View.VISIBLE
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
        if (min == 0) {
            buttonPositive.isEnabled = true
            buttonPositive.visibility = View.VISIBLE
        }

        // Check the input
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Check if it's empty
                val textInput: String = editText.text.toString().trim()
                if (textInput.isEmpty() && (min != 0)) {
                    editText.error = "Field can't be empty"
                } else {
                    editText.error = null
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val textInput: String = editText.text.toString().trim()
                if (textInput.isEmpty() && (min != 0)) {
                    editText.error = "Field can't be empty"
                    buttonPositive.isEnabled = false
                    buttonPositive.visibility = View.INVISIBLE
                } else if (textInput.length < min) {
                    buttonPositive.isEnabled = false
                    buttonPositive.visibility = View.INVISIBLE
                    editText.error = "Field must be at least $min chars"
                } else if (textInput.length > max) {
                    editText.error = "Field must be less than $max chars"
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

        val dialog = builder.create()
        dialog.show()

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            viewModel.sendCustomCommandString(
                customCommand[position].name,
                editText?.text.toString()
            )
            dialog.dismiss()
        }
    }

    private fun customCommandDialogBoolean(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())

        if (customCommand[position].description != null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null)
        builder.setView(customLayout)
        val inputSwitch: SwitchCompat = customLayout.findViewById(R.id.custom_command_boolean)
        inputSwitch.visibility = View.VISIBLE

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        buttonPositive.isEnabled = true
        buttonPositive.visibility = View.VISIBLE

        if(customCommand[position].default_value!=null) {
            inputSwitch.isChecked = customCommand[position].default_value==1
        }

        val dialog = builder.create()
        dialog.show()

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            viewModel.sendCustomCommandString(
                customCommand[position].name,
                inputSwitch.isChecked.toString()
            )
            dialog.dismiss()
        }
    }

    companion object {
        val CONFIG_SENSORS_DIALOG_TAG =
            ExtConfigurationFragment::class.java.name + ".CONFIG_SENSORS_DIALOG_TAG"
    }
}