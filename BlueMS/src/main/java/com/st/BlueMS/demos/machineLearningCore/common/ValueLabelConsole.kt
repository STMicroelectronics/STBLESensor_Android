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
package com.st.BlueMS.demos.machineLearningCore.common

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.st.BlueSTSDK.Debug
import java.lang.StringBuilder
import java.util.regex.Pattern

/**
 * class used to get back the mapping between the raw value and the label
 * @param command command to send to get back the mapped values
 * @param console object used to communicate with the device
 */
internal class ValueLabelConsole(private val command:String, private var console:Debug) {

    /**only one command at the time can run */
    private var mCommandIsRunning:Boolean = false

    private val mTimeout = Handler(Looper.getMainLooper())

    fun loadLabel(onLoadComplete:(ValueLabelMapper?)->Unit){
        console.addDebugOutputListener(LoadValueLabelListener(console, mTimeout) {
            mCommandIsRunning = false
            onLoadComplete(it)
        })
        console.write(command)
    }

    companion object{

        private const val COMMAND_TIMEOUT_MS = 1000L
        private  val REGISTER_INFO: Pattern = Pattern.compile("<(MLC|FSM_OUTS)(\\d+)(_SRC)?>(.*)")
        private  val VALUE_INFO: Pattern = Pattern.compile("(\\d+)='(.*)'")

        /**
         * object that collects and parse the command response
         */
        private class LoadValueLabelListener(
                private val console: Debug,
                private val timerThread: Handler,
                private val callback:(ValueLabelMapper?)->Unit):Debug.DebugOutputListener{

            private val onTimerFired = Runnable{ notifyResult(null) }
            private val receivedData = StringBuilder()

            private fun resetTimer(){
                timerThread.removeCallbacks(onTimerFired)
                timerThread.postDelayed(onTimerFired, COMMAND_TIMEOUT_MS)
            }

            private fun notifyResult(mapper: ValueLabelMapper?){
                console.removeDebugOutputListener(this)
                timerThread.removeCallbacks(onTimerFired)
                callback(mapper)
            }

            override fun onStdOutReceived(debug: Debug, message: String) {
                receivedData.append(message)
                resetTimer()
                Log.d("Console",receivedData.toString())
                if(receivedData.endsWith('\n')){
                    val mapper = buildRegisterMapperFromString(receivedData.removeSuffix("\n"))
                    // if the parser fail, clear the current buffer and wait a new line
                    if(mapper!=null) {
                        notifyResult(mapper)
                    }else{
                        receivedData.clear()
                    }
                }

            }

            private fun buildRegisterMapperFromString(receivedData: CharSequence): ValueLabelMapper? {
                val registerData = receivedData.split(';').filter { it.isNotEmpty() }
                val mapper = ValueLabelMapper()
                registerData.forEach { data ->
                    val splitData = data.split(',').filter { it.isNotEmpty() }
                    val (registerId,algoName) = extractRegisterInfo(splitData[0]) ?: return null
                    mapper.addRegisterName(registerId,algoName)
                    for ( i in 1 until splitData.size){
                        val (value, name ) = extractValueInfo(splitData[i]) ?: return null
                        mapper.addLabel(registerId,value,name)
                    }
                }
                return mapper
            }

            private fun extractValueInfo(valueInfo: String): Pair<Int,String>? {
                val match = VALUE_INFO.matcher(valueInfo)
                if (!match.matches())
                    return null
                val value = match.group(1)?.toInt() ?: return null
                val name = match.group(2) ?: return null
                return Pair(value,name)
            }

            private fun extractRegisterInfo(registerInfo: String): Pair<Int, String>? {
                val match = REGISTER_INFO.matcher(registerInfo)
                if (!match.matches())
                    return null
                var id = match.group(2)?.toInt() ?: return null
                // FSM index start from 1, MLC index start from 0
                if(match.group(1) == "FSM_OUTS") {
                    id -= 1
                }
                val name = match.group(4 ) ?: return null
                return Pair(id,name)
            }

            override fun onStdErrReceived(debug: Debug, message: String) {}

            override fun onStdInSent(debug: Debug, message: String, writeResult: Boolean) {
                timerThread.postDelayed(onTimerFired, COMMAND_TIMEOUT_MS)
            }

        }

    }
}
