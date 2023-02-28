/*******************************************************************************
 * COPYRIGHT(c) 2019 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK

import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.st.BlueSTSDK.Utils.FeatureCoordinate
import java.lang.reflect.InvocationTargetException

import java.util.HashMap
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Object associated to a node that represent BLE GATT Server
 */
class NodeServer(
        charToFeatureClassMap:Map<FeatureCoordinate, Class<out ExportedFeature>>,
        private val mNode: Node) {

    private var mGattServer: BluetoothGattServer? = null
    private var mBluetoothManager: BluetoothManager? = null
    private val mFeatureToCharMap: MutableMap<Class<out ExportedFeature>, BluetoothGattCharacteristic> = HashMap()
    private val mCharToFeatureMap: MutableMap<BluetoothGattCharacteristic, ExportedFeature> = HashMap()
    private val mUuidToServiceMap: MutableMap<UUID, BluetoothGattService> = HashMap()

    private val mNodeListener = CopyOnWriteArrayList<NodeServerListener>()

    /// true if the client required to enable the 2M phy (BLE 5)
    var isLe2MPhySupported = false
    private set

    /// number of payload bytes that can be sent in a single notification package
    val maxPayloadSize
        get() = currentMtu - 3

    /// current mtu used during the connection (max notification length will be currentMtu-3)
    var currentMtu:Int = 23
    private set

    private val mServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            Log.d(TAG, "Our gatt server connection state changed:$newState")
            if(status == BluetoothGatt.GATT_SUCCESS ){
                if(newState == BluetoothGattServer.STATE_CONNECTED){
                    mNodeListener.forEach { it.onConnection(this@NodeServer) }
                }else{
                    mNodeListener.forEach { it.onDisconnection(this@NodeServer) }
                }

            }
        }

        override fun onPhyUpdate(device: BluetoothDevice, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(device, txPhy, rxPhy, status)
            Log.e(TAG, "New txPhy is: $txPhy")
            Log.e(TAG, "New rxPhy is: $rxPhy")
            isLe2MPhySupported = (txPhy == 2 && rxPhy == 2)
            mNodeListener.forEach { it.onPhyUpdate(this@NodeServer,isLe2MPhySupported) }
        }


        override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
            super.onMtuChanged(device, mtu)
            currentMtu = mtu
            Log.e(TAG, "New MTU is: $mtu")
            mNodeListener.forEach { it.onMtuUpdate(this@NodeServer,mtu) }
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice, requestId: Int,
                                              descriptor: BluetoothGattDescriptor,
                                              preparedWrite: Boolean,
                                              responseNeeded: Boolean,
                                              offset: Int, value: ByteArray) {

            // now tell the connected device that this was all successful
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            val characteristic = descriptor.characteristic
            val isEnabled = value.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            Log.d(TAG,"Char: ${characteristic} -> isEnabled:${isEnabled}")
            mCharToFeatureMap[characteristic]?.let {
                if(isEnabled){
                    it.onNotificationEnabled()
                }else{
                    it.onNotificationDisabled()
                }
            }
            if (responseNeeded) {
                mGattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }

        }
    }

    private fun buildExportedFeature(type:Class<out ExportedFeature>):ExportedFeature?{
        try {
            return type.getConstructor(NodeServer::class.java).newInstance(this)
        } catch (e: NoSuchMethodException) {
            return null
        } catch (e: InvocationTargetException) {
            return null
        } catch (e: InstantiationException) {
            return null
        } catch (e: IllegalAccessException) {
            return null
        }
    }

    private fun extractAllServicesUUID(coordinates:Set<FeatureCoordinate>): Set<UUID>{
        return coordinates.map { it.service }.toSet()
    }

    private fun buildNotificableChar(uuid: UUID): BluetoothGattCharacteristic{
        val gattChar = BluetoothGattCharacteristic(uuid, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0)
        val aDesc = BluetoothGattDescriptor(CCCD_UUID, BluetoothGattDescriptor.PERMISSION_WRITE)
        aDesc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gattChar.addDescriptor(aDesc)
        return gattChar
    }

    init {

        extractAllServicesUUID(charToFeatureClassMap.keys).forEach {
            mUuidToServiceMap[it]  = BluetoothGattService(it,BluetoothGattService.SERVICE_TYPE_PRIMARY)
        }


        for ((coordinate, featureClass) in charToFeatureClassMap) {
            buildExportedFeature(featureClass)?.let { feature ->
                val gattChar = buildNotificableChar(coordinate.characteristic)

                mCharToFeatureMap[gattChar] = feature
                mFeatureToCharMap[featureClass] = gattChar
                mUuidToServiceMap[coordinate.service]?.addCharacteristic(gattChar)
            }


        }
    }


    /**
     * Initialize the Gatt Server
     * @param context context used to create the server
     */
    fun initializeGattServer(context: Context) {

        if (mBluetoothManager == null) {
            mBluetoothManager = ContextCompat.getSystemService(context,BluetoothManager::class.java)
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return
            }
        }

        mGattServer = mBluetoothManager?.openGattServer(context, mServerCallback)
        mUuidToServiceMap.values.forEach {
            mGattServer?.addService(it)
        }

    }

    /**
     * Gatt Server Connection
     */
    fun connect() : Boolean {
        return mGattServer?.connect(mNode.device, true) ?: false
    }

    /**
     * Gatt Server Disconnection
     */
    fun disconnect() {
        mGattServer?.clearServices()
        mBluetoothManager?.getConnectedDevices(BluetoothProfile.GATT_SERVER)?.forEach {
            mGattServer?.cancelConnection(it)
        }
        mGattServer?.close()
    }

    /**
     * @return the exported feature of a specific type or null if the server doesn't export the specific feature
     */
    fun <T:ExportedFeature> getExportedFeature(type:Class<out T>) : T?{
        mCharToFeatureMap.values.forEach { feature ->
            if(type.isAssignableFrom(feature.javaClass))
                @Suppress("UNCHECKED_CAST")
                return feature as T
        }
        return null
    }


    /**
     * Send a BLE notification to a specific BLE Characteristic identified by the passed feature class
     * @param feat Feature Class identifying the chosen BLE Characteristic
     * @param dataToWrite data to write updating the characteristic
     */
    fun notifyOnFeature(feat: Class<out ExportedFeature>, dataToWrite: ByteArray) {
        val gattCharacteristic = mFeatureToCharMap[feat] ?: return
        gattCharacteristic.value = dataToWrite
        mGattServer?.notifyCharacteristicChanged(mNode.device, gattCharacteristic, false)
    }


    /**
     * register a new nodeServer listener
     */
    fun addListener(listener: NodeServerListener){
        mNodeListener.addIfAbsent(listener)
    }

    /**
     * remove a nodeServer listener
     */
    fun removeListener(listener: NodeServerListener){
        mNodeListener.remove(listener)
    }

    /**
     * interface to implement to caputre the NodeServer events
     */
    interface NodeServerListener{
        /**
         * function called when a new mtu is agree between the client and the server
         * @param node server that agree the new mtu
         * @param newValue new connection mtu
         */
        fun onMtuUpdate(node:NodeServer, newValue:Int)

        /**
         * function called when a new  PHY is agree between the client and the server
         * @param node server that agree the new PHY
         * @param isUsingPhy2 true if both the client and server are using the 2MPHY
         */
        fun onPhyUpdate(node:NodeServer, isUsingPhy2:Boolean)

        /**
         * function called when a client connect to the server
         */
        fun onConnection(node:NodeServer)

        /**
         * function called when a client disconnect from the server
         */
        fun onDisconnection(node:NodeServer)
    }

    companion object {

        //Client Characteristic Config Descriptor
        private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private val TAG = NodeServer::class.java.canonicalName
    }
}
