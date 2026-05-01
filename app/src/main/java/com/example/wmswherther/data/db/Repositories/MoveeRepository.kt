package com.example.wmswherther.data.db.Repositories

import com.example.wmsRemote.data.db.Dao
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods

class MoveeRepository(private val dao: Dao) {
    suspend fun getCellTypes() : List<CellType>{
        return dao.getCellTypes()
    }
    suspend fun getCellByName(name: String) : Cell {
        return dao.getCellByName(name)
    }
    suspend fun insertCellSync(cell: Cell, change: Change){
        dao.insertCellSync(cell, change)
    }
    suspend fun getGoods() : List<Goods>{
        return dao.getGoods()
    }
    suspend fun getCellById(id: String) : Cell{
        return dao.getCellById(id)
    }
    suspend fun updateCellAsync(cell:Cell, change: Change){
        dao.updateCellAsync(cell,change)
    }
    suspend fun getCatalogById(id: String): Catalog{
        return dao.getCatalogById(id)
    }
    suspend fun insertGoodsAsync(goods: Goods, change: Change){
        dao.insertGoodsAsync(goods, change)
    }
    suspend fun getGoodsById(id: String) : Goods{
        return dao.getGoodsById(id)
    }
    suspend fun deleteGoodsAsync(goods: Goods, change: Change){
        dao.deleteGoodsAsync(goods, change)
    }
    suspend fun updateGoodsAsync(goods: Goods, change: Change){
        dao.updateGoodsAsync(goods, change)
    }
    suspend fun getGoodsByCellId(cellId: String) : List<Goods>{
        return dao.getGoodsByCellId(cellId)
    }
    fun getAllCells() : List<Cell>{
        return dao.getAllCells()
    }
    suspend fun getBarcodeByName(name: String) : Barcode{
        return  dao.getBarcodeByName(name)
    }
}