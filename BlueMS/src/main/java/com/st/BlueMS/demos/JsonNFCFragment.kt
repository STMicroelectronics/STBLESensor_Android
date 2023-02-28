package com.st.BlueMS.demos

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.JsonNFCFeature.FeatureJsonNFC
import com.st.BlueSTSDK.Features.JsonNFCFeature.JsonVCard
import com.st.BlueSTSDK.Features.JsonNFCFeature.JsonWIFI
import com.st.BlueSTSDK.Features.JsonNFCFeature.JsonWriteCommand
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation


@DemoDescriptionAnnotation(name = "NFC Writing",
    iconRes = R.drawable.connectivity_nfc,
    demoCategory = ["Configuration"],
    requireAll = [FeatureJsonNFC::class])
class JsonNFCFragment : BaseDemoFragment() {

    private var mFeature: FeatureJsonNFC?=null

    private lateinit var mWifiCardView: CardView
    private lateinit var mVCardCardView: CardView
    private lateinit var mTextCardView: CardView
    private lateinit var mURLCardView: CardView

    private lateinit var mWifiLinearLayout: LinearLayout
    private lateinit var mWifiCollaps: ImageView

    private lateinit var mVCardCollaps: ImageView
    private lateinit var mVCardLinearLayout: LinearLayout
    private val featureListener = Feature.FeatureListener { _, sample ->

        if(sample is FeatureJsonNFC.FeatureJsonCommandSample) {
            val responseObj = sample.command
            val answers = FeatureJsonNFC.resultCommandReadModes(responseObj)
            var nExpandedCard=0
            if (answers!= null) {
                updateGui {
                    //Show the supported Card
                    //Expand the first 2 of them visible

                    if (answers.toString().contains(FeatureJsonNFC.NFCText)) {
                        mTextCardView.visibility = View.VISIBLE
                        nExpandedCard += 1
                    } else {
                        mTextCardView.visibility = View.GONE
                    }

                    if (answers.toString().contains(FeatureJsonNFC.NFCURL)) {
                        mURLCardView.visibility = View.VISIBLE
                        nExpandedCard += 1
                    } else {
                        mURLCardView.visibility = View.GONE
                    }

                    if (answers.toString().contains(FeatureJsonNFC.NFCWifi)) {
                        mWifiCardView.visibility = View.VISIBLE
                        if(nExpandedCard<2) {
                            nExpandedCard += 1
                        } else {
                            mWifiLinearLayout.isVisible=false
                            mWifiCollaps.setImageResource(R.drawable.ic_expand_view)
                        }
                    } else {
                        mWifiCardView.visibility = View.GONE
                    }

                    if (answers.toString().contains(FeatureJsonNFC.NFCVCard)) {
                        mVCardCardView.visibility = View.VISIBLE
                        if(nExpandedCard<2) {
                            nExpandedCard += 1
                        } else {
                            mVCardLinearLayout.isVisible=false
                            mVCardCollaps.setImageResource(R.drawable.ic_expand_view)
                        }
                    } else {
                        mVCardCardView.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* Inflate the layout */
        val rootView = inflater.inflate(R.layout.fragment_json_nfc_demo, container, false)

        mVCardCardView = rootView.findViewById(R.id.json_nfc_vcard)
        val vCardSendButton: Button = rootView.findViewById(R.id.json_nfc_vcard_write_to_nfc)
        val vCardNameTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_name_input_text)
        val vCardFormattedNameTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_formatted_name_input_text)
        val vCardTitleTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_title_input_text)
        val vCardOrgTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_org_input_text)
        val vCardHomeAddressTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_home_address_input_text)
        val vCardWorkAddressTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_work_address_input_text)
        val vCardAddressTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_address_input_text)
        val vCardHomePhoneTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_home_phone_input_text)
        val vCardWorkPhoneTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_work_phone_input_text)
        val vCardCellularPhoneTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_cellular_phone_input_text)
        val vCardHomeEmailTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_home_email_input_text)
        val vCardWorkEmailTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_work_email_input_text)
        val vCardUrlTextInput: TextView = rootView.findViewById(R.id.json_nfc_vcard_url_input_text)

        vCardSendButton.setOnClickListener {
            val command = JsonWriteCommand(NFCVCard= JsonVCard())

            if(!vCardNameTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.Name = vCardNameTextInput.text.toString()
            }

            if(!vCardFormattedNameTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.FormattedName = vCardFormattedNameTextInput.text.toString()
            }

            if(!vCardTitleTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.Title = vCardTitleTextInput.text.toString()
            }

            if(!vCardOrgTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.Org = vCardOrgTextInput.text.toString()
            }

            if(!vCardHomeAddressTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.HomeAddress = vCardHomeAddressTextInput.text.toString()
            }

            if(!vCardWorkAddressTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.WorkAddress = vCardWorkAddressTextInput.text.toString()
            }

            if(!vCardAddressTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.Address = vCardAddressTextInput.text.toString()
            }

            if(!vCardHomePhoneTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.HomeTel = vCardHomePhoneTextInput.text.toString()
            }

            if(!vCardWorkPhoneTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.WorkTel = vCardWorkPhoneTextInput.text.toString()
            }

            if(!vCardCellularPhoneTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.CellTel = vCardCellularPhoneTextInput.text.toString()
            }

            if(!vCardHomeEmailTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.HomeEmail = vCardHomeEmailTextInput.text.toString()
            }

            if(!vCardWorkEmailTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.WorkEmail = vCardWorkEmailTextInput.text.toString()
            }

            if(!vCardUrlTextInput.text.isNullOrEmpty()) {
                command.NFCVCard?.Url = vCardUrlTextInput.text.toString()
            }
            mFeature?.writeCommandWithArgument(command)
            hideSoftKeyboard(requireActivity(),requireActivity().currentFocus)
            snackBarWithConfirmation("VCard NDEF Record Written on NFC")
        }
        mVCardLinearLayout = rootView.findViewById(R.id.json_nfc_vcard_linear_layout)
        mVCardCollaps = rootView.findViewById(R.id.json_vcard_collaps)
        val vCardHideLinLayout: LinearLayout = rootView.findViewById(R.id.json_nfc_vcard_hide_linear_layout)
        vCardHideLinLayout.setOnClickListener {
            if(mVCardLinearLayout.isVisible) {
                mVCardLinearLayout.isVisible=false
                mVCardCollaps.setImageResource(R.drawable.ic_expand_view)
            } else {
                mVCardLinearLayout.isVisible=true
                mVCardCollaps.setImageResource(R.drawable.ic_collaps_view)
            }
        }

        mWifiCardView = rootView.findViewById(R.id.json_nfc_wifi)
        val wifiSendButton: Button = rootView.findViewById(R.id.json_nfc_wifi_write_to_nfc)
        val wifiSSDIInput: TextView = rootView.findViewById(R.id.json_nfc_wifi_ssid_input_text)
        val wifiPasswordInput: TextView = rootView.findViewById(R.id.json_nfc_wifi_passwd_input_text)

        var wifiAdapterEncrPosition =0
        var wifiAdapterAuthPosition =0

        val authTypeList = FeatureJsonNFC.WifiAuthString.keys.toList()
        val authorizationSpinner = rootView.findViewById<Spinner>(R.id.json_nfc_wifi_auth_type)
        authorizationSpinner.gravity = Gravity.END

        val dataAdapterAuth = ArrayAdapter(
            requireActivity(),
                    android.R.layout.simple_spinner_item, authTypeList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        authorizationSpinner.adapter = dataAdapterAuth

        authorizationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                wifiAdapterAuthPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                wifiAdapterAuthPosition = 0
            }
        }

        val encryptionTypeList = FeatureJsonNFC.WifiEncrString.keys.toList()
        val encryptionSpinner = rootView.findViewById<Spinner>(R.id.json_nfc_wifi_encr_type)
        encryptionSpinner.gravity = Gravity.END

        val dataAdapterEncr = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item, encryptionTypeList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        encryptionSpinner.adapter = dataAdapterEncr

        encryptionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                wifiAdapterEncrPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                wifiAdapterEncrPosition = 0
            }
        }

        wifiSendButton.setOnClickListener {
            val auth =
                FeatureJsonNFC.WifiAuthString[dataAdapterAuth.getItem(wifiAdapterAuthPosition).toString()]
            val encr =
                FeatureJsonNFC.WifiEncrString[dataAdapterEncr.getItem(wifiAdapterEncrPosition).toString()]
            if((auth!=null) && (encr!=null)) {
                val command = JsonWriteCommand(
                    NFCWiFi = JsonWIFI(
                        NetworkSSID = wifiSSDIInput.text.toString(),
                        NetworkKey = wifiPasswordInput.text.toString(),
                        AuthenticationType = auth,
                        EncryptionType = encr
                    )
                )
                mFeature?.writeCommandWithArgument(command)
                hideSoftKeyboard(requireActivity(),requireActivity().currentFocus)
                snackBarWithConfirmation("Wi-Fi NDEF Record Written on NFC")
            }
        }

        mWifiLinearLayout = rootView.findViewById(R.id.json_nfc_wifi_linear_layout)
        mWifiCollaps = rootView.findViewById(R.id.json_nfc_wifi_collaps)
        val wifiHideLinLayout: LinearLayout = rootView.findViewById(R.id.json_nfc_wifi_hide_linear_layout)
        wifiHideLinLayout.setOnClickListener {
            if(mWifiLinearLayout.isVisible) {
                mWifiLinearLayout.isVisible=false
                mWifiCollaps.setImageResource(R.drawable.ic_expand_view)
            } else {
                mWifiLinearLayout.isVisible=true
                mWifiCollaps.setImageResource(R.drawable.ic_collaps_view)
            }
        }

        mTextCardView = rootView.findViewById(R.id.json_nfc_text)
        val textSendButton: Button = rootView.findViewById(R.id.json_nfc_text_write_to_nfc)
        val textInput: TextView = rootView.findViewById(R.id.json_nfc_text_input_text)

        textSendButton.setOnClickListener {
            val command = JsonWriteCommand(
                GenericText = textInput.text.toString()
            )
            mFeature?.writeCommandWithArgument(command)
            hideSoftKeyboard(requireActivity(),requireActivity().currentFocus)
            snackBarWithConfirmation("Text NDEF Record Written on NFC")
        }

        val textLinearLayout: LinearLayout = rootView.findViewById(R.id.json_nfc_text_linear_layout)
        val textCollaps: ImageView = rootView.findViewById(R.id.json_nfc_text_collaps)
        val textHideLinLayout: LinearLayout = rootView.findViewById(R.id.json_nfc_text_hide_linear_layout)
        textHideLinLayout.setOnClickListener {
            if(textLinearLayout.isVisible) {
                textLinearLayout.isVisible=false
                textCollaps.setImageResource(R.drawable.ic_expand_view)
            } else {
                textLinearLayout.isVisible=true
                textCollaps.setImageResource(R.drawable.ic_collaps_view)
            }
        }

        mURLCardView = rootView.findViewById(R.id.json_nfc_url)
        val urlSendButton: Button = rootView.findViewById(R.id.json_nfc_url_write_to_nfc)
        val urlInput: TextView = rootView.findViewById(R.id.json_nfc_url_input_text)

        var urlAdapterTypePosition =0

        val urlTypeList = FeatureJsonNFC.UrlTypeString.keys.toList()
        val urlTypeSpinner = rootView.findViewById<Spinner>(R.id.json_nfc_url_type)
        urlTypeSpinner.gravity = Gravity.END

        val dataAdapterUrlType = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item, urlTypeList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        urlTypeSpinner.adapter = dataAdapterUrlType

        urlTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                urlAdapterTypePosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                urlAdapterTypePosition = 0
            }
        }

        urlSendButton.setOnClickListener {
            val command = JsonWriteCommand(
                NFCURL = dataAdapterUrlType.getItem(urlAdapterTypePosition).toString() + urlInput.text.toString()
            )
            mFeature?.writeCommandWithArgument(command)
            hideSoftKeyboard(requireActivity(),requireActivity().currentFocus)
            snackBarWithConfirmation("URL NDEF Record Written on NFC")
        }

        val urlLinearLayout: LinearLayout = rootView.findViewById(R.id.json_nfc_url_linear_layout)
        val urlCollaps: ImageView = rootView.findViewById(R.id.json_nfc_url_collaps)
        val urlHideLinLayout: LinearLayout = rootView.findViewById(R.id.json_nfc_url_hide_linear_layout)
        urlHideLinLayout.setOnClickListener {
            if(urlLinearLayout.isVisible) {
                urlLinearLayout.isVisible=false
                urlCollaps.setImageResource(R.drawable.ic_expand_view)
            } else {
                urlLinearLayout.isVisible=true
                urlCollaps.setImageResource(R.drawable.ic_collaps_view)
            }
        }
        return rootView
    }

    private fun hideSoftKeyboard(c: Context, focusView: View?) {
        if (focusView == null) return
        val inputMethodManager =
            c.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(focusView.windowToken, 0)
    }

    private fun snackBarWithConfirmation(message: String) {
        view?.let {
            val snack = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
            snack.setAction("OK") { snack.dismiss() }
            snack.show()
        }
    }

    override fun enableNeededNotification(node: Node) {
        mFeature = node.getFeature(FeatureJsonNFC::class.java)
        mFeature?.apply {
            addFeatureListener(featureListener)
            enableNotification()
            writeCommandWithoutArgument(FeatureJsonNFC.ReadModes)
        }
    }

    override fun disableNeedNotification(node: Node) {
        node.getFeature(FeatureJsonNFC::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }
        mFeature = null
    }
}