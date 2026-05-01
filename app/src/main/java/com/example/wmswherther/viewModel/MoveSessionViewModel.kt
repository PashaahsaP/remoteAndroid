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
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Repositories.MoveRepository
import com.example.wmswherther.data.db.Repositories.MoveeRepository
import com.example.wmswherther.data.factory.CellFactory
import com.example.wmswherther.data.factory.ChangeFactory
import com.example.wmswherther.data.factory.GoodsFactory
import com.example.wmswherther.data.factory.MovementFactory
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
        private  val  _isEmptyList = MutableLiveData<Boolean>(true)
        private  val  _isAllSelected = MutableLiveData<Boolean>(true)
    // </editor-fold>
    // <editor-fold desc="properties">
        val isMoving: LiveData<Boolean> get() =_isMoving
        val myData: LiveData<MutableList<MoveSessionItem>> get() = _myData
        val cell : LiveData<String> get() = _cell
        val counter : LiveData<Int> get() = _counter
        val totalCount : LiveData<Int> get() = _totalCount
        val selectedItem: LiveData<Int> get() = _selectedItem
        val isEmptyList: LiveData<Boolean> get() = _isEmptyList
        val isAllSelected : LiveData<Boolean> get() = _isAllSelected

    // </editor-fold>
    // <editor-fold desc="propertiesMethods">
        fun setSelectionForAll(select: Boolean){
            _isAllSelected.value = select
        }
        fun updateMyData(collection: MutableList<MoveSessionItem>){
            _myData.value = collection
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
        fun getIsEmptyList() : Boolean{
        return _isEmptyList.value ?: true
    }
        fun setIsEmptyList(value: Boolean) {
            _isEmptyList.value = value
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
            moveRepo: MoveeRepository,
            barcode: String,
            viewModel: MainViewModel
        ) {
            viewModelScope.launch {
                var list: MutableList<MoveSessionItem> = mutableListOf()
                var totalCount = 0
                withContext(Dispatchers.IO) {
                    var cell = moveRepo.getCellByName(barcode)
                    if (cell != null) {
                        totalCount = loadGoods(moveRepo, cell, viewModel, list)
                        totalCount += loadTe(moveRepo, cell, list)
                    }
                }
                withContext(Dispatchers.Main) {
                    updateMyData(list)
                    _totalCount.value = totalCount
                }
            }
        }



    fun moveItems(barcode: String,
                      moveRepo: MoveeRepository,
                      viewModel: MainViewModel) {
            var listItems : MutableList<MoveSessionItem> = mutableListOf()
            var localCounter : Int = counter.value ?: 0
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    var cellTo = getCellDestination(moveRepo, barcode, viewModel)
                    var allGoods: List<Goods> = moveRepo.getGoods()
                                    .filter { goods -> goods.cellId == cellTo.id }
                    myData.value?.forEach { item ->
                        movingOfItem(item, viewModel, moveRepo, cellTo, allGoods)
                        updateUiList(item, listItems)
                        localCounter -= item.haveCount// for know that need show dialog window when switch to other cell
                    }
                }
                withContext(Dispatchers.Main){
                    updateMyData(listItems)
                    setCounter(localCounter)// for know that need show dialog window when switch to other cell
                }
                updateIsMoving(false)
            }
        }

    fun changeList(barcode: String, moveRepo: MoveeRepository){
        viewModelScope.launch {
            var list: MutableList<MoveSessionItem> = mutableListOf()
            var localCounter = 0
            var dbBarcode: Barcode
            var catalog: Catalog
            dbBarcode = withContext(Dispatchers.IO) {
                moveRepo.getBarcodeByName(barcode)
            } ?: return@launch

            catalog = withContext(Dispatchers.IO) {
                moveRepo.getCatalogById(dbBarcode.catalogId)
            } ?: return@launch

            withContext(Dispatchers.IO){
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

    // </editor-fold>
    // <editor-fold desc="helper function">

    private fun loadTe(
        moveRepo: MoveeRepository,
        cell: Cell,
        list: MutableList<MoveSessionItem>
    ) : Int {
        var totalCount : Int = 0
        moveRepo.getAllCells()
            .filter { innerCell -> innerCell.parentCellId == cell.id }
            .forEach { inner ->
                totalCount += 1
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
        return totalCount
    }

    private suspend fun loadGoods(
        moveRepo: MoveeRepository,
        cell: Cell,
        viewModel: MainViewModel,
        list: MutableList<MoveSessionItem>
    ): Int {
        var totalCount = 0
        moveRepo.getGoodsByCellId(cell.id).forEach { goods: Goods ->
            var catalog = moveRepo.getCatalogById(goods.catalogId)
            if (catalog.supplierId == (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId && goods.isAvailable) {
                totalCount += goods.amount

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
        return totalCount
    }
    fun isCell(cell: String): Boolean {
        if (cell.length == 4 && cell[0] in 'A' .. 'Z' && cell[1].isDigit() && cell[2].isDigit() && cell[3].isDigit()){
            return true
        }
        return false
    }
    private fun updateUiList(
        item: MoveSessionItem,
        listItems: MutableList<MoveSessionItem>
    ) {
        if (item.allCount == item.haveCount) {

        } else if (item.haveCount != item.allCount && item.haveCount != 0) {
            listItems += item.copy(
                haveCount = 0,
                allCount = item.allCount - item.haveCount
            )
        } else if (item.haveCount == 0) {
            listItems += item
        }
    }

    private suspend fun moveCellsToCell(
        item: MoveSessionItem,
        viewModel: MainViewModel,
        moveRepo: MoveeRepository,
        cellTo: Cell
    ) {
        if (item.haveCount == 1) { // ячейка выбрана поэтому 1, больше 1 быть не может
            var changes = ChangeFactory.create(
                entityId = item.catalogId,
                supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
                operationType = OperationType.UpdateCell
            )
            var cell = moveRepo.getCellById(item.catalogId)
            moveRepo.updateCellAsync(cell.copy(parentCellId = cellTo.id), changes)
        }
    }

    private suspend fun getCellDestination(
        moveRepo: MoveeRepository,
        barcode: String,
        viewModel: MainViewModel
    ): Cell {
        var cellTo = moveRepo.getCellByName(barcode)
        if (cellTo == null) {
            var curCell = moveRepo.getCellByName(_cell.value.toString()) // откуда идет перемещение
            var newCell = CellFactory.create(
                typeCellId = curCell.typeCellId,
                parentCellId = curCell.parentCellId,
                name = barcode
            )
            var changes = ChangeFactory.create(
                entityId = newCell.id,
                supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
                operationType = OperationType.InsertCell
            )
            moveRepo.insertCellSync(newCell, changes)
            cellTo = newCell
        }
        return cellTo
    }
    suspend  fun createGoodsInDestinationCell(
        item: MoveSessionItem,
        cellTo: Cell,
        viewModel: MainViewModel,
        moveRepo: MoveeRepository) {
        var goods = GoodsFactory.create(
            amount = item.haveCount,
            cellId = cellTo.id,
            catalogId = item.catalogId,
            isAvailable = true
        )
        var changes = ChangeFactory.create(
            entityId = goods.id,
            supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
            operationType = OperationType.InsertGoods
        )
        moveRepo.insertGoodsAsync(goods,changes)
    }

    suspend fun updateOrRemoveGoodsInSource(
        item: MoveSessionItem,
        viewModel: MainViewModel,
        moveRepo: MoveeRepository
    ) {
        if (item.haveCount == item.allCount) {
            var deleteChanges = ChangeFactory.create(
                entityId = item.goodsId,
                supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
                operationType = OperationType.DeleteGoods
            )
            moveRepo.deleteGoodsAsync(moveRepo.getGoodsById(item.goodsId), deleteChanges)
        } else {
            var updateChange =ChangeFactory.create(
                entityId = item.goodsId,
                supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
                operationType = OperationType.UpdateGoods
            )
            var updatedGoods = moveRepo.getGoodsById(item.goodsId)
                .copy(amount = item.allCount - item.haveCount)
            moveRepo.updateGoodsAsync(updatedGoods, updateChange)
        }
    }
    suspend fun updateGoodsInDestinationCell(
        allGoods: List<Goods>,
        item: MoveSessionItem,
        viewModel: MainViewModel,
        moveRepo: MoveeRepository
    ) {
        var destinationGoods = allGoods.first { goods -> goods.catalogId == item.catalogId }
        var destinationChange: Change = ChangeFactory.create(
            entityId = destinationGoods.id,
            supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
            operationType = OperationType.UpdateGoods
        )
        moveRepo.updateGoodsAsync(destinationGoods.copy(amount = item.haveCount + destinationGoods.amount), destinationChange)
    }
    private suspend fun movingOfItem(
        item: MoveSessionItem,
        viewModel: MainViewModel,
        moveRepo: MoveeRepository,
        cellTo: Cell,
        allGoods: List<Goods>
    ) {
        if (item.isCell) {
            if(item.haveCount != 0){
                insertMovement(moveRepo, item, cellTo, "", viewModel)
            }
            moveCellsToCell(item, viewModel, moveRepo, cellTo)
        } else {
            var catalog = moveRepo.getCatalogById(item.catalogId)
            var listOfGoodsInDestinationCell: List<Goods> = allGoods
                .filter { goodsItem -> goodsItem.cellId == cellTo.id && goodsItem.catalogId == catalog.id }
            // update db
            if(item.haveCount != 0){
                insertMovement(moveRepo, item, cellTo, catalog.id, viewModel)
            }
            if (listOfGoodsInDestinationCell.isEmpty() && item.haveCount != 0) {
                createGoodsInDestinationCell(item, cellTo, viewModel, moveRepo)
                updateOrRemoveGoodsInSource(item, viewModel, moveRepo)
            } else if (listOfGoodsInDestinationCell.isNotEmpty() && item.haveCount != 0) {
                updateGoodsInDestinationCell(
                    allGoods = allGoods,
                    item = item,
                    viewModel = viewModel,
                    moveRepo = moveRepo
                )
                updateOrRemoveGoodsInSource(item, viewModel, moveRepo)
            }

        }
    }

    private suspend fun insertMovement(
        moveRepo: MoveeRepository,
        item: MoveSessionItem,
        cellTo: Cell,
        catalogId: String,
        viewModel: MainViewModel
    ) {
        var movement = MovementFactory.create(
            cellFromId =
                if(item.goodsId!= "") moveRepo.getGoodsById(item.goodsId).cellId
                else moveRepo.getCellById(item.catalogId).parentCellId.toString(),
            cellToId = cellTo.id,
            catalogId = catalogId,
            goodsId = item.goodsId,
            qty = item.haveCount.toString(),
            operationType = OperationType.Movement,
            entityId = if(item.goodsId != "") item.goodsId else item.catalogId// catalog id is id of te if item is as te
        )
        var change = ChangeFactory.create(
            entityId = movement.id,
            supplierId = (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId,
            operationType = OperationType.InsertMovement
        )
        moveRepo.insertMovementAsync(movement, change)
    }

    fun setSelection(checked: Boolean) {
        var list: MutableList<MoveSessionItem> = mutableListOf()
        if (checked){
            setCounter(_totalCount.value?: 0)
            myData.value?.forEach { item -> list.add(item.copy(haveCount = if(item.allCount != 0) item.allCount else item.haveCount ))}
            updateMyData(list)
        }else{
            setCounter(0)
            myData.value?.forEach { item -> list.add(item.copy(haveCount = 0))}
            updateMyData(list)
        }
    }
    // </editor-fold>


}
