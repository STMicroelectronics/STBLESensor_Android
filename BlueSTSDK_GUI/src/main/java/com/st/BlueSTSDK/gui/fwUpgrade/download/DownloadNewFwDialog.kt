/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
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
package com.st.BlueSTSDK.gui.fwUpgrade.download

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.st.BlueSTSDK.gui.R

class DownloadNewFwDialog : DialogFragment() {

    companion object{
        private const val ARG_FW_LOCATION = "EXTRA_FW_LOCATION"
        private const val ARG_FORCE_FW = "ARG_FORCE_FW"

        @JvmStatic
        fun buildDialogForUri(firmwareRemoteLocation:Uri, forceFwUpgrade:Boolean):DialogFragment{
            return DownloadNewFwDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_FW_LOCATION,firmwareRemoteLocation)
                    putByte(ARG_FORCE_FW,if(forceFwUpgrade) 0 else 1 )
                }
            }
        }
    }

    private fun buildDialogMessage(firmwareRemoteLocation:Uri):CharSequence{
        return getString(R.string.cloudLog_fwUpgrade_notification_desc,
                firmwareRemoteLocation.lastPathSegment);
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val fwLocation = arguments?.getParcelable<Uri>(ARG_FW_LOCATION)!!
        val forceFwUpgrade = (arguments?.getByte(ARG_FORCE_FW) ?: 0) == 0.toByte()
        val message = buildDialogMessage(fwLocation)
        return AlertDialog.Builder(requireContext()).apply {

            setTitle(R.string.cloudLog_fwUpgrade_notification_title)
            setIcon(R.drawable.ota_upload_fw)
            setMessage(message)
            setPositiveButton(R.string.cloudLog_fwUpgrade_startUpgrade){ _, _ ->
                DownloadFwFileService.startDownloadFwFile(requireContext(),fwLocation)
            }
            if(!forceFwUpgrade)
                setNegativeButton(R.string.cloudLog_fwUpgrade_canceUpgrade){_,_ -> dismiss()}

        }.create()

    }

}