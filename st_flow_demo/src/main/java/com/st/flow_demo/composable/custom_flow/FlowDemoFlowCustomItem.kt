package com.st.flow_demo.composable.custom_flow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.st.flow_demo.R
import com.st.flow_demo.helpers.canBeUsedAsExp
import com.st.flow_demo.helpers.getOutputIconResourceByName
import com.st.flow_demo.models.Flow
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue


@Composable
fun FlowDemoFlowCustomItem(
    flow: Flow,
    onFlowSelected: () -> Unit = { /** NOOP**/ },
    onPlayFlow: () -> Unit = { /** NOOP**/ },
    onDeleteFlow: () -> Unit = { /** NOOP**/ }
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal),
        shadowElevation = LocalDimensions.current.elevationNormal,
        onClick = onFlowSelected
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

            if(canBeUsedAsExp(flow)) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconNormal)
                        .padding(all = LocalDimensions.current.paddingSmall),
                    painter = painterResource(getOutputIconResourceByName("ic_expr")),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            } else {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconNormal)
                        .padding(all = LocalDimensions.current.paddingSmall),
                    painter = painterResource(
                        id = if (flow.outputs.size > 1) getOutputIconResourceByName(
                            "ic_multi"
                        ) else getOutputIconResourceByName(flow.outputs[0].icon)
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }


            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingLarge))

            Text(
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                text = flow.description
            )

            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingLarge))

            if(flow.outputs.firstOrNull { it.icon=="ic_bluetooth" || it.icon=="ic_usb" || it.icon=="ic_sdcard"}!=null) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconSmall)
                        .clickable { onPlayFlow() },
                    painter = painterResource(id = R.drawable.ic_upload),
                    tint = PrimaryBlue,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))
            }

            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.iconSmall)
                    .clickable { onDeleteFlow() },
                painter = painterResource(id = R.drawable.ic_delete),
                tint = ErrorText,
                contentDescription = null
            )
        }
    }
}