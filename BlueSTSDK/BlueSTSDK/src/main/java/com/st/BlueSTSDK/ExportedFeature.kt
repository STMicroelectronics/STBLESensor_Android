/*******************************************************************************
 * COPYRIGHT(c) 2019 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
/**
 * Base class that the app has to extend when it wants to export some characteristics
 *
 * All the notification are done sequentially into a background thread.
 *
 * */
open class ExportedFeature internal constructor(parent: NodeServer) {

    /**
     * server object that represent the BLE Server
     */
    protected val mParent:NodeServer = parent

    /**
     * tell if the client has enabled the notification into this feature
     */
    var isNotificationEnabled:Boolean = false
    private set

    /**
     * call the ExportedFeatureCallback.onNotificationEnabled to all the listener
     */
    internal fun onNotificationEnabled() {
        isNotificationEnabled = true
        mCallbacks.forEach {
            CALLBACK_THREAD.submit { it.onNotificationEnabled(this) }
        }
    }

    /**
     * call the ExportedFeatureCallback.onNotificationDisabled to all the listener
     */
    internal fun onNotificationDisabled() {
        isNotificationEnabled = false
        mCallbacks.forEach {
            CALLBACK_THREAD.submit { it.onNotificationDisabled(this) }
        }
    }

    private val mCallbacks = CopyOnWriteArrayList<ExportedFeatureCallback>()

    fun addListener(listener:ExportedFeatureCallback){
        mCallbacks.addIfAbsent(listener)
    }

    fun removeListener(listener: ExportedFeatureCallback){
        mCallbacks.remove(listener)
    }

    /**
     * Send the data as a notification to the client
     */
    protected fun notifyData(data:ByteArray){
        mParent.notifyOnFeature(this.javaClass,data)
    }

    companion object{
        // thread where all the callback are called
        private val CALLBACK_THREAD = Executors.newSingleThreadExecutor()

    }

    /**
     * Listener to use to know when the notification are enable/disabled by the client
     */
    interface ExportedFeatureCallback{

        /**
         * function called when the client enable the notification for the exported feature
         */
        fun onNotificationEnabled( onFeature:ExportedFeature )

        /**
         * function called when the client disable the notification for the exported feature
         */
        fun onNotificationDisabled( onFeature:ExportedFeature )
    }

}
