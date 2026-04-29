package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.InventorySessionItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.InventoryDiffItem
import com.example.wmswherther.data.db.Entityes.SessionInventory
import com.example.wmswherther.data.db.Repositories.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class InventorySessionViewModel : ViewModel(){
    private val _items = MutableLiveData<List<InventorySessionItem>>()
    private val _selectedItem = MutableLiveData<Int>()
    private  val _currentCellName = MutableLiveData<String>()


    private  val _currentCountOfCount = MutableLiveData<Int>()
    private  val _countOfCount = MutableLiveData<Int>()
    private  val _isOverCounter = MutableLiveData<Boolean>()
    private  val _finish = MutableLiveData<Boolean>()

    val items: LiveData<List<InventorySessionItem>> get() = _items
    val currentCellName: LiveData<String> get() = _currentCellName
    val CurrentCountOfCount: LiveData<Int> get() = _currentCountOfCount
    val CountOfCount: LiveData<Int> get() = _countOfCount
    val IsOverCounter: LiveData<Boolean> get() = _isOverCounter
    val IsFinish: LiveData<Boolean> get() = _finish

    val stack: ArrayDeque<List<InventorySessionItem>> = ArrayDeque()
    val cellStack: ArrayDeque<String> = ArrayDeque()

    fun setCounterValidation(isOverFlag: Boolean){
        _isOverCounter.value = isOverFlag
    }
    fun setFinishValidation(isOverFlag: Boolean){
        _finish.value = isOverFlag
    }
    fun setCurCountOfCount(count: Int){
        _currentCountOfCount.value = count
    }
    fun setCountOfCount(count: Int){
        _countOfCount.value = count
    }
    suspend fun updateItemsAsync(items: List<InventorySessionItem>){
        viewModelScope.launch {
            withContext(Dispatchers.Main){
                updateItems(items)
            }
        }
    }
    fun updateItems(items: List<InventorySessionItem>){
        var sortedCollection : MutableList<InventorySessionItem> = mutableListOf()
        var teCollection : MutableList<InventorySessionItem> = mutableListOf()
        var otherCollection : MutableList<InventorySessionItem> = mutableListOf()
        var counter = 0
        var curCounter = 0
        var isOver = false
        // get collections
        items.forEach { item ->
            if(item.isExpandable){
                teCollection.add(item)
            }else if(!item.isShown){
                teCollection.add(item)
                counter += item.allCount
                if(item.haveCount > item.allCount){
                    isOver = true
                }
                curCounter += item.haveCount
            }else{
                otherCollection.add(item)
                if(item.haveCount > item.allCount){
                    isOver = true
                }
                counter += item.allCount
                curCounter += item.haveCount
            }

        }
        // insert collections in certain order
        teCollection.forEach { item ->
            sortedCollection.add(item)
        }
        otherCollection.forEach { item ->
            sortedCollection.add(item)
        }
        //set properties
        _items.value = sortedCollection
        setCountOfCount(counter)
        setCurCountOfCount(curCounter)
        setCounterValidation(isOver)
        //set is over session btn
        if(IsOverCounter.value == false && CurrentCountOfCount.value == CountOfCount.value){
            setFinishValidation(true)
        }else{
            setFinishValidation(false)
        }
    }
    suspend fun loadItems (inventoryRepo: InventoryRepository, cell: Cell) : List<InventorySessionItem>{
        // Загрузить goods.
        var listOfGoods: List<Pair<Goods, Cell>> = getAllGoods(inventoryRepo, cell)
        // Создать inventory item.
        var result : List<InventorySessionItem> = listOf()
        // для Cell
        if(isTE(cell.name, inventoryRepo)) {
            var parent = inventoryRepo.getCellById(cell.parentCellId.toString())
            result += InventorySessionItem(
                name = cell.name,
                TE = if (isTE(cell.name, inventoryRepo)) cell.name else "",
                catalogId = "",
                allCount = 1,
                haveCount = 0,
                isExpandable = true,
                isShown = if (isPickerCell(parent.name, inventoryRepo)) true else false
            )
        }

        // для goods
        listOfGoods.forEach { goods ->
            var catalog = inventoryRepo.getCatalogById(goods.first.catalogId)
            result += InventorySessionItem(
                name =  catalog.name,
                TE = cell.name,
                catalogId = catalog.id,
                allCount = goods.first.amount,
                haveCount = 0,
                isExpandable = false,
                isShown = if(isTE(cell.name, inventoryRepo)) false else true)
        }

        // Загрузить cells
        var listOfCells = inventoryRepo.getAllCells().filter { innerCell: Cell -> innerCell.parentCellId == cell.id }
        if(listOfCells.count() == 0){
            return  result
        }else{
            listOfCells.forEach { innerCell ->
                var innerResult = loadItems(inventoryRepo, innerCell)
                result += innerResult
            }
            return result
        }
        // Базовый случай. Если нет ячеек больше то вернуть коллекцию
        // Иначе Загрузить goods  и соединить коллекции


    }
    suspend fun finishSession(
        inventoryRepo: InventoryRepository,
        state: UiState.InventorySessionMenu,
        supplierId: String?
    ){
        var baseCell = inventoryRepo.getCellByName(currentCellName.value.toString())
        if(state.isSupplierModeActive){
            // создание сессии
            var session = SessionInventory(
                id = UUID.randomUUID().toString(),
                supplierId = supplierId,
                cellId = baseCell.id,
                prevSessionId = "",
                status = StatusType.Created.ordinal,
                createdAt = System.currentTimeMillis(),
                startedAt = System.currentTimeMillis(),
                finishedAt = System.currentTimeMillis(),
                other = null
            )
            // Получение данных
            var diffs : List<InventoryDiffItem> = getDiffs(baseCell, items.value ?: listOf(), inventoryRepo, session.id)
            // Загрузить данные в бд
            var changes = Change(
                id = UUID.randomUUID().toString(),
                entityId = session.id,
                operationType = OperationType.InsertInventorySession.ordinal,
                status = StatusType.Created.ordinal,
                supplierId = supplierId,
                other = null
            )
            inventoryRepo.insertInventorySessionAsync(session, changes)
            diffs.forEach { item ->
                changes = Change(
                    id = UUID.randomUUID().toString(),
                    entityId = item.id,
                    operationType = OperationType.InsertInventoryDiff.ordinal,
                    status = StatusType.Created.ordinal,
                    supplierId = supplierId,
                    other = null
                )
                inventoryRepo.insertInventoryDiffItemAsync(item, changes)
            }
        }
        else{
            var sessionId = state.sessionId
            var diffs : List<InventoryDiffItem> = getDiffs(baseCell, items.value ?: listOf(), inventoryRepo, sessionId)
            println(diffs)
            // Загрузить данные в бд
            diffs.forEach { item ->
                var changes = Change(
                    id = UUID.randomUUID().toString(),
                    entityId = item.id,
                    operationType = OperationType.InsertInventoryDiff.ordinal,
                    status = StatusType.Created.ordinal,
                    supplierId = supplierId,
                    other = null
                )
                inventoryRepo.insertInventoryDiffItemAsync(item, changes)
            }

        }

    }
//что если вообще нет такого шк, как будет сохранятся
    suspend fun getDiffs(baseCell: Cell, data: List<InventorySessionItem>, inventoryRepo: InventoryRepository, sessionId: String): List<InventoryDiffItem> {
        // Проверить все те и добавить те которых нет в бд
        // Проверить товар в текущей ячейке
        var goods = inventoryRepo.getGoodsByCellId(baseCell.id)
        var diffs : List<InventoryDiffItem> = listOf()
        var isCorrect = false
        data.filter {inner -> inner.TE == baseCell.name}.forEach { innerGoods ->
            isCorrect = false
            for (item in goods){
                if(item.catalogId == innerGoods.catalogId && innerGoods.TE == baseCell.name){
                    isCorrect = true
                    if(item.amount != innerGoods.haveCount){
                        var diff = InventoryDiffItem(
                            id = UUID.randomUUID().toString(),
                            inventorySessionId = sessionId,
                            catalogId = item.catalogId,
                            parentCellId = baseCell.id,
                            diffCount = item.amount - innerGoods.haveCount,
                            status = StatusType.Created.ordinal,
                            isTE = false,
                            barcode = innerGoods.name,
                            other = null
                        )
                        diffs += diff
                    }
                }
            }
            //Обработка элемента которого нет изначально
            //если это те проверить есть ли такая те у ячейки, если нет то добавить диф
            if(isTE(innerGoods.name, inventoryRepo)){
                var checkCell = inventoryRepo.getCellByName(innerGoods.name)
                if(isCorrect == false  && checkCell == null){
                    var diff = InventoryDiffItem(
                        id = UUID.randomUUID().toString(),
                        inventorySessionId = sessionId,
                        catalogId = innerGoods.catalogId,
                        parentCellId = baseCell.id,
                        diffCount = 0 - innerGoods.haveCount,
                        status = StatusType.Created.ordinal,
                        isTE = true,
                        barcode = innerGoods.name,
                        other = null
                    )
                    diffs += diff
                }
            }else{
                if(isCorrect == false ){
                    var diff = InventoryDiffItem(
                        id = UUID.randomUUID().toString(),
                        inventorySessionId = sessionId,
                        catalogId = innerGoods.catalogId,
                        parentCellId = baseCell.id,
                        diffCount = 0 - innerGoods.haveCount,
                        status = StatusType.Created.ordinal,
                        isTE = false,
                        barcode = innerGoods.name,
                        other = null
                    )
                    diffs += diff
                }
            }
        }
        // Получить все вложенные те
        var cells = inventoryRepo.getChildrenCells(baseCell.id)
        if(cells.isEmpty()){
            // Если пустая ячейка то вернуть диффы
            return diffs
        }else{
            cells.forEach { innerCell ->
                diffs += getDiffs(innerCell, data, inventoryRepo, sessionId)
                return diffs
            }
        }
        return diffs
    }

    fun getSelectedItem() : Int{
        var isCorrect = _selectedItem.value
        if (isCorrect != null)
            return isCorrect
        else
            return  0
    }
    fun setSelectedItem(selectedItemCount: Int){
        _selectedItem.value = selectedItemCount
    }
    fun setCellName(cellName: String){
        _currentCellName.value = cellName
    }

// <editor-fold desc="Helper function">
    private suspend fun getAllGoods(
        inventoryRepo: InventoryRepository,
        cell: Cell
    ): List<Pair<Goods, Cell>> {
        var listOfGoods: List<Pair<Goods, Cell>> = listOf()
        listOfGoods = inventoryRepo.getGoodsByCellId(cell.id)
            .map { goods -> Pair(goods, cell) }

        return listOfGoods
    }
// </editor-fold>

    fun setSelection(checked: Boolean) {
        var list: MutableList<InventorySessionItem> = mutableListOf()
        if (checked){
            items.value?.forEach { item -> list.add(item.copy(haveCount = if(item.allCount != 0) item.allCount else item.haveCount ))}
            updateItems(list)
            //setCurCountOfCount(_countOfCount.value ?: 0)
        }else{
            items.value?.forEach { item -> list.add(item.copy(haveCount = 0))}
            updateItems(list)
            //setCurCountOfCount(0)
        }
    }

}
suspend private fun isTE(cell: String, inventoryRepo: InventoryRepository): Boolean {
    val cells = inventoryRepo.getCellTypes().filter { cellType -> cellType.type == "BoxTE" }

    return cells.any { cellType ->
        val mask = cellType.mask ?: return@any false

        mask.length == cell.length &&
                mask.indices.all { i ->
                    when (mask[i]) {
                        '#' -> cell[i].isDigit()
                        else -> mask[i] == cell[i]
                    }
                }
    }
}
suspend private fun isPickerCell(cell: String, inventoryRepo: InventoryRepository): Boolean {
    val cells = inventoryRepo.getCellTypes().filter { cellType -> cellType.type == "Picker" }

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