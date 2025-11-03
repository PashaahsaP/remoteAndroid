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
                var innerIncomeItems : MutableList<IncomeItem> = mutableListOf()
                innerIncomeItems.add(newTe)
                localViewModel.items.value?.forEach { item ->
                    if(item.haveCount == 0){
                        if(item.teCount < item.allCount){
                            //Создать новый а старый уменьшить в общем количестве
                            var newItem = IncomeItem(
                                name = item.name,
                                TE = item.TE,
                                catalogId = item.catalogId,
                                teCount = 0,
                                allCount = item.teCount,
                                haveCount = item.teCount,
                                isExpandable = item.isExpandable,
                                isExpanded = item.isExpanded,
                                isSelected = item.isSelected,
                                isShown = false
                            )
                            innerIncomeItems.add(newItem)
                            item.allCount = item.allCount - item.teCount
                        }else{
                            //Изменить parentTE и изменить положение в списке, оставить общеее количество таким же(чтобы показывало превышение или ровно)
                            item.TE = TE // потом надо будет найти при повторном прохождении списка
                            item.isShown = false
                            innerIncomeItems.add(item)
                        }
                    }else{
                        if(item.teCount  + item.haveCount < item.allCount){
                            //Cоздать новый элемент где количество общее и имеющиеся берется из те
                            //Изменить старый элемент уменьшив общее количество а количество оставить
                            var newItem = IncomeItem(
                                name = item.name,
                                TE = item.TE,
                                catalogId = item.catalogId,
                                teCount = 0,
                                allCount = item.teCount,
                                haveCount = item.teCount,
                                isExpandable = item.isExpandable,
                                isExpanded = item.isExpanded,
                                isSelected = item.isSelected,
                                isShown = false
                            )
                            item.allCount -= item.teCount
                            item.haveCount -= item.teCount
                            innerIncomeItems.add(newItem)

                        }else{
                            //Cоздать новый элемент где количество общее и имеющиеся берется из те
                            //Изменить старый элемент,где общее количество равно нулю,  а количество оставить
                            var newItem = IncomeItem(
                                name = item.name,
                                TE = item.TE,
                                catalogId = item.catalogId,
                                teCount = 0,
                                allCount = item.teCount,
                                haveCount = item.teCount,
                                isExpandable = item.isExpandable,
                                isExpanded = item.isExpanded,
                                isSelected = item.isSelected,
                                isShown = false
                            )
                            item.haveCount -= item.teCount
                            item.allCount = 0
                            innerIncomeItems.add(newItem)
                        }
                    }

                }
                //пройти список по новой и где есть элементы с таким же те то не добавлять в коллекцию
            })
            lifecycleScope.launch {
                var data: List<IncomeItem> = listOf()
                withContext(Dispatchers.Main){
                    var dao = MainDB.getDB(requireActivity()).getDao()
                    var session = dao.getIncomeSessionById(sessionId.toString())
                    var cellName = dao.getCellById(session.toCellId.toString()).name
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
