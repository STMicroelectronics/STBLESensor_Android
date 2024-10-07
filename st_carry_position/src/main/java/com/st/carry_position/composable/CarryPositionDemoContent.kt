package com.st.carry_position.composable

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.carry_position.CarryPositionType
import com.st.carry_position.CarryPositionViewModel
import com.st.carry_position.R
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryYellow

@Composable
fun CarryPositionDemoContent(
    modifier: Modifier,
    viewModel: CarryPositionViewModel
) {
    val positionData by viewModel.positionData.collectAsStateWithLifecycle()

    val context = LocalContext.current

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                Toast.makeText(context, "Carry position started", Toast.LENGTH_SHORT).show()
            }

            else -> Unit
        }
    }

    val onDeskImage by remember(key1 = positionData.second) {
        derivedStateOf { positionData.first.position.value == CarryPositionType.OnDesk }
    }

    val animatedColorOnDeskImage by animateColorAsState(
        if (onDeskImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val inHandImage by remember(key1 = positionData.second) {
        derivedStateOf { positionData.first.position.value == CarryPositionType.InHand }
    }

    val animatedColorInHandImage by animateColorAsState(
        if (inHandImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val nearHeadImage by remember(key1 = positionData.second) {
        derivedStateOf { positionData.first.position.value == CarryPositionType.NearHead }
    }

    val animatedColorNearHeadImage by animateColorAsState(
        if (nearHeadImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val shirtPocketImage by remember(key1 = positionData.second) {
        derivedStateOf { positionData.first.position.value == CarryPositionType.ShirtPocket }
    }

    val animatedColorShirtPocketImage by animateColorAsState(
        if (shirtPocketImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val trousersPocketImage by remember(key1 = positionData.second) {
        derivedStateOf { positionData.first.position.value == CarryPositionType.TrousersPocket }
    }

    val animatedColorTrousersPocketImage by animateColorAsState(
        if (trousersPocketImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )

    val armSwingImage by remember(key1 = positionData.second) {
        derivedStateOf { positionData.first.position.value == CarryPositionType.ArmSwing }
    }

    val animatedColorArmSwingImage by animateColorAsState(
        if (armSwingImage) PrimaryYellow else Color.Unspecified,
        label = "color"
    )


    val unknown by remember(key1 = positionData.second) {
        derivedStateOf {
            if (positionData.second != null) {
                positionData.first.position.value == CarryPositionType.Unknown
            } else false
        }
    }

    if (unknown) {
        Toast.makeText(context, "Carry position Unknown", Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(end = LocalDimensions.current.paddingNormal),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
            ) {

                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageLarge)
                        .graphicsLayer(
                            alpha = if (inHandImage) {
                                1f
                            } else {
                                0.3f
                            }
                        )
                        .border(
                            BorderStroke(4.dp, animatedColorInHandImage),
                            RoundedCornerShape(size = 32.dp)
                        )
                        .padding(4.dp)
                        .clip(RoundedCornerShape(size = 32.dp)),
                    painter = painterResource(
                        R.drawable.carry_hand
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )

                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageLarge)
                        .graphicsLayer(
                            alpha = if (shirtPocketImage) {
                                1f
                            } else {
                                0.3f
                            }
                        )
                        .border(
                            BorderStroke(4.dp, animatedColorShirtPocketImage),
                            RoundedCornerShape(size = 32.dp)
                        )
                        .padding(4.dp)
                        .clip(RoundedCornerShape(size = 32.dp)),
                    painter = painterResource(
                        R.drawable.carry_shirt
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )

                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageLarge)
                        .graphicsLayer(
                            alpha = if (onDeskImage) {
                                1f
                            } else {
                                0.3f
                            }
                        )
                        .border(
                            BorderStroke(4.dp, animatedColorOnDeskImage),
                            RoundedCornerShape(size = 32.dp)
                        )
                        .padding(4.dp)
                        .clip(RoundedCornerShape(size = 32.dp)),
                    painter = painterResource(
                        R.drawable.carry_desk
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }

            Column(
                modifier = Modifier.weight(0.5f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
            ) {

                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageLarge)
                        .graphicsLayer(
                            alpha = if (nearHeadImage) {
                                1f
                            } else {
                                0.3f
                            }
                        )
                        .border(
                            BorderStroke(4.dp, animatedColorNearHeadImage),
                            RoundedCornerShape(size = 32.dp)
                        )
                        .padding(4.dp)
                        .clip(RoundedCornerShape(size = 32.dp)),
                    painter = painterResource(
                        R.drawable.carry_head
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )

                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageLarge)
                        .graphicsLayer(
                            alpha = if (trousersPocketImage) {
                                1f
                            } else {
                                0.3f
                            }
                        )
                        .border(
                            BorderStroke(4.dp, animatedColorTrousersPocketImage),
                            RoundedCornerShape(size = 32.dp)
                        )
                        .padding(4.dp)
                        .clip(RoundedCornerShape(size = 32.dp)),
                    painter = painterResource(
                        R.drawable.carry_trousers
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )

                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageLarge)
                        .graphicsLayer(
                            alpha = if (armSwingImage) {
                                1f
                            } else {
                                0.3f
                            }
                        )
                        .border(
                            BorderStroke(4.dp, animatedColorArmSwingImage),
                            RoundedCornerShape(size = 32.dp)
                        )
                        .padding(4.dp)
                        .clip(RoundedCornerShape(size = 32.dp)),
                    painter = painterResource(
                        R.drawable.carry_arm
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }
        }

        if (positionData.second == null) {
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = "Waiting data…"
            )
        }
    }
}
