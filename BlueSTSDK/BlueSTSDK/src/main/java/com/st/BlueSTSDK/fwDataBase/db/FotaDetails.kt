package com.st.BlueSTSDK.fwDataBase.db

import com.google.gson.annotations.SerializedName

data class FotaDetails(
    @SerializedName("partial_fota")
    val partial_fota: Int?=0,
    @SerializedName("type")
    val type: BoardFotaType?=BoardFotaType.no,
    @SerializedName("max_chunk_length")
    var max_chunk_length: Int?=0,
    @SerializedName("max_divisor_constraint")
    var max_divisor_constraint: Int?=0,
    @SerializedName("fw_url")
    val fw_url: String?=null,
    @SerializedName("bootloader_type")
    val bootloader_type: BootLoaderType?=BootLoaderType.none,
)
