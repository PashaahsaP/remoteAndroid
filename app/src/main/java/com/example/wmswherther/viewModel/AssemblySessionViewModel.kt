
package com.example.wmsRemote.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.AssemblyItem
import com.example.wmswherther.Classes.PickerItem
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    var _resultCollection = MutableLiveData<List<AssemblyItem>>()

    fun loadCollection(db: MainDB, sessionId: String): Unit
    {
        var dao = db.getDao()
        viewModelScope.launch {
            var data : List<AssemblyItem> = listOf()
            withContext(Dispatchers.IO) {
                data = dao.getPickerItems()
                    .filter { item -> item.sessionId == sessionId && item.status == StatusType.Created.ordinal }
                    .map { item ->
                        var goodsItem = dao.getGoodsById(item.goodsId)
                        var catalog = dao.getCatalogById(goodsItem.catalogId)
                        var cell = dao.getCellById(item.cellId.toString())
                        var barcodes = dao.getBarcodes().filter { item -> item.catalogId == catalog.id }.map { item -> item.name }
                        var pickerList : MutableList<PickerItem> = mutableListOf(PickerItem(catalog.name, barcodes, false))
                        //set selection of last element
                        pickerList += getPickerCell(dao, cell)
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
                    }
            }
            withContext(Dispatchers.Main){
                _resultCollection.value = data
                _items.value = data
                if (data.isNotEmpty()) {
                    _activeElement.value = data.first()
                }
            }
        }
    }

    private suspend fun getPickerCell(dao: Dao, cell: Cell): List<PickerItem> {
        var result :List<PickerItem> = listOf()
        if(isPickerCell(cell.name,dao)){
            result += PickerItem(cell.name, listOf(cell.name), false)
        }else{
            result += PickerItem(cell.name, listOf(cell.name), false)
            var innerCell = dao.getCellById(cell.parentCellId.toString())
            result += getPickerCell(dao,innerCell)

        }
        return result
    }

    /* fun getItem(count: Int) : AssemblyItem{
         val data = _items.value?.get(count)
         if(data != null){
             return data
         }else{
             return  AssemblyItem(1,2,1,2,3,"","",1, listOf())
         }
     }*/
    fun changeMenuStatus(status: Int){
        _menuStatus.value = status
    }
    fun setActiveElement(element: AssemblyItem){
        _activeElement.value = element
    }
    /*fun loadItems(sessionId: Int, db: MainDB, supplierId: Int)
    {
        viewModelScope.launch {
            var items: List<AssemblyItem> = listOf()
            withContext(Dispatchers.IO){
                var func = AdapterHelper.getAssemblyItems[supplierId]
                items = func!!.invoke(sessionId, db)
            }
            withContext(Dispatchers.Main) {
                _count.value = 0
                setActiveElement(items[count.value ?: 0])
                _items.value = items
            }
        }
    }*/
    fun removeElementFromCollection(elem: AssemblyItem?){
        if(elem != null)
            _items.value =  _items.value?.minusElement(elem)
    }

    fun changeAssemblyStatus(enterCell: Int) {
        _assemblyStatus.value = enterCell
    }

    fun finishSession(dao: Dao, outGate: String, sesssionId: String, viewModel: MainViewModel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (isOutCellType(dao, outGate)) {
                    var outCell = dao.getCellsByName(outGate).firstOrNull()
                    if (outCell == null){
                        var cell = Cell(
                            id = UUID.randomUUID().toString(),
                            typeCellId = dao.getCellTypes().filter { item -> item.type == "Outcome" }.firstOrNull()!!.id,
                            parentCellId = null,
                            name = outGate
                        )
                        dao.insertCell(cell)
                        outCell = cell
                    }

                    _resultCollection.value?.forEach { item ->
                        //Надо создать перемещения для goods
                        var cellFrom = dao.getCellByName(item.cell)
                        var movement = Movement(
                            id = UUID.randomUUID().toString(),
                            cellFromId = cellFrom.id,
                            cellToId = outCell.id,
                            catalogId = item.catalogId,
                            goodsId = item.goodsId,
                            qty = item.amount.toString(),
                            userId = "0",
                            executedAt = dao.getPickerItemById(item.assemblyItemId).finishedAt!!.toLong(),
                            operationType = OperationType.InsertMovement.ordinal
                        )
                        var session = dao.getPickerSessionById(sesssionId)
                        val change = Change(
                            id = UUID.randomUUID().toString(),
                            entityId = movement.id,
                            operationType = OperationType.InsertMovement.ordinal,
                            status = StatusType.Created.ordinal,
                            supplierId = session.supplierId,
                            other = null
                        )
                        dao.insertMovementSync(movement, change)
                        //Переместить goods
                        var goods = dao.getGoodsById(item.goodsId)
                        var goodsChange = Change(
                            id = UUID.randomUUID().toString(),
                            entityId = goods.id,
                            operationType = OperationType.UpdateGoods.ordinal,
                            status = StatusType.Finished.ordinal,
                            supplierId = session.supplierId,
                            other = null
                        )
                        dao.updateGoodsAsync(goods.copy(cellId = outCell.id ), goodsChange)

                    }
                    //Изменить статус сессии
                    var session = dao.getPickerSessionById(sesssionId)
                    var sessionChange = Change(
                        id = UUID.randomUUID().toString(),
                        entityId = session.id,
                        operationType = OperationType.UpdatePickerSession.ordinal,
                        status = StatusType.Finished.ordinal,
                        supplierId = session.supplierId,
                        other = null
                    )
                    dao.updatePickerSessionSync(session.copy(status = StatusType.Finished.ordinal.toString()), sessionChange)
                }
            }
            withContext(Dispatchers.Main){
                viewModel.exitFromSession()
                viewModel.exitFromSession()
            }
        }
    }

    suspend fun isOutCellType(dao: Dao, outGate: String): Boolean {
        val types = dao.getCellTypes().filter { cellType -> cellType.type == "Outcome" }
        return types.any {
            cellType -> val mask = cellType.mask ?: return@any false
            mask.length == outGate.length &&
                    mask.indices.all { i -> when (mask[i]) {
                        '*' -> outGate[i].isLetter()
                        '#' -> outGate[i].isDigit()
                        else -> mask[i] == outGate[i] }
                    }
        } }
    }

suspend fun isPickerCell(cell: String, dao: Dao): Boolean {
    val cells = dao.getCellTypes().filter { cellType -> cellType.type == "Picker" }

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