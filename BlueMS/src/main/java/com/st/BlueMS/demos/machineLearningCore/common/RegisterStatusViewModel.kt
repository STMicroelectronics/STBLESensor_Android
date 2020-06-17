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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node

/**
 * FSM and MLC has a very similar output, this class
 */
internal abstract class RegisterStatusViewModel()  : ViewModel(){

    /**
     * command to send to have back the labels associate with the register
     */
    abstract val labelCommand:String

    /**
     * feature to enable
     */
    abstract val featureType:Class<out Feature>

    /**
     * extract the registers values from a feature sample
     */
    abstract fun extractRegisterStatus(sample:Feature.Sample):ShortArray

    private val mRegisterStatus = MutableLiveData<List<RegisterStatus>>()
    val registerStatus: LiveData<List<RegisterStatus>>
        get() = mRegisterStatus

    /**
     * object used to map the raw register value with a label,
     * when set update the view adding the labels to the registers
     */
    private var valueMapper : ValueLabelMapper? = null
    set(value) {
        updateCurrentRegisterStatus(value)
        field = value
    }

    private fun updateCurrentRegisterStatus(mapper: ValueLabelMapper?){
        val currentStatus = mRegisterStatus.value
        if(currentStatus!=null){
            val newStatus = currentStatus.map {
                RegisterStatus(it.registerId, it.value,
                        mapper?.algorithmName(it.registerId),
                        mapper?.valueName(it.registerId, it.value.toInt()))
            }
            mRegisterStatus.postValue(newStatus)
        }
    }

    /**
     * object used to retrieve the register labels
     */
    private var valueMapperConsole : ValueLabelConsole? = null

    private val featureListener = Feature.FeatureListener { _, sample ->
        val status = extractRegisterStatus(sample)
        val registerStatus = status.mapIndexed{id, value ->
            RegisterStatus(id, value,
                    valueMapper?.algorithmName(id),
                    valueMapper?.valueName(id, value.toInt()))
        }
        mRegisterStatus.postValue(registerStatus)
    }

    fun startListenDataFrom(node: Node) {
        node.debug?.let {
            valueMapperConsole = ValueLabelConsole(labelCommand, it)
            valueMapperConsole?.loadLabel{
                valueMapper = it
            }
        }
        node.getFeature(featureType)?.let {
            it.addFeatureListener(featureListener)
            it.read()
            it.enableNotification()
        }
    }

    fun stopListenDataFrom(node: Node) {
        node.getFeature(featureType)?.let {
            it.removeFeatureListener(featureListener)
            it.disableNotification()
        }
    }

}