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
import com.example.wmswherther.data.factory.ChangeFactory
import com.example.wmswherther.data.factory.InventoryDiffFactory
import com.example.wmswherther.data.factory.InventorySessionFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class SortCollectionResult(val counter: Int,
                                val curCount: Int,
                                val isOver: Boolean)

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
        //init
        var sortedCollection : MutableList<InventorySessionItem> = mutableListOf()
        var teCollection : MutableList<InventorySessionItem> = mutableListOf()
        var otherCollection : MutableList<InventorySessionItem> = mutableListOf()

        //sort
        val result = sortCollection(
            items,
            teCollection,
            otherCollection,
            sortedCollection
        )
        _items.value = sortedCollection

        //update properties
        setCountOfCount(result.counter)
        setCurCountOfCount(result.curCount)
        setCounterValidation(result.isOver)
        //set is over session btn
        if(IsOverCounter.value == false && CurrentCountOfCount.value == CountOfCount.value){
            setFinishValidation(true)
        }else{
            setFinishValidation(false)
        }
    }
    suspend fun loadItems (inventoryRepo: InventoryRepository, cell: Cell) : List<InventorySessionItem>{
        var listOfGoods: List<Pair<Goods, Cell>> = getAllGoods(inventoryRepo, cell)
        var result : MutableList<InventorySessionItem> = mutableListOf()

        loadTE(cell, inventoryRepo, result)
        loadGoods(listOfGoods, inventoryRepo, result, cell)
        loadInnerCells(inventoryRepo, cell, result)

        return result
    }



    suspend fun finishSession(
        inventoryRepo: InventoryRepository,
        state: UiState.InventorySessionMenu,
        supplierId: String?
    ){
        var baseCell = inventoryRepo.getCellByName(currentCellName.value.toString())
        if(state.isSupplierModeActive){
            var session = createNewSessionAndSaveInDB(
                supplierId = supplierId,
                baseCell = baseCell,
                inventoryRepo = inventoryRepo,
                status = StatusType.Finished
            )
            createDiffsAndAppendToDb(
                baseCell = baseCell,
                inventoryRepo = inventoryRepo,
                sessionId = session.id,
                supplierId = supplierId
            )
        }
        else{
            createDiffsAndAppendToDb(
                baseCell = baseCell,
                inventoryRepo = inventoryRepo,
                sessionId = state.sessionId,
                supplierId = state.supplierId
            )
        }

    }

    private suspend fun createDiffsAndAppendToDb(
        baseCell: Cell,
        inventoryRepo: InventoryRepository,
        sessionId: String,
        supplierId: String?
    ) {
        // get diffs
        var diffs: List<InventoryDiffItem> =
            getDiffs(baseCell, items.value ?: listOf(), inventoryRepo, sessionId)
        // create changes and save in bd
        diffs.forEach { item ->
            var changes = ChangeFactory.create(
                entityId = item.id,
                supplierId = supplierId.toString(),
                operationType = OperationType.InsertInventoryDiff
            )
            inventoryRepo.insertInventoryDiffItemAsync(item, changes)
        }
    }

    private suspend fun createNewSessionAndSaveInDB(
        supplierId: String?,
        baseCell: Cell,
        inventoryRepo: InventoryRepository,
        status: StatusType
    ): SessionInventory {
        // создание сессии
        var session = InventorySessionFactory.createNotInventoryTask(
            supplierId = supplierId.toString(),
            cellId = baseCell.id,
            status = StatusType.Created
        )
        // Получение данных
        var changes = ChangeFactory.create(
            entityId = session.id,
            supplierId = supplierId.toString(),
            operationType = OperationType.InsertInventorySession
        )

        inventoryRepo.insertInventorySessionAsync(session, changes)
        return session
    }

    /**
     * RecursiveFunc
     */
    suspend fun getDiffs(baseCell: Cell, data: List<InventorySessionItem>, inventoryRepo: InventoryRepository, sessionId: String): List<InventoryDiffItem> {
        // main work
        var goods = inventoryRepo.getGoodsByCellId(baseCell.id)
        var diffs : MutableList<InventoryDiffItem> = mutableListOf()
        var isCorrect = false
        data.filter {inner -> inner.TE == baseCell.name}
            .forEach { innerGoods ->

                isCorrect = false
                isCorrect = prepareBaseCase(goods, innerGoods, baseCell, sessionId, diffs)

                if(isTE(innerGoods.name, inventoryRepo)){
                    prepareTeCase(inventoryRepo, innerGoods, isCorrect, sessionId, baseCell, diffs)
                }else{
                    prepareNewItemCase(isCorrect, sessionId, innerGoods, baseCell, diffs)
                }
        }

        // Stop condition
        var cells = inventoryRepo.getChildrenCells(baseCell.id)
        if(cells.isEmpty()){
            return diffs
        }else{
            cells.forEach { innerCell ->
                diffs += getDiffs(innerCell, data, inventoryRepo, sessionId)
                return diffs
            }
        }
        return diffs
    }

    private fun prepareNewItemCase(
        isCorrect: Boolean,
        sessionId: String,
        innerGoods: InventorySessionItem,
        baseCell: Cell,
        diffs: MutableList<InventoryDiffItem>
    ) {
        //Обработка элемента которого нет изначально
        if (isCorrect == false) {
            var diff = InventoryDiffFactory.create(
                sessionId = sessionId,
                catalogId = innerGoods.catalogId,
                parentCellId = baseCell.id,
                diffCount = 0 - innerGoods.haveCount,
                status = StatusType.Created,
                isTe = false,
                barcoder = innerGoods.name
            )
            diffs += diff
        }
    }

    private suspend fun prepareTeCase(
        inventoryRepo: InventoryRepository,
        innerGoods: InventorySessionItem,
        isCorrect: Boolean,
        sessionId: String,
        baseCell: Cell,
        diffs: MutableList<InventoryDiffItem>
    ) {
        //если это те проверить есть ли такая те у ячейки, если нет то добавить диф
        var checkCell = inventoryRepo.getCellByName(innerGoods.name)
        if (isCorrect == false && checkCell == null) {
            var diff = InventoryDiffFactory.create(
                sessionId = sessionId,
                catalogId = innerGoods.catalogId,
                parentCellId = baseCell.id,
                diffCount = 0 - innerGoods.haveCount,
                status = StatusType.Created,
                isTe = true,
                barcoder = innerGoods.name
            )
            diffs += diff
        }
    }

    /**
     * Case when item is Goods and he's in db, not new
     */
    private fun prepareBaseCase(
        goods: List<Goods>,
        innerGoods: InventorySessionItem,
        baseCell: Cell,
        sessionId: String,
        diffs: MutableList<InventoryDiffItem>
    ): Boolean {
        var isCorrect = false
        for (item in goods) {
            if (item.catalogId == innerGoods.catalogId && innerGoods.TE == baseCell.name) {
                isCorrect = true
                if (item.amount != innerGoods.haveCount) {
                    var diff = InventoryDiffFactory.create(
                        sessionId = sessionId,
                        catalogId = item.catalogId,
                        parentCellId = baseCell.id,
                        diffCount = item.amount - innerGoods.haveCount,
                        status = StatusType.Created,
                        isTe = false,
                        barcoder = innerGoods.name
                    )
                    diffs += diff
                }
            }
        }
        return isCorrect
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
private suspend fun loadInnerCells(
    inventoryRepo: InventoryRepository,
    cell: Cell,
    result: MutableList<InventorySessionItem>
) {
    var listOfCells = inventoryRepo.getAllCells()
        .filter { innerCell: Cell -> innerCell.parentCellId == cell.id }
    if (listOfCells.count() != 0) {
        listOfCells.forEach { innerCell ->
            var innerResult = loadItems(inventoryRepo, innerCell)
            result += innerResult
        }
    }
}

    private suspend fun loadGoods(
        listOfGoods: List<Pair<Goods, Cell>>,
        inventoryRepo: InventoryRepository,
        result: MutableList<InventorySessionItem>,
        cell: Cell
    ) {
        listOfGoods.forEach { goods ->
            var catalog = inventoryRepo.getCatalogById(goods.first.catalogId)
            result += InventorySessionItem(
                name = catalog.name,
                TE = cell.name,
                catalogId = catalog.id,
                allCount = goods.first.amount,
                haveCount = 0,
                isExpandable = false,
                isShown = if (isTE(cell.name, inventoryRepo)) false else true
            )
        }
    }

    private suspend fun loadTE(
        cell: Cell,
        inventoryRepo: InventoryRepository,
        result: MutableList<InventorySessionItem>
    ) {
        if (isTE(cell.name, inventoryRepo)) {
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
    }
    private fun sortCollection(
        items: List<InventorySessionItem>,
        teCollection: MutableList<InventorySessionItem>,
        otherCollection: MutableList<InventorySessionItem>,
        sortedCollection: MutableList<InventorySessionItem>
    ): SortCollectionResult {
    // get collections
    var counter = 0
    var curCounter = 0
    var isOver = false
    items.forEach { item ->
        if (item.isExpandable) {
            teCollection.add(item)
        } else if (!item.isShown) {
            teCollection.add(item)
            counter += item.allCount
            if (item.haveCount > item.allCount) {
                isOver = true
            }
            curCounter += item.haveCount
        } else {
            otherCollection.add(item)
            if (item.haveCount > item.allCount) {
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
    return SortCollectionResult(counter, curCounter, isOver)
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