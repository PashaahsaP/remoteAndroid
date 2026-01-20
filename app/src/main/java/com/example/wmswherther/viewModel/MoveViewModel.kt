package com.example.wmswherther.viewModel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.MainActivity
import com.example.wmsRemote.SupplierItem
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.viewModel.MoveSessionItem
import com.example.wmswherther.Adapters.MoveItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MoveViewModel: ViewModel() {
    private val _suppliers = MutableLiveData<MutableList<MoveItem>>()

    val Suppliers: LiveData<MutableList<MoveItem>> get() = _suppliers

    fun LoadSuppliersFromLocal(activity: FragmentActivity) {
        var supplierList: List<MoveItem> = listOf()
        viewModelScope.launch {
            withContext(Dispatchers.IO)  {
                var dao = MainDB.getDB(activity).getDao()
                supplierList = dao.getAllSuppliers().map { item ->
                    MoveItem(item.name, item.id)
                }
            }
            withContext(Dispatchers.Main) {
                _suppliers.value = supplierList.toMutableList()
            }
        }
    }
}