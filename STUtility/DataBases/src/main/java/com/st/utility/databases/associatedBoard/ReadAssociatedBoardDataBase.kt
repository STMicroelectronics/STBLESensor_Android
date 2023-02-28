package com.st.utility.databases.associatedBoard

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class ReadAssociatedBoardDataBase (val ctx: Context) : CoroutineScope {
    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    fun getAssociatedBoards() : List<AssociatedBoard> {
        val repo = AssociatedBoardRepository.getInstance(ctx)
        val listBoard: List<AssociatedBoard>
        runBlocking{
            listBoard = repo.getAssociatedBoards()
        }
        return listBoard
    }

    fun add(boards: List<AssociatedBoard>) {
        val repo = AssociatedBoardRepository.getInstance(ctx)
        runBlocking{
            repo.add(boards)
        }
    }

    fun removeWithMAC (mac : String) {
        val repo = AssociatedBoardRepository.getInstance(ctx)
        runBlocking{
            repo.removeWithMAC(mac)
        }
    }

    fun removeWithDeviceID (deviceId : String) {
        val repo = AssociatedBoardRepository.getInstance(ctx)
        runBlocking{
            repo.removeWithDeviceID(deviceId)
        }
    }

    fun getBoardDetailsWithMAC(mac:String): AssociatedBoard? {
        val repo = AssociatedBoardRepository.getInstance(ctx)
        val board: AssociatedBoard?
        runBlocking {
            board = repo.getBoardDetailsWithMAC(mac)
        }
        return board
    }

    fun getBoardDetailsWithDeviceID(deviceId:String): AssociatedBoard? {
        val repo = AssociatedBoardRepository.getInstance(ctx)
        val board: AssociatedBoard?
        runBlocking {
            board = repo.getBoardDetailsWithDeviceID(deviceId)
        }
        return board
    }

    fun deleteAllEntries() {
        val repo = AssociatedBoardRepository.getInstance(ctx)
        runBlocking{
            repo.deleteAllEntries()
        }
    }
}