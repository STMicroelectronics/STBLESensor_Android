package com.st.utility.databases.associatedBoard

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AssociatedBoardDao {
    @Query("SELECT * FROM AssociatedBoardsDataBase ORDER BY deviceId DESC")
    suspend fun getAssociatedBoards():List<AssociatedBoard>

    @Query("DELETE FROM AssociatedBoardsDataBase")
    suspend fun deleteAllEntries()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(boards: List<AssociatedBoard>)

    @Query("DELETE FROM AssociatedBoardsDataBase WHERE deviceId = :mac")
    suspend fun removeWithMAC(mac:String)

    @Query("DELETE FROM AssociatedBoardsDataBase WHERE deviceId = :deviceId")
    suspend fun removedWithDeviceID(deviceId:String)

    @Query("SELECT * FROM AssociatedBoardsDataBase WHERE deviceId = :mac")
    suspend fun getBoardDetailsWithMAC(mac:String): AssociatedBoard?

    @Query("SELECT * FROM AssociatedBoardsDataBase WHERE deviceId = :deviceId")
    suspend fun getBoardDetailsWithDeviceID(deviceId:String): AssociatedBoard?
}