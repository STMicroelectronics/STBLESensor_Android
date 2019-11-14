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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueMS.R
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureAudioClassification
import com.st.BlueSTSDK.Features.FeatureAudioClassification.AudioClass
import com.st.BlueSTSDK.Node

class AudioSceneViewModel : ViewModel(){

    private var listener = Feature.FeatureListener { _, sample ->
        val scene = FeatureAudioClassification.getAudioClass(sample)
        _currentState.postValue(scene)

        if(scene != AudioClass.UNKNOWN){
            _viewIsVisible.postValue(true)
        }
    }

    private val _currentState=MutableLiveData<AudioClass>()
    val currentState:LiveData<AudioClass>
        get() = _currentState

    private val _viewIsVisible=MutableLiveData<Boolean>()
    val viewIsVisible:MutableLiveData<Boolean>
        get() = _viewIsVisible

    fun registerListener(node: Node){
        node.getFeature(FeatureAudioClassification::class.java)?.apply {
            addFeatureListener(listener)
        }
    }

    fun removeListener(node:Node){
        node.getFeature(FeatureAudioClassification::class.java)?.apply {
            removeFeatureListener(listener)
        }
    }

}

internal val AudioClass.imageResource:Int
    get() {
        return when(this){
            AudioClass.UNKNOWN -> R.drawable.audio_scene_unkown
            AudioClass.INDOOR -> R.drawable.audio_scene_inside
            AudioClass.OUTDOOR -> R.drawable.audio_scene_outside
            AudioClass.IN_VEHICLE -> R.drawable.audio_scene_invehicle
            AudioClass.BABY_IS_CRYING -> R.drawable.audio_scene_babycrying
            AudioClass.ERROR -> R.drawable.audio_scene_unkown
        }
    }


internal val AudioClass.stringResource:Int
    get() {
        return when(this){
            AudioClass.UNKNOWN -> R.string.audio_scene_unknown
            AudioClass.INDOOR -> R.string.audio_scene_indoor
            AudioClass.OUTDOOR -> R.string.audio_scene_outdoor
            AudioClass.IN_VEHICLE -> R.string.audio_scene_inVehicle
            AudioClass.BABY_IS_CRYING -> R.string.audio_baby_crying
            AudioClass.ERROR -> R.string.audio_scene_unknown
        }
    }

