package com.st.BlueSTSDK.fwDataBase.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BoardFirmware::class], version = 7, exportSchema = false)
abstract class BoardCatalogDataBase : RoomDatabase() {

    abstract fun BoardTypeDao(): BoardCatalogDao

    companion object {
        @Volatile
        private var INSTANCE: BoardCatalogDataBase? = null

        fun getDatabase(context: Context): BoardCatalogDataBase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        BoardCatalogDataBase::class.java,
                        "BoardCatalogDataBase")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}