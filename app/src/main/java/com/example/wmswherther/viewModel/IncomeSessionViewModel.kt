package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.db.Dao
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Repositories.IncomeRepository
import com.example.wmswherther.data.factory.ChangeFactory
import com.example.wmswherther.data.factory.MovementFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class IncomeSessionViewModel : ViewModel() {
    private val _items = MutableLiveData<List<IncomeItem>>()
    private val _selectedItem = MutableLiveData<Int>()
    private  val _currentCellName = MutableLiveData<String>()
    private  val _currentCountOfCount = MutableLiveData<Int>()
    private  val _countOfCount = MutableLiveData<Int>()
    private  val _isOverCounter = MutableLiveData<Boolean>()
    private  val _finish = MutableLiveData<Boolean>()

    val items: LiveData<List<IncomeItem>> get() = _items
    val currentCellName: LiveData<String> get() = _currentCellName
    val CurrentCountOfCount: LiveData<Int> get() = _currentCountOfCount
    val CountOfCount: LiveData<Int> get() = _countOfCount
    val IsOverCounter: LiveData<Boolean> get() = _isOverCounter
    val IsFinish: LiveData<Boolean> get() = _finish
    val stack: ArrayDeque<List<IncomeItem>> = ArrayDeque()
    val cellStack: ArrayDeque<String> = ArrayDeque()

    fun setCounterValidation(isOverFlag: Boolean){
        _isOverCounter.value = isOverFlag
    }
    fun setFinishValidation(isOverFlag: Boolean){
        _finish.value = isOverFlag
    }
    fun setCurCountOfCount(count: Int){
        _currentCountOfCount.value = count
    }
    fun setCountOfCount(count: Int){
        _countOfCount.value = count
    }
    fun updateItems(items: List<IncomeItem>){
        var sortedCollection : MutableList<IncomeItem> = mutableListOf()
        var teCollection : MutableList<IncomeItem> = mutableListOf()
        var otherCollection : MutableList<IncomeItem> = mutableListOf()
        var counter = 0
        var curCounter = 0
        var isOver = false
        items.forEach { item ->
            if(item.isExpandable){
                teCollection.add(item)
            }else if(!item.isShown){
                teCollection.add(item)
                counter += item.allCount
                if(item.haveCount > item.allCount){
                    isOver = true
                }
                curCounter += item.haveCount
            }else{
                otherCollection.add(item)
                if(item.haveCount > item.allCount){
                    isOver = true
                }
                counter += item.allCount
                curCounter += item.haveCount
            }

        }
        teCollection.forEach { item ->
            sortedCollection.add(item)
        }
        otherCollection.forEach { item ->
            sortedCollection.add(item)
        }
        _items.value = sortedCollection
        setCountOfCount(counter)
        setCurCountOfCount(curCounter)
        setCounterValidation(isOver)
        if(IsOverCounter.value == false && CurrentCountOfCount.value == CountOfCount.value){
            setFinishValidation(true)
        }else{
            setFinishValidation(false)
        }
    }

    fun getSelectedItem() : Int{
        var isCorrect = _selectedItem.value
        if (isCorrect != null)
            return isCorrect
        else
            return  0
    }
    fun setSelectedItem(selectedItemCount: Int){
        _selectedItem.value = selectedItemCount
    }
    fun setCellName(cellName: String){
        _currentCellName.value = cellName
    }
    fun updateCollection(incomeRepo: IncomeRepository, barcode: String?){
        var barcoded = Barcode("","","","","")
        viewModelScope.launch {
            var result : List<IncomeItem> = mutableListOf()
            withContext(Dispatchers.IO) {
                barcoded = incomeRepo.getBarcodeByName(if (barcode == null) "" else barcode)

            }
            withContext(Dispatchers.Main){
                if(barcoded != null) {
                    var counter = 0
                    _items.value?.forEach { item ->
                        item.isSelected = false//чтобы не было несколько edit text
                        if (item.catalogId == barcoded.catalogId) {
                            item.haveCount = item.haveCount + 1
                            //item.isSelected = true
                            //setSelectedItem(counter)
                        }
                        counter = counter + 1
                        result += item
                    }
                    updateItems(result)
                }
            }
        }
    }
    suspend fun loadItems (incomeRepo: IncomeRepository, sessionId: String) : List<IncomeItem>{
        var listOfGoods: List<Pair<Goods, Cell>> = listOf()
        listOfGoods = incomeRepo.getGoodsAndTheirCells(sessionId)


        var result : List<IncomeItem> = listOf()
        var previousCellId: String = ""
        var counter: Int = 0
        for (item in listOfGoods){
            var catalog = incomeRepo.getCatalogById(item.first.catalogId)
            if (isTE(item.second.name, incomeRepo)){
                var parentCell = incomeRepo.getCellById(item.second.parentCellId.toString())
                var isShown = if(parentCell.name.contains("IN")) true else false
                if(item.second.id != previousCellId){
                    previousCellId = item.second.id
                    result += IncomeItem(
                        name =  item.second.name,
                        TE = if(isShown) item.second.name else "",
                        catalogId = "",
                        goodsId = "",
                        allCount = item.first.amount,
                        haveCount = 0,
                        isExpandable = true,
                        isShown = isShown)
                    result += IncomeItem(
                        name =  catalog.name,
                        TE = if(isShown) item.second.name else "",
                        catalogId = catalog.id,
                        goodsId = item.first.id,
                        allCount = item.first.amount,
                        haveCount = 0,
                        isExpandable = false,
                        isShown = !isShown)
                }else{
                    result += IncomeItem(
                        name =  catalog.name,
                        TE = if(isShown) item.second.name else "",
                        catalogId = catalog.id,
                        goodsId = item.first.id,
                        allCount = item.first.amount,
                        haveCount = 0,
                        isExpandable = false,
                        isShown = !isShown)
                }
            }else{
                result += IncomeItem(
                    name =  catalog.name,
                    TE = currentCellName.value.toString(),
                    catalogId = catalog.id,
                    goodsId = item.first.id,
                    allCount = item.first.amount,
                    haveCount = 0,
                    isExpandable = false,
                    isShown = true)
            }
        }

        return  result
    }
    suspend fun finishSession(incomeRepo: IncomeRepository, sessionId: String){
        var session = incomeRepo.getIncomeSessionById(sessionId)

        prepareIncomeItem(incomeRepo, session, sessionId)
        updateSession(session, incomeRepo)
    }

    private suspend fun prepareIncomeItem(
        incomeRepo: IncomeRepository,
        session: SessionIncome,
        sessionId: String
    ) {
        items.value?.forEach { item ->

            if (item.haveCount == item.allCount && item.catalogId != "") {
                prepareEqualItem(incomeRepo, item, session, sessionId)
            }
            if (item.haveCount > item.allCount && item.catalogId != "") {
                prepareMoreItem(incomeRepo, item, session, sessionId)
            }
            if (item.haveCount < item.allCount && item.catalogId != "") {
                prepareLessItem(incomeRepo, item, session, sessionId)
            }
        }
    }

    private suspend fun updateSession(
        session: SessionIncome,
        incomeRepo: IncomeRepository
    ) {
        var sessionChange = ChangeFactory.create(
            entityId = session.id,
            supplierId = session.supplierId.toString(),
            operationType = OperationType.UpdateIncomeSession
        )

        incomeRepo.updateIncomeSessionAsync(
            session.copy(status = StatusType.Finished.ordinal, finishedAt = System.currentTimeMillis()),
            change = sessionChange
        )
    }

    private suspend fun prepareLessItem(
        incomeRepo: IncomeRepository,
        item: IncomeItem,
        session: SessionIncome,
        sessionId: String
    ) {
        var goods = incomeRepo.getGoodsById(item.goodsId)
        // если больше то
        // создать перемещение в Less
        var cellLess = incomeRepo.getCellByName("less")
        var diffMove = MovementFactory.create(
            cellFromId = goods.cellId,
            cellToId = cellLess.id,
            catalogId = item.catalogId,
            goodsId = goods.id,
            qty = (item.allCount - item.haveCount).toString(),
            operationType = OperationType.LessMovement,
            entityId = item.goodsId
        )
        var moreChange = ChangeFactory.create(
            entityId = diffMove.id,
            supplierId = session.supplierId.toString(),
            operationType = OperationType.IncomeMovement
        )

        incomeRepo.insertMovementAsync(diffMove, moreChange)
        // увеличить количество товара в основном goods
        // обновить статус goods
        var updateGoodsChange = ChangeFactory.create(
            entityId = item.goodsId,
            supplierId = session.supplierId.toString(),
            operationType = OperationType.UpdateGoods
        )
        incomeRepo.updateGoodsAsync(
            goods.copy(amount = item.haveCount, isAvailable = true),
            updateGoodsChange
        )

        // создать перемещение
        // если количество равно 0 то удалить goods
        if (item.haveCount == 0) {
            var removeChange = ChangeFactory.create(
                entityId = goods.id,
                supplierId = session.id,
                operationType = OperationType.DeleteGoods
            )
            incomeRepo.deleteGoodsAsync(goods = goods, change = removeChange)
        } else {
            var incomeCell = incomeRepo.getCellByName("income")
            var innerMovement = MovementFactory.create(
                cellFromId = incomeCell.id,
                cellToId = session.toCellId.toString(),
                catalogId = item.catalogId,
                goodsId = item.goodsId,
                qty = item.haveCount.toString(),
                operationType = OperationType.IncomeMovement,
                entityId = sessionId
            )
            var movementChange = ChangeFactory.create(
                entityId = innerMovement.id,
                supplierId = session.supplierId.toString(),
                operationType = OperationType.IncomeMovement
            )
            incomeRepo.insertMovementAsync(innerMovement, movementChange)
        }
    }

    private suspend fun prepareMoreItem(
        incomeRepo: IncomeRepository,
        item: IncomeItem,
        session: SessionIncome,
        sessionId: String
    ) {
        var goods = incomeRepo.getGoodsById(item.goodsId)
        // если больше то
        // создать перемещение в More
        var cellMore = incomeRepo.getCellByName("more")
        var diffMove = MovementFactory.create(
            cellFromId = cellMore.id,
            cellToId = goods.cellId,
            catalogId = item.catalogId,
            goodsId = goods.id,
            qty = (item.haveCount - item.allCount).toString(),
            operationType = OperationType.MoreMovement,
            entityId = item.goodsId
        )

        var moreChange = ChangeFactory.create(
            entityId = diffMove.id,
            supplierId = session.supplierId.toString(),
            operationType = OperationType.IncomeMovement
        )
        incomeRepo.insertMovementAsync(diffMove, moreChange)

        // увеличить количество товара в основном goods
        // обновить статус goods
        var updateGoodsChange = ChangeFactory.create(
            entityId = item.goodsId,
            supplierId = session.supplierId.toString(),
            operationType = OperationType.UpdateGoods
        )
        incomeRepo.updateGoodsAsync(
            goods.copy(amount = item.haveCount, isAvailable = true),
            updateGoodsChange
        )

        // создать перемещение
        var incomeCell = incomeRepo.getCellByName("income")
        var innerMovement = MovementFactory.create(
            cellFromId = incomeCell.id,
            cellToId = session.toCellId.toString(),
            catalogId = item.catalogId,
            goodsId = item.goodsId,
            qty = item.haveCount.toString(),
            operationType = OperationType.IncomeMovement,
            entityId = sessionId
        )

        var movementChange = ChangeFactory.create(
            entityId = innerMovement.id,
            supplierId = session.supplierId.toString(),
            operationType = OperationType.IncomeMovement
        )
        incomeRepo.insertMovementAsync(innerMovement, movementChange)
    }

    private suspend fun prepareEqualItem(
        incomeRepo: IncomeRepository,
        item: IncomeItem,
        session: SessionIncome,
        sessionId: String
    ) {
        var innerGoods = incomeRepo.getGoodsById(item.goodsId)
        var goodsChange = ChangeFactory.create(
            innerGoods.id,
            session.supplierId.toString(),
            OperationType.UpdateGoods)
        incomeRepo.updateGoodsAsync(innerGoods.copy(isAvailable = true, amount = innerGoods.amount), goodsChange)

        var incomeCell = incomeRepo.getCellByName("income")
        var innerMovement = MovementFactory.create(
            cellFromId = incomeCell.id,
            cellToId = session.toCellId.toString(),
            catalogId = innerGoods.catalogId,
            goodsId = innerGoods.id,
            qty = innerGoods.amount.toString(),
            operationType = OperationType.IncomeMovement,
            entityId = sessionId
        )

        var movementChange = ChangeFactory.create(
            entityId = innerMovement.id,
            supplierId = session.supplierId.toString(),
            operationType = OperationType.IncomeMovement
        )

        incomeRepo.insertMovementAsync(innerMovement, movementChange)
    }

    fun setSelection(checked: Boolean) {
        var list: MutableList<IncomeItem> = mutableListOf()
        if (checked){
            setCurCountOfCount(_countOfCount.value ?: 0)
            items.value?.forEach { item -> list.add(item.copy(haveCount = if(item.allCount != 0) item.allCount else item.haveCount ))}
            updateItems(list)
        }else{
            setCurCountOfCount(0)
            items.value?.forEach { item -> list.add(item.copy(haveCount = 0))}
            updateItems(list)
        }
    }
    suspend private fun isTE(cell: String, incomeRepo: IncomeRepository): Boolean {

        val cells = incomeRepo.getTETypes()

        return cells.any { cellType ->
            val mask = cellType.mask ?: return@any false

            mask.length == cell.length &&
                    mask.indices.all { i ->
                        when (mask[i]) {
                            '#' -> cell[i].isDigit()
                            else -> mask[i] == cell[i]
                        }
                    }
        }
    }
}