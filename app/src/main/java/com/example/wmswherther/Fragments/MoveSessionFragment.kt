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
        var dao = MainDB.getDB(requireActivity()).getDao()
        var listTypes : List<CellType> = listOf()

        var recyclerView: RecyclerView = binding.rwContainer
        var adapter = MoveSessionAdapter(listOf(), requireActivity(), localViewModel, recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter
        localViewModel.setSelectionForAll(viewModel.IsSelectedMoveList.value ?: false)

        localViewModel.viewModelScope.launch {
            withContext(Dispatchers.IO) {
                listTypes = dao.getCellTypes()
            }
        }
        localViewModel.counter.observe(requireActivity(), { count ->
            if(viewModel.uiState.value is UiState.MoveSessionMenu) {
                val ui = viewModel.uiState.value as UiState.MoveSessionMenu
                if (count == 0) {
                    viewModel.setActiveUi(ui.copy(isEmptyList = true))
                } else {
                    viewModel.setActiveUi(ui.copy(isEmptyList = false))
                }
            }
        })
        viewModel.Barcode.observe(viewLifecycleOwner, { barcode ->
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
                    if(barcode != null)
                        localViewModel.changeList(barcode, dao)
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
            /*etCell.setOnEditorActionListener { textView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    // Выполнить действие
                    val text = etCell.text.toString()
                    if(text != "") {
                        handleTextChange(
                            text,
                            etCell.text.toString().trim(),
                            this@MoveActivity,
                            db,
                            adapter
                        )
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
            btnSearch.setOnClickListener {

                handleTextChange(tvCellName.text.toString(), etCell.text.toString().trim(), this@MoveActivity, db, adapter)

            }*/
            //TODO make that cell displayed in top, move btn collapsed, cancel btn appear, changing flag moving
            btnMove.setOnClickListener{
                localViewModel.updateIsMoving(true)
            }
            btnCancel.setOnClickListener {
                localViewModel.updateIsMoving(false)
            }
            /*etCell.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {
                    textChangesChannel.trySend(editable.toString())
                }
            })
            btnBork.setOnClickListener {
                llMenu.visibility = View.GONE
                CLContainer.visibility = View.VISIBLE
                viewModel.supplier = SupplierType.Bork.ordinal
            }
            btnAtomy.setOnClickListener {
                llMenu.visibility = View.GONE
                CLContainer.visibility = View.VISIBLE
                viewModel.supplier = SupplierType.Atomy.ordinal
            }*/
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
    /*if (cell.length == 4 && cell[0] in 'A' .. 'Z' && cell[1].isDigit() && cell[2].isDigit() && cell[3].isDigit()){
        return true
    }*/
}
fun convertToInt(nullableInt: Int?): Int {
    return nullableInt ?: 0  // If nullableInt is null, use 0 as default
}
