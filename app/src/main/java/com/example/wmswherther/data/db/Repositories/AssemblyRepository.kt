package com.example.wmswherther.data.db.Repositories

import com.example.wmsRemote.data.db.Dao
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.PickerItem
import com.example.wmswherther.data.db.Entityes.SessionPicker

class AssemblyRepository (private val dao : Dao){
    suspend fun getPickerItemBySessionId(sessionId: String) : List<PickerItem>{
        return dao.getPickerItemsBySessionId(sessionId)
    }
    suspend fun insertPickerItem(item: PickerItem){
        dao.insertPickerItem(item)
    }
    suspend fun getAllGoods() : List<Goods>{
        return dao.getGoods()
    }
    suspend fun getAllMovements() : List<Movement>{
        return dao.getAllMovement()
    }
    suspend fun deleteGoods(goods: Goods){
        dao.deleteGoods(goods)
    }
    suspend fun updatePickerItem(item: PickerItem){
        dao.updatePickerItem(item)
    }
    suspend fun getPickerItems() : List<PickerItem>{
        return dao.getPickerItems()
    }
    suspend fun getGoodsById(id: String) : Goods {
        return dao.getGoodsById(id)!!
    }
    suspend fun getCatalogById(id: String) : Catalog {
        return dao.getCatalogById(id)!!
    }
    suspend fun getCellById(id: String) : Cell {
        return dao.getCellById(id)
    }
    suspend fun getBarcodes() : List<Barcode>{
        return dao.getBarcodes()
    }
    suspend fun getCellTypes() : List<CellType>{
        return  dao.getCellTypes()
    }
    suspend fun getPickerSessionById(id: String) : SessionPicker{
        return dao.getPickerSessionById(id)!!
    }
    suspend fun getCellsByName(name: String) : List<Cell>{
        return dao.getCellsByName(name)
    }
    suspend fun insertCellSync(cell:Cell, change: Change) : Cell{
       return dao.insertCellSync(cell, change)
    }
    suspend fun getCellByName(name: String): Cell{
        return dao.getCellByName(name)
    }
    suspend fun getPickerItemById(id: String): PickerItem{
        return dao.getPickerItemById(id)!!
    }
    suspend fun insertMovementSync(movement: Movement, change: Change){
        dao.insertMovementSync(movement,change)
    }
    suspend fun updateGoodsAsync(goods: Goods, change: Change){
        dao.updateGoodsAsync(goods, change)
    }
    suspend fun updatePickerSessionSync(sessionPicker: SessionPicker, change: Change){
        dao.updatePickerSessionSync(sessionPicker,change)
    }
}