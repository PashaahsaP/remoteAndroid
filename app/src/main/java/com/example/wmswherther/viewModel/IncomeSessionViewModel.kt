package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.db.Cell
import com.example.wmsRemote.data.db.MainDB
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.data.db.Barcode
import com.example.wmswherther.data.db.Goods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IncomeSessionViewModel : ViewModel() {
    private val _items = MutableLiveData<List<IncomeItem>>()
    private val _selectedItem = MutableLiveData<Int>()
    private  val _currentCellName = MutableLiveData<String>()


    private  val _currentLineCount = MutableLiveData<Int>()
    private  val _lineCount = MutableLiveData<Int>()
    private  val _currentCountOfCount = MutableLiveData<Int>()
    private  val _countOfCount = MutableLiveData<Int>()
    private  val _isOverCounter = MutableLiveData<Boolean>()
    private  val _finish = MutableLiveData<Boolean>()

    val items: LiveData<List<IncomeItem>> get() = _items
    val selectedItem: LiveData<Int> get() = _selectedItem
    val currentCellName: LiveData<String> get() = _currentCellName
    val CurrentLineCount: LiveData<Int> get() = _currentLineCount
    val LineCount: LiveData<Int> get() = _lineCount
    val CurrentCountOfCount: LiveData<Int> get() = _currentCountOfCount
    val CountOfCount: LiveData<Int> get() = _countOfCount
    val IsOverCounter: LiveData<Boolean> get() = _isOverCounter
    val IsFinish: LiveData<Boolean> get() = _finish

    val stack: ArrayDeque<List<IncomeItem>> = ArrayDeque()
    val cellStack: ArrayDeque<String> = ArrayDeque()

    fun setCounterValidation(isOverFlag: Boolean){
        _isOverCounter.value = isOverFlag
    }
    fun getCounterValidation() : Boolean?{
        return _isOverCounter.value
    }
    fun setFinishValidation(isOverFlag: Boolean){
        _finish.value = isOverFlag
    }
    fun getFinishValidation() : Boolean?{
        return _finish.value
    }
    fun setCurLineCount(count: Int){
        _currentLineCount.value = count
    }
    fun getCurLineCount() : Int{
        return CurrentLineCount.value!!.toInt()
    }
    fun setLineCount(count: Int){
        _lineCount.value = count
    }
    fun getLineCount() : Int{
        return LineCount.value!!.toInt()
    }
    fun setCurCountOfCount(count: Int){
        _currentCountOfCount.value = count
    }
    fun getCurCountOfCount() : Int{
        return CurrentCountOfCount.value!!.toInt()
    }
    fun setCountOfCount(count: Int){
        _countOfCount.value = count
    }
    fun getCountOfCount() : Int{
        return _countOfCount.value!!.toInt()
    }
    fun updateItems(items: List<IncomeItem>){
        var sortedCollection : MutableList<IncomeItem> = mutableListOf()
        var teCollection : MutableList<IncomeItem> = mutableListOf()
        var otherCollection : MutableList<IncomeItem> = mutableListOf()
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
    fun updateCollection(db : MainDB, barcode: String?){
        var barcoded = Barcode("","","","","")
        viewModelScope.launch {
            var result : List<IncomeItem> = mutableListOf()
            withContext(Dispatchers.IO) {
                barcoded = getBarcode(db, if (barcode == null) "" else barcode)

            }
            withContext(Dispatchers.Main){
                if(barcoded != null) {
                    var counter = 0
                    _items.value?.forEach { item ->
                        item.isSelected = false//чтобы не было несколько edit text
                        if (item.catalogId == barcoded.catalogId) {
                            item.haveCount = item.haveCount + 1
                            //item.isSelected = true
                            //setSelectedItem(counter)
                        }
                        counter = counter + 1
                        result += item
                    }
                    updateItems(result)
                }
            }
        }
    }
    suspend fun loadItems (db : MainDB, sessionId: String) : List<IncomeItem>{
        var dao = db.getDao()

        var listOfGoods: List<Pair<Goods, Cell>> = listOf()
        listOfGoods = dao.getAllIncomeItem()
            .filter { item -> item.sessionId == sessionId}
            .map { item ->  dao.getGoodsById(item.goodsId) }
            .map { inner -> Pair(inner, dao.getCellById(inner.cellId)) }

        var result : List<IncomeItem> = listOf()
        var previousCellId: String = ""
        var counter: Int = 0
        for (item in listOfGoods){
            var catalog = dao.getCatalogById(item.first.catalogId)
            if (item.second.typeCellId == "e873f579-44fc-48e1-84d2-f529b77653ee"){//6730f3c3-0a33-4454-a485-520522b64de5
                var parentCell = dao.getCellById(item.second.parentCellId.toString())
                var isShown = if(parentCell.name.contains("IN")) true else false
                if(item.second.id != previousCellId){
                    previousCellId = item.second.id
                    result += IncomeItem(
                        name =  item.second.name,
                        TE = if(isShown) item.second.name else "",
                        catalogId = "",
                        allCount = item.first.amount,
                        haveCount = 0,
                        isExpandable = true,
                        isShown = isShown)
                    result += IncomeItem(
                        name =  catalog.name,
                        TE = if(isShown) item.second.name else "",
                        catalogId = catalog.id,
                        allCount = item.first.amount,
                        haveCount = 0,
                        isExpandable = false,
                        isShown = !isShown)
                }else{
                    result += IncomeItem(
                        name =  catalog.name,
                        TE = if(isShown) item.second.name else "",
                        catalogId = catalog.id,
                        allCount = item.first.amount,
                        haveCount = 0,
                        isExpandable = false,
                        isShown = !isShown)
                }
            }else{
                result += IncomeItem(
                    name =  catalog.name,
                    TE = currentCellName.value.toString(),
                    catalogId = catalog.id,
                    allCount = item.first.amount,
                    haveCount = 0,
                    isExpandable = false,
                    isShown = true)
            }
        }

        return  result
    }
    suspend fun getBarcode(db : MainDB, barcode: String) : Barcode{
        return db.getDao().getBarcodeByName(barcode)
    }
}