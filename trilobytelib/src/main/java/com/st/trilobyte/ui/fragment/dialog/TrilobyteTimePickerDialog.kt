package com.st.trilobyte.ui.fragment.dialog

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.text.format.DateFormat
import android.widget.TimePicker
import java.util.*

class TrilobyteTimePickerDialog : DialogFragment(), android.app.TimePickerDialog.OnTimeSetListener {

    private var mDate: Date? = null

    private var timelistener: TimeListener? = null

    fun getInstance(date: Date, timeListener: TimeListener): TrilobyteTimePickerDialog {
        val dialog = TrilobyteTimePickerDialog()
        dialog.setDate(date)
        return dialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val c = Calendar.getInstance()
        c.time = mDate
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(timePicker: TimePicker, hour: Int, minute: Int) {

        val c = Calendar.getInstance()
        c.set(Calendar.HOUR, hour)
        c.set(Calendar.MINUTE, minute)

        timelistener?.onTimeSet(c.time)
    }

    private fun setDate(date: Date) {
        mDate = date
    }

    // interface

    interface TimeListener {
        fun onTimeSet(date: Date)
    }
}