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
import com.example.wmsRemote.Adapters.AssemblySessionAdapter
import com.example.wmsRemote.R
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmsRemote.databinding.ActivityAssemblyBinding
import com.example.wmsRemote.databinding.FragmentInventorySessionBinding
import com.example.wmsRemote.viewModel.AssemblySessionViewModel
import com.example.wmsRemote.viewModel.AssemblyViewModel
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
        var adapter = AssemblySessionAdapter(requireActivity(), lifecycleScope, localViewModel, listOf())
        var recyclerView: RecyclerView = binding.rwListItem
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

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
                    updateMenuStyle(status)
                }
            }
        })


        return  binding.root
    }
    private fun updateAssemblyStyle(status: Int?) {
        with(binding){
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
        }
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
}