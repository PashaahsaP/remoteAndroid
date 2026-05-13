
package com.example.wmsRemote.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.AssemblyItem
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.PickerItem
import com.example.wmswherther.data.db.Entityes.SessionPicker
import com.example.wmswherther.data.db.Repositories.AssemblyRepository
import com.example.wmswherther.data.factory.CellFactory
import com.example.wmswherther.data.factory.ChangeFactory
import com.example.wmswherther.data.factory.MovementFactory
import com.example.wmswherther.viewModel.MainViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.junit.internal.Classes
import java.util.UUID

class AssemblySessionViewModel : ViewModel() {
    private val _count = MutableLiveData<Int>()
    val menuStatus: LiveData<Int> get() = _menuStatus
    val assemblyStatus: LiveData<Int> get() = _assemblyStatus
    val activeElement: LiveData<AssemblyItem> get() = _activeElement
    val items: LiveData<List<AssemblyItem>> get() = _items
    val resultCollection:LiveData<List<AssemblyItem>> get() = _resultCollection
    val count: LiveData<Int>get() = _count


    var _menuStatus = MutableLiveData<Int> ()
    var _assemblyStatus = MutableLiveData<Int> ()
    var _activeElement = MutableLiveData<AssemblyItem>()
    var _items = MutableLiveData<List<AssemblyItem>>()
    var _resultCollection = MutableLiveData<List<AssemblyItem>>(listOf())
    fun appendResultItem(item : AssemblyItem){
        _resultCollection.value = _resultCollection.value?.plus(item)
    }

    fun loadCollection(assemblyRepo: AssemblyRepository,
                       sessionId: String)
    {
        viewModelScope.launch {
            var data : MutableList<AssemblyItem> = mutableListOf()
            withContext(Dispatchers.IO) {
                data = getPickerItemsAndMappingToAssemblyItems(assemblyRepo, sessionId)
            }
            withContext(Dispatchers.Main){
                _items.value = data
                if (data.isNotEmpty()) {
                    _activeElement.value = data.first()
                }
            }
        }
    }


    fun finishSession(
        assemblyRepo: AssemblyRepository,
        outGate: String,
        sesssionId: String,
        viewModel: MainViewModel
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (isOutCellType(assemblyRepo, outGate)) {
                    var session = assemblyRepo.getPickerSessionById(sesssionId)
                    var outCell = getOutCell(assemblyRepo, outGate, session)
                    var items = assemblyRepo.getPickerItems().filter { it.sessionId == session.id }
                    moveItemsToOutGate(items, assemblyRepo, outCell, session)
                    changeSessionStatus(session, StatusType.Finished, assemblyRepo)
                }
                }
            withContext(Dispatchers.Main){
                viewModel.exitFromSession()//need for vm event
                viewModel.exitFromSession()
            }
        }
    }

    private suspend fun moveItemsToOutGate(
        items: List<PickerItem>,
        assemblyRepo: AssemblyRepository,
        outCell: Cell,
        session: SessionPicker
    ) {
        for (item in items) {
            if (!_resultCollection.value!!.any { it.goodsId == item.goodsId }) {
                prepareNotExistingItem(item, assemblyRepo, session)
            }
            if (_resultCollection.value!!.any { it.goodsId == item.goodsId && it.amount == assemblyRepo.getGoodsById(item.goodsId).amount }) {
                prepareOkItem(assemblyRepo, outCell, session)
            }
            if (_resultCollection.value!!.any { it.goodsId == item.goodsId && it.amount != assemblyRepo.getGoodsById(item.goodsId).amount }) {
                prepareDiffCountItem(item, _resultCollection.value!!.first { it.goodsId == item.goodsId }, assemblyRepo, outCell, session
                )
            }
        }
    }

    suspend fun prepareDiffCountItem(
        dbItem: PickerItem,
        resultItem: AssemblyItem,
        assemblyRepo: AssemblyRepository,
        outCell: Cell,
        session: SessionPicker
    ) {
        var cellFrom = assemblyRepo.getCellById(dbItem.cellId.toString())
        var cellLess = assemblyRepo.getCellByName("less")
        var cellMore = assemblyRepo.getCellByName("more")
        var goods = assemblyRepo.getGoodsById(dbItem.goodsId)
        if(resultItem.amount < goods.amount){
            // in less
            var movement = MovementFactory.create(
                cellFromId = cellFrom.id,
                cellToId = outCell.id,
                catalogId = assemblyRepo.getCatalogById(goods.catalogId).id,
                goodsId = goods.id,
                qty = resultItem.amount.toString(),
                operationType = OperationType.AssemblyMovement,
                entityId = session.id
            )
            val change = ChangeFactory.create(
                entityId = movement.id,
                supplierId = session.supplierId,
                operationType = OperationType.InsertMovement,
                payload = Gson().toJson(movement)
            )
            assemblyRepo.insertMovementSync(movement, change)

            var goodsChange = ChangeFactory.create(
                entityId = goods.id,
                supplierId = session.supplierId,
                operationType = OperationType.UpdateGoods,
                payload = Gson().toJson(goods.copy(cellId = outCell.id, amount = resultItem.amount))
            )
            assemblyRepo.updateGoodsAsync(goods.copy(cellId = outCell.id, amount = resultItem.amount), goodsChange)

            var lessMovement = MovementFactory.create(
                cellFromId = cellFrom.id,
                cellToId = cellLess.id,
                catalogId = assemblyRepo.getCatalogById(goods.catalogId).id,
                goodsId = goods.id,
                qty = (goods.amount -  resultItem.amount).toString(),
                operationType = OperationType.AssemblyMovement,
                entityId = session.id
            )
            val lessChange = ChangeFactory.create(
                entityId = movement.id,
                supplierId = session.supplierId,
                operationType = OperationType.InsertMovement,
                payload = Gson().toJson(lessMovement)
            )
            assemblyRepo.insertMovementSync(lessMovement, lessChange)

        }
        if(resultItem.amount > goods.amount){
            var movement = MovementFactory.create(
                cellFromId = cellFrom.id,
                cellToId = outCell.id,
                catalogId = assemblyRepo.getCatalogById(goods.catalogId).id,
                goodsId = goods.id,
                qty = resultItem.amount.toString(),
                operationType = OperationType.AssemblyMovement,
                entityId = session.id
            )
            val change = ChangeFactory.create(
                entityId = movement.id,
                supplierId = session.supplierId,
                operationType = OperationType.InsertMovement,
                payload = Gson().toJson(movement)
            )
            assemblyRepo.insertMovementSync(movement, change)

            var goodsChange = ChangeFactory.create(
                entityId = goods.id,
                supplierId = session.supplierId,
                operationType = OperationType.UpdateGoods,
                payload = Gson().toJson(goods.copy(cellId = outCell.id, amount = resultItem.amount))
            )
            assemblyRepo.updateGoodsAsync(goods.copy(cellId = outCell.id, amount = resultItem.amount), goodsChange)

            var moreMovement = MovementFactory.create(
                cellFromId = cellMore.id,
                cellToId = outCell.id,
                catalogId = assemblyRepo.getCatalogById(goods.catalogId).id,
                goodsId = goods.id,
                qty = (resultItem.amount - goods.amount ).toString(),
                operationType = OperationType.AssemblyMovement,
                entityId = session.id
            )
            val moreChange = ChangeFactory.create(
                entityId = movement.id,
                supplierId = session.supplierId,
                operationType = OperationType.InsertMovement,
                payload = Gson().toJson(moreMovement)
            )
            assemblyRepo.insertMovementSync(moreMovement, moreChange)
        }
        var movement = MovementFactory.create(
            cellFromId = cellFrom.id,
            cellToId = outCell.id,
            catalogId = assemblyRepo.getCatalogById(goods.catalogId).id,
            goodsId = goods.id,
            qty = goods.amount.toString(),
            operationType = OperationType.AssemblyMovement,
            entityId = session.id
        )
        val change = ChangeFactory.create(
            entityId = movement.id,
            supplierId = session.supplierId,
            operationType = OperationType.InsertMovement,
            payload = Gson().toJson(movement)
        )

        assemblyRepo.insertMovementSync(movement, change)
        assemblyRepo.deleteGoods(goods)
        //Переместить goods
    }

    suspend fun prepareNotExistingItem(
        dbItem: PickerItem,
        assemblyRepo: AssemblyRepository,
        session: SessionPicker){
        var cellFrom = assemblyRepo.getCellById(dbItem.cellId.toString())
        var cellTo = assemblyRepo.getCellByName("less")
        var goods = assemblyRepo.getGoodsById(dbItem.goodsId)
        var movement = MovementFactory.create(
            cellFromId = cellFrom.id,
            cellToId = cellTo.id,
            catalogId = assemblyRepo.getCatalogById(goods.catalogId).id,
            goodsId = goods.id,
            qty = goods.amount.toString(),
            operationType = OperationType.AssemblyMovement,
            entityId = session.id
        )
        val change = ChangeFactory.create(
            entityId = movement.id,
            supplierId = session.supplierId,
            operationType = OperationType.InsertMovement,
            payload = Gson().toJson(movement)
        )

        assemblyRepo.insertMovementSync(movement, change)
        assemblyRepo.deleteGoods(goods)
        //Переместить goods
    }
    suspend fun prepareOkItem(
        assemblyRepo: AssemblyRepository,
        outCell: Cell,
        session: SessionPicker){
        _resultCollection.value?.forEach { item ->
            //Надо создать перемещения для goods
            var cellFrom = assemblyRepo.getCellByName(item.cell)
            var movement = MovementFactory.create(
                cellFromId = cellFrom.id,
                cellToId = outCell.id,
                catalogId = item.catalogId,
                goodsId = item.goodsId,
                qty = item.amount.toString(),
                operationType = OperationType.AssemblyMovement,
                entityId = session.id
            )
            val change = ChangeFactory.create(
                entityId = movement.id,
                supplierId = session.supplierId,
                operationType = OperationType.InsertMovement,
                payload = Gson().toJson(movement)
            )

            assemblyRepo.insertMovementSync(movement, change)
            //Переместить goods
            var goods = assemblyRepo.getGoodsById(item.goodsId)
            var goodsChange = ChangeFactory.create(
                entityId = goods.id,
                supplierId = session.supplierId,
                operationType = OperationType.UpdateGoods,
                payload = Gson().toJson(goods.copy(cellId = outCell.id))
            )
            assemblyRepo.updateGoodsAsync(goods.copy(cellId = outCell.id), goodsChange)

        }
    }

    private suspend fun changeSessionStatus(session: SessionPicker, status: StatusType, assemblyRepo: AssemblyRepository) {
        var sessionChange = ChangeFactory.create(
            entityId = session.id,
            supplierId = session.supplierId,
            operationType = OperationType.UpdatePickerSession,
            payload = Gson().toJson(session)
        )
        assemblyRepo.updatePickerSessionSync(
            session.copy(status = status.ordinal),
            sessionChange
        )
    }
    private suspend fun getPickerItemsAndMappingToAssemblyItems(
        assemblyRepo: AssemblyRepository,
        sessionId: String
    ) : MutableList<AssemblyItem> {
        return assemblyRepo.getPickerItems()
            .filter { item -> item.sessionId == sessionId && item.status == StatusType.Created.ordinal }
            .map { item ->
                var goodsItem = assemblyRepo.getGoodsById(item.goodsId)
                var catalog = assemblyRepo.getCatalogById(goodsItem.catalogId)
                var cell = assemblyRepo.getCellById(item.cellId.toString())
                var barcodes = assemblyRepo.getBarcodes()
                    .filter { item -> item.catalogId == catalog.id }
                    .map { item -> item.name }
                var pickerList: MutableList<com.example.wmswherther.Classes.PickerItem> =
                    mutableListOf(com.example.wmswherther.Classes.PickerItem(catalog.name, barcodes, false))
                //set selection of last element
                pickerList += getPickerCell(assemblyRepo, cell)
                var lastElement = pickerList[pickerList.lastIndex]
                lastElement.isSelected = true
                pickerList[pickerList.lastIndex] = lastElement

                AssemblyItem(
                    sessionId = sessionId,
                    catalogId = goodsItem.catalogId,
                    assemblyItemId = item.id,
                    goodsId = item.goodsId,
                    amount = goodsItem.amount,
                    cell = cell.name,
                    name = catalog.name,
                    status = StatusType.Created.ordinal,
                    pickerList = pickerList.reversed()
                )
            }.toMutableList()
    }
    private suspend fun getOutCell(assemblyRepo: AssemblyRepository, outGate: String, session: SessionPicker): Cell {
        var outCell = assemblyRepo.getCellsByName(outGate).firstOrNull()
        if (outCell == null) {
            var cell = CellFactory.create(
                typeCellId = assemblyRepo.getCellTypes().filter { item -> item.type == "Outcome" }.firstOrNull()!!.id,
                parentCellId = null,
                name = outGate
            )
            val change = ChangeFactory.create(
                entityId = cell.id,
                supplierId = session.supplierId,
                operationType = OperationType.InsertMovement,
                payload = Gson().toJson(cell)
            )
            outCell = assemblyRepo.insertCellSync(cell, change)
        }
        return assemblyRepo.getCellById(outCell.id)
    }
    suspend fun isOutCellType(assemblyRepo: AssemblyRepository, outGate: String): Boolean {
        val types = assemblyRepo.getCellTypes().filter { cellType -> cellType.type == "Outcome" }
        return types.any {
            cellType -> val mask = cellType.mask ?: return@any false
            mask.length == outGate.length &&
                    mask.indices.all { i -> when (mask[i]) {
                        '*' -> outGate[i].isLetter()
                        '#' -> outGate[i].isDigit()
                        else -> mask[i] == outGate[i] }
                    }
        } }
    private suspend fun getPickerCell(assemblyRepo: AssemblyRepository, cell: Cell): List<com.example.wmswherther.Classes.PickerItem> {
        var result :List<com.example.wmswherther.Classes.PickerItem> = listOf()
        if(isPickerCell(cell.name,assemblyRepo)){
            result += com.example.wmswherther.Classes.PickerItem(cell.name, listOf(cell.name), false)
        }else{
            result += com.example.wmswherther.Classes.PickerItem(cell.name, listOf(cell.name), false)
            var innerCell = assemblyRepo.getCellById(cell.parentCellId.toString())
            result += getPickerCell(assemblyRepo,innerCell)

        }
        return result
    }
    }

suspend fun isPickerCell(cell: String, assemblyRepo: AssemblyRepository): Boolean {
    val cells = assemblyRepo.getCellTypes().filter { cellType -> cellType.type == "Picker" }

    return cells.any { cellType ->
        val mask = cellType.mask ?: return@any false

        mask.length == cell.length &&
                mask.indices.all { i ->
                    when (mask[i]) {
                        '*' -> cell[i].isLetter()
                        '#' -> cell[i].isDigit()
                        else -> mask[i] == cell[i]
                    }
                }
    }

}
