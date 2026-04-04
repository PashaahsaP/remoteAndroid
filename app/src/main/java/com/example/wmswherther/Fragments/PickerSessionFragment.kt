package com.example.wmswherther.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.Adapters.AssemblySessionAdapter
import com.example.wmsRemote.Adapters.AssemblySessionMainAdapter
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.AssemblySessionMenuType
import com.example.wmsRemote.databinding.ActivityAssemblyBinding
import com.example.wmsRemote.viewModel.AssemblySessionViewModel
import com.example.wmswherther.Classes.AssemblyItem
import com.example.wmswherther.Classes.PickerItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PickerSessionFragment: Fragment() {

    private var _binding: ActivityAssemblyBinding? = null
    private lateinit var localViewModel: AssemblySessionViewModel
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for AssemblyMain")
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val db = MainDB.getDB(requireActivity())
        _binding = ActivityAssemblyBinding.inflate(layoutInflater)
        localViewModel = ViewModelProvider(this).get(AssemblySessionViewModel::class.java)
        localViewModel._menuStatus.value = AssemblySessionMenuType.ScanningMode.ordinal
        var adapter = AssemblySessionAdapter(requireActivity(), lifecycleScope, localViewModel, listOf())
        var mainAdapter = AssemblySessionMainAdapter(requireActivity(), lifecycleScope, localViewModel, listOf())

        var recyclerView: RecyclerView = binding.rwListItem
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        var recyclerViewMain: RecyclerView = binding.rwListMain
        recyclerViewMain.layoutManager = LinearLayoutManager(requireActivity())
        recyclerViewMain.adapter = mainAdapter

        localViewModel.items.observe(viewLifecycleOwner, { items ->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    adapter.updateData(items)
                }
            }
        })
        localViewModel.assemblyStatus.observe(viewLifecycleOwner, { status ->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    updateAssemblyStyle(status)
                }
            }
        })
        localViewModel.menuStatus.observe(viewLifecycleOwner, { status ->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    //updateMenuStyle(status)
                }
            }
        })
        localViewModel.activeElement.observe(viewLifecycleOwner, { element->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    mainAdapter.updateData(element.pickerList)
                    updateActiveElement(element)
                }
            }
        })


        viewModel.Barcode.observe(viewLifecycleOwner, { barcode ->
            if(barcode != "" && viewModel.uiState.value is UiState.AssemblySessionMenu) {
                lifecycleScope.launch {
                    var isCountMode : Boolean = false
                    var activeElementList = localViewModel.activeElement.value!!.pickerList
                    withContext(Dispatchers.IO) {
                        var assemblySession = viewModel.uiState.value as UiState.AssemblySessionMenu
                        var db = MainDB.getDB(requireActivity())
                        var dao = db.getDao()
                        var curOperation = localViewModel.menuStatus.value

                        if(curOperation == AssemblySessionMenuType.ScanningMode.ordinal){
                            // Найти активный элемент в списке
                            for (counter in 0 .. activeElementList.count() - 1){
                                // Получение текущего элемента
                                var currElement = activeElementList[counter]
                                if(currElement.isSelected){
                                    // Если это последний элемент то надо переключить режим на ввод количества
                                    // Обновить адаптер текущего элемента
                                    if(counter + 1  == activeElementList.size){
                                        isCountMode = true
                                        localViewModel._menuStatus.value = AssemblySessionMenuType.CountMode.ordinal
                                        activeElementList.map { item -> item.isSelected = false }

                                    }
                                    // Иначе сделать следующий элемент в списке выбранным
                                    // Обновить адаптер текущего элемента
                                    else{
                                        activeElementList[counter].isSelected = false
                                        activeElementList[counter + 1].isSelected = true
                                        break
                                    }
                                }
                            }
                        }
                    }
                    withContext(Dispatchers.Main){
                        if(isCountMode){
                            localViewModel._menuStatus.value = AssemblySessionMenuType.CountMode.ordinal
                        }
                        var t = localViewModel._activeElement.value
                        t!!.pickerList = activeElementList
                        localViewModel._activeElement.value = t
                    }
                }
            }
        })
        var sessionId = (viewModel.uiState.value as UiState.AssemblySessionMenu).sessionId
        localViewModel.loadCollection(db,sessionId)

        return  binding.root
    }
    private fun updateAssemblyStyle(status: Int?) {
       /* with(binding){
            if(StatusType.EnterCell.ordinal == status){
                tvCell.setTextColor(resources.getColor(R.color.white))
                tvGoodsName.setTextColor(resources.getColor(R.color.regularGrey))
                tvBarcode.setTextColor(resources.getColor(R.color.regularGrey))
                etCount.setTextColor(resources.getColor(R.color.regularGrey))
                etInput.isEnabled = true
                etInput.requestFocus()
                etCount.isEnabled = false
            }else if(StatusType.EnterBarcode.ordinal == status){
                tvGoodsName.setTextColor(resources.getColor(R.color.white))
                tvBarcode.setTextColor(resources.getColor(R.color.white ))
                tvCell.setTextColor(resources.getColor(R.color.regularGrey))
            }else{
                tvGoodsName.setTextColor(resources.getColor(R.color.regularGrey))
                tvBarcode.setTextColor(resources.getColor(R.color.regularGrey))
                etCount.setTextColor(resources.getColor(R.color.white))
                etCount.isEnabled = true
                etInput.isEnabled = false
                etCount.requestFocus()
            }
        }*/
    }
    private fun updateMenuStyle(status: Int?) {
        if(status == 0){
            binding.llMenuContainer.visibility = View.VISIBLE
            binding.llAssemblyContainer.visibility = View.GONE
        }
        else if(status == 1){
            binding.llMenuContainer.visibility = View.GONE
            binding.llAssemblyContainer.visibility = View.VISIBLE
        }
    }
    private fun updateActiveElement(newItem: AssemblyItem) {
       /* with(binding){
            tvCell.text = newItem!!.cell
            //tvBarcode.text = "456546546"
            tvGoodsName.text = newItem.name
            etCount.setText(newItem.amount.toString())
        }*/
    }
}