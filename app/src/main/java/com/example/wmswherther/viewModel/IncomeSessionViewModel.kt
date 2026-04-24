package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.data.db.Dao
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.SessionIncome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class IncomeSessionViewModel : ViewModel() {
    private val _items = MutableLiveData<List<IncomeItem>>()
    private val _selectedItem = MutableLiveData<Int>()
    private  val _currentCellName = MutableLiveData<String>()


    private  val _currentLineCount = MutableLiveData<Int>()
    private  val _lineCount = MutableLiveData<Int>()
    private  val _currentCountOfCount = MutableLiveData<Int>()
    private  val _countOfCount = MutableLiveData<Int>()
    private  val _isOverCounter = MutableLiveData<Boolean>()
    private  val _finish = MutableLiveData<Boolean>()

    val items: LiveData<List<IncomeItem>> get() = _items
    val selectedItem: LiveData<Int> get() = _selectedItem
    val currentCellName: LiveData<String> get() = _currentCellName
    val CurrentLineCount: LiveData<Int> get() = _currentLineCount
    val LineCount: LiveData<Int> get() = _lineCount
    val CurrentCountOfCount: LiveData<Int> get() = _currentCountOfCount
    val CountOfCount: LiveData<Int> get() = _countOfCount
    val IsOverCounter: LiveData<Boolean> get() = _isOverCounter
    val IsFinish: LiveData<Boolean> get() = _finish

    val stack: ArrayDeque<List<IncomeItem>> = ArrayDeque()
    val cellStack: ArrayDeque<String> = ArrayDeque()

    fun setCounterValidation(isOverFlag: Boolean){
        _isOverCounter.value = isOverFlag
    }
    fun getCounterValidation() : Boolean?{
        return _isOverCounter.value
    }
    fun setFinishValidation(isOverFlag: Boolean){
        _finish.value = isOverFlag
    }
    fun getFinishValidation() : Boolean?{
        return _finish.value
    }
    fun setCurLineCount(count: Int){
        _currentLineCount.value = count
    }
    fun getCurLineCount() : Int{
        return CurrentLineCount.value!!.toInt()
    }
    fun setLineCount(count: Int){
        _lineCount.value = count
    }
    fun getLineCount() : Int{
        return LineCount.value!!.toInt()
    }
    fun setCurCountOfCount(count: Int){
        _currentCountOfCount.value = count
    }
    fun getCurCountOfCount() : Int{
        return CurrentCountOfCount.value!!.toInt()
    }
    fun setCountOfCount(count: Int){
        _countOfCount.value = count
    }
    fun getCountOfCount() : Int{
        return _countOfCount.value!!.toInt()
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
    fun updateCollection(db : MainDB, barcode: String?){
        var barcoded = Barcode("","","","","")
        viewModelScope.launch {
            var result : List<IncomeItem> = mutableListOf()
            withContext(Dispatchers.IO) {
                barcoded = getBarcode(db, if (barcode == null) "" else barcode)

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
    suspend fun loadItems (db : MainDB, sessionId: String) : List<IncomeItem>{
        var dao = db.getDao()

        var listOfGoods: List<Pair<Goods, Cell>> = listOf()
        listOfGoods = dao.getAllIncomeItem()
            .filter { item -> item.sessionId == sessionId}
            .map { item ->  dao.getGoodsById(item.goodsId) }
            .map { inner -> Pair(inner, dao.getCellById(inner.cellId)) }

        var result : List<IncomeItem> = listOf()
        var previousCellId: String = ""
        var counter: Int = 0
        for (item in listOfGoods){
            var catalog = dao.getCatalogById(item.first.catalogId)
            if (isTE(item.second.name, dao)){
                var parentCell = dao.getCellById(item.second.parentCellId.toString())
                var isShown = if(parentCell.name.contains("IN")) true else false
                if(item.second.id != previousCellId){
                    previousCellId = item.second.id
                    result += IncomeItem(
                        name =  item.second.name,
                        TE = if(isShown) item.second.name else "",
                        catalogId = "",
                        goodsId = item.first.id,
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
    suspend fun getBarcode(db : MainDB, barcode: String) : Barcode {
        return db.getDao().getBarcodeByName(barcode)
    }
    suspend fun finishSession(db: MainDB, sessionId: String){
        var dao = db.getDao()
        var session = dao.getIncomeSessionById(sessionId)

        items.value?.forEach { item ->
            if(item.haveCount == item.allCount){
                prepareEqualItem(dao, item, session, sessionId)
            }
            if(item.haveCount > item.allCount){
                prepareMoreItem(dao, item, session, sessionId)
            }
            if(item.haveCount < item.allCount){
                prepareLessItem(dao, item, session, sessionId)
            }
        }
        updateSession(session, dao)
    }

    private suspend fun updateSession(
        session: SessionIncome,
        dao: Dao
    ) {
        var sessionChange = Change(
            id = UUID.randomUUID().toString(),
            entityId = session.id,
            operationType = OperationType.UpdateIncomeSession.ordinal,
            status = StatusType.Finished.ordinal,
            supplierId = session.supplierId,
            other = null
        )
        dao.updateIncomeSessionAsync(
            session.copy(
                status = StatusType.Finished.ordinal,
                finishedAt = System.currentTimeMillis()
            ), change = sessionChange
        )
    }

    private suspend fun prepareLessItem(
        dao: Dao,
        item: IncomeItem,
        session: SessionIncome,
        sessionId: String
    ) {
        var goods = dao.getGoodsById(item.goodsId)
        // если больше то
        // создать перемещение в Less
        var cellLess = dao.getCellByName("less")
        var diffMove = Movement(
            id = UUID.randomUUID().toString(),
            cellFromId = goods.cellId,
            cellToId = cellLess.id,
            catalogId = item.catalogId,
            goodsId = goods.id,
            qty = (item.allCount - item.haveCount).toString(),
            userId = 1,
            executedAt = System.currentTimeMillis(),
            operationType = OperationType.LessMovement.ordinal,
            entityId = item.goodsId
        )
        var moreChange = Change(
            id = UUID.randomUUID().toString(),
            entityId = diffMove.id,
            operationType = OperationType.IncomeMovement.ordinal,
            status = StatusType.Work.ordinal,
            supplierId = session.supplierId,
            other = null
        )
        dao.insertMovementSync(diffMove, moreChange)
        // увеличить количество товара в основном goods
        // обновить статус goods
        var updateGoodsChange = Change(
            id = UUID.randomUUID().toString(),
            entityId = item.goodsId,
            operationType = OperationType.UpdateGoods.ordinal,
            status = StatusType.Created.ordinal,
            supplierId = session.supplierId,
            other = null
        )
        dao.updateGoodsAsync(
            goods.copy(amount = item.haveCount, isAvailable = true),
            updateGoodsChange
        )

        // создать перемещение
        var incomeCell = dao.getCellByName("income")
        var innerMovement = Movement(
            id = UUID.randomUUID().toString(),
            cellFromId = incomeCell.id,
            cellToId = session.toCellId.toString(),
            catalogId = item.catalogId,
            goodsId = item.goodsId,
            qty = item.haveCount.toString(),
            userId = 1,
            executedAt = System.currentTimeMillis(),
            operationType = OperationType.IncomeMovement.ordinal,
            entityId = sessionId
        )
        var movementChange = Change(
            id = UUID.randomUUID().toString(),
            entityId = innerMovement.id,
            operationType = OperationType.IncomeMovement.ordinal,
            status = StatusType.Work.ordinal,
            supplierId = session.supplierId,
            other = null
        )
        dao.insertMovementSync(innerMovement, movementChange)
    }

    private suspend fun prepareMoreItem(
        dao: Dao,
        item: IncomeItem,
        session: SessionIncome,
        sessionId: String
    ) {
        var goods = dao.getGoodsById(item.goodsId)
        // если больше то
        // создать перемещение в More
        var cellMore = dao.getCellByName("more")
        var diffMove = Movement(
            id = UUID.randomUUID().toString(),
            cellFromId = cellMore.id,
            cellToId = goods.cellId,
            catalogId = item.catalogId,
            goodsId = goods.id,
            qty = (item.haveCount - item.allCount).toString(),
            userId = 1,
            executedAt = System.currentTimeMillis(),
            operationType = OperationType.MoreMovement.ordinal,
            entityId = item.goodsId
        )
        var moreChange = Change(
            id = UUID.randomUUID().toString(),
            entityId = diffMove.id,
            operationType = OperationType.IncomeMovement.ordinal,
            status = StatusType.Work.ordinal,
            supplierId = session.supplierId,
            other = null
        )
        dao.insertMovementSync(diffMove, moreChange)
        // увеличить количество товара в основном goods
        // обновить статус goods
        var updateGoodsChange = Change(
            id = UUID.randomUUID().toString(),
            entityId = item.goodsId,
            operationType = OperationType.UpdateGoods.ordinal,
            status = StatusType.Created.ordinal,
            supplierId = session.supplierId,
            other = null
        )
        dao.updateGoodsAsync(
            goods.copy(amount = item.haveCount, isAvailable = true),
            updateGoodsChange
        )

        // создать перемещение
        var incomeCell = dao.getCellByName("income")
        var innerMovement = Movement(
            id = UUID.randomUUID().toString(),
            cellFromId = incomeCell.id,
            cellToId = session.toCellId.toString(),
            catalogId = item.catalogId,
            goodsId = item.goodsId,
            qty = item.haveCount.toString(),
            userId = 1,
            executedAt = System.currentTimeMillis(),
            operationType = OperationType.IncomeMovement.ordinal,
            entityId = sessionId
        )
        var movementChange = Change(
            id = UUID.randomUUID().toString(),
            entityId = innerMovement.id,
            operationType = OperationType.IncomeMovement.ordinal,
            status = StatusType.Work.ordinal,
            supplierId = session.supplierId,
            other = null
        )
        dao.insertMovementSync(innerMovement, movementChange)
    }

    private suspend fun prepareEqualItem(
        dao: Dao,
        item: IncomeItem,
        session: SessionIncome,
        sessionId: String
    ) {
        var innerGoods = dao.getGoodsById(item.goodsId)
        var goodsChange = Change(
            id = UUID.randomUUID().toString(),
            entityId = innerGoods.id,
            operationType = OperationType.UpdateGoods.ordinal,
            status = StatusType.Work.ordinal,
            supplierId = session.supplierId,
            other = null
        )
        dao.updateGoodsAsync(innerGoods.copy(isAvailable = true), goodsChange)

        var incomeCell = dao.getCellByName("income")
        var innerMovement = Movement(
            id = UUID.randomUUID().toString(),
            cellFromId = incomeCell.id,
            cellToId = session.toCellId.toString(),
            catalogId = innerGoods.catalogId,
            goodsId = innerGoods.id,
            qty = innerGoods.amount.toString(),
            userId = 1,
            executedAt = System.currentTimeMillis(),
            operationType = OperationType.IncomeMovement.ordinal,
            entityId = sessionId
        )
        var movementChange = Change(
            id = UUID.randomUUID().toString(),
            entityId = innerMovement.id,
            operationType = OperationType.IncomeMovement.ordinal,
            status = StatusType.Work.ordinal,
            supplierId = session.supplierId,
            other = null
        )
        dao.insertMovementSync(innerMovement, movementChange)
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
    suspend private fun isTE(cell: String, dao: Dao): Boolean {
        val cells = dao.getCellTypes().filter { cellType -> cellType.type == "BoxTE" }

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