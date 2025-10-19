package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.db.MainDB
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.data.db.Barcode
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
                            item.isSelected = true
                            setSelectedItem(counter)
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
        var coll = dao.getAllIncomeItem().filter { item -> item.sessionId == sessionId}
        var result = coll.map { item ->
            var goods = dao.getGoodsById(item.goodsId)
            var catalog = dao.getCatalogById(goods.catalogId)
            IncomeItem(catalog.name, catalog.id, 0, goods.amount)
        }
        return result
    }
    suspend fun getBarcode(db : MainDB, barcode: String) : Barcode{
        return db.getDao().getBarcodeByName(barcode)
    }


}