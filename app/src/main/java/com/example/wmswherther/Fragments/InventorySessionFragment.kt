package com.example.wmswherther.Fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.FragmentInventorySessionBinding
import com.example.wmswherther.Adapters.InventorySessionAdapter
import com.example.wmswherther.Classes.InventorySessionItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.viewModel.InventorySessionViewModel
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        var listTypes : List<CellType> = listOf()
        var recyclerView: RecyclerView = binding.rwInventorySessionList
        var adapterCollection = mutableListOf<InventorySessionItem>()
        if( localViewModel.items.value != null){
            adapterCollection = localViewModel.items.value as MutableList<InventorySessionItem>
        }
        val adapter = InventorySessionAdapter(adapterCollection, recyclerView, localViewModel, requireActivity(), viewModel)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

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
        /*viewModel.Barcode.observe(viewLifecycleOwner, { barcode ->
            //TODO сделать чтобы была сортировка по те, количеству и прочему перед добавлением
            //TODO  Если нажал ТЕ надо сделать чтобы можно было отменить добавление товара в те.
            if(barcode != "" && viewModel.uiState.value is UiState.MoveSessionMenu) {
                if (isCell(barcode, listTypes)) {
                    if (localViewModel.isMoving.value != null && localViewModel.isMoving.value!!) {
                        localViewModel.moveItems(barcode, dao, viewModel)
                        // если числа равны то смена ячейки
                        // иначе создается новый goods
                        //TODO перемещение элементов если нажата клавиша
                    } else {
                        if(viewModel.uiState.value is UiState.MoveSessionMenu) {
                            val uiState = viewModel.uiState.value as UiState.MoveSessionMenu
                            if (!uiState.isEmptyList){
                                val dialog = AlertDialog.Builder(requireActivity())
                                    .setTitle("Выход")
                                    .setMessage("Есть остканированный товар, при переходе в другую ячейку прогресс сбросится!")
                                    .setPositiveButton("Да") { _, _ ->
                                        localViewModel.updateCell(barcode)
                                        localViewModel.loadData(dao, barcode, viewModel)
                                        localViewModel.setCounter(0)
                                    }
                                    .setNegativeButton("Нет", null)
                                    .create()
                                dialog.show()
                            }else {
                                localViewModel.updateCell(barcode)
                                localViewModel.loadData(dao, barcode, viewModel)
                            }
                        }
                    }
                } else {
                    localViewModel.changeList(barcode, dao)
                    //TODO шк тут надо, НАЙТИ в бд и ...
                }
            }
        })*/

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
                binding.rwInventorySessionList.setPadding(20, 0, 20, 80)
                binding.btnFinish.visibility = View.VISIBLE
            }else{
                binding.rwInventorySessionList.setPadding(20, 0, 20, 40)
                binding.btnFinish.visibility = View.GONE
            }
        })

        InitOrder(localViewModel)

        return  binding.root
    }

    private fun InitOrder(localViewModel: InventorySessionViewModel) {
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

}