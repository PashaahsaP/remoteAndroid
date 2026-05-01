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