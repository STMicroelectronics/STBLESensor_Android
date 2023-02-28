package com.st.BlueSTSDK.fwDataBase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.st.BlueSTSDK.BuildConfig
import com.st.BlueSTSDK.fwDataBase.db.*
import com.st.BlueSTSDK.fwDataBase.network.BoardCatalogService
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.util.*

class BoardCatalogRepository(
    private val remoteSyncDao: RemoteSyncBoardCatalogDao,
    private val localDeviceFirmware: BoardCatalogDao,
    private val remoteDeviceCatalog: BoardCatalogService
) {

    suspend fun getFwDetailsDB(): List<BoardFirmware> {
        if (needRemoteSync()) {
            syncLocalDb()
        }
        return localDeviceFirmware.getDeviceFirmwares()
    }

    suspend fun getRemoteBleCharacteristics(): List<BleCharacteristic> {
        if (needRemoteSync()) {
            syncLocalDb()
        }
        return remoteDeviceCatalog.getFirmwares().characteristics
    }

    suspend fun resetDb(url: String?=null) {
        localDeviceFirmware.deleteAllEntries()
        syncLocalDb(url)
    }

    suspend fun getFwDetailsNode(device_id: String, ble_fw_id: String): BoardFirmware? {
        if (needRemoteSync()) {
            syncLocalDb()
        }
        return localDeviceFirmware.getFwForDevice(device_id, ble_fw_id)
    }

    suspend fun getAllFwForFwName(device_id: String,fw_name: String): List<BoardFirmware>? {
        if (needRemoteSync()) {
            syncLocalDb()
        }
        return localDeviceFirmware.getAllFwForFwName(device_id,fw_name)
    }

    suspend fun getFwCompatibleWithNode(device_id: String): List<BoardFirmware>? {
        if (needRemoteSync()) {
            syncLocalDb()
        }
        return localDeviceFirmware.getFwCompatibleWhiteNode(device_id)
    }

    private suspend fun needRemoteSync(): Boolean {
        val minSync = Date().time - MIN_REMOTE_SYNC_TIME_MS
        val appVersion = BuildConfig.BLUESTSDK_DB_BASE_VERSION
        //Check again the DataBase checkSum if it was tested more that 24hours ago..
        // or if there is a new DB TAG version
        if ((remoteSyncDao.lastSync.before(Date(minSync))) ||
            (!remoteSyncDao.lastAppVersion.equals(appVersion))){
        //    if(true){
            val dBCheckSum = readChecksumDb()
            Log.i("RemoteDB", "dBCheckSum=$dBCheckSum")
            //Log.i("RemoteDB", "OlddBCheckSum=${remoteSyncDao.lastChecksum}")
            var dBsEqual: Boolean = remoteSyncDao.lastChecksum.equals(dBCheckSum)
            Log.i("RemoteDB", "==$dBsEqual")
            remoteSyncDao.lastChecksum = dBCheckSum

            if(!remoteSyncDao.lastAppVersion.equals(appVersion)) {
                Log.i("RemoteDB", "force Re-load")
                dBsEqual=false
            }
            remoteSyncDao.lastAppVersion = appVersion
            Log.i("RemoteDB", "lastAppVersion=${remoteSyncDao.lastAppVersion}")
            //necessity to read the DB if the 2 strings are different
            return !dBsEqual
        } else {
            Log.i("RemoteDB", "No Necessity to download")
            return false
        }
    }

    suspend fun readLocalFileDb(ctx: Context, uri: Uri): Pair<Int, String?> {
        val jsonFileString = getJsonDataFromUri(ctx, uri)
        var error: String?=null

        val gson = GsonBuilder()
            .create()
        val boardCatalog: BoardCatalog? = try {
            gson.fromJson(jsonFileString, BoardCatalog::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e("BoardTypeRepository", "error parsing the DataBase: $e")
            error = e.toString()
            null
        }
        if (boardCatalog != null) {
            //Just for debug
            boardCatalog.bleListBoardFirmware_v2.forEachIndexed { idx, firmware ->
                Log.i(
                    "New Entry ",
                    "> Item $idx:\n$firmware"
                )
            }
            //Add only the Experimental firmware
            val bleListBoardFirmware: List<BoardFirmware> =
                boardCatalog.bleListBoardFirmware_v2.filter { it.ble_fw_id == "0xFF" }

            //We need before to delete all the entries already present on DB with the same FwId&BoardId
            bleListBoardFirmware.forEach { localDeviceFirmware.deleteFwForDevice(it.ble_dev_id,it.ble_fw_id)}
            //Now we add the new entries
            localDeviceFirmware.add(bleListBoardFirmware)
            return Pair(bleListBoardFirmware.size,error)
        }
        return Pair(0,error)
    }

    @Throws(IOException::class)
    private fun getJsonDataFromUri(context: Context, uri: Uri): String? {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    private suspend fun syncLocalDb(url: String?=null) {
        try {
            val remoteData =
                if(url!=null) {
                    Log.i("syncLocalDb url",url+"catalog.json")
                    remoteDeviceCatalog.getFirmwaresFromUrl(url+"catalog.json")
                } else {
                    Log.i("syncLocalDb",url+"catalog.json")
                    remoteDeviceCatalog.getFirmwares()
                }
            //Just for debug
            remoteData.bleListBoardFirmware_v2.let {
                remoteData.bleListBoardFirmware_v2.forEachIndexed { idx, firmware ->
                    Log.i(
                        "RemoteDB",
                        "V2> Item $idx:\n$firmware"
                    )
                }
            }
            remoteData.bleListBoardFirmware_v1.let {
                remoteData.bleListBoardFirmware_v1.forEachIndexed { idx, firmware ->
                    Log.i(
                        "RemoteDB",
                        "V1> Item $idx:\n$firmware"
                    )
                }
            }
            //We need to implement a way to validate the Models
            remoteData.bleListBoardFirmware_v2.let {
                //We need before to delete all the entries already present on DB with the same FwId&BoardId
                remoteData.bleListBoardFirmware_v2.forEach { localDeviceFirmware.deleteFwForDevice(it.ble_dev_id,it.ble_fw_id)}
                localDeviceFirmware.add(remoteData.bleListBoardFirmware_v2)
            }
            remoteData.bleListBoardFirmware_v1.let {

                //We need before to delete all the entries already present on DB with the same FwId&BoardId
                remoteData.bleListBoardFirmware_v1.forEach { localDeviceFirmware.deleteFwForDevice(it.ble_dev_id,it.ble_fw_id)}
                localDeviceFirmware.add(remoteData.bleListBoardFirmware_v1)
            }
            remoteSyncDao.lastSync = Date()
            remoteSyncDao.lastAppVersion = BuildConfig.BLUESTSDK_DB_BASE_VERSION
        } catch (e: Exception) {
            Log.e(this::javaClass.name, "Error sync: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    private suspend fun readChecksumDb(): String? {
        try {
            val remoteDbInfo = remoteDeviceCatalog.getDBVersion()
            return remoteDbInfo.checksum
        } catch (e: Exception) {
            Log.e(this::javaClass.name, "Error sync: " + e.localizedMessage)
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private const val MIN_REMOTE_SYNC_TIME_MS = 24 * 60 * 60 * 1000L

        private var instance: BoardCatalogRepository? = null
        fun getInstance(ctx: Context, baseUrl: String): BoardCatalogRepository {
            synchronized(this) {
                if (instance == null) {
                    val sharedPreferences = ctx.getSharedPreferences(
                        BoardCatalogRepository::javaClass.name, Context.MODE_PRIVATE
                    )
                    instance = BoardCatalogRepository(
                        RemoteSyncBoardCatalogDao(sharedPreferences),
                        BoardCatalogDataBase.getDatabase(ctx).BoardTypeDao(),
                        BoardCatalogService.buildInstance(baseUrl)
                    )
                }
                return instance!!
            }
        }
    }
}