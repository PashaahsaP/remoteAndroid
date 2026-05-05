package com.example.wmswherther.data.db.Repositories

import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.SessionIncome
import java.time.LocalDate
import java.util.UUID

class IncomeRepository (private val dao: Dao) {
    suspend fun getAllIncomeSession() : List<SessionIncome>{
        return dao.getAllIncomeSession()
    }
    suspend fun getAllActiveIncomeSession() : List<TaskMenuItem> {
        var suppliers = dao.getAllSuppliers()

        var result = dao.getAllIncomeSession()
            .filter { item -> item.status == StatusType.Created.ordinal }
            .map { item ->
                var supplier = suppliers.firstOrNull { inner -> inner.id == item.supplierId }
                TaskMenuItem(
                    supplier = supplier!!.name,
                    progress = "0/1",
                    number = item.id,
                    date = LocalDate.now().toString(),
                    supplierId = supplier!!.id,
                    sessionId = supplier!!.id

                )}
        return result
    }
    suspend fun getIncomeSessionById(sessionId : String) : SessionIncome{
        return dao.getIncomeSessionById(sessionId)
    }
    suspend fun getAllGoods(): List<Goods>{
        return dao.getGoods()
    }
    suspend fun getAllMovement(): List<Movement>{
        return dao.getAllMovement()
    }
    suspend fun getGoodsAndTheirCells(sessionId : String) : List<Pair<Goods, Cell>>{
       return dao.getAllIncomeItem()
            .filter { item -> item.sessionId == sessionId}
            .map { item ->  dao.getGoodsById(item.goodsId) }
            .map { inner -> Pair(inner, dao.getCellById(inner.cellId)) }
    }
    suspend fun getGoodsById(goodsId : String) : Goods {
        return dao.getGoodsById(goodsId)
    }
    suspend fun deleteGoodsAsync(goods : Goods, change: Change)  {
        dao.deleteGoodsAsync(goods, change)
    }
    suspend fun updateGoodsAsync(goods: Goods, change: Change){
        dao.updateGoodsAsync(goods, change)
    }
    suspend fun getBarcodeByName(barcode : String) : Barcode {
        return dao.getBarcodeByName(barcode)
    }
    suspend fun getTETypes() : List<CellType>{
       return dao.getCellTypes().filter { cellType -> cellType.type == "BoxTE" }
    }
    suspend fun getCellById(id: String): Cell{
        return dao.getCellById(id)
    }
    suspend fun getCellByName(name: String): Cell{
        return dao.getCellByName(name)
    }
    suspend fun getCatalogById(catalogId : String) : Catalog{
       return dao.getCatalogById(catalogId)
    }
    suspend fun insertMovementAsync(movement: Movement, change: Change){
        dao.insertMovementSync(movement, change)
    }
    suspend fun updateIncomeSessionAsync(session: SessionIncome, change: Change){
        dao.updateIncomeSessionAsync(session, change)
    }

}