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

    val items: LiveData<List<IncomeItem>> get() = _items
    val selectedItem: LiveData<Int> get() = _selectedItem
    val stack: ArrayDeque<List<IncomeItem>> = ArrayDeque()


    fun updateItems(items: List<IncomeItem>){
        _items.value = items
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

    //Получить список при помощи определенного элемента(те)
    //Если элемент списка это те то запустить новую фунцию и скрыть эту те
    //Иначе обновить видимость элемента
    suspend fun loadItems (db : MainDB, sessionId: String) : List<IncomeItem>{
        var dao = db.getDao()
        var listOfGoods: List<Pair<Goods, Cell>> = listOf()
        listOfGoods = dao.getAllIncomeItem()
            .filter { item -> item.sessionId == sessionId}
            .map { item ->  dao.getGoodsById(item.goodsId) }
            .map { inner -> Pair(inner, dao.getCellById(inner.cellId)) }

        var result : List<IncomeItem> = listOf()
        var previousCellId: String = ""
        for (item in listOfGoods){
            var catalog = dao.getCatalogById(item.first.catalogId)
            if (item.second.typeCellId == "8423d2f4-5890-4052-86f9-e9f5a234fa23"){
                var parentCell = dao.getCellById(item.second.parentCellId.toString())
                var isShown = if(parentCell.name.contains("IN")) true else false
                if(item.second.id != previousCellId){
                    previousCellId = item.second.id
                    result += IncomeItem(
                        name =  item.second.name,
                        TE = if(isShown) item.second.name else "",
                        catalogId = catalog.id,
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
                    TE = "",
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