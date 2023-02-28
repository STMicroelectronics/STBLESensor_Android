package com.st.STM32WBA

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel

class OtaWBAViewModel : ViewModel() {

    //Default Values... Not Valid values
    private var mAddress: CharSequence = ""
    private var mFirmwareType = -1
    private var mSelectedFw: Uri? = null
    private var mNbSectors: CharSequence = "";
    private var mWaitedTime: CharSequence = "";
    private var mIsForceItChecked: Boolean = false;
    private var mIsForceIt2Checked: Boolean = false;

    fun set_Address(address: CharSequence) {
        mAddress = address
    }

    fun get_Address() : CharSequence {
        return mAddress
    }

    // WB firmware Type: Application or User Conf
    fun set_FirmwareType(fw: Int) {
        mFirmwareType = fw
    }

    fun get_FirmwareType() : Int {
        return mFirmwareType
    }

    //Firmware URI
    fun set_SelectedFw(uri: Uri?) {
        mSelectedFw = uri
    }

    fun get_SelectedFw(): Uri? {
        return mSelectedFw
    }

    fun set_NbSectors(nbSectors : CharSequence) {
        mNbSectors = nbSectors
    }

    fun get_NbSectors() : CharSequence {
        return mNbSectors
    }

    fun set_WaitedTime(waitedTime : CharSequence) {
        mWaitedTime = waitedTime
    }

    fun get_WaitedTime() : CharSequence {
        return mWaitedTime
    }

    fun set_IsForceItChecked(isForceItChecked : Boolean) {
        mIsForceItChecked = isForceItChecked
    }

    fun get_IsForceItChecked() : Boolean {
        return mIsForceItChecked
    }

    fun set_IsForceIt2Checked(isForceIt2Checked : Boolean) {
        mIsForceIt2Checked = isForceIt2Checked
    }

    fun get_IsForceIt2Checked() : Boolean {
        return mIsForceIt2Checked
    }

    fun get_fileContent(contentResolver: ContentResolver): ByteArray {
        if(mSelectedFw != null) {
            return try {
                val stream = contentResolver.openInputStream(mSelectedFw!!)
                val strData = stream!!.readBytes()
                stream.close()
                strData
            } catch (e: Exception) {
                ByteArray(0)
            }
        } else {
            return ByteArray(0)
        }
    }
}