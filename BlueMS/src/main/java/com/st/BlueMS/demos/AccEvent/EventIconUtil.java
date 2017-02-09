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

package com.st.BlueMS.demos.AccEvent;

import android.support.annotation.DrawableRes;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent.DetectableEvent;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent.AccelerationEvent;

/**
 * class that connect an acceleration event to an image resource
 */
class EventIconUtil {

    static
    @DrawableRes
    int getEventIcon(@AccelerationEvent int event) {
        switch (event) {
            case FeatureAccelerationEvent.ORIENTATION_TOP_LEFT:
                return R.drawable.acc_event_orientation_top_left;
            case FeatureAccelerationEvent.ORIENTATION_TOP_RIGHT:
                return R.drawable.acc_event_orientation_top_right;
            case FeatureAccelerationEvent.ORIENTATION_BOTTOM_LEFT:
                return R.drawable.acc_event_orientation_bottom_left;
            case FeatureAccelerationEvent.ORIENTATION_BOTTOM_RIGHT:
                return R.drawable.acc_event_orientation_bottom_right;
            case FeatureAccelerationEvent.ORIENTATION_UP:
                return R.drawable.acc_event_orientation_up;
            case FeatureAccelerationEvent.ORIENTATION_DOWN:
                return R.drawable.acc_event_orientation_down;
            case FeatureAccelerationEvent.TILT:
                return R.drawable.acc_event_tilt;
            case FeatureAccelerationEvent.FREE_FALL:
                return R.drawable.acc_event_free_fall;
            case FeatureAccelerationEvent.SINGLE_TAP:
                return R.drawable.acc_event_tap_single;
            case FeatureAccelerationEvent.DOUBLE_TAP:
                return R.drawable.acc_event_tap_double;
            case FeatureAccelerationEvent.WAKE_UP:
                return R.drawable.acc_event_wake_up;
            case FeatureAccelerationEvent.PEDOMETER:
                return R.drawable.acc_event_pedometer;
            case FeatureAccelerationEvent.NO_EVENT:
            default:
                return R.drawable.acc_event_none;
        }
    }


    static
    @DrawableRes
    int getDefaultIcon(DetectableEvent event) {
        switch (event){
            case ORIENTATION:
                return getEventIcon(FeatureAccelerationEvent.ORIENTATION_TOP_LEFT);
            case FREE_FALL:
                return getEventIcon(FeatureAccelerationEvent.FREE_FALL);
            case SINGLE_TAP:
                return getEventIcon(FeatureAccelerationEvent.SINGLE_TAP);
            case DOUBLE_TAP:
                return getEventIcon(FeatureAccelerationEvent.DOUBLE_TAP);
            case WAKE_UP:
                return getEventIcon(FeatureAccelerationEvent.WAKE_UP);
            case PEDOMETER:
                return getEventIcon(FeatureAccelerationEvent.PEDOMETER);
            case TILT:
                return getEventIcon(FeatureAccelerationEvent.TILT);
            case NONE:
            default:
                return getEventIcon(FeatureAccelerationEvent.NO_EVENT);
        }
    }

}
