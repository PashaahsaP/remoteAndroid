package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wmsRemote.data.db.MainDB
import com.example.wmswherther.Classes.TaskMenuItem
import java.time.LocalDate

class IncomeMenuViewModel : ViewModel() {
    private val _tasksList =  MutableLiveData<List<TaskMenuItem>>()
    var tasksList: LiveData<List<TaskMenuItem>> = _tasksList

    fun setTaskCollection(list: List<TaskMenuItem>){
        _tasksList.value = list
    }

    fun updateSupplierList(db : MainDB) : List<TaskMenuItem>{
            var data: List<TaskMenuItem> = listOf()
                var dao = db.getDao()
                var suppliers = dao.getAllSuppliers()
                dao.getAllIncomeSession().forEach { item ->
                    data += TaskMenuItem(
                        supplier = suppliers.firstOrNull { inner -> inner.id == item.supplierId }!!.name,
                        progress = "0/1",
                        number = item.id,
                        date = LocalDate.now().toString()
                    )
                }
        return data
    }




}