package com.example.wmsRemote.viewModel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.Adapters.AdapterHelper
import com.example.wmsRemote.Adapters.MoveAdapter
import com.example.wmsRemote.MoveActivity
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.SupplierType
import com.example.wmsRemote.databinding.ActivityMoveBinding
import com.example.wmsRemote.models.processMoving
import com.example.wmswherther.HelperFunction
import com.example.wmswherther.data.db.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MoveItem(val item: Triple<Int, String, Pair<Int, Int>>, val isSelected: Boolean)
class MovingViewModel : ViewModel() {
    private val _myData = MutableLiveData<MutableList<MoveItem>>()
    private val _isMoving = MutableLiveData<Boolean>()
    private val _cell = MutableLiveData<String>()

    val isMoving: LiveData<Boolean> get() =_isMoving
    val myData: LiveData<MutableList<MoveItem>> get() = _myData
    val cell : LiveData<String> get() = _cell

    var supplier : Int =SupplierType.Bork.ordinal
    var client = Request()
    var ip = "192.168.6.208"

    fun updateMyData(collection: MutableList<MoveItem>){
        _myData.value = collection
    }
    fun updateItem(moveItem: MoveItem){
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                var list: MutableList<MoveItem> = mutableListOf()
                _myData.value!!.forEach { item ->
                    if (item.item.first == moveItem.item.first) {
                        list += moveItem
                    } else {
                        list += item
                    }

                }
                _myData.value = list
            }
        }
    }
    fun updateIsMoving(isMoving: Boolean){
        _isMoving.value = isMoving
    }
    fun updateCell(cell: String){
        _cell.value = cell
    }
    fun searchBtnHandler(
        text: String,
        context: MoveActivity,
        db: MainDB,
        adapter: MoveAdapter,
        binding: ActivityMoveBinding
    ) {
        if (isMoving!!.value == true) {
            if (isCell(text)) {
                var result: MutableList<MoveItem> = mutableListOf()
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        binding.swipe.isRefreshing = true
                    }
                    withContext(Dispatchers.IO) {
                        result = processMoving(_myData.value, cell, db, viewModelScope, text, supplier,context)
                    }
                    withContext(Dispatchers.Main) {
                        updateMyData(result)
                        updateIsMoving(false)
                        binding.swipe.isRefreshing = false
                    }
                }

            } else {
                Toast.makeText(context, "Need scan cell", Toast.LENGTH_SHORT).show()
            }

        } else {
            if (isCell(text)) {

                updateCell(text)
                var result: List<MoveItem> = listOf()
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        binding.swipe.isRefreshing = true
                    }
                    withContext(Dispatchers.IO) {
                       // var cell = HelperFunction.retryRequest(context){client.getCellByName(ip, text)}
                        /*if(cell.length() != 0) {
                            var func = AdapterHelper.getMoveItems[supplier]
                            result = func!!.invoke(db,supplier, cell, context)
                        }*/
                    }
                    withContext(Dispatchers.Main) {
                        binding.swipe.isRefreshing = false
                    }
                    updateMyData(result.toMutableList())
                }
                //change list of item
            } else if (text != "") {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        var listOfItems = _myData.value
                        var func = AdapterHelper.getUpdatedMoveItems[supplier]
                        var list = func!!.invoke(db, supplier, listOfItems, text, context)
                        withContext(Dispatchers.Main) {
                            updateMyData(list.sortedByDescending { it.item.third.first }.toMutableList())
                        }
                    }
                }
            }
        }
    }
    fun isCell(cell: String): Boolean {
        if (cell.length == 4 && cell[0] in 'A' .. 'Z' && cell[1].isDigit() && cell[2].isDigit() && cell[3].isDigit()){
            return true
        }
        return false
    }
    fun convertToInt(nullableInt: Int?): Int {
        return nullableInt ?: 0  // If nullableInt is null, use 0 as default
    }
}
