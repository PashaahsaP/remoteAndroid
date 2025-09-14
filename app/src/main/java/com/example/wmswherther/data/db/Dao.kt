package com.example.wmsRemote.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    // <editor-fold desc="catalogAtomy">
    @Insert
    fun insertCatalogAtomy(catalog: CatalogAtomy) : Long
    @Update
    suspend fun updateCatalogAtomy(catalog: CatalogAtomy)
    @Query("select * from catalog_atomy")
    fun getAllCatalogsAtomy(): List<CatalogAtomy>
    @Query("select * from catalog_atomy where id =:catalogid")
    suspend fun getCatalogAtomy(catalogid: Int): CatalogAtomy
    @Delete
    suspend fun deleteCatalog(catalog: CatalogAtomy)
    // </editor-fold>
    // <editor-fold desc="catalogBork">
    @Insert
    fun insertCatalogBork(catalog: CatalogBork) : Long
    @Update
    suspend fun updateCatalogBork(catalog: CatalogBork)
    @Query("select * from catalog_bork")
    fun getAllCatalogsBork(): List<CatalogBork>
    @Query("select * from catalog_bork where id =:catalogid")
    suspend fun getCatalogBorkById(catalogid: Int): CatalogBork
    @Query("select * from catalog_bork where name =:catalogName")
    suspend fun getCatalogBorkByName(catalogName: String): CatalogBork
    @Delete
    suspend fun deleteCatalog(catalog: CatalogBork)
    // </editor-fold>
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
    // <editor-fold desc="goodsAtomy">
    @Insert
    fun insertGoodsAtomy(goods: GoodsAtomy) : Long
    @Update
    suspend fun updateGoodsAtomy(goods: GoodsAtomy)
    @Query("SELECT * FROM goods_atomy")
    fun getAllGoodsAtomy(): List<GoodsAtomy>
    @Query("SELECT * FROM goods_atomy WHERE id =:goodsId")
    suspend fun getGoodsAtomy(goodsId: Int): GoodsAtomy
    @Query("SELECT * FROM goods_atomy WHERE TE =:goodsTE")
    suspend fun getGoodsAtomyByTE(goodsTE: String): GoodsAtomy
    @Query("SELECT * FROM goods_atomy WHERE cellId =:cellId")
    suspend fun getAllGoodsAtomyByCellId(cellId: Int): List<GoodsAtomy>
    @Delete
    suspend fun deleteGoodsAtomy(goods: GoodsAtomy)
    // </editor-fold>
    // <editor-fold desc="goodsBork">
    @Insert
    fun insertGoodsBork(goods: GoodsBork) : Long
    @Update
    suspend fun updateGoodsBork(goods: GoodsBork)
    @Query("SELECT * FROM goods_bork")
    fun getAllGoodsBork(): List<GoodsBork>
    @Query("SELECT * FROM goods_bork WHERE cellId =:cellId")
    fun getAllGoodsBorkByCellId(cellId: Int): List<GoodsBork>
    @Query("SELECT * FROM goods_bork WHERE id =:goodsId")
    suspend fun getGoodsBork(goodsId: Int): GoodsBork
    @Delete
    suspend fun deleteGoodsBork(goods: GoodsBork)
    // </editor-fold>
    // <editor-fold desc="assemblySession">
    @Insert
    fun insertAssemblySession(assembly: AssemblySession) : Long
    @Update
    suspend fun updateAssemblySession(assembly: AssemblySession)
    @Query("SELECT * FROM assembly_session")
    fun getAllAssemblySession(): List<AssemblySession>
    @Query("SELECT * FROM assembly_session  WHERE id =:assemblyId")
    suspend fun getAssemblySession(assemblyId: Int): AssemblySession
    @Delete
    suspend fun deleteAssemblySession(assembly: AssemblySession)
    // </editor-fold>
    // <editor-fold desc="assemblyBorkItem">
    @Insert
    fun insertAssemblyBorkItem(borkItem: AssemblyBorkItem) : Long
    @Update
    suspend fun updateAssemblyBorkItem(borkItem: AssemblyBorkItem)
    @Query("SELECT * FROM assembly_bork_item")
    fun getAllAssemblyBorkItem(): List<AssemblyBorkItem>
    @Query("SELECT * FROM assembly_bork_item WHERE assemblyId =:sessionId")
    fun getAllAssemblyBorkItemById(sessionId: Int): List<AssemblyBorkItem>
    @Query("SELECT * FROM assembly_bork_item  WHERE id =:borkItemId")
    suspend fun getAssemblyBorkItem(borkItemId: Int): AssemblyBorkItem
    @Delete
    suspend fun deleteAssemblyBorkItem(assembly: AssemblyBorkItem)
    // </editor-fold>
    // <editor-fold desc="assemblyAtomyItem">
    @Insert
    fun insertAssemblyAtomyItem(atomyItem: AssemblyAtomyItem) : Long
    @Update
    suspend fun updateAssemblyAtomyItem(atomyItem: AssemblyAtomyItem)
    @Query("SELECT * FROM assembly_atomy_item")
    fun getAllAssemblyAtomyItem(): List<AssemblyAtomyItem>
    @Query("SELECT * FROM assembly_atomy_item  WHERE id =:atomyItemId")
    suspend fun getAssemblyAtomyItem(atomyItemId: Int): AssemblyAtomyItem
    @Delete
    suspend fun deleteAssemblyAtomyItem(atomyItem: AssemblyAtomyItem)
    // </editor-fold>
    // <editor-fold desc="shipment">
    @Insert
    fun insertShipment(shipment: Shipment) : Long
    @Update
    suspend fun updateShipment(shipment: Shipment)
    @Query("SELECT * FROM shipment")
    fun getAllShipments(): List<Shipment>
    @Query("SELECT * FROM shipment  WHERE id =:shipmentId")
    suspend fun getShipment(shipmentId: Int): Shipment
    @Delete
    suspend fun deleteShipment(shipment: Shipment)
    // </editor-fold>
    // <editor-fold desc="barcodeBork">
    @Insert
    fun insertBorkBarcode(barcodeBork: BarcodeBork) : Long
    @Update
    suspend fun updateBorkBarcode(barcodeBork: BarcodeBork)
    @Query("SELECT * FROM barcode_bork")
    fun getAllBorkBarcode(): List<BarcodeBork>
    @Query("SELECT * FROM barcode_bork  WHERE id =:barcodeBorkId")
    suspend fun getBorkBarcodeById(barcodeBorkId: Int): BarcodeBork
    @Query("SELECT * FROM barcode_bork  WHERE name =:barcodeBork")
    suspend fun getBorkBarcodeByName(barcodeBork: String): BarcodeBork
    @Delete
    suspend fun deleteBorkBarcode(barcodeBork: BarcodeBork)
    // </editor-fold>

}