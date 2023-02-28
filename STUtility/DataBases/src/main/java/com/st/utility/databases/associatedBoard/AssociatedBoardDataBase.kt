package com.st.utility.databases.associatedBoard

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AssociatedBoard::class], version = 2, exportSchema = false)
abstract class AssociatedBoardDataBase : RoomDatabase() {

    abstract fun associatedBoardDao(): AssociatedBoardDao

    companion object {
        @Volatile
        private var INSTANCE: AssociatedBoardDataBase? = null

        fun getDatabase(context: Context): AssociatedBoardDataBase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AssociatedBoardDataBase::class.java,
                    "AssociatedBoardsDataBase")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
