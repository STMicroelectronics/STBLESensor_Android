package com.st.blesensor.cloud.stazure.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "RegisteredDevice")
internal data class RegisteredDevice(
        @PrimaryKey
        @ColumnInfo(name = "id")
        val id:String,
        @ColumnInfo(name = "name")
        val name:String,
        @ColumnInfo(name = "connectionString")
        val connectionString: String
)