package com.st.blesensor.cloud.stazure.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RegisteredDevice::class], version = 1, exportSchema = false)
internal abstract class RegisteredDeviceDatabase : RoomDatabase() {

    abstract fun registeredDevices(): RegisteredDeviceDao;

    companion object {
        @Volatile
        private var INSTANCE: RegisteredDeviceDatabase? = null

        fun getDatabase(context: Context): RegisteredDeviceDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        RegisteredDeviceDatabase::class.java,
                        "RegisteredDeviceDatabase"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}