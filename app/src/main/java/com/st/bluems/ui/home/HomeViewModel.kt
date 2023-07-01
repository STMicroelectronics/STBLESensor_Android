/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.common.Status
import com.st.blue_sdk.models.ConnectionStatus
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.models.NodeState
import com.st.login.api.StLoginManager
import com.st.preferences.StPreferences
import com.st.user_profiling.model.AuthorizedActions
import com.st.user_profiling.model.LevelProficiency
import com.st.user_profiling.model.ProfileType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences,
    private val loginManager: StLoginManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val contentResolver = context.contentResolver
    private val _scanBleDevices = MutableStateFlow<List<Node>>(emptyList())
    val scanBleDevices = _scanBleDevices.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _canExploreCatalog = MutableStateFlow(false)
    val canExploreCatalog = _canExploreCatalog.asStateFlow()
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()
    private val _isExpert = MutableStateFlow(false)
    val isExpert = _isExpert.asStateFlow()
    private val _isBetaRelease = MutableStateFlow(false)
    val isBetaRelease = _isBetaRelease.asStateFlow()
    private val _connectionStatus = MutableStateFlow(ConnectionStatus())
    val connectionStatus = _connectionStatus.asStateFlow()
    private val _boardName = MutableStateFlow("")
    val boardName = _boardName.asStateFlow()
    val pinnedDevices = stPreferences.getFavouriteDevices()
    private var connectionJob: Job? = null

    private var numberCheckedTimes = 0

    private var activityResultRegistryOwner: ActivityResultRegistryOwner? = null

    fun startScan() {
        viewModelScope.launch {
            blueManager.scanNodes().map { resource ->
                _isLoading.tryEmit(value = resource.status == Status.LOADING)

                resource.data ?: emptyList()
            }.collect { nodes ->
                _scanBleDevices.tryEmit(value = nodes)
            }
        }
    }

    fun connect(
        nodeId: String,
        maxConnectionRetries: Int = MAX_RETRY_CONNECTION,
        maxPayloadSize: Int = 248,
        onNodeReady: (() -> Unit)? = null
    ) {
        connectionJob?.cancel()
        connectionJob = viewModelScope.launch {
            var retryCount = 0
            var callback = onNodeReady

            blueManager.connectToNode(nodeId = nodeId, maxPayloadSize = maxPayloadSize).collect { node ->
                _connectionStatus.value = node.connectionStatus
                _boardName.value = node.boardType.name

                val previousNodeState = node.connectionStatus.prev
                val currentNodeState = node.connectionStatus.current

                Log.d(
                    TAG,
                    "Node state (prev: $previousNodeState - current: $currentNodeState) retryCount: $retryCount"
                )

                if (previousNodeState == NodeState.Connecting && currentNodeState == NodeState.Disconnected) {
                    retryCount += 1

                    if (retryCount > maxConnectionRetries) {
                        return@collect
                    }

                    Log.d(TAG, "Retry connection...")
                    blueManager.connectToNode(nodeId)
                }

                if (currentNodeState == NodeState.Ready) {
                    //Adding a Delay for allowing the subscription to exported BLE char for BlueVoice FullDuplex and FullBand
                    delay(500)
                    callback?.invoke()
                    callback = null
                }
            }
        }
    }

    fun setLocalBoardCatalog(fileUri: Uri) {
        viewModelScope.launch {
            blueManager.setBoardCatalog(
                fileUri = fileUri,
                contentResolver = contentResolver
            )
        }
    }

    fun readBetaCatalog() {
        viewModelScope.launch {
            val url: String = ""
            blueManager.reset(url)
        }
    }

    fun readReleaseCatalog() {
        viewModelScope.launch {
            blueManager.reset()
        }
    }

    fun addToPinnedDevices(nodeId: String) {
        stPreferences.setFavouriteDevice(nodeId = nodeId)
    }

    fun removeFromPinnedDevices(nodeId: String) {
        stPreferences.unsetFavouriteDevice(nodeId = nodeId)
    }

    fun openGitHubSourceCode() {
        Intent(Intent.ACTION_VIEW).also { intent ->
            intent.data = Uri.parse("https://github.com/STMicroelectronics/STBlueMS_Android")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun openAboutUsPage() {
        Intent(Intent.ACTION_VIEW).also { intent ->
            intent.data =
                Uri.parse("https://www.st.com/content/st_com/en/about/st_company_information/who-we-are.html")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun openPrivacyPoliciPage() {
        Intent(Intent.ACTION_VIEW).also { intent ->
            intent.data = Uri.parse("https://www.st.com/content/st_com/en/common/privacy-portal/corporate-privacy-statement.html")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun initLoginManager(
        activity: Activity
    ) {
        viewModelScope.launch {
            activityResultRegistryOwner = activity as ActivityResultRegistryOwner

            _isLoggedIn.value = loginManager.isLoggedIn()

            val level = LevelProficiency.fromString(stPreferences.getLevelProficiency())
            val levelProficiency = level
                ?.isAuthorizedTo(AuthorizedActions.EXPLORE_CATALOG) ?: false
            val profileType = ProfileType.fromString(stPreferences.getProfileType())
                ?.isAuthorizedTo(AuthorizedActions.EXPLORE_CATALOG) ?: false

            _canExploreCatalog.value = levelProficiency && profileType
            _isExpert.value = level == LevelProficiency.EXPERT
        }
    }

    fun checkProfileLevel() {
        viewModelScope.launch {

            val level = LevelProficiency.fromString(stPreferences.getLevelProficiency())
            val levelProficiency = level?.isAuthorizedTo(AuthorizedActions.EXPLORE_CATALOG) ?: false
            val profileType = ProfileType.fromString(stPreferences.getProfileType())
                ?.isAuthorizedTo(AuthorizedActions.EXPLORE_CATALOG) ?: false

            _canExploreCatalog.value = levelProficiency && profileType

            _isExpert.value = level == LevelProficiency.EXPERT
        }
    }

    fun login() {
        viewModelScope.launch {
            activityResultRegistryOwner?.let {

                loginManager.login(it.activityResultRegistry)
                _isLoggedIn.value = loginManager.isLoggedIn()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            activityResultRegistryOwner?.let {
                loginManager.logout(it.activityResultRegistry)
            }
            _isLoggedIn.value = false
        }
    }

    fun checkVersionBetaRelease() {
        _isBetaRelease.value = stPreferences.isBetaApplication()
    }

    fun switchVersionBetaRelease() {
        numberCheckedTimes++
        if (numberCheckedTimes == 7) {
            numberCheckedTimes = 0
            val isBetaVersion = stPreferences.isBetaApplication()
            if(isBetaVersion) {
                //Move to Release Version
                stPreferences.setBetaApplicationFlag(false)
                //Load the Release Catalog
                readReleaseCatalog()
                _isBetaRelease.value = false
            } else {
                //Move to Beta Version
                stPreferences.setBetaApplicationFlag(true)
                //Load the Beta Catalog
                readBetaCatalog()
                _isBetaRelease.value = true
            }
        }
    }

    fun profileShow(level: LevelProficiency, type: ProfileType) {
        stPreferences.setLevelProficiency(level = level.name)
        stPreferences.setProfileType(profile = type.name)
    }

    companion object {
        private const val TAG = "HomeViewModel"
        private const val MAX_RETRY_CONNECTION = 3
    }
}
