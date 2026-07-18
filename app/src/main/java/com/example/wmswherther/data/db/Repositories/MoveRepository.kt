package com.example.wmswherther.data.db.Repositories

import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.MoveSessionItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.factory.CellFactory
import com.example.wmswherther.data.factory.ChangeFactory
import com.example.wmswherther.viewModel.MainViewModel
import com.google.gson.Gson
import java.util.UUID

class MoveRepository(
    private val dao: Dao
) {


    /**
     * Обновление или удаление [Goods], если количество после перемещение 0.
     */



    suspend fun moveGoodsToCell(
        item: MoveSessionItem,
        allGoods: List<Goods>,
        cellTo: Cell,
        viewModel: MainViewModel
    ) {



    }
    /**
     * @param entityId Получение id сущности над которым будет происходить какое то действие
     * @param viewModel Нужна для получения id поставщика
     * @param operationType Для понимания типа операции, когда будет выполнятся синхронизация удаленной и локальных баз
     */

    suspend fun getCell(barcode: String,
                        viewModel: MainViewModel,
                        sourceCellName: String): Cell {
        var cell = dao.getCellByName(barcode)
        if (cell == null) {
            var curCell = dao.getCellByName(sourceCellName) // откуда идет перемещение
            var newCell = CellFactory.create(
                typeCellId = curCell.typeCellId,
                parentCellId = curCell.parentCellId,
                name = barcode
            )
            var changes = ChangeFactory.create(
                payload = Gson().toJson(newCell),
                payloadBefore = Gson().toJson(newCell),
                entityId = newCell.id,
                supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
                operationType = OperationType.InsertCell
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
            var cell = dao.getCellById(item.catalogId)

            var changes = ChangeFactory.create(
                payload = Gson().toJson(cell.copy(parentCellId = cellTo.id)),
                payloadBefore = Gson().toJson(cell),
                entityId = cell.id,
                supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
                operationType = OperationType.UpdateCell
            )

            dao.updateCellAsync(cell.copy(parentCellId = cellTo.id), changes)
        }
    }
}