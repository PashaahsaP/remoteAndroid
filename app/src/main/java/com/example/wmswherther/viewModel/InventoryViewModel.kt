package com.example.wmswherther.viewModel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.db.MainDB
import com.example.wmswherther.Classes.InventoryItem
import com.example.wmswherther.Classes.MoveItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventoryViewModel : ViewModel() {
    private val _suppliers = MutableLiveData<MutableList<InventoryItem>>()

    val Suppliers: LiveData<MutableList<InventoryItem>> get() = _suppliers

    fun LoadSuppliersFromLocal(activity: FragmentActivity) {
        var supplierList: List<InventoryItem> = listOf()
        viewModelScope.launch {
            withContext(Dispatchers.IO)  {
                var dao = MainDB.getDB(activity).getDao()
                supplierList = dao.getAllSuppliers().map { item ->
                    InventoryItem(item.name, item.id)
                }
            }
            withContext(Dispatchers.Main) {
                _suppliers.value = supplierList.toMutableList()
            }
        }
    }
}