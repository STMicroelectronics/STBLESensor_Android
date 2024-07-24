package com.st.registers_demo.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.st.registers_demo.R
import com.st.registers_demo.common.RegisterStatus
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun RegisterStatusView(
    registerName: String,
    register: RegisterStatus
) {
    AnimatedContent(targetState = register, label = "") { it ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingSmall)
                ) {
                    val fulRegisterName = "$registerName: ${it.registerId}"
                    Text(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        text = fulRegisterName
                    )

                    it.algorithmName?.let {
                        Text(
                            modifier = Modifier.padding(start = LocalDimensions.current.paddingSmall),
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            text = it
                        )
                    }


                    val registerValueStr = if (it.label != null)
                        String.format("Value: %s (0x%X)", it.label, it.value)
                    else
                        String.format("Value: 0x%X", it.value)

                    Text(
                        modifier = Modifier.padding(start = LocalDimensions.current.paddingSmall),
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        text = registerValueStr
                    )
                }

                Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconNormal),
                    painter = painterResource(
                        getRegisterIconResourceByName(
                            it.label ?: "Default"
                        )
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }
        }
    }
}

fun getRegisterIconResourceByName(name: String) = when (name) {
//Activity Recognition
    "Walking" -> R.drawable.mlc_walking
    "Running" -> R.drawable.mlc_running
    "Standing" -> R.drawable.mlc_standing
    "Biking" -> R.drawable.mlc_biking
    "Driving" -> R.drawable.mlc_driving
    //Head Gesture
    "Nod" -> R.drawable.mlc_nod
    "Shake" -> R.drawable.mlc_shake
    "Swing" -> R.drawable.mlc_swing
    "Steady head" -> R.drawable.mlc_steady_head
    //Vibration
    "No vibration" -> R.drawable.mlc_no_vibration
    "Low vibration" -> R.drawable.mlc_low_vibration
    "High vibration" -> R.drawable.mlc_high_vibration
    //Asset Tracking
    "Stationary upright" -> R.drawable.mlc_stationary_upright
    "Stationary not upright" -> R.drawable.mlc_stationary_no_upright
    "Motion" -> R.drawable.mlc_motion
    "Shaking" -> R.drawable.mlc_shaking
    //Door opening/closing/still
    "Door closing" -> R.drawable.mlc_door_closing
    "Door still" -> R.drawable.mlc_door_still
    "Door Opening" -> R.drawable.mlc_door_opening
    //Gym activity recognition
    "No activity" -> R.drawable.mlc_standing
    "Biceps curls" -> R.drawable.mlc_biceps_curls
    "Lateral raises" -> R.drawable.mlc_lateral_raises
    "Squat" -> R.drawable.mlc_squat
    //Vehicle
    "Car moving" -> R.drawable.mlc_car_moving
    "Car still" -> R.drawable.mlc_car_still
    //Yoga Pose
    "The tree" -> R.drawable.mlc_the_tree
    "Boat pose" -> R.drawable.mlc_boat_pose
    "Bow pose" -> R.drawable.mlc_bow_pose
    "Plank inverse" -> R.drawable.mlc_plank_inverse
    "Side angle" -> R.drawable.mlc_side_angle
    "Plank" -> R.drawable.mlc_plank
    "Meditation pose" -> R.drawable.mlc_meditation_pose
    "Cobra" -> R.drawable.mlc_cobra
    "Child" -> R.drawable.mlc_child
    "Downward dog pose" -> R.drawable.mlc_downward_dog_pose
    "Seated forward" -> R.drawable.mlc_seated_forward
    "Bridge" -> R.drawable.mlc_bridge
    else -> R.drawable.registers_demo_icon
}

@Composable
@Preview
fun RegisterStatusViewExample() {
    val register = RegisterStatus(
        registerId = 1,
        value = 7,
        algorithmName = "Algorithm",
        label = "Door closing"
    )
    RegisterStatusView(registerName = "Register", register = register)
}