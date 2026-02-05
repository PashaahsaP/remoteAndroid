package com.example.wmsRemote.viewModel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.Adapters.MoveSessionAdapter
import com.example.wmsRemote.MoveActivity
import com.example.wmsRemote.data.db.Cell
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmsRemote.data.enums.SupplierType
import com.example.wmsRemote.databinding.FragmentMoveSessionBinding
import com.example.wmsRemote.models.processMoving
import com.example.wmswherther.Classes.MoveSessionItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.Change
import com.example.wmswherther.data.db.Goods
import com.example.wmswherther.data.db.Request
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


class MoveSessionViewModel : ViewModel() {
    private val _myData = MutableLiveData<MutableList<MoveSessionItem>>()
    private val _isMoving = MutableLiveData<Boolean>()
    private val _cell = MutableLiveData<String>()
    private val _selectedItem = MutableLiveData<Int>()


    val isMoving: LiveData<Boolean> get() =_isMoving
    val myData: LiveData<MutableList<MoveSessionItem>> get() = _myData
    val cell : LiveData<String> get() = _cell
    val selectedItem: LiveData<Int> get() = _selectedItem

    var supplier : Int = SupplierType.Bork.ordinal
    var client = Request()
    var ip = "192.168.6.208"

    fun updateMyData(collection: MutableList<MoveSessionItem>){
        _myData.value = collection
    }
    fun changeList(barcode: String, dao: Dao){
        viewModelScope.launch {
            var list: MutableList<MoveSessionItem> = mutableListOf()
            withContext(Dispatchers.IO) {
                var dbBarcode = dao.getBarcodeByName(barcode)
                var catalog = dao.getCatalogById(dbBarcode.catalogId)

                myData.value?.forEach { item ->
                    if(item.catalogId == catalog.id && item.haveCount < item.allCount){
                        list.add(item.copy(haveCount = item.haveCount + 1))
                    }else {
                        list.add((item))
                    }
                }
            }
            withContext(Dispatchers.Main){
                updateMyData(list)
            }
        }
    }
    fun setSelectedItem(selectedItemCount: Int){
        _selectedItem.value = selectedItemCount
    }
    fun getSelectedItem() : Int{
        var isCorrect = _selectedItem.value
        if (isCorrect != null)
            return isCorrect
        else
            return  0
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
        adapter: MoveSessionAdapter,
        binding: FragmentMoveSessionBinding
    ) {
        if (isMoving!!.value == true) {
            if (isCell(text)) {
                var result: MutableList<MoveSessionItem> = mutableListOf()
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
                var result: List<MoveSessionItem> = listOf()
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
                       /* var listOfItems = _myData.value
                        var func = AdapterHelper.getUpdatedMoveSessionItems[supplier]
                        var list = func!!.invoke(db, supplier, listOfItems, text, context)
                        withContext(Dispatchers.Main) {
                            updateMyData(list.sortedByDescending { it.item.third.first }.toMutableList())
                        }*/
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
    fun loadData(
        dao: Dao,
        barcode: String,
        viewModel: MainViewModel
    ) {
        viewModelScope.launch {
            var list: MutableList<MoveSessionItem> = mutableListOf()
            withContext(Dispatchers.IO) {
                var cell = dao.getCellByName(barcode)
                if (cell != null) {
                    dao.getGoodsByCellId(cell.id).forEach { goods: Goods ->
                        var catalog = dao.getCatalogById(goods.catalogId)
                        if (catalog.supplierId == (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId && goods.isAvailable) {
                            list.add(
                                MoveSessionItem(
                                    isSelected = false,
                                    haveCount = 0,
                                    allCount = goods.amount,
                                    name = catalog.name,
                                    catalogId = catalog.id,
                                    goodsId = goods.id
                                )
                            )
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                updateMyData(list)
            }
        }
    }
    fun convertToInt(nullableInt: Int?): Int {
        return nullableInt ?: 0  // If nullableInt is null, use 0 as default
    }

    //Если количество равно то надо полностью изменить
    fun moveItems(barcode: String, dao: Dao, viewModel: MainViewModel) {
        var listItems : List<MoveSessionItem> = listOf()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var cellTo = getCell(dao, barcode, viewModel, _cell.value.toString())
                var allGoods: List<Goods> = dao.getGoods().filter { goods -> goods.cellId == cellTo.id }

                myData.value?.forEach { item ->
                    var catalog = dao.getCatalogById(item.catalogId)
                    var listOfGoodsInCellTo : List<Goods> = allGoods.filter {
                            goodsItem -> goodsItem.cellId == cellTo.id && goodsItem.catalogId == catalog.id
                    }
                    if(listOfGoodsInCellTo.isEmpty()){
                        createGoods(dao, item, cellTo, viewModel)
                    }else{
                        updateGoods(dao, item, cellTo, viewModel, barcode, allGoods)
                    }
                    //update ui
                    if (item.haveCount != item.allCount &&  item.haveCount != 0) {
                        listItems += item.copy(haveCount = 0, allCount = item.allCount - item.haveCount)
                    } else if (item.haveCount == 0){
                        listItems += item
                    }

                }
            }
            withContext(Dispatchers.Main){
                updateMyData(listItems.toMutableList())
            }
            updateIsMoving(false)
        }
    }

    suspend  fun createGoods(dao: Dao, item: MoveSessionItem, cellTo: Cell, viewModel: MainViewModel) {
        var goods = Goods(
            id = UUID.randomUUID().toString(),
            amount = item.haveCount,
            cellId = cellTo.id,
            catalogId = item.catalogId,
            createdAt = System.currentTimeMillis(),
            true,
            other = null
        )
        var changes = Change(
            id = UUID.randomUUID().toString(),
            entityId = goods.id,
            operationType = OperationType.InsertGoods.ordinal,
            status = StatusType.Created.ordinal,
            supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
            other = null
        )
        dao.insertGoodsAsync(goods,changes)

        val changedElement = dao.getGoodsById(item.goodsId)
        if(item.haveCount == item.allCount){
            //удалить запись
            val localChanges = Change(
                id = UUID.randomUUID().toString(),
                entityId = item.goodsId,
                operationType = OperationType.DeleteGoods.ordinal,
                status = StatusType.Created.ordinal,
                supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
                other = null
            )
            dao.deleteGoodsAsync(changedElement, localChanges)
        }else{
            //изменить запись
            val localChanges = Change(
                id = UUID.randomUUID().toString(),
                entityId = item.goodsId,
                operationType = OperationType.UpdateGoods.ordinal,
                status = StatusType.Created.ordinal,
                supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
                other = null
            )

            dao.updateGoodsAsync(changedElement.copy(amount = changedElement.amount - item.haveCount), localChanges)
        }
    }
    suspend fun updateGoods(dao: Dao, item: MoveSessionItem, cell: Cell, viewModel: MainViewModel, barcode: String, allGoods: List<Goods>) {

        var goods = dao.getGoodsById(item.goodsId)

        // коллекция элементов в ячейке куда идет перемещение,
        // нужно если в целевой ячейке есть такой же каталог товара, чтобы просто изменить количество товара(и удалить элемент если осталось 0 в
        // ячейке откуда идет перемещение)

        var changes = Change(
            id = UUID.randomUUID().toString(),
            entityId = goods.id,
            operationType = OperationType.UpdateGoods.ordinal,
            status = StatusType.Created.ordinal,
            supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
            other = null
        )
        dao.updateGoodsAsync(goods.copy(cellId = this.cell.id), changes)
    }

    private suspend fun getCell(
        dao: Dao,
        barcode: String,
        viewModel: MainViewModel,
        sourceCellName: String
    ): Cell {
        var cell = dao.getCellByName(barcode)
        if (cell == null) {
            var curCell = dao.getCellByName(sourceCellName) // откуда идет перемещение
            var newCell = Cell(
                id = UUID.randomUUID().toString(),
                typeCellId = curCell.typeCellId,
                parentCellId = curCell.parentCellId,
                name = barcode
            )
            var changes = Change(
                id = UUID.randomUUID().toString(),
                entityId = newCell.id,
                operationType = OperationType.InsertCell.ordinal,
                status = StatusType.Created.ordinal,
                supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
                other = null
            )
            dao.insertCellSync(newCell, changes)
            cell = newCell
        }
        return cell
    }
}
