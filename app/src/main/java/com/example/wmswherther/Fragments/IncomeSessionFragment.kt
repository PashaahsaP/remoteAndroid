package com.example.wmswherther.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.ActivityMainBinding
import com.example.wmsRemote.databinding.FragmentIncomeSessionBinding
import com.example.wmswherther.Adapters.IncomeSessionAdapter
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.data.db.Barcode
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


        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val localViewModel = ViewModelProvider(requireActivity()).get(IncomeSessionViewModel::class)
            _binding = FragmentIncomeSessionBinding.inflate(inflater)


            var recyclerView: RecyclerView = binding.rwIncomeSessionList
            var adapter = IncomeSessionAdapter(listOf(), recyclerView, localViewModel, requireActivity())
            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
            recyclerView.adapter = adapter
            val sessionId = arguments?.getString("id")

            localViewModel.items.observe(viewLifecycleOwner,{ items ->
                adapter.updateCollection(items, localViewModel.getSelectedItem())
                recyclerView.smoothScrollToPosition(localViewModel.getSelectedItem())
            })
            viewModel.Barcode.observe(viewLifecycleOwner, { barcode ->
                if(barcode != "") {
                    lifecycleScope.launch {
                        var newItems: List<IncomeItem> = listOf()
                        var bar =
                            MainDB.getDB(requireActivity()).getDao().getBarcodeByName(barcode)
                        if (bar != null && bar is Barcode) {
                            withContext(Dispatchers.IO) {
                                localViewModel.items.value?.forEach { item ->
                                    var teCount = item.teCount
                                    if(viewModel.IsIncomeSessionTEModeActive.value == true){
                                        teCount = teCount + 1
                                    }
                                    if (item.catalogId == bar.catalogId && localViewModel.currentCellName.value.toString() == item.TE) {
                                        newItems += IncomeItem(
                                            name = item.name,
                                            TE = item.TE,
                                            catalogId = item.catalogId,
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
                    // <editor-fold desc="adding TE to new collection">
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
                                if(item.allCount <= item.teCount){
                                    item.TE = TE
                                    item.isShown = false
                                    item.teCount = 0
                                }else{
                                    var newItem = item.copy(TE = TE, teCount = 0, isShown = false, haveCount = item.teCount, allCount = item.teCount)
                                    innerIncomeItems.add(newItem)
                                    item.allCount = item.allCount - item.teCount
                                    item.haveCount = item.haveCount - item.teCount
                                }
                                //Создать новый элемент если have - te != 0, иначе перенести весь элемент
                            }
                        }
                    }
                    // </editor-fold>
                    // <editor-fold desc="Получение полного списка за исключением тех что были обработаны и добавлены ранее">


                    localViewModel.items.value?.forEach { item ->
                        if (item.TE == TE && it                                            не добавлять те, которая была в начале обработана
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

                    for (i in 0..< innerIncomeItems.size){
                        if(!innerIncomeItems[i].isExpandable && innerIncomeItems[i].haveCount != 0 && innerIncomeItems[i].allCount != 0){
                            for (j in (i + 1) ..< innerIncomeItems.size){
                                innerIncomeItems[i].haveCount += innerIncomeItems[j].haveCount
                                innerIncomeItems[i].allCount += innerIncomeItems[j].allCount
                                innerIncomeItems[j].haveCount = 0
                                innerIncomeItems[j].allCount = 0
                            }
                        }
                        if(!innerIncomeItems[i].isExpandable && innerIncomeItems[i].allCount == 0 && innerIncomeItems[i].haveCount == 0)
                            continue
                        else
                            result += innerIncomeItems[i]
                    }
                    // </editor-fold>
                    // <editor-fold desc="обнуление ТЕ счетчика">
                    localViewModel.items.value?.forEach { item ->
                        if (item.TE != TE && item.allCount != 0) {
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
                    data = localViewModel.loadItems(MainDB.getDB(requireActivity()), sessionId.toString())
                }
                withContext(Dispatchers.Main){
                    localViewModel.updateItems(data)
                    localViewModel.setSelectedItem(0)
                }
            }

            return  binding.root
        }

}
