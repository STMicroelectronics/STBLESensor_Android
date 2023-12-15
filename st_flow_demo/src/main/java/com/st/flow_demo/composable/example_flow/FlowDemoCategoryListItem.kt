package com.st.flow_demo.composable.example_flow

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.st.flow_demo.R
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.Shapes

@Composable
fun FlowDemoCategoryListItem(
    category: String,
    onCategorySelected: () -> Unit = { /** NOOP**/ }
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal,
        onClick = onCategorySelected
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

            //For category only Bluetooth icon
            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.iconNormal)
                    .padding(all = LocalDimensions.current.paddingSmall),
                painter = painterResource(id = R.drawable.ic_unfold_more),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingLarge))

            Text(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                text = category
            )

            Spacer(modifier = Modifier.weight(1.0f))

            Icon(
                modifier = Modifier.size(size = LocalDimensions.current.iconNormal),
                painter = painterResource(id = R.drawable.ic_right_arrow),
                tint = PrimaryBlue,
                contentDescription = null
            )
        }
    }
}