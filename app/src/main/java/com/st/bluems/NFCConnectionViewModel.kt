package com.st.bluems

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NFCConnectionViewModel: ViewModel() {

    private val _nfcNodeId = MutableStateFlow<String?>(null)
    val nfcNodeId = _nfcNodeId.asStateFlow()

    private val _pairingPin= MutableStateFlow<ByteArray?>(null)
    val pairingPin = _pairingPin.asStateFlow()


    fun setNFCPairingPin(pin: ByteArray?) {
        _pairingPin.value = pin
        if(pin!=null) {
            Log.i("NFC", "NFC PairingPin = ${String(pin)}")
        }
    }

    fun setNFCNodeId(nodeTag: String?) {
        _nfcNodeId.value = nodeTag
        if(nodeTag!=null) {
            Log.i("NFC", "NFC NodeId = $nodeTag")
        }
    }
}