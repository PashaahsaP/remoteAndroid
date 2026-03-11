package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.InventorySessionItem
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.InventoryDiffItem
import com.example.wmswherther.data.db.Entityes.SessionInventory
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
    suspend fun loadItems (db: MainDB, cell: Cell) : List<InventorySessionItem>{
        // Загрузить goods.
        var dao = db.getDao()
        var listOfGoods: List<Pair<Goods, Cell>> = getAllGoods(dao, cell)
        // Создать inventory item.
        var result : List<InventorySessionItem> = listOf()
        // для Cell
        if(isTE(cell.name, dao)) {
            var parent = dao.getCellById(cell.parentCellId.toString())
            result += InventorySessionItem(
                name = cell.name,
                TE = if (isTE(cell.name, dao)) cell.name else "",
                catalogId = "",
                allCount = 1,
                haveCount = 0,
                isExpandable = true,
                isShown = if (isPickerCell(parent.name, dao)) true else false
            )
        }

        // для goods
        listOfGoods.forEach { goods ->
            var catalog = dao.getCatalogById(goods.first.catalogId)
            result += InventorySessionItem(
                name =  catalog.name,
                TE = cell.name,
                catalogId = catalog.id,
                allCount = goods.first.amount,
                haveCount = 0,
                isExpandable = false,
                isShown = if(isTE(cell.name, dao)) false else true)
        }

        // Загрузить cells
        var listOfCells = dao.getAllCells().filter { innerCell: Cell -> innerCell.parentCellId == cell.id }
        if(listOfCells.count() == 0){
            return  result
        }else{
            listOfCells.forEach { innerCell ->
                var innerResult = loadItems(db, innerCell)
                result += innerResult
            }
            return result
        }
        // Базовый случай. Если нет ячеек больше то вернуть коллекцию
        // Иначе Загрузить goods  и соединить коллекции


    }
    suspend fun finishSession(dao: Dao, isSupplierModeActive: Boolean, supplierId: String?){
        var baseCell = dao.getCellByName(currentCellName.value.toString())
        if(isSupplierModeActive){
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
            var diffs : List<InventoryDiffItem> = getDiffs(baseCell, items.value ?: listOf(), dao, session.id)
        }
        else{
            // Загрузить все элементы которые есть в ячейки и сверить с тем что есть на текущий момент

            // Если элемента нет в базовой коллекция(что в ячейке лежит) то создать diff  items  и указать ссылку на сессию.
            // Также добавить статус для сессии changed
            // Иначе correct
        }

    }

    suspend fun getDiffs(baseCell: Cell, data: List<InventorySessionItem>, dao: Dao, sessionId: String): List<InventoryDiffItem> {
        // Проверить товар в текущей ячейке
        var goods = dao.getGoodsByCellId(baseCell.id)
        var diffs : List<InventoryDiffItem> = listOf()
        goods.forEach { innerGoods ->
            var isCorrect = false
            for (item in data){
                if(item.catalogId == innerGoods.catalogId && item.TE == baseCell.name){
                    isCorrect = true
                    if(item.haveCount != innerGoods.amount){
                        var diff = InventoryDiffItem(
                            id = UUID.randomUUID().toString(),
                            inventorySessionId = sessionId,
                            catalogId = item.catalogId,
                            parentCellId = baseCell.id,
                            diffCount = innerGoods.amount - item.haveCount,
                            status = StatusType.Created.ordinal,
                            other = null
                        )
                        diffs += diff
                    }
                }
            }
            if(isCorrect == false){
                var diff = InventoryDiffItem(
                    id = UUID.randomUUID().toString(),
                    inventorySessionId = sessionId,
                    catalogId = innerGoods.catalogId,
                    parentCellId = baseCell.id,
                    diffCount = 0 - innerGoods.amount,
                    status = StatusType.Created.ordinal,
                    other = null
                )
                diffs += diff
            }
        }
        // Получить все вложенные те
        var cells = dao.getChildrenCells(baseCell.id)
        if(cells.isEmpty()){
            // Если пустая ячейка то вернуть диффы
            return diffs
        }else{
            cells.forEach { innerCell ->
                return getDiffs(innerCell, data, dao, sessionId)
            }
        }

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
        dao: Dao,
        cell: Cell
    ): List<Pair<Goods, Cell>> {
        var listOfGoods: List<Pair<Goods, Cell>> = listOf()
        listOfGoods = dao.getGoodsByCellId(cell.id)
            .map { goods -> Pair(goods, cell) }

        return listOfGoods
    }
// </editor-fold>
    suspend fun getBarcode(db : MainDB, barcode: String) : Barcode {
        return db.getDao().getBarcodeByName(barcode)
    }
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
suspend private fun isTE(cell: String, dao: Dao): Boolean {
    val cells = dao.getCellTypes().filter { cellType -> cellType.type == "BoxTE" }

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
suspend private fun isPickerCell(cell: String, dao: Dao): Boolean {
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