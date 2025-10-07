package com.example.wmsRemote.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wmswherther.data.db.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    // <editor-fold desc="cell">
        @Insert
        fun insertCell(cell: Cell) : Long
        @Update
        suspend fun updateCell(cell: Cell)
        @Query("SELECT * FROM cells")
        fun getAllCells(): List<Cell>
        @Query("SELECT * FROM cells WHERE id =:cellId")
        suspend fun getCellById(cellId: Int): Cell
        @Query("SELECT * FROM cells WHERE name =:cellName")
        suspend fun getCellByName(cellName: String): Cell
        @Delete
        suspend fun deleteCell(cell: Cell)
    // </editor-fold>
    // <editor-fold desc="Supplier">
        @Insert
        fun insertSupplier(supplier: Supplier)
    // </editor-fold>


}