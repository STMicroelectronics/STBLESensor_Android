package com.st.BlueMS.demos.HighSpeedDatalog2.config

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.st.clab.stwin.gui.R

data class SaveSettings(
    var storeLocalCopy:Boolean = false,
    var setAsDefault:Boolean = false
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (storeLocalCopy) 1 else 0)
        parcel.writeByte(if (setAsDefault) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SaveSettings> {
        override fun createFromParcel(parcel: Parcel): SaveSettings {
            return SaveSettings(parcel)
        }

        override fun newArray(size: Int): Array<SaveSettings?> {
            return arrayOfNulls(size)
        }
    }
}

class HSD2SaveDialogFragment(var isSDMounted:Boolean): DialogFragment(){
    private lateinit var currentStatus: SaveSettings

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        currentStatus = savedInstanceState?.getParcelable(SELECTION_STATUS) ?: SaveSettings()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.saveConf_title)


        if (!isSDMounted){
            val booleanStatus = booleanArrayOf(currentStatus.storeLocalCopy)
            builder.setMultiChoiceItems(
                R.array.saveConf_simple_choose,
                booleanStatus
            ) { _, which, isChecked ->
                when (which) {
                    0 -> currentStatus.storeLocalCopy = isChecked
                }
            }
        }
        else {
            val booleanStatus = booleanArrayOf(currentStatus.storeLocalCopy,currentStatus.setAsDefault)
            builder.setMultiChoiceItems(
                R.array.saveConf_choose,
                booleanStatus
            ) { _, which, isChecked ->
                when (which) {
                    0 -> currentStatus.storeLocalCopy = isChecked
                    1 -> currentStatus.setAsDefault = isChecked
                }
            }
        }

        builder.setPositiveButton(R.string.saveConf_save){ _, _ -> onSaveClicked()}
        builder.setNegativeButton(R.string.saveConf_cancel){ _, _ -> dismiss()}

        return builder.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SELECTION_STATUS,currentStatus)
    }


    private fun onSaveClicked() {
        parentFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK,
            encapsulateSettings(currentStatus)
        )
    }

    companion object{
        private val SELECTION_STATUS = HSD2SaveDialogFragment::class.java.name+".SELECTION_STATUS"

        fun extractSaveSettings(intent: Intent?): SaveSettings?{
            return if(intent?.hasExtra(SELECTION_STATUS) == true){
                intent.getParcelableExtra(SELECTION_STATUS)
            }else{
                null
            }
        }

        private fun encapsulateSettings(settings: SaveSettings): Intent {
            return Intent().apply {
                putExtra(SELECTION_STATUS,settings)
            }
        }
    }
}
