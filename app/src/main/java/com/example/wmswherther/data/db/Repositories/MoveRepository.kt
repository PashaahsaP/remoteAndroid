package com.example.wmswherther.data.db.Repositories

import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.MoveSessionItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.viewModel.MainViewModel
import java.util.UUID

class MoveRepository(
    private val dao: Dao
) {
    suspend  fun createGoodsInDestinationCell(
        item: MoveSessionItem,
        cellTo: Cell,
        viewModel: MainViewModel) {
        var goods : Goods = createGoods(item.catalogId, item.haveCount, cellTo)
        var changes = createChangeForGoods(goods.id, viewModel, OperationType.InsertGoods)
        dao.insertGoodsAsync(goods,changes)
    }
    private fun createGoods(
        catalogId: String,
        amount: Int,
        cellTo: Cell
    ) = Goods(
        id = UUID.randomUUID().toString(),
        amount = amount,
        cellId = cellTo.id,
        catalogId = catalogId,
        createdAt = System.currentTimeMillis(),
        true,
        other = null
    )
    /**
     * Обновление или удаление [Goods], если количество после перемещение 0.
     */
     suspend fun updateOrRemoveGoodsInSource(
        item: MoveSessionItem,
        viewModel: MainViewModel,
        dao: Dao
    ) {
        if (item.haveCount == item.allCount) {
            var deleteChanges =
                createChangeForGoods(item.goodsId, viewModel, OperationType.DeleteGoods)
            dao.deleteGoodsAsync(dao.getGoodsById(item.goodsId), deleteChanges)
        } else {
            var updateChange =
                createChangeForGoods(item.goodsId, viewModel, OperationType.UpdateGoods)
            var updatedGoods =
                dao.getGoodsById(item.goodsId).copy(amount = item.allCount - item.haveCount)
            dao.updateGoodsAsync(updatedGoods, updateChange)
        }
    }

     suspend fun updateGoodsInDestinationCell(
        allGoods: List<Goods>,
        item: MoveSessionItem,
        viewModel: MainViewModel,
        dao: Dao
    ) {
        var destinationGoods = allGoods.first { goods -> goods.catalogId == item.catalogId }
        var destinationChange: Change = createChangeForGoods(destinationGoods.id, viewModel, OperationType.UpdateGoods)
        dao.updateGoodsAsync(destinationGoods.copy(amount = item.haveCount + destinationGoods.amount), destinationChange)
    }
    suspend fun moveGoodsToCell(
        item: MoveSessionItem,
        allGoods: List<Goods>,
        cellTo: Cell,
        viewModel: MainViewModel
    ) {

        var catalog = dao.getCatalogById(item.catalogId)
        var listOfGoodsInCellTo: List<Goods> = allGoods
            .filter { goodsItem -> goodsItem.cellId == cellTo.id && goodsItem.catalogId == catalog.id }
        // update db
        if (listOfGoodsInCellTo.isEmpty() && item.haveCount != 0) {
            createGoodsInDestinationCell(item, cellTo, viewModel)
            updateOrRemoveGoodsInSource(item, viewModel, dao)
        } else if (listOfGoodsInCellTo.isNotEmpty() && item.haveCount != 0) {
            updateGoodsInDestinationCell(
                allGoods,
                item,
                viewModel,
                dao
            )
            updateOrRemoveGoodsInSource(item, viewModel, dao)
        }

    }
    /**
     * @param entityId Получение id сущности над которым будет происходить какое то действие
     * @param viewModel Нужна для получения id поставщика
     * @param operationType Для понимания типа операции, когда будет выполнятся синхронизация удаленной и локальных баз
     */
    private fun createChangeForGoods(
        entityId: String,
        viewModel: MainViewModel,
        operationType: OperationType
    ) = Change(
        id = UUID.randomUUID().toString(),
        entityId = entityId,
        operationType = operationType.ordinal,
        status = StatusType.Created.ordinal,
        supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
        other = null
    )
    suspend fun getCell(barcode: String,
                        viewModel: MainViewModel,
                        sourceCellName: String): Cell {
        var cell = dao.getCellByName(barcode)
        if (cell == null) {
            var curCell = dao.getCellByName(sourceCellName) // откуда идет перемещение
            var newCell = Cell(
                id = UUID.randomUUID().toString(),
                typeCellId = curCell.typeCellId,
                parentCellId = curCell.parentCellId,
                name = barcode
            )
            var changes = Change(
                id = UUID.randomUUID().toString(),
                entityId = newCell.id,
                operationType = OperationType.InsertCell.ordinal,
                status = StatusType.Created.ordinal,
                supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
                other = null
            )
            dao.insertCellSync(newCell, changes)
            cell = newCell
        }
        return cell
    }

    suspend fun moveCellToCell(item: MoveSessionItem,
                               cellTo: Cell, dao: Dao,
                               viewModel: MainViewModel) {
        if(item.haveCount == 1) { // ячейка выбрана поэтому 1, больше 1 быть не может
            var changes = Change(
                id = UUID.randomUUID().toString(),
                entityId = item.catalogId,
                operationType = OperationType.UpdateCell.ordinal,
                status = StatusType.Created.ordinal,
                supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
                other = null
            )
            var cell = dao.getCellById(item.catalogId)
            dao.updateCellAsync(cell.copy(parentCellId = cellTo.id), changes)
        }
    }
}