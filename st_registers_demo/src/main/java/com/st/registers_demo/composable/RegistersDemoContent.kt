package com.st.registers_demo.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.registers_demo.RegistersDemoViewModel
import com.st.registers_demo.common.RegistersDemoType
import com.st.ui.theme.LocalDimensions

@Composable
fun RegisterDemoContent(
    modifier: Modifier,
    demoType: RegistersDemoType,
    viewModel: RegistersDemoViewModel
) {

    val registerName = when (demoType) {
        RegistersDemoType.MLC -> "Decision Tree"
        RegistersDemoType.FSM -> "Program"
        RegistersDemoType.STRED -> "STRed"
    }

    val registersData by viewModel.registersData.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
    ) {
        items(registersData.toList()) { register ->
            RegisterStatusView(registerName = registerName, register = register)
        }

        item {
            Spacer(
                Modifier.windowInsetsBottomHeight(
                    WindowInsets.systemBars
                )
            )
        }
    }
}