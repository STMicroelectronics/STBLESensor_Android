package com.st.BlueSTSDK.fwDataBase.db

import com.google.gson.annotations.SerializedName
import com.st.BlueSTSDK.fwDataBase.db.BoardFirmware
import java.util.*

data class BoardCatalog(
    @SerializedName("bluestsdk_v2")
    val bleListBoardFirmware_v2: List<BoardFirmware>,
    @SerializedName("bluestsdk_v1")
    val bleListBoardFirmware_v1: List<BoardFirmware>,
    @SerializedName("characteristics")
    val characteristics: List<BleCharacteristic>,
    @SerializedName("date")
    val date: Date,
    @SerializedName("version")
    val version: String,
    @SerializedName("checksum")
    val checksum: String
)
