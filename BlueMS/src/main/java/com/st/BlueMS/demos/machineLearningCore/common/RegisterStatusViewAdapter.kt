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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueMS.R

internal class RegisterStatusViewAdapter(@StringRes private val registerWithNameFormat:Int,
                                         @StringRes private val registerWithoutNameFormat:Int )
    : ListAdapter<RegisterStatus,RegisterStatusViewAdapter.ViewHolder>(RegisterStatusDiffCallback()){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mlc_register_status, parent, false)
        return ViewHolder(registerWithNameFormat,registerWithoutNameFormat,view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.displayValue(getItem(position))
    }


    class ViewHolder(@StringRes private val registerWithNameFormat:Int,
                     @StringRes private val registerWithoutNameFormat:Int,
                     item: View) : RecyclerView.ViewHolder(item) {
        private val registerLabel = item.findViewById<TextView>(R.id.mlc_registerId)
        private val registerValue = item.findViewById<TextView>(R.id.mlc_registerValue)
        private val iconValue = item.findViewById<ImageView>(R.id.mlc_registerIcon)

        fun displayValue(status: RegisterStatus) {
            val context = itemView.context
            val registerName = status.algorithmName
            val registerLabelStr = if(registerName != null){
                context.getString(registerWithNameFormat,status.registerId, registerName)
            }else{
                context.getString(registerWithoutNameFormat,status.registerId)
            }
            val label = status.label
            val registerValueStr = if( label != null)
                context.getString(R.string.mlc_registerValueWithLabel_format,label,status.value)
            else
                context.getString(R.string.mlc_registerValue_format,status.value)

            if(label!=null) {
                iconValue.setImageResource(iconImageMap.getOrDefault(label,R.drawable.neural_network_demo_icon))
            } else {
                iconValue.setImageResource(R.drawable.neural_network_demo_icon);
            }

            registerLabel.text = registerLabelStr
            registerValue.text = registerValueStr
        }
    }
}

private var iconImageMap = mapOf(
    //Activity Recognition
    "Walking" to R.drawable.mlc_walking,
    "Running" to R.drawable.mlc_running,
    "Standing" to R.drawable.mlc_standing,
    "Biking" to R.drawable.mlc_biking,
    "Driving" to R.drawable.mlc_driving,
    //Head Gesture
    "Nod" to R.drawable.mlc_nod,
    "Shake" to R.drawable.mlc_shake,
    "Swing" to R.drawable.mlc_swing,
    "Steady head" to R.drawable.mlc_steady_head,
    //Vibration
    "No vibration" to R.drawable.mlc_no_vibration,
    "Low vibration" to R.drawable.mlc_low_vibration,
    "High vibration" to R.drawable.mlc_high_vibration,
    //Asset Tracking
    "Stationary upright" to R.drawable.mlc_stationary_upright,
    "Stationary not upright" to R.drawable.mlc_stationary_no_upright,
    "Motion" to R.drawable.mlc_motion,
    "Shaking" to R.drawable.mlc_shaking,
    //Door opening/closing/still
    "Door closing" to R.drawable.mlc_door_closing,
    "Door still" to R.drawable.mlc_door_still,
    "Door Opening" to R.drawable.mlc_door_opening,
    //Gym activity recognition
    "No activity" to R.drawable.mlc_standing,
    "Biceps curls" to R.drawable.mlc_biceps_curls,
    "Lateral raises" to R.drawable.mlc_lateral_raises,
    "Squat" to R.drawable.mlc_squat,
    //Vehicle
    "Car moving" to R.drawable.mlc_car_moving,
    "Car still" to R.drawable.mlc_car_still,
    //Yoga Pose
    "The tree" to R.drawable.mlc_the_tree,
    "Boat pose" to R.drawable.mlc_boat_pose,
    "Bow pose" to R.drawable.mlc_bow_pose,
    "Plank inverse" to R.drawable.mlc_plank_inverse,
    "Side angle" to R.drawable.mlc_side_angle,
    "Plank" to R.drawable.mlc_plank,
    "Meditation pose" to R.drawable.mlc_meditation_pose,
    "Cobra" to R.drawable.mlc_cobra,
    "Child" to R.drawable.mlc_child,
    "Downward dog pose" to R.drawable.mlc_downward_dog_pose,
    "Seated forward" to R.drawable.mlc_seated_forward,
    "Bridge" to R.drawable.mlc_bridge
)

internal class RegisterStatusDiffCallback() : DiffUtil.ItemCallback<RegisterStatus>() {

    override fun areItemsTheSame(oldItem: RegisterStatus, newItem: RegisterStatus): Boolean {
        return oldItem.registerId == newItem.registerId
    }

    override fun areContentsTheSame(oldItem: RegisterStatus, newItem: RegisterStatus): Boolean {
        return oldItem == newItem
    }

}