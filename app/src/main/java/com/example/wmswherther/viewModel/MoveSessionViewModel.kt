package com.example.wmsRemote.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.MoveSessionItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Repositories.MoveRepository
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


class MoveSessionViewModel : ViewModel() {
    // <editor-fold desc="varibles">

   /* var supplier : Int = SupplierType.Bork.ordinal
    var client = Request()
    var ip = "192.168.6.208"*/
        private val _myData = MutableLiveData<MutableList<MoveSessionItem>>()
        private val _isMoving = MutableLiveData<Boolean>()
        private val _cell = MutableLiveData<String>()
        private val _counter = MutableLiveData(0)
        private val _totalCount = MutableLiveData(99999999)
        private val _selectedItem = MutableLiveData<Int>()
    // </editor-fold>
    // <editor-fold desc="properties">
        val isMoving: LiveData<Boolean> get() =_isMoving
        val myData: LiveData<MutableList<MoveSessionItem>> get() = _myData
        val cell : LiveData<String> get() = _cell
        val counter : LiveData<Int> get() = _counter
        val totalCount : LiveData<Int> get() = _totalCount
        val selectedItem: LiveData<Int> get() = _selectedItem
    // </editor-fold>
    // <editor-fold desc="propertiesMethods">
        fun updateMyData(collection: MutableList<MoveSessionItem>){
            _myData.value = collection
        }
        fun changeList(barcode: String, dao: Dao){
            viewModelScope.launch {
                var list: MutableList<MoveSessionItem> = mutableListOf()
                var localCounter = 0
                withContext(Dispatchers.IO) {
                    var dbBarcode = dao.getBarcodeByName(barcode)
                    var catalog = dao.getCatalogById(dbBarcode.catalogId)
                    myData.value?.forEach { item ->
                        if(item.catalogId == catalog.id && item.haveCount < item.allCount){
                            list.add(item.copy(haveCount = item.haveCount + 1))
                            localCounter += 1
                        }else {
                            list.add((item))
                        }
                    }
                }
                withContext(Dispatchers.Main){
                    updateMyData(list)
                    setCounter(getCounter() + localCounter)

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
        fun getCounter() : Int{
            return _counter.value ?: 0
        }
        fun setCounter(value: Int) {
            _counter.value = value
        }
        fun updateIsMoving(isMoving: Boolean){
            _isMoving.value = isMoving
        }
        fun updateCell(cell: String){
            _cell.value = cell
        }
    // </editor-fold>
    // <editor-fold desc="UI  actions">
        fun loadData(
            dao: Dao,
            barcode: String,
            viewModel: MainViewModel
        ) {
            viewModelScope.launch {
                var list: MutableList<MoveSessionItem> = mutableListOf()
                var totalCount = 0
                withContext(Dispatchers.IO) {
                    var cell = dao.getCellByName(barcode)
                    if (cell != null) {
                        dao.getGoodsByCellId(cell.id).forEach { goods: Goods ->
                            var catalog = dao.getCatalogById(goods.catalogId)
                            if (catalog.supplierId == (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId && goods.isAvailable) {
                                totalCount +=  goods.amount

                                list.add(
                                    MoveSessionItem(
                                        isSelected = false,
                                        haveCount = 0,
                                        allCount = goods.amount,
                                        name = catalog.name,
                                        catalogId = catalog.id,
                                        goodsId = goods.id,
                                        isCell = false

                                    )
                                )
                            }
                        }
                        dao.getAllCells()
                            .filter { innerCell-> innerCell.parentCellId == cell.id }
                            .forEach { inner ->
                                totalCount +=  1
                                list.add(
                                    MoveSessionItem(
                                        name = inner.name,
                                        haveCount = 0,
                                        catalogId = inner.id,//TODO создать новый класс в котором будет свойство cellID
                                        goodsId = "",
                                        allCount = 1,
                                        isSelected = false,
                                        isCell = true
                                    )
                                )
                            }
                    }
                }
                withContext(Dispatchers.Main) {
                    updateMyData(list)
                    _totalCount.value = totalCount
                }
            }
        }

        fun moveItems(barcode: String,
                      dao: Dao,
                      viewModel: MainViewModel) {
            var listItems : List<MoveSessionItem> = listOf()
            var moveRepo = MoveRepository(dao)
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    var cellTo = moveRepo.getCell(dao, barcode, viewModel, _cell.value.toString())
                    var allGoods: List<Goods> = dao.getGoods().filter { goods -> goods.cellId == cellTo.id }
                    myData.value?.forEach { item ->
                        if (item.isCell){
                            moveRepo.moveCellToCell(item, cellTo, dao, viewModel)//TODO make validation is Cell
                        }else {
                            moveRepo.moveGoodsToCell(dao, item, allGoods, cellTo, moveRepo, viewModel)
                        }
                        // update ui
                        if(item.allCount == item.haveCount){

                        }
                        else if (item.haveCount != item.allCount && item.haveCount != 0) {
                            listItems += item.copy(
                                haveCount = 0,
                                allCount = item.allCount - item.haveCount
                            )
                        } else if (item.haveCount == 0) {
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


    // </editor-fold>
    // <editor-fold desc="helper function">


    fun isCell(cell: String): Boolean {
        if (cell.length == 4 && cell[0] in 'A' .. 'Z' && cell[1].isDigit() && cell[2].isDigit() && cell[3].isDigit()){
            return true
        }
        return false
    }

    fun setSelection(checked: Boolean) {
        /*var list: MutableList<MoveSessionItem> = mutableListOf()
        if (checked){
            setCounter(_totalCount.value?: 0)
            myData.value?.forEach { item -> list.add(item.copy(haveCount = item.allCount))}
            updateMyData(list)
        }else{
            setCounter(0)
            myData.value?.forEach { item -> list.add(item.copy(haveCount = 0))}
            updateMyData(list)
        }*/
    }
    // </editor-fold>


}
