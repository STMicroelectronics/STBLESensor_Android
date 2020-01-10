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
import com.st.BlueSTSDK.Features.FeatureActivity
import com.st.BlueSTSDK.Features.FeatureActivity.ActivityType
import com.st.BlueSTSDK.Node

internal class ActivityRecognitionViewModel : ViewModel(){

    private fun showDefaultActivity(activityType: ActivityType){
        _currentState.postValue(activityType.imageResource)
        _currentDescriptionStrId.postValue(activityType.stringResource)
        if(activityType != ActivityType.NO_ACTIVITY){
            _viewIsVisible.postValue(true)
        }
    }

    private fun showAdultPresenceActivity(activityType: ActivityType){
        _viewIsVisible.postValue(true)
        if(activityType == ActivityType.ADULT_IN_CAR){
            _currentState.postValue(activityType.imageResource)
            _currentDescriptionStrId.postValue(activityType.stringResource)
        }else{
            _currentState.postValue(R.drawable.activity_adult_not_in_car)
            _currentDescriptionStrId.postValue(R.string.activityRecognition_adultNotInCar)
        }
    }

    private var listener = Feature.FeatureListener { _, sample ->
        val activityType = FeatureActivity.getActivityStatus(sample)
        val algo = FeatureActivity.getAlgorithmType(sample)

        when(algo){
            4.toShort() -> showAdultPresenceActivity(activityType)
            else -> showDefaultActivity(activityType)
        }
    }

    private val _currentState= MutableLiveData<Int>()
    val currentState: LiveData<Int>
        get() = _currentState

    private val _currentDescriptionStrId=MutableLiveData<Int>()
    val currentDescriptionStrId:LiveData<Int>
        get() = _currentDescriptionStrId

    private val _viewIsVisible= MutableLiveData<Boolean>()
    val viewIsVisible: LiveData<Boolean>
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

internal val ActivityType.imageResource: Int
    get() {
        return when(this){
            ActivityType.NO_ACTIVITY -> R.drawable.activity_unkown
            ActivityType.STATIONARY -> R.drawable.activity_stationary
            ActivityType.WALKING -> R.drawable.activity_walking
            ActivityType.FASTWALKING -> R.drawable.activity_fastwalking
            ActivityType.JOGGING -> R.drawable.activity_jogging
            ActivityType.BIKING -> R.drawable.activity_biking
            ActivityType.DRIVING -> R.drawable.activity_driving
            ActivityType.STAIRS -> R.drawable.activity_stairs
            ActivityType.ADULT_IN_CAR -> R.drawable.activity_adult_in_car
            ActivityType.ERROR -> R.drawable.activity_unkown
        }
    }

internal val ActivityType.stringResource: Int
    get() {
        return when(this){
            ActivityType.NO_ACTIVITY -> R.string.activityRecognition_unknownImageDesc
            ActivityType.STATIONARY -> R.string.activityRecognition_stationaryImageDesc
            ActivityType.WALKING -> R.string.activityRecognition_walkingImageDesc
            ActivityType.FASTWALKING -> R.string.activityRecognition_fastWalkingImageDesc
            ActivityType.JOGGING -> R.string.activityRecognition_joggingImageDesc
            ActivityType.BIKING -> R.string.activityRecognition_bikingImageDesc
            ActivityType.DRIVING -> R.string.activityRecognition_drivingImageDesc
            ActivityType.STAIRS -> R.string.activityRecognition_stairsImageDesc
            ActivityType.ADULT_IN_CAR -> R.string.activityRecognition_adultInCar
            ActivityType.ERROR -> R.string.activityRecognition_unknownImageDesc
        }
    }