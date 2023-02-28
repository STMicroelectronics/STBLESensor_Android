package com.st.trilobyte.ui.fragment.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.widget.DatePicker
import java.util.*

class TrilobyteDatePickerDialog : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var mDate: Date? = null

    private var dateListener: DateListener? = null

    fun getInstance(date: Date): TrilobyteDatePickerDialog {
        val dialog = TrilobyteDatePickerDialog()
        dialog.setDate(date)
        return dialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val c = Calendar.getInstance()
        c.time = mDate
        val day = c.get(Calendar.DAY_OF_MONTH)
        val month = c.get(Calendar.MONTH)
        val year = c.get(Calendar.YEAR)

        return DatePickerDialog(context!!, this, year, month, day)
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, day)

        dateListener?.onDateSet(c.time)
    }

    private fun setDate(date: Date) {
        mDate = date
    }

    // interface

    interface DateListener {
        fun onDateSet(date: Date)
    }
}