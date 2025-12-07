package com.example.wmsRemote.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.wmswherther.data.db.Barcode
import com.example.wmswherther.data.db.Catalog
import com.example.wmswherther.data.db.CellType
import com.example.wmswherther.data.db.Change
import com.example.wmswherther.data.db.Goods
import com.example.wmswherther.data.db.IncomeItem
import com.example.wmswherther.data.db.SessionIncome
import com.example.wmswherther.data.db.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    

    // <editor-fold desc="Cell">
    @Insert
    fun insertCell(cell: Cell)
    @Transaction
    fun insertCellSync(cell: Cell, change: Change) : Pair<Unit, Unit> {
        val from = insertCell(cell)
        var to = insertCellChanges(change)
        return from to to
    }
   /* @Update
    suspend fun updateCell(cell: Cell)*/
    @Query("SELECT * FROM cells")
    fun getAllCells(): List<Cell>
    @Query("SELECT * FROM cells WHERE id =:cellId")
    suspend fun getCellById(cellId: String): Cell
    @Query("SELECT * FROM cells WHERE name =:cellName")
    suspend fun getCellByName(cellName: String): Cell
    @Query("SELECT * FROM cells WHERE parentCellId =:parentCellId")
    suspend fun getChildrenCells(parentCellId: String): List<Cell>
    /*@Delete
    suspend fun deleteCell(cell: Cell)*/
    // </editor-fold>
    // <editor-fold desc="IncomeSession">
    @Insert
    fun insertIncomeSession(incomeSession: SessionIncome)
    @Insert
    fun insertIncomeSessionAsync(incomeSession: SessionIncome, change: Change) : Pair<Unit, Unit>{
        val from = insertIncomeSession(incomeSession)
        val to = insertIncomeSessionChanges(change)
        return from to to
    }
    @Query("SELECT * FROM sessions_income")
    fun getAllIncomeSession(): List<SessionIncome>
    @Query("SELECT * FROM sessions_income WHERE id =:sessionId")
    suspend fun getIncomeSessionById(sessionId: String): SessionIncome
    // </editor-fold>
    // <editor-fold desc="IncomeItem">
    @Insert
    fun insertIncomeItem(incomeItem: IncomeItem)
    @Insert
    fun insertIncomeItemSync(incomeItem: IncomeItem, change: Change){
        val from = insertIncomeItem(incomeItem)
        val to = insertIncomeItemChanges(change)
    }
    @Query("SELECT * FROM income_items")
    fun getAllIncomeItem(): List<IncomeItem>
    @Query("SELECT * FROM income_items WHERE sessionId =:incomeSessionId")
    fun getAllIncomeItemBySessionId(incomeSessionId: String): List<IncomeItem>
    // </editor-fold>
    // <editor-fold desc="CellTypes">
    @Insert
    fun insertCellType(cellType: CellType)
    @Insert
    fun insertCellTypeSync(cellType: CellType, change: Change):Pair<Unit, Unit>{
        val from = insertCellType(cellType)
        val to = insertCellTypeChanges(change)
        return from to to
    }
    @Query("SELECT * FROM cell_types WHERE type =:cellTypeName")
    suspend fun getCellTypeByName(cellTypeName: String): List<CellType>
    @Query("SELECT * FROM cell_types WHERE id =:cellTypeId")
    suspend fun getCellTypeById(cellTypeId: String): CellType
    // </editor-fold>
    // <editor-fold desc="Supplier">
    @Insert
    fun insertSupplier(supplier: Supplier)
    @Insert
    fun insertSupplierSync(supplier: Supplier, change: Change) : Pair<Unit, Unit> {
        val from = insertSupplier(supplier)
        val  to = insertSupplierChanges(change)
        return from to to
    }
    @Query("SELECT * FROM suppliers")
    fun getAllSuppliers(): List<Supplier>
    // </editor-fold>
    // <editor-fold desc="Catalog">
    @Insert
    fun insertCatalog(catalog: Catalog)
    @Transaction
    fun insertCatalogSync(catalog: Catalog, change: Change) : Pair<Unit, Unit>{
        val from = insertCatalog(catalog)
        val to = insertCatalogChanges(change)
        return from to to
    }
    @Query("SELECT * FROM catalogs WHERE id =:catalogId")
    suspend fun getCatalogById(catalogId: String): Catalog
    @Query("SELECT * FROM catalogs ")
    suspend fun getCatalogs(): List<Catalog>
    // </editor-fold>
    // <editor-fold desc="Goods">
        @Insert
        fun insertGoods(goods: Goods)
        @Transaction
        fun insertGoodsAsync(goods: Goods, change: Change) : Pair<Unit, Unit>{
            val from = insertGoods(goods)
            val to = insertGoodsChanges(change)
            return from to to
        }
        @Query("SELECT * FROM goods WHERE catalogId =:catalogId")
        suspend fun getGoodsByCatalogId(catalogId: String): List<Goods>
        @Query("SELECT * FROM goods WHERE id =:goodsId")
        suspend fun getGoodsById(goodsId: String): Goods
        @Query("SELECT * FROM goods WHERE cellId =:cellId")
        suspend fun getGoodsByCellId(cellId: String): List<Goods>
        @Query("SELECT * FROM goods ")
        suspend fun getGoods(): List<Goods>
    // </editor-fold>
    // <editor-fold desc="Barcodes">
    @Insert
    fun insertBarcode(barcode: Barcode)
    @Transaction
    fun insertBarcodeAsync(barcode:Barcode, change: Change) : Pair<Unit, Unit>{
        val from = insertBarcode(barcode)
        var to = insertBarcodeChanges(change)
        return from to to
    }
    @Query("SELECT * FROM barcodes WHERE name =:barcodeName")
    suspend fun getBarcodeByName(barcodeName: String): Barcode
    @Query("SELECT * FROM barcodes")
    suspend fun getBarcodes(): List<Barcode>
    // </editor-fold>
    // <editor-fold desc="Changes">
    @Insert
    fun insertCellChanges(change: Change)
    @Insert
    fun insertIncomeSessionChanges(change: Change)
    @Insert
    fun insertIncomeItemChanges(change: Change)
    @Insert
    fun insertCellTypeChanges(change: Change)
    @Insert
    fun insertSupplierChanges(change: Change)
    @Insert
    fun insertCatalogChanges(change: Change)
    @Insert
    fun insertGoodsChanges(change: Change)
    @Insert
    fun insertBarcodeChanges(change: Change)



    // </editor-fold>

}