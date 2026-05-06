package com.example.wmswherther.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.Adapters.MoveSessionAdapter
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.FragmentMoveSessionBinding
import com.example.wmsRemote.viewModel.MoveSessionViewModel
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Repositories.MoveeRepository
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MoveSessionFragment: Fragment() {

    private var _binding: FragmentMoveSessionBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentIncome")
    private val viewModel: MainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val localViewModel = ViewModelProvider(requireActivity()).get(MoveSessionViewModel::class)
        _binding = FragmentMoveSessionBinding.inflate(inflater)
        var listTypes : List<CellType> = listOf()
        var moveRepo: MoveeRepository = ServiceLocator.moveRepository
            ?: MoveeRepository(MainDB.getDB(requireActivity()).getDao())

        var recyclerView: RecyclerView = binding.rwContainer
        var adapter = MoveSessionAdapter(listOf(), requireActivity(), localViewModel, recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter
        localViewModel.setSelectionForAll(viewModel.IsSelectedMoveList.value ?: false)

        localViewModel.viewModelScope.launch {
            withContext(Dispatchers.IO) {
                listTypes = moveRepo.getCellTypes()
            }
        }
       /* localViewModel.counter.observe(requireActivity(), { count ->
            if(viewModel.uiState.value is UiState.MoveSessionMenu) {
                val ui = viewModel.uiState.value as UiState.MoveSessionMenu
                if (count == 0) {
                    viewModel.setActiveUi(ui.copy(isEmptyList = true))
                } else {
                    viewModel.setActiveUi(ui.copy(isEmptyList = false))
                }
            }
        })*/
        viewModel.Barcode.observe(viewLifecycleOwner, { barcode ->
            //TODO сделать чтобы была сортировка по те, количеству и прочему перед добавлением
            //TODO  Если нажал ТЕ надо сделать чтобы можно было отменить добавление товара в те.
            if(barcode != "" && viewModel.uiState.value is UiState.MoveSessionMenu) {
                if (isCell(barcode, listTypes)) {
                    if (localViewModel.isMoving.value != null && localViewModel.isMoving.value!!) {
                        localViewModel.moveItems(barcode, moveRepo, viewModel)
                        // если числа равны то смена ячейки
                        // иначе создается новый goods
                        //TODO перемещение элементов если нажата клавиша
                    } else {
                        if(viewModel.uiState.value is UiState.MoveSessionMenu) {
                            val totalCount = localViewModel.myData.value!!.toList().sumOf { it.haveCount }
                            if (totalCount != 0){
                                val dialog = AlertDialog.Builder(requireActivity())
                                    .setTitle("Выход")
                                    .setMessage("Есть остканированный товар, при переходе в другую ячейку прогресс сбросится!")
                                    .setPositiveButton("Да") { _, _ ->
                                        localViewModel.updateCell(barcode)
                                        localViewModel.loadData(moveRepo, barcode, viewModel)
                                        localViewModel.setCounter(0)
                                    }
                                    .setNegativeButton("Нет", null)
                                    .create()
                                dialog.show()
                            }else {
                                localViewModel.updateCell(barcode)
                                localViewModel.loadData(moveRepo, barcode, viewModel)
                            }
                        }
                    }
                } else {
                    if(barcode != null)
                        localViewModel.changeList(barcode, moveRepo)
                    //TODO шк тут надо, НАЙТИ в бд и ...
                }
            }
        })
        viewModel.IsSelectedMoveList.observe(viewLifecycleOwner, { state ->
            localViewModel.setSelection(state)
        })
        localViewModel.isMoving.observe(requireActivity(), {isMove->
            if (isMove) {
                binding.btnMove.visibility = View.GONE
                binding.btnCancel.visibility = View.VISIBLE
            }else {
                binding.btnMove.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.GONE
            }
        })
        localViewModel.cell.observe(requireActivity(), {str ->
            if (str != ""){
                binding.btnMove.visibility = View.VISIBLE
            }else{
                binding.btnMove.visibility = View.GONE
            }
            binding.tvCellName.text = str
        })
        localViewModel.myData.observe(requireActivity(), {data ->
            adapter.updateData(data, localViewModel.getSelectedItem())
        })

        with(binding){
            //TODO make that cell displayed in top, move btn collapsed, cancel btn appear, changing flag moving
            btnMove.setOnClickListener{
                localViewModel.updateIsMoving(true)
            }
            btnCancel.setOnClickListener {
                localViewModel.updateIsMoving(false)
            }
        }

        return binding.root
    }

}


fun isCell(cell: String, list: List<CellType>): Boolean {
    list.forEach { cellType ->
        if(cellType.mask!!.length == cell.length){
            return  true
        }
    }
    return false
}
fun convertToInt(nullableInt: Int?): Int {
    return nullableInt ?: 0  // If nullableInt is null, use 0 as default
}
