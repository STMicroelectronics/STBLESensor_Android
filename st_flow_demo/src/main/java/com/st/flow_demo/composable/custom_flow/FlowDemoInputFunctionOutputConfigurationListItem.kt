package com.st.flow_demo.composable.custom_flow

import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.st.flow_demo.R
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey5
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue

@Composable
fun FlowDemoInputFunctionOutputConfigurationListItem(
    @DrawableRes iconId: Int,
    label: String,
    hasSettings: Boolean,
    couldBeDeleted: Boolean=false,
    onConfig: () -> Unit = { /** NOOP**/ },
    onDelete: () -> Unit = { /** NOOP**/ }
) {
    Surface(
        //shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                end = LocalDimensions.current.paddingSmall
            )
            .border(
                1.dp,
                Grey5,
                shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal)
            )
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

            Spacer(modifier = Modifier.weight(1.0f))

            if (hasSettings) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconSmall)
                        .padding(end  = LocalDimensions.current.paddingNormal)
                        .clickable { onConfig() },
                    painter = painterResource(id = R.drawable.ic_settings),
                    tint = PrimaryBlue,
                    contentDescription = null,
                )
            }

            if(couldBeDeleted) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconSmall)
                        .padding(end  = LocalDimensions.current.paddingNormal)
                        .clickable { onDelete() },
                    painter = painterResource(id = R.drawable.ic_delete),
                    tint = ErrorText,
                    contentDescription = null,
                )
            }
        }
    }
}