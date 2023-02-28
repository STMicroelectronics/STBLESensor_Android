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
package com.st.BlueMS.demos.multiNN

import android.os.Handler
import android.os.Looper
import com.st.BlueSTSDK.Debug
import java.lang.StringBuilder
import java.util.regex.Pattern

internal typealias CurrentAlgorithmCallback = (Int?)-> Unit
internal typealias AvailableAlgorithmsCallback = (List<AvailableAlgorithm>?)->Unit

internal class MultiNNConsole(private val console: Debug){

    private var mCommandIsRunning:Boolean = false
    private val mTimeout = Handler(Looper.getMainLooper())

    fun enableAlgorithm(algo:AvailableAlgorithm, onAlgorithmEnabled:()->Unit){
        val cmd = String.format(SET_ALGO_FORMAT,algo.index)
        console.addDebugOutputListener(object  : Debug.DebugOutputListener{
            override fun onStdOutReceived(debug: Debug, message: String) {
            }

            override fun onStdErrReceived(debug: Debug, message: String) {  }

            override fun onStdInSent(debug: Debug, message: String, writeResult: Boolean) {
                onAlgorithmEnabled()
                debug.removeDebugOutputListener(this)
            }

        })
        console.write(cmd)
    }

    fun getCurrentAlgorithmIndex(callback: CurrentAlgorithmCallback):Boolean{
        if(mCommandIsRunning)
            return false
        mCommandIsRunning=true
        console.addDebugOutputListener(CurrentAlgorithmListener(console,mTimeout){ data ->
            mCommandIsRunning=false
            callback(data)
        })
        console.write(GET_CURRENT_ALGO)
        return true
    }

    fun getAvailableAlgorithms(callback: AvailableAlgorithmsCallback):Boolean{
        if(mCommandIsRunning)
            return false
        mCommandIsRunning=true
        console.addDebugOutputListener(AvailableAlgorithmsListener(console,mTimeout){ data ->
            mCommandIsRunning=false
            callback(data)
        })
        console.write(GET_AVAILABLE_ALGOS)
        return true
    }

    companion object{
        private const val GET_AVAILABLE_ALGOS = "getAllAIAlgo\n"
        private const val GET_CURRENT_ALGO = "getAIAlgo\n"
        private const val SET_ALGO_FORMAT = "setAIAlgo %d\n"
        private const val COMMAND_TIMEOUT_MS = 500L
        //regexp: a number follow by - follow by a string follow by an optional ,
        private  val AVAILABLE_ALGOS_REG_EXP:Pattern = Pattern.compile("((\\d+-.+,?)+)\\n")

        private class CurrentAlgorithmListener(
                private val console:Debug,
                private val timerThread:Handler,
                private val callback:CurrentAlgorithmCallback):Debug.DebugOutputListener{

            private val onTimerFired = Runnable{ notifyResult(null) }

            init {
                timerThread.postDelayed(onTimerFired, COMMAND_TIMEOUT_MS)
            }

            private fun notifyResult(index:Int?){
                console.removeDebugOutputListener(this)
                timerThread.removeCallbacks(onTimerFired)
                callback(index)
            }

            override fun onStdOutReceived(debug: Debug, message: String) {
                if(message.endsWith('\n')){
                    val index = message.toIntOrNull()
                    notifyResult(index)
                }

            }

            override fun onStdErrReceived(debug: Debug, message: String) {}

            override fun onStdInSent(debug: Debug, message: String, writeResult: Boolean) {}

        }

        private class AvailableAlgorithmsListener(
                private val console:Debug,
                private val timerThread:Handler,
                private val callback:AvailableAlgorithmsCallback):Debug.DebugOutputListener{

            private val onTimerFired = Runnable{ notifyResult(null) }
            private val receivedData = StringBuilder()

            init {
                timerThread.postDelayed(onTimerFired, COMMAND_TIMEOUT_MS)
            }

            private fun resetTimer(){
                timerThread.removeCallbacks(onTimerFired)
                timerThread.postDelayed(onTimerFired, COMMAND_TIMEOUT_MS)
            }

            private fun notifyResult(algos:List<AvailableAlgorithm>?){
                console.removeDebugOutputListener(this)
                timerThread.removeCallbacks(onTimerFired)
                if(algos?.isEmpty() == false) {
                    callback(algos)
                }else {
                    callback(null)
                }
            }

            override fun onStdOutReceived(debug: Debug, message: String) {
                receivedData.append(message)
                resetTimer()
                val matcher  = AVAILABLE_ALGOS_REG_EXP.matcher(receivedData)
                if(matcher.find()){
                    // get only the char that are matching the reg exp
                    val rawData = matcher.group().removeTerminatorCharacters()
                    val algos = extractAlgos(rawData)
                    notifyResult(algos)
                }


            }

            private fun extractAlgos(rawData: String): List<AvailableAlgorithm> {
                return rawData.split(',').mapNotNull { algo ->
                    val component = algo.split('-')
                    val index = component.getOrNull(0)?.toIntOrNull()
                    val name = component.getOrNull(1)
                    if(index!=null && name !=null){
                        AvailableAlgorithm(index,name)
                    }else
                        null
                }
            }

            override fun onStdErrReceived(debug: Debug, message: String) {}

            override fun onStdInSent(debug: Debug, message: String, writeResult: Boolean) {}

        }

    }
}

private fun String.removeTerminatorCharacters(): String {
    return this.replace("\n", "").replace("\r", "")
}