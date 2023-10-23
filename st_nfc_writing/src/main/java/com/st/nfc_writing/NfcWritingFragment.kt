/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.nfc_writing

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.st.blue_sdk.features.extended.json_nfc.request.JsonCommand
import com.st.blue_sdk.features.extended.json_nfc.request.JsonVCard
import com.st.blue_sdk.features.extended.json_nfc.request.JsonWIFI
import com.st.core.ARG_NODE_ID
import com.st.nfc_writing.databinding.NfcWritingFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NfcWritingFragment : Fragment() {

    companion object {
        private const val TAG = "NfcWritingFragment"
    }

    private val viewModel: NfcWritingViewModel by viewModels()
    private lateinit var binding: NfcWritingFragmentBinding
    private lateinit var nodeId: String

    private lateinit var mWifiCardView: CardView
    private lateinit var mVCardCardView: CardView
    private lateinit var mTextCardView: CardView
    private lateinit var mURLCardView: CardView

    private lateinit var mWifiLinearLayout: LinearLayout
    private lateinit var mWifiCollaps: ImageView

    private lateinit var mVCardCollaps: ImageView
    private lateinit var mVCardLinearLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NfcWritingFragmentBinding.inflate(inflater, container, false)
        nodeId = arguments?.getString("nodeId")
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        mVCardCardView = binding.jsonNfcVcard
        val vCardSendButton: Button = binding.jsonNfcVcardWriteToNfc
        val vCardNameTextInput: TextView = binding.jsonNfcVcardNameInputText
        val vCardFormattedNameTextInput: TextView = binding.jsonNfcVcardFormattedNameInputText
        val vCardTitleTextInput: TextView = binding.jsonNfcVcardTitleInputText
        val vCardOrgTextInput: TextView = binding.jsonNfcVcardOrgInputText
        val vCardHomeAddressTextInput: TextView = binding.jsonNfcVcardHomeAddressInputText
        val vCardWorkAddressTextInput: TextView = binding.jsonNfcVcardWorkAddressInputText
        val vCardAddressTextInput: TextView = binding.jsonNfcVcardAddressInputText
        val vCardHomePhoneTextInput: TextView = binding.jsonNfcVcardHomePhoneInputText
        val vCardWorkPhoneTextInput: TextView = binding.jsonNfcVcardWorkPhoneInputText
        val vCardCellularPhoneTextInput: TextView = binding.jsonNfcVcardCellularPhoneInputText
        val vCardHomeEmailTextInput: TextView = binding.jsonNfcVcardHomeEmailInputText
        val vCardWorkEmailTextInput: TextView = binding.jsonNfcVcardWorkEmailInputText
        val vCardUrlTextInput: TextView = binding.jsonNfcVcardUrlInputText

        vCardSendButton.setOnClickListener {
            val commandvCard = JsonCommand(NFCVCard = JsonVCard())

            if (!vCardNameTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.Name = vCardNameTextInput.text.toString()
            }

            if (!vCardFormattedNameTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.FormattedName = vCardFormattedNameTextInput.text.toString()
            }

            if (!vCardTitleTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.Title = vCardTitleTextInput.text.toString()
            }

            if (!vCardOrgTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.Org = vCardOrgTextInput.text.toString()
            }

            if (!vCardHomeAddressTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.HomeAddress = vCardHomeAddressTextInput.text.toString()
            }

            if (!vCardWorkAddressTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.WorkAddress = vCardWorkAddressTextInput.text.toString()
            }

            if (!vCardAddressTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.Address = vCardAddressTextInput.text.toString()
            }

            if (!vCardHomePhoneTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.HomeTel = vCardHomePhoneTextInput.text.toString()
            }

            if (!vCardWorkPhoneTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.WorkTel = vCardWorkPhoneTextInput.text.toString()
            }

            if (!vCardCellularPhoneTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.CellTel = vCardCellularPhoneTextInput.text.toString()
            }

            if (!vCardHomeEmailTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.HomeEmail = vCardHomeEmailTextInput.text.toString()
            }

            if (!vCardWorkEmailTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.WorkEmail = vCardWorkEmailTextInput.text.toString()
            }

            if (!vCardUrlTextInput.text.isNullOrEmpty()) {
                commandvCard.NFCVCard?.Url = vCardUrlTextInput.text.toString()
            }
            viewModel.writeJsonCommand(nodeId, commandvCard)

            hideSoftKeyboard(requireActivity(), requireActivity().currentFocus)
            snackBarWithConfirmation("VCard NDEF Record Written on NFC")
        }
        mVCardLinearLayout = binding.jsonNfcVcardLinearLayout
        mVCardCollaps = binding.jsonVcardCollaps
        val vCardHideLinLayout: LinearLayout = binding.jsonNfcVcardHideLinearLayout
        vCardHideLinLayout.setOnClickListener {
            if (mVCardLinearLayout.isVisible) {
                mVCardLinearLayout.isVisible = false
                mVCardCollaps.setImageResource(R.drawable.ic_expand_view)
            } else {
                mVCardLinearLayout.isVisible = true
                mVCardCollaps.setImageResource(R.drawable.ic_collaps_view)
            }
        }

        mWifiCardView = binding.jsonNfcWifi
        val wifiSendButton: Button = binding.jsonNfcWifiWriteToNfc
        val wifiSSDIInput: TextView = binding.jsonNfcWifiSsidInputText
        val wifiPasswordInput: TextView = binding.jsonNfcWifiPasswdInputText

        var wifiAdapterEncrPosition = 0
        var wifiAdapterAuthPosition = 0

        val authTypeList = JsonCommand.WifiAuthString.keys.toList()
        val authorizationSpinner: Spinner = binding.jsonNfcWifiAuthType
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
                view: View?,
                position: Int,
                id: Long
            ) {
                wifiAdapterAuthPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                wifiAdapterAuthPosition = 0
            }
        }

        val encryptionTypeList = JsonCommand.WifiEncrString.keys.toList()
        val encryptionSpinner: Spinner = binding.jsonNfcWifiEncrType
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
                view: View?,
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
                JsonCommand.WifiAuthString[dataAdapterAuth.getItem(wifiAdapterAuthPosition)
                    .toString()]
            val encr =
                JsonCommand.WifiEncrString[dataAdapterEncr.getItem(wifiAdapterEncrPosition)
                    .toString()]
            if ((auth != null) && (encr != null)) {
                val commandWifi = JsonCommand(
                    NFCWiFi = JsonWIFI(
                        NetworkSSID = wifiSSDIInput.text.toString(),
                        NetworkKey = wifiPasswordInput.text.toString(),
                        AuthenticationType = auth,
                        EncryptionType = encr
                    )
                )
                //Write the Command to the Board
                viewModel.writeJsonCommand(nodeId, commandWifi)
                hideSoftKeyboard(requireActivity(), requireActivity().currentFocus)
                snackBarWithConfirmation("Wi-Fi NDEF Record Written on NFC")
            }
        }

        mWifiLinearLayout = binding.jsonNfcWifiLinearLayout
        mWifiCollaps = binding.jsonNfcWifiCollaps
        val wifiHideLinLayout: LinearLayout = binding.jsonNfcWifiHideLinearLayout
        wifiHideLinLayout.setOnClickListener {
            if (mWifiLinearLayout.isVisible) {
                mWifiLinearLayout.isVisible = false
                mWifiCollaps.setImageResource(R.drawable.ic_expand_view)
            } else {
                mWifiLinearLayout.isVisible = true
                mWifiCollaps.setImageResource(R.drawable.ic_collaps_view)
            }
        }

        mTextCardView = binding.jsonNfcText
        val textSendButton: Button = binding.jsonNfcTextWriteToNfc
        val textInput: TextView = binding.jsonNfcTextInputText

        textSendButton.setOnClickListener {
            val commandText = JsonCommand(
                GenericText = textInput.text.toString()
            )
            //Write the Command to the Board
            viewModel.writeJsonCommand(nodeId, commandText)
            hideSoftKeyboard(requireActivity(), requireActivity().currentFocus)
            snackBarWithConfirmation("Text NDEF Record Written on NFC")
        }

        val textLinearLayout: LinearLayout = binding.jsonNfcTextLinearLayout
        val textCollaps: ImageView = binding.jsonNfcTextCollaps
        val textHideLinLayout: LinearLayout = binding.jsonNfcTextHideLinearLayout
        textHideLinLayout.setOnClickListener {
            if (textLinearLayout.isVisible) {
                textLinearLayout.isVisible = false
                textCollaps.setImageResource(R.drawable.ic_expand_view)
            } else {
                textLinearLayout.isVisible = true
                textCollaps.setImageResource(R.drawable.ic_collaps_view)
            }
        }

        mURLCardView = binding.jsonNfcUrl
        val urlSendButton: Button = binding.jsonNfcUrlWriteToNfc
        val urlInput: TextView = binding.jsonNfcUrlInputText

        var urlAdapterTypePosition = 0

        val urlTypeList = JsonCommand.UrlTypeString.keys.toList()
        val urlTypeSpinner: Spinner = binding.jsonNfcUrlType
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
                view: View?,
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
            val commandUrl = JsonCommand(
                NFCURL = dataAdapterUrlType.getItem(urlAdapterTypePosition)
                    .toString() + urlInput.text.toString()
            )
            //Write the Command to the Board
            viewModel.writeJsonCommand(nodeId, commandUrl)
            hideSoftKeyboard(requireActivity(), requireActivity().currentFocus)
            snackBarWithConfirmation("URL NDEF Record Written on NFC")
        }

        val urlLinearLayout: LinearLayout = binding.jsonNfcUrlLinearLayout
        val urlCollaps: ImageView = binding.jsonNfcUrlCollaps
        val urlHideLinLayout: LinearLayout = binding.jsonNfcUrlHideLinearLayout
        urlHideLinLayout.setOnClickListener {
            if (urlLinearLayout.isVisible) {
                urlLinearLayout.isVisible = false
                urlCollaps.setImageResource(R.drawable.ic_expand_view)
            } else {
                urlLinearLayout.isVisible = true
                urlCollaps.setImageResource(R.drawable.ic_collaps_view)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.supportedModes.collect {
                    if (it.supportedModes.value != null) {
                        //If we had a valid answer for the supported NFC modes
                        Log.i(TAG, it.supportedModes.value!!.Answer.toString())
                        UpdateNFCDemo(it.supportedModes.value!!.Answer.toString())
                    }
                }
            }
        }
    }

    private fun UpdateNFCDemo(answer: String?) {
        var nExpandedCard = 0
        if (answer != null) {
            //Show the supported Card
            //Expand the first 2 of them visible

            if (answer.toString().contains(JsonCommand.NFCText)) {
                mTextCardView.visibility = View.VISIBLE
                nExpandedCard += 1
            } else {
                mTextCardView.visibility = View.GONE
            }

            if (answer.toString().contains(JsonCommand.NFCURL)) {
                mURLCardView.visibility = View.VISIBLE
                nExpandedCard += 1
            } else {
                mURLCardView.visibility = View.GONE
            }

            if (answer.toString().contains(JsonCommand.NFCWifi)) {
                mWifiCardView.visibility = View.VISIBLE
                if (nExpandedCard < 2) {
                    nExpandedCard += 1
                } else {
                    mWifiLinearLayout.isVisible = false
                    mWifiCollaps.setImageResource(R.drawable.ic_expand_view)
                }
            } else {
                mWifiCardView.visibility = View.GONE
            }

            if (answer.toString().contains(JsonCommand.NFCVCard)) {
                mVCardCardView.visibility = View.VISIBLE
                if (nExpandedCard < 2) {
                    nExpandedCard += 1
                } else {
                    mVCardLinearLayout.isVisible = false
                    mVCardCollaps.setImageResource(R.drawable.ic_expand_view)
                }
            } else {
                mVCardCardView.visibility = View.GONE
            }
        }
    }

    private fun hideSoftKeyboard(c: Context, focusView: View?) {
        if (focusView == null) return
        val inputMethodManager =
            c.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(focusView.windowToken, 0)
    }

    private fun snackBarWithConfirmation(message: String) {
        view?.let {
            val snack = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
            snack.setAction("OK") { snack.dismiss() }
            snack.show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
