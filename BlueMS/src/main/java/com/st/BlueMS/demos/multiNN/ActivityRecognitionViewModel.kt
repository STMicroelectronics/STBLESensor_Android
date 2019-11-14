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
import android.util.Log
import com.st.BlueMS.R
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureActivity
import com.st.BlueSTSDK.Node

internal class ActivityRecognitionViewModel : ViewModel(){

    private var listener = Feature.FeatureListener { _, sample ->
        val activityType = FeatureActivity.getActivityStatus(sample)
        _currentState.postValue(activityType)
        if(activityType!= FeatureActivity.ActivityType.NO_ACTIVITY){
            _viewIsVisible.postValue(true)
        }
    }

    private val _currentState= MutableLiveData<FeatureActivity.ActivityType>()
    val currentState: LiveData<FeatureActivity.ActivityType>
        get() = _currentState

    private val _viewIsVisible= MutableLiveData<Boolean>()
    val viewIsVisible: MutableLiveData<Boolean>
        get() = _viewIsVisible


    fun registerListener(node: Node){
        node.getFeature(FeatureActivity::class.java)?.apply {
            addFeatureListener(listener)
        }
    }

    fun removeListener(node:Node){
        node.getFeature(FeatureActivity::class.java)?.apply {
            removeFeatureListener(listener)
        }
    }
}

internal val FeatureActivity.ActivityType.imageResource: Int
    get() {
        return when(this){
            FeatureActivity.ActivityType.NO_ACTIVITY -> R.drawable.activity_unkown
            FeatureActivity.ActivityType.STATIONARY -> R.drawable.activity_stationary
            FeatureActivity.ActivityType.WALKING -> R.drawable.activity_walking
            FeatureActivity.ActivityType.FASTWALKING -> R.drawable.activity_fastwalking
            FeatureActivity.ActivityType.JOGGING -> R.drawable.activity_jogging
            FeatureActivity.ActivityType.BIKING -> R.drawable.activity_biking
            FeatureActivity.ActivityType.DRIVING -> R.drawable.activity_driving
            FeatureActivity.ActivityType.STAIRS -> R.drawable.activity_stairs
            FeatureActivity.ActivityType.ERROR -> R.drawable.activity_unkown
        }
    }

internal val FeatureActivity.ActivityType.stringResource: Int
    get() {
        return when(this){
            FeatureActivity.ActivityType.NO_ACTIVITY -> R.string.activityRecognition_unknownImageDesc
            FeatureActivity.ActivityType.STATIONARY -> R.string.activityRecognition_stationaryImageDesc
            FeatureActivity.ActivityType.WALKING -> R.string.activityRecognition_walkingImageDesc
            FeatureActivity.ActivityType.FASTWALKING -> R.string.activityRecognition_fastWalkingImageDesc
            FeatureActivity.ActivityType.JOGGING -> R.string.activityRecognition_joggingImageDesc
            FeatureActivity.ActivityType.BIKING -> R.string.activityRecognition_bikingImageDesc
            FeatureActivity.ActivityType.DRIVING -> R.string.activityRecognition_drivingImageDesc
            FeatureActivity.ActivityType.STAIRS -> R.string.activityRecognition_stairsImageDesc
            FeatureActivity.ActivityType.ERROR -> R.string.activityRecognition_unknownImageDesc
        }
    }