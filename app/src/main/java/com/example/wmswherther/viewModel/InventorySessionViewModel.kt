package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmswherther.Classes.InventorySessionItem
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.SessionInventory

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
    fun updateItems(items: List<InventorySessionItem>){
        var sortedCollection : MutableList<InventorySessionItem> = mutableListOf()
        var teCollection : MutableList<InventorySessionItem> = mutableListOf()
        var otherCollection : MutableList<InventorySessionItem> = mutableListOf()
        var counter = 0
        var curCounter = 0
        var isOver = false
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
        teCollection.forEach { item ->
            sortedCollection.add(item)
        }
        otherCollection.forEach { item ->
            sortedCollection.add(item)
        }
        _items.value = sortedCollection
        setCountOfCount(counter)
        setCurCountOfCount(curCounter)
        setCounterValidation(isOver)
        if(IsOverCounter.value == false && CurrentCountOfCount.value == CountOfCount.value){
            setFinishValidation(true)
        }else{
            setFinishValidation(false)
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
    suspend fun loadItems (db: MainDB, cell: Cell) : List<InventorySessionItem>{
        // Загрузить goods.
        var dao = db.getDao()
        var listOfGoods: List<Pair<Goods, Cell>> = getAllGoods(dao, cell)
        // Создать inventory item.
        var result : List<InventorySessionItem> = listOf()
        // для Cell
        if(cell.typeCellId == "1078e222-0d70-47f7-a295-5827ea9ad1f5") {
            result += InventorySessionItem(
                name = cell.name,
                TE = if (cell.typeCellId == "e873f579-44fc-48e1-84d2-f529b77653ee") cell.name else "",
                catalogId = "",
                allCount = 1,
                haveCount = 0,
                isExpandable = true,
                isShown = if (cell.typeCellId == "e873f579-44fc-48e1-84d2-f529b77653ee") false else true
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
                isShown = if(cell.typeCellId == "e873f579-44fc-48e1-84d2-f529b77653ee") false else true)
        }

        // Загрузить cells
        var listOfCells = dao.getAllCells().filter { cell: Cell -> cell.parentCellId == cell.id }
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
}