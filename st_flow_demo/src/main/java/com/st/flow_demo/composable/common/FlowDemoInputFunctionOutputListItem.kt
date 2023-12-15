package com.st.flow_demo.composable.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.st.ui.theme.Grey5
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun FlowDemoInputFunctionOutputListItem(@DrawableRes iconId: Int, label: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                end = LocalDimensions.current.paddingSmall
            )
            .border(1.dp, Grey5,shape = Shapes.small)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.iconSmall)
                    .padding(all = LocalDimensions.current.paddingSmall),
                painter = painterResource(id = iconId),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

            Text(
                fontSize = 14.sp,
                text = label
            )
        }
    }
}