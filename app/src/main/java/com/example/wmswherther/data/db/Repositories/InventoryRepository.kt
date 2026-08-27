package com.example.wmswherther.data.db.Repositories

import com.example.wmsRemote.data.db.Dao
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.InventoryDiffItem
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.SessionInventory
import com.example.wmswherther.data.db.Entityes.Supplier

class InventoryRepository(private val dao: Dao)  {
    suspend fun getDiffs(): List<InventoryDiffItem>{
        return dao.getInventoryDiffItems()
    }
    suspend fun getAllSuppliers() : List<Supplier> {
        return dao.getAllSuppliers()
    }
    suspend fun getAllGoods() : List<Goods>{
        return dao.getGoods()
    }
    suspend fun getInventorySessions() : List<SessionInventory>{
        return dao.getInventorySessions()
    }
    suspend fun getSupplierById(id: Int) : Supplier?{
        return dao.getSupplierById(id)
    }
    suspend fun getCellTypes() : List<CellType>{
        return dao.getCellTypes()
    }
    suspend fun getCellByName(name: String) : Cell {
        return dao.getCellByName(name)
    }
    suspend fun getBarcodeByName(name: String) : Barcode{
        return dao.getBarcodeByName(name)
    }
    suspend fun getCatalogById(id: String) : Catalog {
        return dao.getCatalogById(id)
    }
    suspend fun getCellTypeByName(name: String): List<CellType>{
        return dao.getCellTypeByName(name)
    }
    suspend fun insertCell(cell : Cell){
        dao.insertCell(cell)
    }
    suspend fun getGoodsByCellId(id: String) : List<Goods> {
        return dao.getGoodsByCellId(id)
    }
    suspend fun getCellById(id: String) : Cell{
        return dao.getCellById(id)
    }
    suspend fun getAllCells(): List<Cell>{
        return dao.getAllCells()
    }
    suspend fun getInventorySessionById(id: String) : SessionInventory{
        return dao.getInventorySessionById(id)
    }
    suspend fun insertInventorySessionAsync(sessionInventory: SessionInventory, change: Change){
        dao.insertInventorySessionAsync(sessionInventory, change)
    }
    suspend fun insertInventoryDiffItemAsync(diff: InventoryDiffItem, change: Change){
        dao.insertInventoryDiffItemAsync(diff, change)
    }
    suspend fun getChildrenCells(id: String) : List<Cell>{
       return dao.getChildrenCells(id)
    }
    suspend fun getAllMovement() : List<Movement>{
        return dao.getAllMovement()
    }
}