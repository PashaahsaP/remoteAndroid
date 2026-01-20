package com.example.wmswherther.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.FragmentIncomeBinding
import com.example.wmsRemote.databinding.FragmentMoveBinding
import com.example.wmswherther.Adapters.IncomeMenuAdapter
import com.example.wmswherther.Adapters.MoveAdapter
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.viewModel.IncomeMenuViewModel
import com.example.wmswherther.viewModel.MainViewModel
import com.example.wmswherther.viewModel.MoveViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MoveFragment : Fragment() {
    private var _binding: FragmentMoveBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentMove")
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val localViewModel = ViewModelProvider(requireActivity()).get(MoveViewModel::class)
        _binding = FragmentMoveBinding.inflate(inflater)
        var adapter = MoveAdapter(mutableListOf())
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