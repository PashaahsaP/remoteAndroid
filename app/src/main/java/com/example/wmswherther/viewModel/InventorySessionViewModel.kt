package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmswherther.Classes.InventorySessionItem
import com.example.wmswherther.Classes.MoveSessionItem
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.SessionInventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    suspend fun finishSession(dao: Dao){

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