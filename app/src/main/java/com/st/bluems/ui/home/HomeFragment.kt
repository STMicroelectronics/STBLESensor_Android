/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.st.bluems.NFCConnectionViewModel
import com.st.bluems.ui.composable.ConnectionStatusDialog
import com.st.bluems.ui.composable.DeviceListScreen
import com.st.ui.theme.BlueMSTheme
import com.st.user_profiling.StUserProfilingConfig
import com.st.user_profiling.model.LevelProficiency
import com.st.user_profiling.model.ProfileType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private val nfcViewModel: NFCConnectionViewModel by activityViewModels()

    private val _isBleEnabled = MutableStateFlow(false)
    private val isBleEnabled = _isBleEnabled.asStateFlow()
    private val bleReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED ||
                intent?.action == BluetoothAdapter.ACTION_REQUEST_ENABLE
            ) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> _isBleEnabled.value = false
                    BluetoothAdapter.STATE_ON -> _isBleEnabled.value = true
                }
            }
        }
    }

    private val bleRequestLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                _isBleEnabled.value = true
            }
        }

    private val locationRequestLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            //if (result.resultCode == Activity.RESULT_OK) {
                _isLocationEnabled.value = isLocationEnable(requireContext())
            //}
        }

    private val _isLocationEnabled = MutableStateFlow(false)
    private val isLocationEnabled = _isLocationEnabled.asStateFlow()

    private var blePairingReceiver : BroadcastReceiver?=null

    fun onBoundStateChange(context: Context?, intent: Intent) {
        val boundState = intent.getIntExtra(
            BluetoothDevice.EXTRA_BOND_STATE,
            BluetoothDevice.BOND_NONE
        )
        //android lollipop show 2 notification -> we delete the pin only when we have finish
        // to bound
        if(boundState != BluetoothDevice.BOND_BONDING) {
            nfcViewModel.setNFCPairingPin(null)  //the pairing is done -> remove the pin
        }
    }


    @SuppressLint("MissingPermission")
    fun onPairingRequest(context: Context?, intent: Intent) {
        val pairingPin = nfcViewModel.pairingPin.value
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
        if((device!=null) && (pairingPin!=null)){
            val node = viewModel.getNodeFromNodeId(device.address)
            node?.device?.setPin(pairingPin)
           //nfcViewModel.setNFCPairingPin(null)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initLoginManager(requireActivity())
//        viewModel.checkVersionBetaRelease()
//        viewModel.checkBoardsCatalogPresence()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        super.onResume()
//        viewModel.initLoginManager(requireActivity())
        viewModel.checkVersionBetaRelease()
        viewModel.checkServerForced()
        viewModel.checkBoardsCatalogPresence()

        requireContext().registerReceiver(
            bleReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )

        if(nfcViewModel.pairingPin.value!=null) {
            blePairingReceiver =  object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent != null) {
                        when(intent.action) {
                            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> onBoundStateChange(context, intent)
                            BluetoothDevice.ACTION_PAIRING_REQUEST -> onPairingRequest(context,intent)
                        }
                    }
                }
            }
            requireContext().registerReceiver(
                blePairingReceiver,
                IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
                    addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                }
            )
        }

        //Because if we change the Profile&Level on DemoList...
        //when we will come back to HomeFragment,
        //the variable for allowing the catalog exploration was not updated
        //So we force a further check
        viewModel.checkProfileLevel()

        StUserProfilingConfig.onDone = { level: LevelProficiency, type: ProfileType ->
            viewModel.profileShow(level = level, type = type)

            val navOptions: NavOptions = navOptions {
                popUpTo(com.st.bluems.R.id.homeFragment) { inclusive = true }
            }

            findNavController().navigate(
                directions = HomeFragmentDirections.actionUserProfilingNavGraphToHomeFragment(), navOptions  = navOptions
            )
        }
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(bleReceiver)

        blePairingReceiver?.let {
            requireContext().unregisterReceiver(blePairingReceiver)
            blePairingReceiver = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bluetoothManager =
            ContextCompat.getSystemService(requireContext(), BluetoothManager::class.java)
        val bleAdapter = bluetoothManager?.adapter

        _isBleEnabled.value = bleAdapter?.isEnabled ?: false

        _isLocationEnabled.value = isLocationEnable(requireContext())

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    val isBleEnabled by isBleEnabled.collectAsStateWithLifecycle()
                    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
                    val connectionBoardName by viewModel.boardName.collectAsStateWithLifecycle()
                    val isLocationEnabled by isLocationEnabled.collectAsStateWithLifecycle()

                    DeviceListScreen(
                        viewModel = viewModel,
                        nfcViewModel = nfcViewModel,
                        isBleEnabled = isBleEnabled,
                        navController = findNavController(),
                        onEnableBle = {
                            bleRequestLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        },
                        isLocationEnable = isLocationEnabled,
                        onEnableLocation = {
                            locationRequestLauncher.launch(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                    )

                    ConnectionStatusDialog(connectionStatus = connectionStatus, boardName = connectionBoardName)
                }
            }
        }
    }
}


fun isLocationEnable(context: Context): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return LocationManagerCompat.isLocationEnabled(manager)
}
