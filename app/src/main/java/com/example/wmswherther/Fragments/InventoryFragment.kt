package com.example.wmswherther.Fragments

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.databinding.FragmentInventoryBinding
import com.example.wmswherther.Adapters.IncomeMenuAdapter
import com.example.wmswherther.Adapters.InventoryAdapter
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.viewModel.InventoryViewModel
import com.example.wmswherther.viewModel.MainViewModel

class InventoryFragment: Fragment() {
    private var _binding: FragmentInventoryBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentInventory")
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val localViewModel = ViewModelProvider(requireActivity()).get(InventoryViewModel::class)
        _binding = FragmentInventoryBinding.inflate(inflater)

        var supplierAdapter = InventoryAdapter(mutableListOf(), this, viewModel)
        var supplierRecyclerView: RecyclerView = binding.rwSupplierMenu
        supplierRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        supplierRecyclerView.adapter = supplierAdapter

        var orderAdapter = IncomeMenuAdapter(mutableListOf(), this, viewModel)
        var orderRecyclerView: RecyclerView = binding.rwOrderMenu
        orderRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        orderRecyclerView.adapter = orderAdapter


        viewModel.uiState.observe(viewLifecycleOwner, {state ->
            var st = state as UiState.InventoryMenu
            if(st.isSupplierModeActive){
                binding.rwSupplierMenu.visibility = View.VISIBLE
                binding.rwOrderMenu.visibility = View.GONE
            }else{
                binding.rwSupplierMenu.visibility = View.GONE
                binding.rwOrderMenu.visibility = View.VISIBLE
            }
        })
        localViewModel.Suppliers.observe(requireActivity(), { items ->
            supplierAdapter.updateData(items)
        })
        localViewModel.Orders.observe(requireActivity(), { items ->
            orderAdapter.updateMenuItems(items)
        })

        localViewModel.LoadSuppliers(requireActivity())
        localViewModel.LoadOrder(requireActivity())


        return  binding.root
    }
}