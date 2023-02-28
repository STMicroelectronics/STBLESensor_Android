package com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.st.blesensor.cloud.R
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.zxing.integration.android.IntentIntegrator
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAPIToken
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfigured
import com.st.blesensor.cloud.AzureIoTCentralPnP.Network.AzureIoTDeviceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception


class AzureIoTCentralPnPCloudAppConfig: Fragment() {

    private lateinit var mButtonPositive: Button
    private lateinit var mButtonNegative: Button
    private lateinit var mButtonHelp: ImageView
    private lateinit var mButtonShare: ImageView
    private lateinit var mApplicationName: TextView
    private lateinit var mShareableLink: TextView
    private lateinit var mAPITokenReceived: TextView
    private lateinit var mButtonScanQRCode: ExtendedFloatingActionButton
    private lateinit var mInputDashboardURL: TextView

    private lateinit var mCloudAppConfigViewModel: AzureIoTCentralPnPCloudAppConfigViewModel
    private var mCurrentCloudApp: CloudAppConfigured?=null
    private lateinit var mQrResultLauncher : ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.cloud_config_azure_iot_central_pnp_cloud_app_config, container, false)

        mCloudAppConfigViewModel = ViewModelProvider(requireActivity()).get(AzureIoTCentralPnPCloudAppConfigViewModel::class.java)

        mCurrentCloudApp = mCloudAppConfigViewModel.getSelectedCloudApp()

        mButtonPositive = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_config_app_positive)
        mButtonPositive.setOnClickListener{
            if(mCurrentCloudApp!=null) {
                //Read the values from Text Entry for Manual insertion
                mCurrentCloudApp!!.authorizationKey = mAPITokenReceived.text.toString().trim()
                mCurrentCloudApp!!.cloudApp.url =
                    "https://${mInputDashboardURL.text.trim()}.azureiotcentral.com"
                mCloudAppConfigViewModel.configurationDone(mCurrentCloudApp!!)
            }
        }

        mButtonNegative = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_config_app_negative)
        mButtonNegative.setOnClickListener {
            if(mCurrentCloudApp!=null) {
                mCurrentCloudApp!!.configurationDone = false
                mCloudAppConfigViewModel.setSelectedCloudApp(mCurrentCloudApp!!)
            }
            mCloudAppConfigViewModel.configurationAborted()
        }

        mApplicationName = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_config_app_textview)
        if(mCurrentCloudApp!=null) {
            mApplicationName.text = mCurrentCloudApp!!.cloudApp.name
        }

        mButtonHelp = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_element_config_help)
        mButtonHelp.setOnClickListener{
            showHelpMessage();
        }

        mAPITokenReceived = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_element_config_received_token)

        if(mCurrentCloudApp!=null) {
            if (mCurrentCloudApp!!.authorizationKey != null) {
                mAPITokenReceived.text = mCurrentCloudApp!!.authorizationKey

                //This just for displaying ok button ... when url e auth key are present at the beginning
                //in theory this situation should not be present...
                if (mCurrentCloudApp!!.cloudApp.url.toString().isNotEmpty()) {
                    setDoneButton(true)
                }
            }
        }

        mInputDashboardURL = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_element_config_app_name)
        if(mCurrentCloudApp!=null) {
            val cloudDashboardName = mCurrentCloudApp!!.cloudApp.url
                .toString()
                .removePrefix("https://").removeSuffix(".azureiotcentral.com")
            mInputDashboardURL.text = cloudDashboardName
        }


        setTextWatchersAndDoneButton(mAPITokenReceived,mInputDashboardURL)

        mButtonShare = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_element_config_share)
        mButtonShare.setOnClickListener{
            if(mCurrentCloudApp!=null) {
                if (mCurrentCloudApp!!.cloudApp.shareable_link != null) {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, mCurrentCloudApp!!.cloudApp.shareable_link)
                        type = "text/uri"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, "IoT Central App link")
                    startActivity(shareIntent)
                }
            }
        }

        mShareableLink = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_element_config_shareable_link)
            if(mCurrentCloudApp!=null) {
                if (mCurrentCloudApp!!.cloudApp.shareable_link != null) {
                    mShareableLink.text = mCurrentCloudApp!!.cloudApp.shareable_link
                } else {
                    //Disable Sharing button
                    mButtonShare.isEnabled = false
                    mButtonShare.alpha = 0.2f
                }
            }

        if(mCurrentCloudApp!=null) {
            mQrResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == Activity.RESULT_OK) {
                        val result = IntentIntegrator.parseActivityResult(it.resultCode, it.data)

                        if (result.contents != null) {
                            // Do something with the contents (this is usually a URL)
                            mAPITokenReceived.text = result.contents

                            //Update the Authorization Key
                            mCurrentCloudApp!!.authorizationKey = result.contents

                            //Read the API Token Details
                            CoroutineScope(Dispatchers.IO).launch {
                                val tokenID =
                                    mCurrentCloudApp!!.authorizationKey!!.replace(
                                        ".*&skn=".toRegex(),
                                        ""
                                    )
                                        .replace("&se=.*".toRegex(), "")
                                mCurrentCloudApp!!.apiToken = readAPITokenDetails(tokenID)

                                setDoneButton(true)
                            }
                        }
                    }
                }
        }

        mButtonScanQRCode = rootView.findViewById(R.id.cloud_config_azure_iot_central_pnp_config_app_scan_qr_code)
        mButtonScanQRCode.setOnClickListener{
            startScanner()
        }

        return rootView
    }

    private suspend fun readAPITokenDetails(tokenID: String): CloudAPIToken? {
        var token: CloudAPIToken? = null
        if(mCurrentCloudApp!=null) {
            val mAzureIoTService = AzureIoTDeviceService.buildInstance(
                mCurrentCloudApp!!.cloudApp.url,
                mCurrentCloudApp!!.authorizationKey!!
            )
            try {
                token = mAzureIoTService.getTokenDetailsByID(tokenID)
                Log.d("IoTPnP", "getTokenDetailsByID $token")
            } catch (e: Exception) {
                Log.e(this::javaClass.name, "Error sync: " + e.localizedMessage)
                e.printStackTrace()
            }
        }
        return token
    }

    // Start the QR Scanner
    private fun startScanner() {
        val scanner = IntentIntegrator(requireActivity())
        // QR Code Format
        scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        // Set Text Prompt at Bottom of QR code Scanner Activity
        scanner.setPrompt("Scan API token QR code")
        // Start Scanner (don't use initiateScan() unless if you want to use OnActivityResult)
        mQrResultLauncher.launch(scanner.createScanIntent())
    }

    private fun setDoneButton(state: Boolean) {
        if(mCurrentCloudApp!=null) {
            if (state) {
                mButtonPositive.isEnabled = true
                mButtonPositive.visibility = View.VISIBLE
                mCurrentCloudApp!!.configurationDone = true
            } else {
                mButtonPositive.isEnabled = false
                mButtonPositive.visibility = View.INVISIBLE
                mCurrentCloudApp!!.configurationDone = false
            }
        }
    }

    private fun setTextWatchersAndDoneButton(textView1: TextView, textView2: TextView) {
        textView1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val textInput: String = textView1.text.toString()
                if (textInput.isEmpty()){
                    textView1.error = "Field can't be empty"
                    setDoneButton(false)
                } else {
                    textView1.error = null
                    if(textView2.text.trim().isNotEmpty()) {
                        setDoneButton(true)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        textView2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val textInput: String = textView2.text.toString()
                if (textInput.isEmpty()){
                    textView2.error = "Field can't be empty";
                    setDoneButton(false)
                } else {
                    textView2.error = null
                    if(textView1.text.trim().isNotEmpty()) {
                        setDoneButton(true)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showHelpMessage() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        val customLayout = layoutInflater.inflate(R.layout.cloud_config_azure_iot_central_pnp_help, null);
        builder.setView(customLayout);

        val buttonClose: Button = customLayout.findViewById(R.id.cloud_azure_iot_central_pnp_application_help_close_button)

        val dialog = builder.create();
        dialog.show();

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }
    }
}