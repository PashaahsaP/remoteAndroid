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
import com.example.wmswherther.data.db.Repositories.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventoryViewModel : ViewModel() {
    private val _suppliers = MutableLiveData<MutableList<InventoryItem>>()
    private val _orders = MutableLiveData<MutableList<TaskMenuItem>>()
    private val _isActiveSupplierMode = MutableLiveData<Boolean>()

    val Suppliers: LiveData<MutableList<InventoryItem>> get() = _suppliers
    val Orders: LiveData<MutableList<TaskMenuItem>> get() = _orders


    fun LoadSuppliers(activity: FragmentActivity, inventoryRepo: InventoryRepository) {
        var supplierList: List<InventoryItem> = listOf()
        viewModelScope.launch {
            withContext(Dispatchers.IO)  {

                supplierList = inventoryRepo.getAllSuppliers().map { item ->
                    InventoryItem(item.name, item.id)
                }
            }
            withContext(Dispatchers.Main) {
                _suppliers.value = supplierList.toMutableList()
            }
        }
    }
    fun LoadOrder(activity: FragmentActivity, inventoryRepo: InventoryRepository) {
        var ordersList: List<TaskMenuItem> = listOf()
        viewModelScope.launch {
            withContext(Dispatchers.IO)  {
                ordersList = inventoryRepo.getInventorySessions().map { item ->
                    var supplier =  inventoryRepo.getSupplierById(item.supplierId ?: "")
                    var cellName =  inventoryRepo.getCellById(    item.cellId ?: "").name
                    TaskMenuItem(supplier.id, item.id, supplier.name, StatusType.Created.name, "", cellName)
                }
            }
            withContext(Dispatchers.Main) {
                _orders.value = ordersList.toMutableList()
            }
        }
    }
}