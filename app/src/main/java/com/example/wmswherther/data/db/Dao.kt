package com.example.wmsRemote.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wmswherther.data.db.Catalog
import com.example.wmswherther.data.db.CellType
import com.example.wmswherther.data.db.Goods
import com.example.wmswherther.data.db.IncomeItem
import com.example.wmswherther.data.db.SessionIncome
import com.example.wmswherther.data.db.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    

    // <editor-fold desc="Cell">
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
    // <editor-fold desc="IncomeSession">
    @Insert
    fun insertIncomeSession(incomeSession: SessionIncome)
    @Query("SELECT * FROM sessions_income")
    fun getAllIncomeSession(): List<SessionIncome>
    // </editor-fold>
    // <editor-fold desc="IncomeItem">
    @Insert
    fun insertIncomeItem(incomeItem: IncomeItem)
    @Query("SELECT * FROM income_items")
    fun getAllIncomeItem(): List<IncomeItem>
    @Query("SELECT * FROM income_items WHERE sessionId =:incomeSessionId")
    fun getAllIncomeItemBySessionId(incomeSessionId: String): List<IncomeItem>
    // </editor-fold>
    // <editor-fold desc="CellTypes">
    @Insert
    fun insertCellType(cellType: CellType)
    @Query("SELECT * FROM cell_types WHERE type =:cellTypeName")
    suspend fun getCellTypeByName(cellTypeName: String): List<CellType>
    // </editor-fold>
    // <editor-fold desc="Supplier">
    @Insert
    fun insertSupplier(supplier: Supplier)
    @Query("SELECT * FROM suppliers")
    fun getAllSuppliers(): List<Supplier>
    // </editor-fold>
    // <editor-fold desc="Catalog">
        @Insert
        fun insertCatalog(catalog: Catalog)
    // </editor-fold>
    // <editor-fold desc="Goods">
        @Insert
        fun insertGoods(goods: Goods)
    // </editor-fold>

}