package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.db.MainDB
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.data.db.Repositories.IncomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class IncomeMenuViewModel : ViewModel() {
    private val _tasksList =  MutableLiveData<List<TaskMenuItem>>()
    var tasksList: LiveData<List<TaskMenuItem>> = _tasksList

    fun setTaskCollection(list: List<TaskMenuItem>){
        _tasksList.value = list
    }

    suspend fun updateSupplierList(incomeRepo: IncomeRepository) : List<TaskMenuItem>{
        var data: List<TaskMenuItem> = listOf()
        data = incomeRepo.getAllActiveIncomeSession()
        return data
    }

}