package com.example.wmswherther.viewModel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.InventoryItem
import com.example.wmswherther.Classes.TaskMenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventoryViewModel : ViewModel() {
    private val _suppliers = MutableLiveData<MutableList<InventoryItem>>()
    private val _orders = MutableLiveData<MutableList<TaskMenuItem>>()

    val Suppliers: LiveData<MutableList<InventoryItem>> get() = _suppliers
    val Orders: LiveData<MutableList<TaskMenuItem>> get() = _orders

    fun LoadSuppliers(activity: FragmentActivity) {
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
    fun LoadOrder(activity: FragmentActivity) {
        var ordersList: List<TaskMenuItem> = listOf()
        viewModelScope.launch {
            withContext(Dispatchers.IO)  {
                var dao = MainDB.getDB(activity).getDao()
                ordersList = dao.getInventorySessions().map { item ->
                    var supplierName =  dao.getSupplierById(item.supplierId ?: "").name
                    var cellName =  dao.getCellById(    item.cellId ?: "").name
                    TaskMenuItem(supplierName, StatusType.Created.name, "", cellName)
                }
            }
            withContext(Dispatchers.Main) {
                _orders.value = ordersList.toMutableList()
            }
        }
    }
}