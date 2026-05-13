package com.example.wmswherther.Fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wmsRemote.R
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.FragmentIncomeSessionBinding
import com.example.wmswherther.Adapters.IncomeSessionAdapter
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Repositories.AssemblyRepository
import com.example.wmswherther.data.db.Repositories.IncomeRepository
import com.example.wmswherther.data.db.Repositories.InventoryRepository
import com.example.wmswherther.data.db.Repositories.MoveeRepository
import com.example.wmswherther.data.db.SyncWorker
import com.example.wmswherther.data.factory.IncomeItemFactory
import com.example.wmswherther.viewModel.IncomeSessionViewModel
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
object ServiceLocator {
    var incomeRepository: IncomeRepository? = null
    var moveRepository: MoveeRepository? = null
    var inventoryRepository: InventoryRepository? = null
    var assemblyRepository: AssemblyRepository?= null
}
class IncomeSessionFragment : Fragment() {

        private var _binding: FragmentIncomeSessionBinding? = null
        private val binding
            get() = _binding ?: throw IllegalStateException("Binding for FragmentIncome")
        private val viewModel: MainViewModel by activityViewModels()
        private val localViewModel: IncomeSessionViewModel by activityViewModels()

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = FragmentIncomeSessionBinding.inflate(inflater, container, false)
            var recyclerView: RecyclerView = binding.rwIncomeSessionList
            var adapterCollection = mutableListOf<IncomeItem>()
            if( localViewModel.items.value != null){
                adapterCollection = localViewModel.items.value as MutableList<IncomeItem>
            }
            val adapter = IncomeSessionAdapter(adapterCollection, recyclerView, localViewModel, requireActivity(), viewModel)
            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
            recyclerView.adapter = adapter
            val sessionId = arguments?.getString("id")
            val incomeRepo = ServiceLocator.incomeRepository
                ?: IncomeRepository(MainDB.getDB(requireActivity()).getDao())



            viewModel.Barcode.observe(viewLifecycleOwner, { barcode ->
                prepareBarcodeAndUpdateUI(barcode, incomeRepo)
            })
            viewModel.TE.observe(viewLifecycleOwner, { TE ->
                var innerIncomeItems: MutableList<IncomeItem> = mutableListOf()

                if(TE != "") {
                    appendTEOrCreateNewTE(TE, innerIncomeItems)
                    extractTEItemsFromMainCollection(TE, innerIncomeItems)
                    appendRemainigItemWithSameTE(TE, innerIncomeItems)
                    var result: MutableList<IncomeItem> = removeDuplication(innerIncomeItems)
                    resetTeCountAndAppendRemainingItemsToResult(TE, result)
                }
            })
            viewModel.IsCloseTE.observe(viewLifecycleOwner, { isClosed ->
                resetTeCounterToZeroAndSwitchTeButton(isClosed)
            })
            viewModel.IsSelectedIncomeList.observe(viewLifecycleOwner, {flag : Boolean->
                localViewModel.setSelection(flag)
            })
            viewModel.IsFinishIncomeSession.observe(viewLifecycleOwner, { status ->
                if(status) {
                    if(localViewModel.CurrentCountOfCount.value != localViewModel.CountOfCount.value 
                        || localViewModel.IsOverCounter.value == true){
                        val view = LayoutInflater.from(activity)
                            .inflate(R.layout.dialog, null)
                        val btnYes = view.findViewById<Button>(R.id.btnYes)
                        val btnNo = view.findViewById<Button>(R.id.btnNo)

                        var dialog = AlertDialog.Builder(requireActivity())
                            .setView(view)
                            .create()

                        btnYes.setOnClickListener {
                            finishSessionAndReturnToPreviousFragment(incomeRepo, sessionId)
                        }
                        btnNo.setOnClickListener {
                            dialog.dismiss()
                        }
                        }


                    }else{
                        finishSessionAndReturnToPreviousFragment(incomeRepo, sessionId)

                    }

            })
            localViewModel.CurrentCountOfCount.observe(viewLifecycleOwner, {counter ->
                binding.tvLineCounter.text = "${counter.toString()}  /"
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
                showFinishButton(flag)
            })
            localViewModel.items.observe(viewLifecycleOwner,{ items ->
                adapter.updateCollection(items, localViewModel.getSelectedItem())
                //recyclerView.smoothScrollToPosition(localViewModel.getSelectedItem())
            })
            localViewModel.currentCellName.observe(viewLifecycleOwner, { cellName ->
                binding.tvCellName.text = cellName
            })
            //todo set red color or black
            

            binding.btnFinish.setOnClickListener {
                finishSessionAndReturnToPreviousFragment(incomeRepo, sessionId)

            }

            initSession(incomeRepo, sessionId)
            
            return  binding.root
        }

    private fun prepareBarcodeAndUpdateUI(
        barcode: String,
        incomeRepo: IncomeRepository
    ) {
        if (barcode != "" && viewModel.uiState.value is UiState.IncomeSessionMenu) {
            lifecycleScope.launch {
                var newItems: MutableList<IncomeItem> = mutableListOf()
                var bar = incomeRepo.getBarcodeByName(barcode)
                if (bar != null && bar is Barcode) {
                    var isAdded = false
                    withContext(Dispatchers.IO) {
                        isAdded = increaseCounterInMainListForBarcode(bar, isAdded, newItems)
                        createNewItemIfNotFoundInMainList(isAdded, incomeRepo, bar, newItems)
                    }
                    withContext(Dispatchers.Main) {
                        updateUiAndReturnFocus(newItems)
                    }
                }
            }
        }
    }

    private fun updateUiAndReturnFocus(newItems: List<IncomeItem>): Boolean? {
        localViewModel.updateItems(newItems)
        var binding = viewModel.getMainBinding()
        return if (viewModel.IsScanningActive.value == true) {
            binding?.etIncomeBarcode?.requestFocus()

        } else {
            binding?.etIncomeBarcodeScan?.requestFocus()
        }
    }

    private suspend fun createNewItemIfNotFoundInMainList(
        isAdded: Boolean,
        incomeRepo: IncomeRepository,
        bar: Barcode,
        newItems: MutableList<IncomeItem>
    ) {
        if (!isAdded) {
            var catalog = incomeRepo.getCatalogById(bar.catalogId)
            if (catalog != null) {
                newItems += IncomeItemFactory.createNewGoods(
                    name = catalog.name,
                    catalogId = catalog.id,
                    parentCellId = incomeRepo.getCellByName(localViewModel.currentCellName.value.toString()).id,
                    parentCellName = localViewModel.currentCellName.value.toString(),
                    supplierId = catalog.supplierId,
                    allCount = 0,
                    teCount = if (viewModel.IsIncomeSessionTEModeActive.value == true) 1 else 0
                )
            }
        }
    }

    private fun increaseCounterInMainListForBarcode(
        bar: Barcode,
        isAdded: Boolean,
        newItems: MutableList<IncomeItem>
    ): Boolean {
        var isAdded1 = isAdded
        localViewModel.items.value?.forEach { item ->
            var teCount = item.teCount
            if ((viewModel.uiState.value as UiState.IncomeSessionMenu).isTEModeActive) {
                teCount = teCount + 1
            }
            if (item is IncomeItem.GoodsItem &&
                item.catalogId == bar.catalogId && localViewModel.currentCellName.value.toString() == item.parentCellName) {
                isAdded1 = true
                newItems += IncomeItemFactory.copyGoodsPlusOne(item)
            }else if (item is IncomeItem.NewGoodsItem &&
                item.catalogId == bar.catalogId && localViewModel.currentCellName.value.toString() == item.parentCellName){
                isAdded1 = true
                newItems += IncomeItemFactory.copyNewGoodsPlusOne(item)
            }
            else {
                newItems += item
            }

        }
        return isAdded1
    }

    private fun appendRemainigItemWithSameTE(
        TE: String?,
        innerIncomeItems: MutableList<IncomeItem>
    ) {
        localViewModel.items.value?.forEach { item ->//TODO какая то хуета тут
            if (item.parentCellName == TE && item.getName() != item.parentCellName)//чтобы повторно не добавлять те, которая была в начале обработана. Добивание коллекции
            {
                innerIncomeItems.add(item)
            }
        }//надо перебрать имеющиеся элементы в новой коллекции и если есть дубликаты сложить их
        innerIncomeItems.forEach { item ->
            if (!item.isExpandable) {

            }
        }
    }



    private fun removeDuplication(innerIncomeItems: MutableList<IncomeItem>): MutableList<IncomeItem> {
        var result: MutableList<IncomeItem> = mutableListOf()

        result += innerIncomeItems.first()//группировка по catalogId чтобы в последующем сложить дубликаты
        innerIncomeItems.removeFirst()
        var goodsItems : List<IncomeItem.GoodsItem> = innerIncomeItems.filterIsInstance<IncomeItem.GoodsItem>()
        var newGoodsItems : List<IncomeItem.NewGoodsItem> = innerIncomeItems.filterIsInstance<IncomeItem.NewGoodsItem>()
        goodsItems
            .groupBy { it.catalogId }
            .map { (id, group) ->
                IncomeItemFactory.createGroupingGoods(
                    goods = group.first(),
                    haveCount = group.sumOf { it.haveCount },
                    allCount = group.sumOf { it.allCount },
                    teCount = group.sumOf { it.teCount }
                )

            }.forEach { item -> result += item }

        newGoodsItems
            .groupBy { it.catalogId }
            .map { (id, group) ->
                IncomeItemFactory.createGroupingNewGoods(
                    goods = group.first(),
                    haveCount = group.sumOf { it.haveCount },
                    allCount = group.sumOf { it.allCount },
                    teCount = group.sumOf { it.teCount }
                )

            }.forEach { item -> result += item }
        return result
    }

    private fun resetTeCountAndAppendRemainingItemsToResult(
        TE: String?,
        result: MutableList<IncomeItem>
    ) {
        localViewModel.items.value?.forEach { item ->
            if (item.parentCellName != TE && !(item.haveCount == 0 && item.allCount == 0)) {
                item.teCount = 0
                result.add(item)
            }
        }
        localViewModel.updateItems(result)
    }

    private fun extractTEItemsFromMainCollection(
        TE: String,
        innerIncomeItems: MutableList<IncomeItem>
    ) {
        localViewModel.items.value?.forEach { item ->
            if (item.teCount != 0) {
                if (item.haveCount < item.allCount) {
                    //То создать новый элемент, в старом уменьшить have и te
                    var newItem = IncomeItemFactory.copyGoodsOrNewGoods(
                        goods = item,
                        parentCellName = TE,
                        haveCount = item.teCount,
                        allCount = item.teCount
                    )
                    innerIncomeItems.add(newItem)
                    item.allCount = item.allCount - item.teCount
                    item.haveCount = item.haveCount - item.teCount

                } else if (item.haveCount == item.allCount) {
                    if ((item.haveCount - item.teCount) == 0) {
                        item.parentCellName = TE
                        item.isShown = false
                        item.teCount = 0
                    } else {
                        var newItem = IncomeItemFactory.copyGoodsOrNewGoods(
                            goods = item,
                            parentCellName = TE,
                            haveCount = item.teCount,
                            allCount = item.teCount
                        )
                        innerIncomeItems.add(newItem)
                        item.allCount = item.allCount - item.teCount
                        item.haveCount = item.haveCount - item.teCount
                    }
                    //То проверить (have - te) == 0
                    //Если да просто перенести элемент, иначе разделить, из старого вычесть have и te


                } else {
                    if ((item.haveCount - item.teCount) == 0) {
                        item.parentCellName = TE
                        item.isShown = false
                        item.teCount = 0
                    } else {

                        var newItem = IncomeItemFactory.copyGoodsOrNewGoods(
                            goods = item,
                            parentCellName = TE,
                            haveCount = item.teCount,
                            allCount = item.allCount
                        )

                        innerIncomeItems.add(newItem)
                        var newCount = item.allCount - item.teCount
                        item.allCount = if (newCount < 0) 0 else newCount
                        item.haveCount = item.haveCount - item.teCount
                    }
                    //Создать новый элемент если have - te != 0, иначе перенести весь элемент
                }

            }
        }
    }

    private fun appendTEOrCreateNewTE(
        TE: String,
        innerIncomeItems: MutableList<IncomeItem>
    ) {
        localViewModel.items.value!!.forEach { item ->
            if (item is IncomeItem.TEItem &&  item.teName == TE) {
                innerIncomeItems.add(item)
            }
        }
        if (innerIncomeItems.size == 0) {
            var newTe = IncomeItemFactory.createNewTE(
                name = TE,
                parentCellName = localViewModel.currentCellName.value.toString(),
            )
            innerIncomeItems.add(newTe)
        }
    }

    private fun resetTeCounterToZeroAndSwitchTeButton(isClosed: Boolean?) {
        var clearedCollection: MutableList<IncomeItem> = mutableListOf()
        lifecycleScope.launch {
            if (isClosed == false) {
                withContext(Dispatchers.IO) {
                    localViewModel.items.value?.forEach { item ->
                        item.teCount = 0
                        clearedCollection.add(item)
                    }
                }
                withContext(Dispatchers.Main) {
                    localViewModel.updateItems(clearedCollection.toList())
                    viewModel.switchTeButton()
                }
            }
        }
    }

    private fun finishSessionAndReturnToPreviousFragment(
        incomeRepo: IncomeRepository,
        sessionId: String?
    ) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                localViewModel.finishSession(
                    incomeRepo = incomeRepo,
                    sessionId = sessionId.toString()
                )
                pushChanges(requireActivity())
            }
            withContext(Dispatchers.Main) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun showFinishButton(flag: Boolean) {
        if (flag) {
            binding.rwIncomeSessionList.setPadding(20, 0, 20, 80)
            binding.btnFinish.visibility = View.VISIBLE
        } else {
            binding.rwIncomeSessionList.setPadding(20, 0, 20, 40)
            binding.btnFinish.visibility = View.GONE
        }
    }

    private fun initSession(
        incomeRepo: IncomeRepository,
        sessionId: String?
    ) {
        lifecycleScope.launch {
            var data: List<IncomeItem> = listOf()
            var cell: Cell
            withContext(Dispatchers.Main) {
                var session = incomeRepo.getIncomeSessionById(sessionId.toString())
                println("########")
                println(incomeRepo.getAllIncomeSession())
                cell = incomeRepo.getCellById(session.toCellId.toString())
                localViewModel.cellStack.addLast(cell.name)
                localViewModel.setCellName(cell.name)
            }
            withContext(Dispatchers.IO) {
                data = localViewModel.loadItems(
                    incomeRepo = incomeRepo,
                    sessionId = sessionId.toString(),
                    cell = cell
                )
            }
            withContext(Dispatchers.Main) {
                localViewModel.updateItems(data)
                localViewModel.setSelectedItem(0)
            }
        }
    }

}
private fun pushChanges(requireActivity: FragmentActivity) {
    val data = Data.Builder()
        .putString("sync_type", "PUSH")
        .build()

    val request =
        OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(data)
            .build()

    WorkManager.getInstance(requireActivity)
        .enqueue(request)
}
//TODO когда выходишь из заявки то вылезает окно те мода