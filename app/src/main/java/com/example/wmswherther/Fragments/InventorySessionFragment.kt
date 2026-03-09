package com.example.wmswherther.Fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.FragmentInventorySessionBinding
import com.example.wmswherther.Adapters.InventorySessionAdapter
import com.example.wmswherther.Classes.InventorySessionItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.viewModel.InventorySessionViewModel
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class InventorySessionFragment: Fragment() {

    private var _binding: FragmentInventorySessionBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentIncome")
    private val viewModel: MainViewModel by activityViewModels()
    private val localViewModel: InventorySessionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val localViewModel = ViewModelProvider(requireActivity()).get(InventorySessionViewModel::class)
        _binding = FragmentInventorySessionBinding.inflate(inflater, container, false)

        var recyclerView: RecyclerView = binding.rwInventorySessionList
        var adapterCollection = mutableListOf<InventorySessionItem>()
        if( localViewModel.items.value != null){
            adapterCollection = localViewModel.items.value as MutableList<InventorySessionItem>
        }
        val adapter = InventorySessionAdapter(adapterCollection, recyclerView, localViewModel, requireActivity(), viewModel)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter
        val db = MainDB.getDB(requireActivity())

        initOrder(localViewModel)

        binding.btnFinish.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    localViewModel.finishSession(db.getDao())
                }
            }
        }

        localViewModel.items.observe(viewLifecycleOwner,{ items ->
            var curLineCounter = 0
            var lineCounter = 0
            var curCounterOfCounter = 0
            var counterOfCounter = 0
            items.forEach { item ->
                if(item.catalogId != ""){
                    lineCounter += 1
                    if(item.haveCount == item.allCount){
                        curLineCounter += 1
                    }
                    curCounterOfCounter += item.haveCount
                    counterOfCounter += item.haveCount
                }
            }
            adapter.updateCollection(items, localViewModel.getSelectedItem())
            recyclerView.smoothScrollToPosition(localViewModel.getSelectedItem())
        })
        viewModel.Barcode.observe(viewLifecycleOwner, { barcode ->
            if(barcode != "" && viewModel.uiState.value is UiState.InventorySessionMenu) {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        var inventorySession = viewModel.uiState.value as UiState.InventorySessionMenu
                        var db = MainDB.getDB(requireActivity())
                        var dao = db.getDao()
                        if (isPickerCell(barcode, dao)) {
                            if (inventorySession.isSupplierModeActive) {  // Проверить isOrderMode Active то ничего делать т.к. это заявка(особенность)
                                loadNewCell(localViewModel, barcode, db)
                            }
                        } else {
                            if(isTE(barcode, dao)){
                                prepareTE(inventorySession, dao, localViewModel, barcode)
                            }else{
                                prepareBarcode(dao, barcode, localViewModel)
                            }
                        }
                    }

                }
            }else{
                localViewModel.updateItems(listOf())
            }

        })
        localViewModel.CurrentCountOfCount.observe(viewLifecycleOwner, {counter ->
            var itemsInVm : List<InventorySessionItem> = listOf()
            var cellName : String = ""
            itemsInVm = localViewModel.items.value ?: listOf()
            cellName = localViewModel.currentCellName.value.toString()
            lifecycleScope.launch {
                if(isTE(cellName, dao = db.getDao())) {
                    var coll: List<InventorySessionItem> = listOf()
                    withContext(Dispatchers.IO) {
                        itemsInVm.forEach { item ->
                            if (item.name == cellName) {
                                coll += item.copy(haveCount = if (localViewModel.CountOfCount.value!! <= localViewModel.CurrentCountOfCount.value!!) 1 else 0)
                            } else {
                                coll += item
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        binding.tvLineCounter.text = "${counter.toString()}  /"
                        if (coll != localViewModel.items.value) {
                            localViewModel.updateItems(coll)
                        }
                    }
                }else{
                    binding.tvLineCounter.text = "${counter.toString()}  /"
                }
            }

            // Если количество товара добило счетчик и ячейка текущая является те то
            // Изменить счетчик для те
            // Иначе сбросить счетчик на 0 для te
        })
        localViewModel.CountOfCount.observe(viewLifecycleOwner, {counter ->
            binding.tvCounterCounter.text = counter.toString()
        })
        localViewModel.IsOverCounter.observe(viewLifecycleOwner, {flag ->
            if(flag) {
                binding.tvCounterCounter.setTextColor(ContextCompat.getColor(requireActivity(), R.color.regularRed))
                binding.tvLineCounter.setTextColor(ContextCompat.getColor(requireActivity(), R.color.regularRed))
                //binding.tvLineCounter.setTextColor(R.color.regularRed.toInt())
            }else{
                binding.tvCounterCounter.setTextColor(Color.BLACK)
                binding.tvLineCounter.setTextColor(Color.BLACK)
            }
        })
        localViewModel.IsFinish.observe(viewLifecycleOwner, {flag ->
            if(flag) {
                binding.rwInventorySessionList.setPadding(20, 0, 20, 80)
                binding.btnFinish.visibility = View.VISIBLE
            }else{
                binding.rwInventorySessionList.setPadding(20, 0, 20, 40)
                binding.btnFinish.visibility = View.GONE
            }
        })
        localViewModel.currentCellName.observe(viewLifecycleOwner, { cellName ->
            binding.tvCellName.text = cellName
        })
        viewModel.IsActiveSession.observe(viewLifecycleOwner, {flag ->
            if (flag){
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        localViewModel.finishSession(db.getDao())
                    }
                }
                viewModel.switchActivityOfInventorySession()
            }
        })
        viewModel.IsSelectedInventoryList.observe(viewLifecycleOwner, {flag->
            localViewModel.setSelection(flag)
        })




        return  binding.root
    }
    //TODO refactor func
    private suspend fun prepareBarcode(
        dao: Dao,
        barcode: String,
        localViewModel: InventorySessionViewModel
    ) {
        // поиск товара и изменение его количества
        // найти barcode
        var searchBarcode: Barcode = dao.getBarcodeByName(barcode)
        var newCol: List<InventorySessionItem> = listOf()
        if (searchBarcode != null) {
            var isHave = false // если вообще не было в коллекции такого элемента то добавить новый(с условим того что он есть каталоге)

            localViewModel.items.value?.forEach { inner ->
                if (inner.catalogId == searchBarcode.catalogId && localViewModel.currentCellName.value.toString() == inner.TE) {
                    newCol += inner.copy(haveCount = inner.haveCount + 1)
                    isHave = true
                } else {
                    newCol += inner
                }
            }
            if(!isHave){
                var catalogItem = dao.getCatalogById(searchBarcode.catalogId)
                newCol += InventorySessionItem(
                    name = catalogItem.name,
                    TE = localViewModel.currentCellName.value.toString(),
                    catalogId = searchBarcode.catalogId,
                    haveCount = 1,
                    allCount = 0,
                    teCount = 0,
                    isSelected = false,
                    isExpanded = false,
                    isExpandable = false,
                    isShown = true
                )
            }
        }else{
            var isHave = false // если вообще не было в коллекции такого элемента то добавить новый
            //обработать случай отсутствия в бд
            localViewModel.items.value?.forEach { inner ->
                if (inner.name == barcode) {
                    newCol += inner.copy(haveCount = inner.haveCount + 1)
                    isHave = true
                } else {
                    newCol += inner
                }
            }
            if(!isHave) {
                newCol += InventorySessionItem(
                    name = barcode,
                    TE = localViewModel.currentCellName.value.toString(),
                    catalogId = "${-1}",
                    haveCount = 1,
                    allCount = 0,
                    teCount = 0,
                    isSelected = false,
                    isExpanded = false,
                    isExpandable = false,
                    isShown = true
                )
            }
            localViewModel.updateItemsAsync(newCol)
            }
    }

    private suspend fun prepareTE(
        inventorySession: UiState.InventorySessionMenu,
        dao: Dao,
        localViewModel: InventorySessionViewModel,
        barcode: String
    ) {
        var curCell = dao.getCellByName(localViewModel.currentCellName.value.toString())
        if(localViewModel.currentCellName.value != barcode) {
            if (inventorySession.isTEIsCell) {//TODO сделать счетчик для те 0/1
                lifecycleScope.launch {
                    var list: MutableList<InventorySessionItem> = mutableListOf()
                    var value = localViewModel.items.value!!.toList()

                    withContext(Dispatchers.IO) {
                        localViewModel.items.value?.forEach { elem ->
                            if (elem.name == barcode) {
                                list.add(elem.copy(isExpanded = true))
                            } else if (elem.TE == barcode) {
                                list.add(elem.copy(isShown = true))
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        localViewModel.stack.addLast(value)
                        localViewModel.setCellName(barcode)
                        localViewModel.cellStack.addLast(localViewModel.currentCellName.value.toString())
                        localViewModel.updateItems(list.toList())
                    }
                }
            }
            // либо выделить все элементы
            else {
                var newCol: List<InventorySessionItem> = listOf()
                var te = dao.getAllCells()
                    .firstOrNull { inner -> inner.parentCellId == curCell.id && inner.name == barcode }
                if (te != null) {
                    // Загрузить в текущию коллекцию элементы у которых те равна отсканированной те

                    localViewModel.items.value?.forEach { item ->
                        if (item.TE == te.name && !item.isExpanded) {
                            newCol += item.copy(haveCount = item.allCount)
                        } else {
                            newCol += item
                        }
                    }

                } else {
                    localViewModel.items.value?.forEach { item ->
                        newCol += item
                    }
                    newCol += InventorySessionItem(
                        name = barcode,
                        TE = localViewModel.currentCellName.value.toString(),
                        catalogId = "${-1}",
                        haveCount = 1,
                        allCount = 0,
                        teCount = 0,
                        isSelected = false,
                        isExpanded = false,
                        isExpandable = true,
                        isShown = true
                    )
                }
                localViewModel.updateItemsAsync(newCol ?: listOf())
            }
        }
    }

    suspend private fun loadNewCell(
        localViewModel: InventorySessionViewModel,
        barcode: String,
        db: MainDB
    ) {
        // если было что то отсканированно в текущий момент то спросить уверен ли
        lifecycleScope.launch {
            var inventoryItems : List<InventorySessionItem> = listOf()
            withContext(Dispatchers.IO){
                var cell = getCell(db, barcode)
                inventoryItems = localViewModel.loadItems(db, cell)
            }
            withContext(Dispatchers.Main){
                if (localViewModel.CurrentCountOfCount.value != 0) {
                    val dialog = AlertDialog.Builder(requireActivity())
                        .setTitle("Смена ячейки")
                        .setMessage("Есть отсканированный товар, точно хотите сменить ячейку?")
                        .setPositiveButton("Да") { _, _ ->
                            localViewModel.setCellName(barcode)
                            localViewModel.cellStack.addLast(barcode)
                            localViewModel.updateItems(inventoryItems)
                        }
                        .setNegativeButton("Нет", null)
                        .create()
                    dialog.show()
                }else {
                    // Иначе сменить активную ячейку
                    localViewModel.setCellName(barcode)
                    localViewModel.cellStack.addLast(barcode)
                    localViewModel.updateItems(inventoryItems)
                }
            }
        }

    }

    private suspend fun getCell(
        db: MainDB,
        barcode: String
    ) : Cell {
        var dao = db.getDao()
        var cell = dao.getCellByName(barcode)
        if(cell == null){
            if(isPickerCell(barcode, dao)) {
                var cellType = dao.getCellTypeByName("Picker").first()
                val uuid = UUID.randomUUID()
                var newCell = Cell(
                    id = uuid.toString(),
                    typeCellId = cellType.id,
                    parentCellId = dao.getCellByName(localViewModel.currentCellName.value.toString()).id,
                    name = barcode
                )
                db.getDao().insertCell(newCell)
                return newCell
            }
        }

        return cell

    }

    private fun initOrder(localViewModel: InventorySessionViewModel) {
        if (viewModel.uiState.value is UiState.InventorySessionMenu) {
            var state = viewModel.uiState.value as UiState.InventorySessionMenu
            var cell: Cell
            if (!state.isSupplierModeActive) {//
                lifecycleScope.launch {
                    var data: List<InventorySessionItem> = listOf()
                    withContext(Dispatchers.Main) {
                        var dao = MainDB.getDB(requireActivity()).getDao()
                        var session = dao.getInventorySessionById(state.sessionId)
                        cell = dao.getCellById(session.cellId.toString())
                        localViewModel.cellStack.addLast(cell.name)
                        localViewModel.setCellName(cell.name)
                    }
                    withContext(Dispatchers.IO) {
                        data = localViewModel.loadItems(MainDB.getDB(requireActivity()), cell)
                    }
                    withContext(Dispatchers.Main) {
                        localViewModel.updateItems(data)
                        localViewModel.setSelectedItem(0)
                    }
                }
            }
        }
    }//вызывается когда в inventoryMenu выбран режим заказы и выбран определенный заказ
    private fun loadCell(localViewModel: InventorySessionViewModel, cell: Cell) {
        if (viewModel.uiState.value is UiState.InventorySessionMenu) {
            var state = viewModel.uiState.value as UiState.InventorySessionMenu
            var cell: Cell
            if (!state.isSupplierModeActive) {//
                lifecycleScope.launch {
                    var data: List<InventorySessionItem> = listOf()
                    withContext(Dispatchers.Main) {
                        var dao = MainDB.getDB(requireActivity()).getDao()
                        var session = dao.getInventorySessionById(state.sessionId)
                        cell = dao.getCellById(session.cellId.toString())
                        localViewModel.cellStack.addLast(cell.name)
                        localViewModel.setCellName(cell.name)
                    }
                    withContext(Dispatchers.IO) {
                        data = localViewModel.loadItems(MainDB.getDB(requireActivity()), cell)
                    }
                    withContext(Dispatchers.Main) {
                        localViewModel.updateItems(data)
                        localViewModel.setSelectedItem(0)
                    }
                }
            }
        }
    }//вызывается когда в inventoryMenu выбран режим заказы и выбран определенный заказ
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
    suspend private fun isPickerCell(cell: String, dao: Dao): Boolean {
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
}