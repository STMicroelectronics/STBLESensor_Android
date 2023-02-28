package com.st.STM32WB.p2pDemo.feature

import androidx.lifecycle.ViewModel

class P2pDemoViewModel : ViewModel() {
    private var mLedStatus = false
    private var mAlarmText: String?=null

    fun get_LedStatus(): Boolean {
        return mLedStatus
    }
    fun set_LedStatus(new: Boolean) {
        mLedStatus = new
    }

    fun get_AlarmText(): String? {
        return mAlarmText
    }

    fun set_AlarmText(new: String?) {
        mAlarmText = new
    }
}