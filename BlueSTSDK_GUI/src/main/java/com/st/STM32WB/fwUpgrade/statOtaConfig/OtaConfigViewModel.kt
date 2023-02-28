package com.st.STM32WB.fwUpgrade.statOtaConfig

import android.net.Uri
import androidx.lifecycle.ViewModel

class OtaConfigViewModel : ViewModel() {

    //Default Values... Not Valid values
    private var mWB_board = 0
    private var mFirmwareType = -1
    private var mFirstSector: Short = -1;
    private var mMaxSectorSelected: Short = -1
    private var mCustomMemory: Boolean = false
    private var mSelectedFw: Uri? = null

    // WB board type
    fun set_WB_board(board: Int) {
        mWB_board = board
    }

    fun get_WB_board(): Int {
        return mWB_board
    }

    // WB firmware Type: Application/BLE
    fun set_FirmwareType(fw: Int) {
        mFirmwareType = fw
    }

    fun get_FirmwareType() : Int {
        return mFirmwareType
    }

    //First Sector to delete for Custom Memory Layout
    fun set_FirstSector(sector : Short) {
        mFirstSector = sector
    }

    fun get_firstSector() : Short {
        return mFirstSector
    }

    //Last Sector to delete for Custom Memory Layout
    fun set_MaxSectorSelected(sector : Short) {
        mMaxSectorSelected = sector
    }

    fun get_MaxSectorSelected() : Short {
        return mMaxSectorSelected
    }

    //Firmware URI
    fun set_SelectedFw(uri: Uri?) {
        mSelectedFw = uri
    }

    fun get_SelectedFw(): Uri? {
        return mSelectedFw
    }

    //For Custom Memory Flag
    fun set_CustomMemory(value: Boolean) {
        mCustomMemory= value
    }

    fun get_CustomMemory(): Boolean {
        return mCustomMemory
    }
}