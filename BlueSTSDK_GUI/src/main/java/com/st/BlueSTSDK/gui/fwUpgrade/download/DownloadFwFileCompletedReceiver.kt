/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
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

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeActivity

/**
 * Wait the fw file download ends and start the fw upgrade activity when the file is downloaded
 */
class DownloadFwFileCompletedReceiver(
        context: Context,
        private val mNode: Node) : BroadcastReceiver(), LifecycleObserver {

    private val appContext = context.applicationContext

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null && action == DownloadFwFileService.ACTION_DOWNLOAD_COMPLETE) {
            val file = intent.getParcelableExtra<Uri>(DownloadFwFileService.EXTRA_DOWNLOAD_LOCATION)
            if(file!=null) {
                val startFwActivity = FwUpgradeActivity.getStartIntent(context, mNode, true, file)
                startFwActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(startFwActivity)
            }
        }
    }

    /**
     * register the receiver to the local broadcast
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun registerReceiver() {
        LocalBroadcastManager.getInstance(appContext)
                .registerReceiver(this, DOWNLOAD_COMPLETE_FILTER)
    }

    /**
     * unregister the receiver to the local broadcast
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun unregisterReceiver() {
        LocalBroadcastManager.getInstance(appContext)
                .unregisterReceiver(this)
    }

    companion object {

        private val DOWNLOAD_COMPLETE_FILTER = IntentFilter(DownloadFwFileService.ACTION_DOWNLOAD_COMPLETE)
    }

}
