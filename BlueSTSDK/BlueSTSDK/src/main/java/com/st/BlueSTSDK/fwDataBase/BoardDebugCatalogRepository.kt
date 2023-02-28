package com.st.BlueSTSDK.fwDataBase

import android.graphics.Color
import android.util.Log
import android.widget.TextView
import com.st.BlueSTSDK.fwDataBase.db.BoardFirmware
import com.st.BlueSTSDK.fwDataBase.network.BoardCatalogService
import java.lang.Exception

class BoardDebugCatalogRepository(private val remoteDeviceCatalog: BoardCatalogService) {

    suspend fun getFwDetailsDebugDB(tvTestValidity: TextView): List<BoardFirmware> {
        val firmwaresList = ArrayList<BoardFirmware>()

        try {
            val remoteData = remoteDeviceCatalog.getFirmwares()
            //Just for debug
            remoteData.bleListBoardFirmware_v2.let {
                remoteData.bleListBoardFirmware_v2.forEachIndexed { idx, firmware -> Log.i("RemoteDB", "V2> Item $idx:\n$firmware") }
                remoteData.bleListBoardFirmware_v2.forEach{
                    firmwaresList.add(it)
                }
            }

            remoteData.bleListBoardFirmware_v1.let {
                remoteData.bleListBoardFirmware_v1.forEachIndexed { idx, firmware -> Log.i("RemoteDB", "V1> Item $idx:\n$firmware") }

                remoteData.bleListBoardFirmware_v1.forEach{
                    firmwaresList.add(it)
                }
            }

            tvTestValidity.setTextColor(Color.GREEN)
            tvTestValidity.text = "VALID Catalog"

            return firmwaresList.toList()

        } catch (e: Exception) {
            Log.e(this::javaClass.name, "Error sync: " + e.localizedMessage)
            e.printStackTrace()

            tvTestValidity.setTextColor(Color.RED)
            tvTestValidity.text = "INVALID Catalog"

            return firmwaresList.toList()
        }
    }
}