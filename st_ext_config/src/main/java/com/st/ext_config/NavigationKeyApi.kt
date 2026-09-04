package com.st.ext_config

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.st.blue_sdk.features.extended.ext_configuration.BanksStatus
import com.st.download_terms.StDownloadTermsConfig
import com.st.download_terms.composable.DownloadLicenseAgreementScreen
import com.st.ext_config.composable.FwDownloadNavScreen
import com.st.ext_config.composable.FwUpgradeScreen
import com.st.ext_config.ui.cert.CertRegistrationScreen
import com.st.ext_config.ui.cert.CertViewModel
import com.st.ext_config.ui.ext_config.ExtConfigScreen
import com.st.ext_config.ui.ext_config.ExtConfigViewModel
import com.st.ext_config.ui.fw_download.FwDownloadViewModel
import com.st.ext_config.ui.fw_upgrade.FwUpgradeViewModel
import com.st.ui.theme.LocalDimensions
import kotlinx.serialization.Serializable

@Serializable
data class ExtConfigurationNavKey(
    val nodeId: String
) : NavKey

@Serializable
data class FwDownloadNavKey(
    val nodeId: String,
    val banksStatus: BanksStatus?
) : NavKey

@Serializable
data class CertRequestNavKey(
    val nodeId: String
) : NavKey

@Serializable
data class CertRegistrationNavKey(
    val nodeId: String
) : NavKey

@Serializable
data class FwUpgradeNavKey(
    val nodeId: String,
    val url: String
) : NavKey

@Serializable
data object DownloadTermsNavKey : NavKey

@Composable
fun EntryProviderScope<NavKey>.ExtConfigurationNavScreen(
    backState: NavBackStack<NavKey>,
    viewModel: ExtConfigViewModel
) {
    entry<ExtConfigurationNavKey> { key ->
        ExtConfigScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = LocalDimensions.current.paddingNormal),
            viewModel = viewModel,
            nodeId = key.nodeId,
            goToFwDownload = {
                backState.add(
                    FwDownloadNavKey(
                        nodeId = key.nodeId,
                        banksStatus = viewModel.banksStatus
                    )
                )
            },
            goToCertRequest = {
                backState.add(CertRequestNavKey(nodeId = key.nodeId))
            },
            goToCertRegistration = {
                backState.add(CertRegistrationNavKey(nodeId = key.nodeId))
            }
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.FwDownloadNavScreen(
    backState: NavBackStack<NavKey>
) {
    entry<FwDownloadNavKey> { key ->
        val fwDownloadViewModel: FwDownloadViewModel = hiltViewModel()
        FwDownloadNavScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = LocalDimensions.current.paddingNormal),
            nodeId = key.nodeId,
            banksStatus = key.banksStatus,
            viewModel = fwDownloadViewModel,
            backState = backState
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.FwUpgradeNavScreen() {
    entry<FwUpgradeNavKey> { key ->
        val fwUpgradeViewModel: FwUpgradeViewModel = hiltViewModel()
        FwUpgradeScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = LocalDimensions.current.paddingNormal),
            viewModel = fwUpgradeViewModel,
            nodeId = key.nodeId,
            fwUrl = key.url,
            fwLock = false
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.CertRequestNavScreen() {
    entry<CertRequestNavKey> { key ->
        val certViewModel: CertViewModel = hiltViewModel()
        CertRegistrationScreen(
            nodeId = key.nodeId,
            viewModel = certViewModel
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.DownloadTermsNavScreen(
    backState: NavBackStack<NavKey>
) {
    entry<DownloadTermsNavKey> {
        DownloadLicenseAgreementScreen(onLicenseAgree = { decision ->
            backState.removeLastOrNull()
            StDownloadTermsConfig.onDone(decision)
        })
    }
}


@Composable
fun EntryProviderScope<NavKey>.CertRegistrationNavScreen() {
    entry<CertRegistrationNavKey> { key ->
        Text(text = "CertRequestFragment")
    }
}