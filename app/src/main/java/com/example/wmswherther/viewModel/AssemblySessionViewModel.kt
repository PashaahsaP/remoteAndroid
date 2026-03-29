
package com.example.wmsRemote.viewModel

import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.Adapters.AdapterHelper
import com.example.wmsRemote.AssemblyActivity
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmsRemote.models.client
import com.example.wmswherther.Classes.AssemblyItem
import com.example.wmswherther.Classes.PickerItem
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AssemblySessionViewModel : ViewModel() {

    private val _count = MutableLiveData<Int>()
    val menuStatus: LiveData<Int> get() = _menuStatus
    val assemblyStatus: LiveData<Int> get() = _assemblyStatus
    val activeElement: LiveData<AssemblyItem> get() = _activeElement
    val items: LiveData<List<AssemblyItem>> get() = _items
    val count: LiveData<Int>get() = _count


    var _menuStatus = MutableLiveData<Int> ()
    var _assemblyStatus = MutableLiveData<Int> ()
    var _activeElement = MutableLiveData<AssemblyItem>()
    var _items = MutableLiveData<List<AssemblyItem>>()

    fun loadCollection(db: MainDB, sessionId: String): Unit
    {
        var dao = db.getDao()
        viewModelScope.launch {
            var data : List<AssemblyItem> = listOf()
            withContext(Dispatchers.IO) {
                data = dao.getPickerItems()
                    .filter { item -> item.sessionId == sessionId }
                    .map { item ->
                        var goodsItem = dao.getGoodsById(item.goodsId)
                        var catalog = dao.getCatalogById(goodsItem.catalogId)
                        var cell = dao.getCellById(item.cellId.toString())
                        var barcodes = dao.getBarcodes().filter { item -> item.catalogId == catalog.id }.map { item -> item.name }
                        var pickerList : List<PickerItem> = listOf(PickerItem(catalog.name, barcodes))
                        pickerList += getPickerCell(dao, cell)
                        AssemblyItem(
                    sessionId = sessionId,
                    catalogId = goodsItem.catalogId,
                    assemblyItemId = item.id,
                    amount = goodsItem.amount,
                    cell = cell.name,
                    name = catalog.name,
                    status = StatusType.Created.ordinal,
                    pickerList = pickerList
                )
                    }
            }
            withContext(Dispatchers.Main){
                _items.value = data
                _activeElement.value = data.first()
            }
        }
    }

    private suspend fun getPickerCell(dao: Dao, cell: Cell): List<PickerItem> {
        var result :List<PickerItem> = listOf()
        if(isPickerCell(cell.name,dao)){
            result += PickerItem(cell.name, listOf(cell.name))
        }else{
            result += PickerItem(cell.name, listOf(cell.name))
            var innerCell = dao.getCellById(cell.parentCellId.toString())
            result += getPickerCell(dao,innerCell)

        }
        return result
    }

    /* fun getItem(count: Int) : AssemblyItem{
         val data = _items.value?.get(count)
         if(data != null){
             return data
         }else{
             return  AssemblyItem(1,2,1,2,3,"","",1, listOf())
         }
     }*/
    fun changeMenuStatus(status: Int){
        _menuStatus.value = status
    }
    fun setActiveElement(element: AssemblyItem){
        _activeElement.value = element
    }
    /*fun loadItems(sessionId: Int, db: MainDB, supplierId: Int)
    {
        viewModelScope.launch {
            var items: List<AssemblyItem> = listOf()
            withContext(Dispatchers.IO){
                var func = AdapterHelper.getAssemblyItems[supplierId]
                items = func!!.invoke(sessionId, db)
            }
            withContext(Dispatchers.Main) {
                _count.value = 0
                setActiveElement(items[count.value ?: 0])
                _items.value = items
            }
        }
    }*/
    fun removeElementFromCollection(elem: AssemblyItem?){
        if(elem != null)
            _items.value =  _items.value?.minusElement(elem)
    }

    fun changeAssemblyStatus(enterCell: Int) {
        _assemblyStatus.value = enterCell
    }

    /*fun searchBtnHandler(trim: String, assemblyActivity: AssemblyActivity, db: MainDB) {
        if(assemblyStatus.value == StatusType.EnterCell.ordinal) {
            if (activeElement.value!!.cell != trim) {
                var act = items.value?.firstOrNull { item -> item.cell == trim }
                if(act != null) {
                    setActiveElement(act)
                }
            }
        }


        var isHaveBarcodeCount = activeElement.value?.barcodes?.filter { item -> item == trim }
       if(activeElement.value!!.cell == trim && assemblyStatus.value == StatusType.EnterCell.ordinal){
            changeAssemblyStatus(StatusType.EnterBarcode.ordinal)
        }else if (isHaveBarcodeCount?.count() != 0 && assemblyStatus.value == StatusType.EnterBarcode.ordinal){
            changeAssemblyStatus(StatusType.EnterCount.ordinal)
        }
    }*/
    /*suspend fun searchBtnHandlerCount(trim: String, assemblyActivity: AssemblyActivity, db: MainDB) {
        val amount = activeElement.value?.amount
       if(trim.isDigitsOnly()) {
           val amountInt = trim.toInt()
           if (amountInt > 0 && amountInt <= amount.toString().toInt()) {
               val cur = activeElement.value
               var assItem = client.getAssemblyBorkItemById(ip, cur!!.assemblyItemId)
               client.updateAssemblyBorkItem(
                   ip, AssemblyItem(
                       assemblyItemId = assItem["id"].toString().toInt(),
                       barcodes = listOf(),
                       supplierId = 1,
                       catalogId = cur.catalogId,
                       cell = cur.cell,
                       status = cur.status,
                       name = cur.name,
                       amount = cur.amount,
                       sessionId = cur.sessionId,
                   )
               )
               if (_items.value?.count() == 1) {
                   var assemblyId = assItem["assemblyId"].toString()
                   var assembly = client.getAssemblySessionById(ip, assemblyId)
                   client.updateAssemblySession(ip, AssemblySession(
                       id = assembly["id"].toString().toInt(),
                       supplier = 1,
                       out = "1",
                       status = 1,
                       finished_at = "",
                       created_at = "",
                       lines = assembly["lines"].toString().toInt(),
                       amount =  assembly["amount"].toString().toInt()
                   ))
                   viewModelScope.launch {
                       withContext(Dispatchers.Main) {
                           _sessions.value = listOf()
                           loadCollection(db)
                           changeMenuStatus(0)
                       }
                   }
               } else
                   viewModelScope.launch {
                       withContext(Dispatchers.Main) {
                           removeElementFromCollection(_activeElement.value)
                           val next = getItem(_count.value ?: 0)
                           setActiveElement(next)
                       }
                   }


            }else{
                viewModelScope.launch {
                    withContext(Dispatchers.Main){
                        Toast.makeText(assemblyActivity,"Число должно быть больше 0 и не больше ${amount}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }else{
           viewModelScope.launch {
               withContext(Dispatchers.Main){
                   Toast.makeText(assemblyActivity,"Должны быть только числа", Toast.LENGTH_SHORT).show()
               }
           }
        }
    }*/
}
suspend fun isPickerCell(cell: String, dao: Dao): Boolean {
    val cells = dao.getCellTypes().filter { cellType -> cellType.type == "Picker" }

    return cells.any { cellType ->
        val mask = cellType.mask ?: return@any false

        mask.length == cell.length &&
                mask.indices.all { i ->
                    when (mask[i]) {
                        '*' -> cell[i].isLetter()
                        '#' -> cell[i].isDigit()
                        else -> mask[i] == cell[i]
                    }
                }
    }
}