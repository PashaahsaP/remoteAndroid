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

    suspend fun loadItems (db : MainDB, sessionId: String) : List<IncomeItem>{
        var dao = db.getDao()
        var listOfGoods: List<Pair<Goods, Cell>> = listOf()
        listOfGoods = dao.getAllIncomeItem()
            .filter { item -> item.sessionId == sessionId}
            .map { item ->  dao.getGoodsById(item.goodsId) }
            .map { inner -> Pair(inner, dao.getCellById(inner.cellId)) }

        var previousCellId: String = ""
        var result : List<IncomeItem> = listOf()
        for (item in listOfGoods){
            var catalog = dao.getCatalogById(item.first.catalogId)
            if (item.second.typeCellId == "6730f3c3-0a33-4454-a485-520522b64de5"){
                if(item.second.id != previousCellId){
                    previousCellId = item.second.id
                    result += IncomeItem(item.second.name, "", 0, 0, isExpandable = true, isChild = false)
                    result += IncomeItem(catalog.name, item.first.catalogId, 0, item.first.amount, isExpandable = false, isChild = true, isShown = false)
                }else{
                    result += IncomeItem(catalog.name, item.first.catalogId, 0, item.first.amount, isExpandable = false, isChild = true, isShown = false)
                }
            }else{
                result += IncomeItem(catalog.name, item.first.catalogId, 0, item.first.amount, isExpandable = false, isChild = false, isShown = true)
            }
        }
       return result
    }
    suspend fun getBarcode(db : MainDB, barcode: String) : Barcode{
        return db.getDao().getBarcodeByName(barcode)
    }


}