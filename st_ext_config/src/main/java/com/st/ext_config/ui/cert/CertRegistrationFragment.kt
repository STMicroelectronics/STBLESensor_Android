/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.ui.cert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.navArgs
import com.st.ext_config.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.LocalDimensions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CertRegistrationFragment : Fragment() {

    private val viewModel: CertViewModel by viewModels()
    private val navArgs: CertRegistrationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val nodeId = navArgs.nodeId

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    CertRegistrationScreen(
                        nodeId = nodeId,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun CertRegistrationScreen(
    modifier: Modifier = Modifier,
    nodeId: String,
    viewModel: CertViewModel
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.startDemo(nodeId = nodeId)
            Lifecycle.Event.ON_STOP -> viewModel.stopDemo(nodeId = nodeId)
            else -> Unit
        }
    }


    val uid by viewModel.uid.collectAsStateWithLifecycle()
    val cert by viewModel.cert.collectAsStateWithLifecycle()

    CertRegistrationScreen(
        modifier = modifier,
        uid = uid,
        cert = cert,
        onRegisterCert = { uid, cert ->
            // Show Cloud selection
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertRegistrationScreen(
    modifier: Modifier = Modifier,
    uid: String,
    cert: String,
    onRegisterCert: (String, String) -> Unit = { _, _ -> /** NOOP **/ }
) {
    var internalUid by rememberSaveable(uid) {
        mutableStateOf(value = uid)
    }
    var showCert by rememberSaveable {
        mutableStateOf(value = false)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = LocalDimensions.current.paddingNormal)
            .verticalScroll(state = rememberScrollState())
    ) {
        Text(
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            text = stringResource(id = R.string.st_extConfig_boardSecurity_certRegistration)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = internalUid,
            label = {
                Text(
                    text = stringResource(id = R.string.st_extConfig_boardReport_uid),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            onValueChange = {
                internalUid = it
            }
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Row(modifier = Modifier.fillMaxWidth()) {
            BlueMsButtonOutlined(
                text = if (showCert)
                    stringResource(id = R.string.st_extConfig_boardSecurity_hideCert)
                else
                    stringResource(id = R.string.st_extConfig_boardSecurity_showCert),
                onClick = {
                    showCert = !showCert
                }
            )

            Spacer(modifier = Modifier.weight(weight = 1f))

            BlueMsButton(
                text = stringResource(id = R.string.st_extConfig_boardSecurity_registerCert),
                onClick = { onRegisterCert(internalUid, cert) }
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

        AnimatedVisibility(
            visible = showCert,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Text(
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                modifier = Modifier.fillMaxWidth(),
                text = cert,
            )
        }
    }
}

