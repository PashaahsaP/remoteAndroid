package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.Classes.IInventoryItem
import com.example.wmsRemote.data.db.MainDB
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.data.db.Supplier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class IncomeSessionViewModel : ViewModel() {
    private val _tasksList =  MutableLiveData<List<TaskMenuItem>>()
    var tasksList: LiveData<List<TaskMenuItem>> = _tasksList

    fun updateSupplierList(db : MainDB){
        viewModelScope.launch {
            var data: List<TaskMenuItem> = listOf()
            withContext(Dispatchers.IO) {
            var dao = db.getDao()
            var suppliers = dao.getAllSuppliers()
            dao.getAllIncomeSession().forEach { item ->
                data += TaskMenuItem(
                    supplier = suppliers.firstOrNull { inner -> inner.id == item.supplierId }!!.name,
                    progress = "0/1",
                    number = "",
                    date = LocalDate.now().toString()
                )
            }
            }
            withContext(Dispatchers.Main)
            {
                _tasksList.value = data
            }
        }
    }




}