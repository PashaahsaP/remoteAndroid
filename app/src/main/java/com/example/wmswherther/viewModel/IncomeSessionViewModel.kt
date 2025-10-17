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
    val items: LiveData<List<IncomeItem>> get() = _items

    fun updateItems(items: List<IncomeItem>){
        _items.value = items
    }

    fun updateCollection(db : MainDB, barcode: String?){
        var barcoded = Barcode("","","","","")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                barcoded = getBarcode(db, if (barcode == null) "" else barcode)

            }
            withContext(Dispatchers.Main){
                var result : List<IncomeItem> = mutableListOf()
                _items.value?.forEach{ item ->
                    if (item.catalogId == barcoded.catalogId){
                        item.haveCount = item.haveCount + 1
                    }
                    result += item
                }
                updateItems(result)
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