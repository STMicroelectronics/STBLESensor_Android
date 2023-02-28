/*
 * Copyright (c) 2020  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 * STMicroelectronics company nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 * in a directory whose title begins with st_images may only be used for internal purposes and
 * shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 * icons, pictures, logos and other images that are provided with the source code in a directory
 * whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.STWINBoard_Gui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.st.STWINBoard_Gui.Utils.SaveSettings
import com.st.clab.stwin.gui.R

internal class HSDConfigSaveDialogFragment : DialogFragment(){

    private lateinit var currentStatus: SaveSettings

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        currentStatus = savedInstanceState?.getParcelable(SELECTION_STATUS) ?: SaveSettings()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.saveConf_title)

        val booleanStatus = booleanArrayOf(currentStatus.storeLocalCopy,currentStatus.setAsDefault)
        builder.setMultiChoiceItems(R.array.saveConf_choose,booleanStatus ){ _, which, isChecked ->
            when(which){
                0 -> currentStatus.storeLocalCopy = isChecked
                1 -> currentStatus.setAsDefault = isChecked
            }
        }

        builder.setPositiveButton(R.string.saveConf_save){_,_ -> onSaveClicked()}
        builder.setNegativeButton(R.string.saveConf_cancel){ _, _ -> dismiss()}

        return builder.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SELECTION_STATUS,currentStatus)
    }


    private fun onSaveClicked() {
        parentFragment?.onActivityResult(targetRequestCode,Activity.RESULT_OK,
                encapsulateSettings(currentStatus)
        )
    }

    companion object{
        private val SELECTION_STATUS = HSDConfigSaveDialogFragment::class.java.name+".SELECTION_STATUS"

        fun extractSaveSettings(intent:Intent?): SaveSettings?{
            return if(intent?.hasExtra(SELECTION_STATUS) == true){
                intent.getParcelableExtra(SELECTION_STATUS)
            }else{
                null
            }
        }

        private fun encapsulateSettings(settings: SaveSettings):Intent{
            return Intent().apply {
                putExtra(SELECTION_STATUS,settings)
            }
        }
    }
}