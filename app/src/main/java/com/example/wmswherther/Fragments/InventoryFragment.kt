package com.example.wmswherther.Fragments

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
import com.example.wmswherther.Adapters.InventoryAdapter
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
        var adapter = InventoryAdapter(mutableListOf(), this, viewModel)
        var recyclerView: RecyclerView = binding.rwIncomeMenu
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        localViewModel.Suppliers.observe(requireActivity(), { items ->
            adapter.updateData(items)
        })

        localViewModel.LoadSuppliersFromLocal(requireActivity())


        return  binding.root
    }
}