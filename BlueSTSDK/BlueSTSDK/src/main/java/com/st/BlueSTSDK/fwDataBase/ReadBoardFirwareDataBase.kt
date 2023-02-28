package com.st.BlueSTSDK.fwDataBase

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.TextView
import com.google.gson.GsonBuilder
import com.st.BlueSTSDK.fwDataBase.db.BoardFirmware
import com.st.BlueSTSDK.fwDataBase.network.BoardCatalogService
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import com.st.BlueSTSDK.BuildConfig
import com.st.BlueSTSDK.fwDataBase.db.BleCharacteristic

class ReadBoardFirmwareDataBase(val ctx: Context) : CoroutineScope {
    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    fun readDb() {
        resetDb(BuildConfig.BLUESTSDK_DB_BASE_URL)
    }

    fun readDbBeta() {
        resetDb(BuildConfig.BLUESTSDK_DB_BASE_BETA_URL)
    }

    fun readDebugDB(tvTestValidity: TextView, tvToUpdate: TextView) {
        val repo =
            BoardDebugCatalogRepository(BoardCatalogService.buildInstance(BuildConfig.BLUESTSDK_DB_BASE_BETA_URL))
        var listFw: List<BoardFirmware>
        CoroutineScope(Dispatchers.Main).launch {
            listFw = repo.getFwDetailsDebugDB(tvTestValidity)
            Log.d("FIRMWARES COUNT", listFw.size.toString())
            tvToUpdate.text = GsonBuilder().setPrettyPrinting().create().toJson(listFw)
        }
    }

    private fun resetDb(baseUrl: String? = null) {
        val repo =
            if (baseUrl != null) {
                BoardCatalogRepository.getInstance(ctx, baseUrl)
            } else {
                BoardCatalogRepository.getInstance(ctx, BuildConfig.BLUESTSDK_DB_BASE_URL)
            }
        runBlocking {
            repo.resetDb(baseUrl)
        }
    }

    fun readLocalFileDb(ctx: Context, uri: Uri): Pair<Int, String?> {
        val repo = BoardCatalogRepository.getInstance(ctx, BuildConfig.BLUESTSDK_DB_BASE_URL)
        var boardsAdded: Pair<Int, String?>
        runBlocking {
            boardsAdded = repo.readLocalFileDb(ctx, uri)
        }
        return boardsAdded
    }

    //This is used for SDK V2 board... where a firmware is defined by Board and Firmware ID
    fun getFwDetailsNode(device_id: Int, opt_byte_0: Int, opt_byte_1: Int): BoardFirmware? {
        val repo = BoardCatalogRepository.getInstance(ctx, BuildConfig.BLUESTSDK_DB_BASE_URL)
        val fwDetails: BoardFirmware?
        val bleFwId =
            if (opt_byte_0 != 0x00) {
                opt_byte_0 //FW id
            } else {
                opt_byte_1 + 256 //We use the next Option byte
            }

        if (bleFwId < 0) {
            //For a custom Firmware we don't have a firmware model inside the DB
            return null
        }

        runBlocking {
            fwDetails = repo.getFwDetailsNode("0x%02X".format(device_id), "0x%02X".format(bleFwId))
        }
        return fwDetails
    }

    fun getFwDetailsNode(device_id: Int, ble_fw_id: String): BoardFirmware? {
        val repo = BoardCatalogRepository.getInstance(ctx, BuildConfig.BLUESTSDK_DB_BASE_URL)
        val fwDetails: BoardFirmware?

        runBlocking {
            fwDetails = repo.getFwDetailsNode("0x%02X".format(device_id), ble_fw_id)
        }
        return fwDetails
    }

    fun getAllBleCharacteristics() : List<BleCharacteristic> {
        val repo = BoardCatalogRepository.getInstance(ctx, BuildConfig.BLUESTSDK_DB_BASE_URL)
        val listOfBleChars: List<BleCharacteristic>
        runBlocking {
            listOfBleChars = repo.getRemoteBleCharacteristics()
        }
        return listOfBleChars
    }


    fun getAllFwForFwName(device_id: Int, fw_name: String): List<BoardFirmware>? {
        val repo = BoardCatalogRepository.getInstance(ctx, BuildConfig.BLUESTSDK_DB_BASE_URL)
        val listFw: List<BoardFirmware>?

        runBlocking {
            listFw = repo.getAllFwForFwName("0x%02X".format(device_id), fw_name)
        }
        return listFw
    }

    fun getAllFwForFwName(device_id: String, fw_name: String): List<BoardFirmware>? {
        val repo = BoardCatalogRepository.getInstance(ctx, BuildConfig.BLUESTSDK_DB_BASE_URL)
        val listFw: List<BoardFirmware>?

        runBlocking {
            listFw = repo.getAllFwForFwName(device_id, fw_name)
        }
        return listFw
    }

    fun getFirstFwUpdateForFwNameDifferentFromVersion(
        device_id: String,
        fw_name: String,
        fw_version: String
    ): BoardFirmware? {
        val repo = BoardCatalogRepository.getInstance(ctx, BuildConfig.BLUESTSDK_DB_BASE_URL)
        val listFw: List<BoardFirmware>?

        runBlocking {
            listFw = repo.getAllFwForFwName(device_id, fw_name)
        }
        return listFw?.filter { it.fw_version > fw_version }?.minByOrNull { it.fw_version }
    }

    fun getListOfFwCompatibleWithBoardId(device_id: Int): List<BoardFirmware>? {
        val repo = BoardCatalogRepository.getInstance(ctx, BuildConfig.BLUESTSDK_DB_BASE_URL)
        val listFw: List<BoardFirmware>?

        runBlocking {
            listFw = repo.getFwCompatibleWithNode("0x%02X".format(device_id))
        }
        return listFw
    }

    fun getListOfFwCompatibleWithBoardId(device_id: String): List<BoardFirmware>? {
        val repo = BoardCatalogRepository.getInstance(ctx, BuildConfig.BLUESTSDK_DB_BASE_URL)
        val listFw: List<BoardFirmware>?

        runBlocking {
            listFw = repo.getFwCompatibleWithNode(device_id)
        }
        return listFw
    }

    //This is used for SDK V1 board... where a firmware is defined by Board and result of versionFw Debug console command
    fun getFwDetailsNodeSDKV1(device_id: Int, running_fw: String): BoardFirmware? {
        val repo = BoardCatalogRepository.getInstance(ctx, BuildConfig.BLUESTSDK_DB_BASE_URL)
        val fwDetails: BoardFirmware?

        runBlocking {
            fwDetails = repo.getFwDetailsNode("0x%02X".format(device_id), running_fw)
        }
        return fwDetails
    }
}