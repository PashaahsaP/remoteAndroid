package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wmsRemote.data.db.MainDB
import com.example.wmswherther.Classes.IncomeItem

class IncomeSessionViewModel : ViewModel() {
    private val _items = MutableLiveData<List<IncomeItem>>()
    val items: LiveData<List<IncomeItem>> get() = _items

    fun updateItems(items: List<IncomeItem>){
        _items.value = items
}
    suspend fun loadItems (db : MainDB, sessionId: String) : List<IncomeItem>{
        var dao = db.getDao()
        var coll = dao.getAllIncomeItem().filter { item -> item.sessionId == sessionId}
        var result = coll.map { item ->
            var goods = dao.getGoodsById(item.goodsId)
            var catalog = dao.getCatalogById(goods.catalogId)
            IncomeItem(catalog.name, 0, goods.amount)
        }
        return result
    }
}