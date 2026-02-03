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
import com.example.wmsRemote.Adapters.MoveSessionAdapter
import com.example.wmsRemote.MoveActivity
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.FragmentMoveSessionBinding
import com.example.wmsRemote.viewModel.MoveSessionViewModel
import com.example.wmswherther.Classes.MoveSessionItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.Goods
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
        var recyclerView: RecyclerView = binding.rwContainer
        var adapter = MoveSessionAdapter(listOf(), requireActivity(), localViewModel, viewModel,recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        localViewModel.myData.observe(requireActivity(), {data ->
            adapter.updateCollection(data, localViewModel.getSelectedItem())
        })

        viewModel.Barcode.observe(viewLifecycleOwner, { barcode ->
            //TODO сделать чтобы была сортировка по те, количеству и прочему перед добавлением
            //TODO  Если нажал ТЕ надо сделать чтобы можно было отменить добавление товара в те.
            if(isCell(barcode)){
                if(localViewModel.isMoving.value != null && localViewModel.isMoving.value!!){

                }else{
                    lifecycleScope.launch {
                        var list : MutableList<MoveSessionItem> = mutableListOf()
                        withContext(Dispatchers.IO) {
                            var dao = MainDB.getDB(requireActivity()).getDao()
                            var cell = dao.getCellByName(barcode)
                            if(cell != null){
                                dao.getGoodsByCellId(cell.id).forEach{ goods: Goods ->
                                    var catalog = dao.getCatalogById(goods.catalogId)
                                    if(catalog.supplierId == (viewModel.uiState.value as UiState.MoveSessionMenu).supplierId){
                                        list.add(
                                            MoveSessionItem(
                                                isSelected = false,
                                                haveCount = 0,
                                                allCount = goods.amount,
                                                name = catalog.name
                                        )
                                        )
                                    }
                                }
                            }
                        }
                        withContext(Dispatchers.Main){
                            localViewModel.updateMyData(list)
                        }
                    }
                }
            }else{

            }
//            if(barcode != "" && viewModel.IsActiveSearchWindow.value == false) {
//                lifecycleScope.launch {
//                    var newItems: List<MoveItem> = listOf()
//                    var bar =
//                        MainDB.getDB(requireActivity()).getDao().getBarcodeByName(barcode)
//                    if (bar != null && bar is Barcode) {
//                        var isAdded = false
//                        withContext(Dispatchers.IO) {
//                            localViewModel.items.value?.forEach { item ->
//                                var teCount = item.teCount
//                                if((viewModel.uiState.value as UiState.IncomeSessionMenu).isTEModeActive){
//                                    teCount = teCount + 1
//                                }
//                                if (item.catalogId == bar.catalogId && localViewModel.currentCellName.value.toString() == item.TE) {
//                                    isAdded = true
//                                    newItems += IncomeItem(
//                                        name = item.name,
//                                        TE = item.TE,
//                                        catalogId = item.catalogId,
//                                        haveCount = item.haveCount + 1,
//                                        allCount = item.allCount,
//                                        teCount = teCount,
//                                        isSelected = item.isSelected,
//                                        isExpanded = item.isExpanded,
//                                        isShown = item.isShown,
//                                        isExpandable = item.isExpandable)
//                                }else {
//                                    newItems += item
//                                }
//
//                            }
//                            if(!isAdded){
//                                var catalog = MainDB.getDB(requireActivity()).getDao().getCatalogById(bar.catalogId)
//                                if (catalog != null){
//                                    newItems += IncomeItem(
//                                        name = catalog.name,
//                                        TE = localViewModel.currentCellName.value.toString(),
//                                        catalogId = catalog.id,
//                                        haveCount = 1,
//                                        allCount = 0,
//                                        teCount = if (viewModel.IsIncomeSessionTEModeActive.value == true) 1 else 0,
//                                        isSelected = false,
//                                        isExpanded = false,
//                                        isShown = true,
//                                        isExpandable = false)
//                                }
//                            }
//                        }
//                        withContext(Dispatchers.Main) {
//                            localViewModel.updateItems(newItems)
//                            var binding = viewModel.getMainBinding()
//                            if(viewModel.IsScanningActive.value == true) {
//                                binding?.etIncomeBarcode?.requestFocus()
//
//                            }else{
//                                binding?.etIncomeBarcodeScan?.requestFocus()
//                            }
//                        }
//                    }
//                }
//            }

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
        localViewModel.myData.observe(requireActivity(), {data ->
            adapter.updateCollection(data, localViewModel.getSelectedItem())
        })
        localViewModel.cell.observe(requireActivity(), {str ->
            if (str != ""){
                binding.btnMove.visibility = View.VISIBLE
            }else{
                binding.btnMove.visibility = View.GONE
            }
            binding.tvCellName.text = str
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

        fun handleTextChange(
            text: String,
            trim: String,
            moveActivity: MoveActivity,
            db: MainDB,
            adapter: MoveSessionAdapter
        ) {
            if (text != "") {
                localViewModel.searchBtnHandler(trim, moveActivity, db, adapter, binding)
                /* binding.etCell.text.clear()
                 binding.etCell.requestFocus()*/
            }
        }
        return binding.root
    }







}


private fun isCell(cell: String): Boolean {
    /*if (cell.length == 4 && cell[0] in 'A' .. 'Z' && cell[1].isDigit() && cell[2].isDigit() && cell[3].isDigit()){
        return true
    }*/
    return true
}
fun convertToInt(nullableInt: Int?): Int {
    return nullableInt ?: 0  // If nullableInt is null, use 0 as default
}
