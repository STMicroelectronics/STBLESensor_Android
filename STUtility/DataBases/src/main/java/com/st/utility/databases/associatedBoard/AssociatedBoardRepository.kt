package com.st.utility.databases.associatedBoard

import android.content.Context


class AssociatedBoardRepository (private val localDeviceFirmware : AssociatedBoardDao) {

    suspend fun getAssociatedBoards() : List<AssociatedBoard> {
        return localDeviceFirmware.getAssociatedBoards()
    }

    suspend fun add(boards: List<AssociatedBoard>) {
        localDeviceFirmware.add(boards)
    }

    suspend fun removeWithMAC(mac: String) {
        localDeviceFirmware.removeWithMAC(mac)
    }

    suspend fun removeWithDeviceID(deviceId:String) {
        localDeviceFirmware.removedWithDeviceID(deviceId)
    }

    suspend fun deleteAllEntries() {
        localDeviceFirmware.deleteAllEntries()
    }

    suspend fun getBoardDetailsWithMAC(mac:String): AssociatedBoard? {
        return localDeviceFirmware.getBoardDetailsWithMAC(mac)
    }

    suspend fun getBoardDetailsWithDeviceID(deviceId:String): AssociatedBoard? {
        return localDeviceFirmware.getBoardDetailsWithDeviceID(deviceId)
    }

    companion object {

        private var instance: AssociatedBoardRepository? = null
        fun getInstance(ctx: Context): AssociatedBoardRepository {
            synchronized(this){
                if(instance == null){
                    instance = AssociatedBoardRepository(AssociatedBoardDataBase.getDatabase(ctx).associatedBoardDao())
                }
                return instance!!
            }
        }
    }
}