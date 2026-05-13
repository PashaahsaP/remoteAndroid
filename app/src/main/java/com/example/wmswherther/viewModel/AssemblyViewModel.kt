package com.example.wmsRemote.viewModel

import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.Adapters.AdapterHelper
import com.example.wmsRemote.AssemblyActivity
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmsRemote.models.client
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.data.db.Entityes.SessionPicker
import com.example.wmswherther.data.db.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AssemblyViewModel : ViewModel() {

    val items: LiveData<List<TaskMenuItem>> get() = _items

    var _items = MutableLiveData<List<TaskMenuItem>>()

    fun updateItems(items: List<TaskMenuItem>) {
        _items.value = items
    }

    fun loadSessions(dao: Dao){
        var data : List<TaskMenuItem> = listOf()
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                data = dao.getPickerSessions()
                    .filter { item-> item.status == StatusType.Created.ordinal }
                    .map { item -> TaskMenuItem(
                        item.supplierId,
                        item.id.toString(),
                        "Some",
                        "0/1",
                        "4",
                        item.createdAt.toString() )
                }
            }
            withContext(Dispatchers.Main){
                updateItems(data)
            }
        }
    }
    fun startSession(dao: Dao, sessionId: String){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                var session = dao.getPickerSessionById(sessionId)
               // dao.updatePickerSession(session.copy(status = StatusType.Work.ordinal.toString()))
            }
        }
    }
}