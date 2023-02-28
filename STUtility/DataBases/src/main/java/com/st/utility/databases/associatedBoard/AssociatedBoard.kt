package com.st.utility.databases.associatedBoard

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["deviceId"], tableName = "AssociatedBoardsDataBase")
data class AssociatedBoard(
    @ColumnInfo(name = "deviceId", index = true)
    val deviceId: String,



    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "type")
    val type: ConnectivityType,
    @ColumnInfo(name = "configuration")
    val configuration: String?,
    @ColumnInfo(name = "configurationSignaled")
    val configurationSignaled: String?,
    @ColumnInfo(name = "certificate")
    var certificate: String?,
    @ColumnInfo(name = "selfSigned")
    var selfSigned: Boolean=false,
    @ColumnInfo(name = "deviceProfile")
    val deviceProfile: String?
) {
    /**
     * Connectivity En
     */
    enum class ConnectivityType {
        nfc, lora, sigfox, ble, unknown, Sigfox, lte, wifi, Lora_ttn, http
    }
}
