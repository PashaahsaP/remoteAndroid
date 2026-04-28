package com.example.wmswherther.Fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.FragmentIncomeSessionBinding
import com.example.wmswherther.Adapters.IncomeSessionAdapter
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Repositories.IncomeRepository
import com.example.wmswherther.viewModel.IncomeSessionViewModel
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            var incomeRepo = IncomeRepository(MainDB.getDB(requireActivity()).getDao())

            viewModel.Barcode.observe(viewLifecycleOwner, { barcode ->
                //TODO сделать чтобы была сортировка по те, количеству и прочему перед добавлением
                //TODO  Если нажал ТЕ надо сделать чтобы можно было отменить добавление товара в те.
                if(barcode != "" && viewModel.uiState.value is UiState.IncomeSessionMenu) {
                    lifecycleScope.launch {
                        var newItems: List<IncomeItem> = listOf()
                        var bar = incomeRepo.getBarcodeByName(barcode)

                        if (bar != null && bar is Barcode) {
                            var isAdded = false
                            withContext(Dispatchers.IO) {
                                localViewModel.items.value?.forEach { item ->
                                    var teCount = item.teCount
                                    if((viewModel.uiState.value as UiState.IncomeSessionMenu).isTEModeActive){
                                        teCount = teCount + 1
                                    }
                                    if (item.catalogId == bar.catalogId && localViewModel.currentCellName.value.toString() == item.TE) {
                                        isAdded = true
                                        newItems += IncomeItem(
                                            name = item.name,
                                            TE = item.TE,
                                            catalogId = item.catalogId,
                                            goodsId = item.goodsId,
                                            haveCount = item.haveCount + 1,
                                            allCount = item.allCount,
                                            teCount = teCount,
                                            isSelected = item.isSelected,
                                            isExpanded = item.isExpanded,
                                            isShown = item.isShown,
                                            isExpandable = item.isExpandable)
                                    }else {
                                        newItems += item
                                    }

                                }
                                if(!isAdded){
                                    var catalog = incomeRepo.getCatalogById(bar.catalogId)
                                    if (catalog != null){
                                        newItems += IncomeItem(
                                            name = catalog.name,
                                            TE = localViewModel.currentCellName.value.toString(),
                                            catalogId = catalog.id,
                                            goodsId = "",
                                            haveCount = 1,
                                            allCount = 0,
                                            teCount = if (viewModel.IsIncomeSessionTEModeActive.value == true) 1 else 0,
                                            isSelected = false,
                                            isExpanded = false,
                                            isShown = true,
                                            isExpandable = false)
                                    }
                                }
                            }
                            withContext(Dispatchers.Main) {
                                localViewModel.updateItems(newItems)
                                var binding = viewModel.getMainBinding()
                                if(viewModel.IsScanningActive.value == true) {
                                    binding?.etIncomeBarcode?.requestFocus()

                                }else{
                                    binding?.etIncomeBarcodeScan?.requestFocus()
                                }
                            }
                        }
                    }
                }

            })
            viewModel.TE.observe(viewLifecycleOwner, { TE ->
                var innerIncomeItems: MutableList<IncomeItem> = mutableListOf()

                if(TE != "") {//надо найти есть ли те, если есть то добавить ее и ее элементы в новую коллекцию, а потом оставшиеся элементы
                    // <editor-fold desc="Добавление те в новую коллекцию если не существует создание новой и добавление">
                    localViewModel.items.value!!.forEach { item ->
                        if (item.name == TE){
                            innerIncomeItems.add(item)
                        }
                    }
                    if(innerIncomeItems.size == 0) {
                        var newTe = IncomeItem(
                            name = TE,
                            TE = TE,
                            catalogId = "",
                            goodsId = "",
                            haveCount = 0,
                            allCount = 0,
                            teCount = 0,
                            isExpanded = false,
                            isExpandable = true
                        )
                        innerIncomeItems.add(newTe)
                    }
                    // </editor-fold>
                    // <editor-fold desc="create new collection by te counter">
                    localViewModel.items.value?.forEach { item ->
                        if (item.teCount != 0) {
                            if (item.haveCount < item.allCount) {
                                //То создать новый элемент, в старом уменьшить have и te
                                var newItem = item.copy(TE = TE, teCount = 0, isShown = false, haveCount = item.teCount, allCount = item.teCount)
                                innerIncomeItems.add(newItem)
                                item.allCount = item.allCount - item.teCount
                                item.haveCount = item.haveCount - item.teCount

                            } else if(item.haveCount == item.allCount) {
                                if((item.haveCount - item.teCount) == 0){
                                    item.TE = TE
                                    item.isShown = false
                                    item.teCount = 0
                                }else{
                                    var newItem = item.copy(TE = TE, teCount = 0, isShown = false, haveCount = item.teCount, allCount = item.teCount)
                                    innerIncomeItems.add(newItem)
                                    item.allCount = item.allCount - item.teCount
                                    item.haveCount = item.haveCount - item.teCount
                                }
                                //То проверить (have - te) == 0
                                //Если да просто перенести элемент, иначе разделить, из старого вычесть have и te


                            }
                            else{
                                if((item.haveCount - item.teCount) == 0){
                                    item.TE = TE
                                    item.isShown = false
                                    item.teCount = 0
                                }else{
                                    var newItem = item.copy(TE = TE, teCount = 0, isShown = false, haveCount = item.teCount, allCount = item.allCount)
                                    innerIncomeItems.add(newItem)
                                    var newCount = item.allCount - item.teCount
                                    item.allCount = if (newCount < 0) 0 else newCount
                                    item.haveCount = item.haveCount - item.teCount
                                }
                                //Создать новый элемент если have - te != 0, иначе перенести весь элемент
                            }

                        }
                    }
                    // </editor-fold>
                    // <editor-fold desc="Получение полного списка за исключением тех что были обработаны и добавлены ранее">


                    localViewModel.items.value?.forEach { item ->//TODO какая то хуета тут
                        if (item.TE == TE && item.name != item.TE)//чтобы повторно не добавлять те, которая была в начале обработана. Добивание коллекции
                        {
                            innerIncomeItems.add(item)
                        }
                    }//надо перебрать имеющиеся элементы в новой коллекции и если есть дубликаты сложить их
                    innerIncomeItems.forEach{ item ->
                        if(!item.isExpandable){

                        }
                    }
                    // </editor-fold>
                    // <editor-fold desc="remove duplication item from list">


                    var result: MutableList<IncomeItem> = mutableListOf()

                    result += innerIncomeItems.first()
                    innerIncomeItems.removeFirst()
                    innerIncomeItems
                        .groupBy { it.catalogId }
                        .map {(id, group) ->
                            IncomeItem(
                                name = group.first().name,
                                TE = group.first().TE,
                                catalogId = id,
                                goodsId = group.first().goodsId,
                                haveCount = group.sumOf { it.haveCount },
                                allCount = group.sumOf { it.allCount },
                                teCount = group.sumOf { it.teCount },
                                isSelected = group.first().isSelected,
                                isExpandable = group.first().isExpandable,
                                isExpanded = group.first().isExpanded,
                                isShown = group.first().isShown,
                            )
                        }.forEach { item -> result += item }
                    // </editor-fold>
                    // <editor-fold desc="обнуление ТЕ счетчика">
                    localViewModel.items.value?.forEach { item ->
                        if (item.TE != TE && !(item.haveCount == 0 && item.allCount == 0)) {
                            item.teCount = 0
                            result.add(item)
                        }
                    }
                    // </editor-fold>
                    localViewModel.updateItems(result)
                    //В новой коллекции уже есть те и вновь созданные элементы
                    //Если есть элементы где
                    //пройти список по новой и где есть элементы с таким же те то не добавлять в коллекцию
                }
            })
            viewModel.IsCloseTE.observe(viewLifecycleOwner, { isClosed ->
                var clearedCollection : MutableList<IncomeItem> = mutableListOf()
                lifecycleScope.launch {
                    if (isClosed == false) {
                        withContext(Dispatchers.IO) {
                            localViewModel.items.value?.forEach { item ->
                                clearedCollection.add(item.copy(teCount = 0))
                            }
                        }
                        withContext(Dispatchers.Main) {
                            localViewModel.updateItems(clearedCollection.toList())
                            viewModel.switchTeButton()
                        }
                    }
                }
            })
            viewModel.IsSelectedIncomeList.observe(viewLifecycleOwner, {flag : Boolean->
                localViewModel.setSelection(flag)
            })
            viewModel.IsFinishIncomeSession.observe(viewLifecycleOwner, { status ->
                if(status) {
                    if(localViewModel.CurrentCountOfCount.value != localViewModel.CountOfCount.value || localViewModel.IsOverCounter.value == true){
                        val dialogNotification = AlertDialog.Builder(requireActivity())
                            .setTitle("Предупреждение")
                            .setMessage("Есть не отсканированный товар. Уверены что хотите завершить приёмку")
                            .setPositiveButton("Да") { _, _ ->
                                lifecycleScope.launch {
                                    withContext(Dispatchers.IO) {
                                        localViewModel.finishSession(
                                            incomeRepo = incomeRepo,
                                            sessionId = sessionId.toString()
                                        )
                                    }
                                    withContext(Dispatchers.Main) {
                                        requireActivity().onBackPressedDispatcher.onBackPressed()
                                    }
                                }
                            }
                            .setNegativeButton("Нет", null)
                            .create()

                        dialogNotification.show()
                    }else{
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                localViewModel.finishSession(
                                    incomeRepo = incomeRepo,
                                    sessionId = sessionId.toString()
                                )
                            }
                            withContext(Dispatchers.Main) {
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    }
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
                if(flag) {
                    binding.rwIncomeSessionList.setPadding(20, 0, 20, 80)
                    binding.btnFinish.visibility = View.VISIBLE
                }else{
                    binding.rwIncomeSessionList.setPadding(20, 0, 20, 40)
                    binding.btnFinish.visibility = View.GONE
                }
            })
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
                //доделать апдейт






                adapter.updateCollection(items, localViewModel.getSelectedItem())
                recyclerView.smoothScrollToPosition(localViewModel.getSelectedItem())
            })
            localViewModel.currentCellName.observe(viewLifecycleOwner, { cellName ->
                binding.tvCellName.text = cellName
            })
            //todo set red color or black
            lifecycleScope.launch {
                var data: List<IncomeItem> = listOf()
                withContext(Dispatchers.Main){
                    var dao = MainDB.getDB(requireActivity()).getDao()
                    var session = dao.getIncomeSessionById(sessionId.toString())
                    var cellName = dao.getCellById(session.toCellId.toString()).name
                    localViewModel.cellStack.addLast(cellName)
                    localViewModel.setCellName(cellName)
                }
                withContext(Dispatchers.IO){
                    data = localViewModel.loadItems(
                        incomeRepo = incomeRepo,
                        sessionId =  sessionId.toString())
                }
                withContext(Dispatchers.Main){
                    localViewModel.updateItems(data)
                    localViewModel.setSelectedItem(0)
                }
            }

            binding.btnFinish.setOnClickListener {
                lifecycleScope.launch {
                   withContext(Dispatchers.IO) {
                       localViewModel.finishSession(
                           incomeRepo = incomeRepo,
                           sessionId = sessionId.toString())

                    }
                    withContext(Dispatchers.Main){
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }

            return  binding.root
        }

}
//TODO когда выходишь из заявки то вылезает окно те мода