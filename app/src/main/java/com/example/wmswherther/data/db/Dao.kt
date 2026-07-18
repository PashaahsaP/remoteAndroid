package com.example.wmsRemote.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Credential
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.IncomeItem
import com.example.wmswherther.data.db.Entityes.InventoryDiffItem
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.OutcomeItem
import com.example.wmswherther.data.db.Entityes.PackageEntity
import com.example.wmswherther.data.db.Entityes.PickerItem
import com.example.wmswherther.data.db.Entityes.Service
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.SessionInventory
import com.example.wmswherther.data.db.Entityes.SessionOutcome
import com.example.wmswherther.data.db.Entityes.SessionPicker
import com.example.wmswherther.data.db.Entityes.Supplier
import com.example.wmswherther.data.db.Entityes.TrueSign
import com.example.wmswherther.data.db.Entityes.User

@Dao
interface Dao {
    

    // <editor-fold desc="Cell">
    @Insert
    suspend fun insertCell(cell: Cell)
    @Update
    suspend fun updateCell(cell: Cell)
    @Transaction
    suspend fun insertCellSync(cell: Cell, change: Change) : Cell {
        val from = insertCell(cell)
        var to = insertCellChanges(change)
        return cell
    }
   /* @Update
    suspend fun updateCell(cell: Cell)*/
    @Query("SELECT * FROM cells")
    fun getAllCells(): List<Cell>
    @Query("SELECT * FROM cells WHERE id =:cellId")
    suspend fun getCellById(cellId: String): Cell
    @Query("SELECT * FROM cells WHERE name =:cellName")
    suspend fun getCellsByName(cellName: String): List<Cell>
    @Query("SELECT * FROM cells WHERE name =:cellName")
    suspend fun getCellByName(cellName: String): Cell
    @Query("SELECT * FROM cells WHERE parentCellId =:parentCellId")
    suspend fun getChildrenCells(parentCellId: String): List<Cell>
    @Transaction
    suspend fun updateCellAsync(cell: Cell, change: Change) : Pair<Unit, Unit>{
        val from = updateCell(cell)
        val to = updateCellChanges(change)
        return from to to
    }
    /*@Delete
    suspend fun deleteCell(cell: Cell)*/
    // </editor-fold>
    // <editor-fold desc="cellTypes">
    @Query("SELECT * FROM cell_types")
    suspend fun getCellTypes(): List<CellType>
    // </editor-fold>
    // <editor-fold desc="IncomeSession">
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIncomeSession(incomeSession: SessionIncome)
    @Insert
    suspend fun insertIncomeSessionAsync(incomeSession: SessionIncome, change: Change) : Pair<Unit, Unit>{
        val from = insertIncomeSession(incomeSession)
        val to = insertIncomeSessionChanges(change)
        return from to to
    }
    @Query("SELECT * FROM sessions_income")
    suspend fun getAllIncomeSession(): List<SessionIncome>
    @Query("SELECT * FROM sessions_income WHERE id =:sessionId")
    suspend fun getIncomeSessionById(sessionId: String): SessionIncome
    @Update
    suspend fun updateIncomeSession(session: SessionIncome)
    @Transaction
    suspend fun updateIncomeSessionAsync(session: SessionIncome, change: Change) : Pair<Unit, Unit>{
        val from = updateIncomeSession(session)
        val to = updateIncomeSessionChanges(change)
        return from to to
    }
    // </editor-fold>
    // <editor-fold desc="TrueSign">
    @Insert
    fun insertTrueSign(sign: TrueSign)

    @Query("SELECT * FROM true_signs")
    fun getAllTrueSign(): List<TrueSign>

    // </editor-fold>
    // <editor-fold desc="Service">

    @Query("SELECT * FROM services")
    fun getAllService(): List<Service>
    @Insert
    fun insertService(service: Service)

    // </editor-fold>
    // <editor-fold desc="Package entities">
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPackageItem(packageItem: PackageEntity)
    @Transaction
    suspend fun insertIncomeItemSync(packageItem: PackageEntity, change: Change){
        val from = insertPackageItem(packageItem)
        val to = insertPackageChanges(change)
    }
    @Query("SELECT * FROM package_entities")
    fun getAllPackageItems(): List<PackageEntity>

    // </editor-fold>
    // <editor-fold desc="IncomeItem">
    @Insert
    fun insertIncomeItem(incomeItem: IncomeItem)
    @Transaction
    suspend fun insertIncomeItemSync(incomeItem: IncomeItem, change: Change){
        val from = insertIncomeItem(incomeItem)
        val to = insertIncomeItemChanges(change)
    }
    @Query("SELECT * FROM income_items")
    fun getAllIncomeItem(): List<IncomeItem>
    @Query("SELECT * FROM income_items WHERE sessionId =:incomeSessionId")
    fun getAllIncomeItemBySessionId(incomeSessionId: String): List<IncomeItem>
    // </editor-fold>
    // <editor-fold desc="OutcomeItem">
    @Insert
    fun insertOutcomeItem(outcomeItem: OutcomeItem)
    @Transaction
    suspend fun insertOutcomeItemSync(outcomeItem: OutcomeItem, change: Change){
        val from = insertOutcomeItem(outcomeItem)
        val to = insertOutcomeChanges(change)
    }
    @Query("SELECT * FROM outcome_items")
    fun getAllOutcomeItems(): List<OutcomeItem>
    @Query("SELECT * FROM outcome_items WHERE sessionId =:outcomeSessionId")
    fun getAllOutcomeItemBySessionId(outcomeSessionId: String): List<OutcomeItem>
    // </editor-fold>
    // <editor-fold desc="OutcomeSession">

    @Query("SELECT * FROM sessions_outcome")
    fun getAllOutcomeSession(): List<SessionOutcome>

    @Insert
    fun insertOutcomeSession(sessionOutcome: SessionOutcome)
    // </editor-fold>
    // <editor-fold desc="CellTypes">
    @Insert
    fun insertCellType(cellType: CellType)
    @Insert
    suspend fun insertCellTypeSync(cellType: CellType, change: Change):Pair<Unit, Unit>{
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
    suspend fun insertSupplierSync(supplier: Supplier, change: Change) : Pair<Unit, Unit> {
        val from = insertSupplier(supplier)
        val  to = insertSupplierChanges(change)
        return from to to
    }
    @Query("SELECT * FROM suppliers")
    fun getAllSuppliers(): List<Supplier>
    @Query("SELECT * FROM suppliers WHERE id =:id")
    suspend fun getSupplierById(id: Int): Supplier
    // </editor-fold>
    // <editor-fold desc="Credential">
    @Insert
    fun insertCredential(credential: Credential) : Long
    @Query("SELECT * FROM credentials")
    fun getAllCredential(): List<Credential>
    @Query("SELECT * FROM credentials WHERE id =:id")
    suspend fun getCredentialById(id: Long): Credential
    // </editor-fold>
    // <editor-fold desc="User">
    @Insert
    fun insertUser(user: User)
    @Query("SELECT * FROM users")
    fun getAllUser(): List<User>
    @Query("SELECT * FROM users WHERE id =:id")
    suspend fun getUserById(id: Long): User
    // </editor-fold>
    // <editor-fold desc="Catalog">
    @Insert
    fun insertCatalog(catalog: Catalog)
    @Transaction
    suspend fun insertCatalogSync(catalog: Catalog, change: Change) : Pair<Unit, Unit>{
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
        suspend fun insertGoodsAsync(goods: Goods, change: Change) : Pair<Unit, Unit>{
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
        @Update
        suspend fun updateGoods(goods: Goods)
        @Delete
        suspend fun deleteGoods(goods: Goods)
        @Transaction
        suspend fun updateGoodsAsync(goods: Goods, change: Change) : Pair<Unit, Unit>{
            val from = updateGoods(goods)
            val to = updateGoodsChanges(change)
            return from to to
        }
        @Transaction
        suspend fun deleteGoodsAsync(goods: Goods, change: Change) : Pair<Unit, Unit>{
            val from = deleteGoods(goods)
            val to = deleteGoodsChanges(change)
            return from to to
        }
    // </editor-fold>
    // <editor-fold desc="Barcodes">
    @Insert
    fun insertBarcode(barcode: Barcode)
    @Transaction
    suspend fun insertBarcodeAsync(barcode: Barcode, change: Change) : Pair<Unit, Unit>{
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
    suspend fun insertPackageChanges(change: Change)
    @Insert
    suspend fun insertOutcomeChanges(change: Change)
    @Insert
    suspend fun insertMovementChanges(change: Change)
    @Insert
    suspend fun insertCellChanges(change: Change)
    @Insert
    suspend fun insertIncomeSessionChanges(change: Change)
    @Insert
    suspend fun insertIncomeItemChanges(change: Change)
    @Insert
    suspend fun insertCellTypeChanges(change: Change)
    @Insert
    suspend fun insertSupplierChanges(change: Change)
    @Insert
    suspend fun insertCatalogChanges(change: Change)
    @Insert
    suspend fun insertGoodsChanges(change: Change)
    @Insert
    suspend fun insertBarcodeChanges(change: Change)
    @Insert
    suspend fun insertInventoryDiffItemChanges(change: Change)
    @Insert
    suspend fun insertInventorySessionChanges(change: Change)
    @Insert
    suspend fun insertPickerSessionChanges(change: Change)
    @Insert
    suspend fun insertPickerItemChanges(change: Change)
    @Insert
    suspend fun updateGoodsChanges(change: Change)
    @Insert
    suspend fun updatePickerSessionChanges(change: Change)
    @Insert
    suspend fun updateIncomeSessionChanges(change: Change)
    @Insert
    suspend fun updateCellChanges(change: Change)
    @Delete
    suspend fun deleteGoodsChanges(change: Change)
    @Query("SELECT * FROM changes")
    fun getAllChanges(): List<Change>
    @Update
    suspend fun updateChange(change: Change)
    // </editor-fold>
    // <editor-fold desc="Movement">
    @Insert
    suspend fun insertMovement(movement: Movement)
    @Update
    suspend fun updateMovement(movement: Movement)
    @Transaction
    suspend fun insertMovementSync(movement: Movement, change: Change) : Pair<Unit, Unit> {
        val from = insertMovement(movement)
        var to = insertMovementChanges(change)
        return from to to
    }
    @Query("SELECT * FROM movements")
    fun getAllMovement(): List<Movement>
    @Query("SELECT * FROM movements WHERE id =:movementId")
    suspend fun getMovementById(movementId: String): Movement
    // </editor-fold>
    // <editor-fold desc="InventoryDiffItem">
    @Insert
    fun insertInventoryDiffItem(diff: InventoryDiffItem)
    @Transaction
    suspend fun insertInventoryDiffItemAsync(diff: InventoryDiffItem, change: Change) : Pair<Unit, Unit>{
        val from = insertInventoryDiffItem(diff)
        var to = insertInventoryDiffItemChanges(change)
        return from to to
    }
    @Query("SELECT * FROM inventory_diff_items WHERE id =:id")
    suspend fun getInventoryDiffItemById(id: String): InventoryDiffItem
    @Query("SELECT * FROM inventory_diff_items")
    suspend fun getInventoryDiffItems(): List<InventoryDiffItem>
    // </editor-fold>
    // <editor-fold desc="SessionInventory">
    @Insert
    fun insertInventorySession(session: SessionInventory)
    @Transaction
    suspend fun insertInventorySessionAsync(session: SessionInventory, change: Change) : Pair<Unit, Unit>{
        val from = insertInventorySession(session)
        var to = insertInventorySessionChanges(change)
        return from to to
    }
    @Query("SELECT * FROM sessions_inventory WHERE id =:id")
    suspend fun getInventorySessionById(id: String): SessionInventory
    @Query("SELECT * FROM sessions_inventory")
    suspend fun getInventorySessions(): List<SessionInventory>


    // </editor-fold>
    // <editor-fold desc="SessionPicker">
    @Insert
    fun insertPickerSession(session: SessionPicker)
    @Transaction
    suspend fun insertPickerSessionAsync(session: SessionPicker, change: Change) : Pair<Unit, Unit>{
        val from = insertPickerSession(session)
        var to = insertPickerSessionChanges(change)
        return from to to
    }
    @Query("SELECT * FROM sessions_picker WHERE id =:id")
    suspend fun getPickerSessionById(id: String): SessionPicker
    @Query("SELECT * FROM sessions_picker")
    suspend fun getPickerSessions(): List<SessionPicker>
    @Update
    suspend fun updatePickerSession(item: SessionPicker)
    @Transaction
    suspend fun updatePickerSessionSync(session: SessionPicker, change: Change) : Pair<Unit, Unit> {
        val from = updatePickerSession(session)
        var to = insertPickerSessionChanges(change)
        return from to to
    }
    // </editor-fold>
    // <editor-fold desc="PickerItem">
    @Insert
    fun insertPickerItem(item: PickerItem)
    @Transaction
    suspend fun insertPickerItemAsync(item: PickerItem, change: Change) : Pair<Unit, Unit>{
        val from = insertPickerItem(item)
        var to = insertPickerItemChanges(change)
        return from to to
    }
    @Query("SELECT * FROM picker_items WHERE id =:id")
    suspend fun getPickerItemById(id: String): PickerItem
    @Query("SELECT * FROM picker_items")
    suspend fun getPickerItems(): List<PickerItem>
    @Query("SELECT * FROM picker_items WHERE sessionId=:sessionId")
    suspend fun getPickerItemsBySessionId(sessionId: String): List<PickerItem>
    @Update
    suspend fun updatePickerItem(item: PickerItem)
    // </editor-fold>
}