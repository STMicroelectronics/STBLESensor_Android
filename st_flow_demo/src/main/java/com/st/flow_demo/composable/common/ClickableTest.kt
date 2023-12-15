package com.st.flow_demo.composable.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.sp

import com.st.ui.theme.Grey0
import com.st.ui.theme.LocalDimensions

import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.Shapes

@Composable
fun ClickableTest(modifier: Modifier = Modifier,text: String, onClick: () -> Unit = { /** NOOP**/ }) {
    Surface(
        modifier = modifier,
        shape = Shapes.extraSmall,
        //shadowElevation = LocalDimensions.current.elevationNormal,
        onClick = onClick,
        color = PrimaryBlue
    ) {
        Text(
            modifier = Modifier
                .padding(all = LocalDimensions.current.paddingSmall),
            fontSize = 14.sp,
            text = text,
            color = Grey0,
            //color = PrimaryBlue
        )
    }
}