package com.st.BlueSTSDK.fwDataBase

import android.content.SharedPreferences
import java.util.*

class RemoteSyncBoardCatalogDao(private val storage: SharedPreferences){

    var lastAppVersion: String?
    get() {
        return storage.getString(LAST_SYNC_VER,null)
    }
    set(value) {
        storage.edit()
            .putString(LAST_SYNC_VER,value)
            .apply()
    }

    var lastSync: Date
        get(){
            return Date(storage.getLong(LAST_SYNC_KEY,0))
        }
        set(value) {
            storage.edit()
                .putLong(LAST_SYNC_KEY,value.time)
                .apply()
        }

    var lastChecksum: String?
        get() {
            return storage.getString(LAST_CHECKSUM_KEY,null)
        }
        set(value) {
            storage.edit()
                .putString(LAST_CHECKSUM_KEY,value)
                .apply()
        }

    companion object{
        private val LAST_SYNC_KEY = RemoteSyncBoardCatalogDao::class.java.name+".LAST_SYNC_KEY"
        private val LAST_SYNC_VER = RemoteSyncBoardCatalogDao::class.java.name+".LAST_SYNC_VER"
        private val LAST_CHECKSUM_KEY = RemoteSyncBoardCatalogDao::class.java.name+"LAST_CHECKSUM_KEY"
    }

}