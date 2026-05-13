package com.example.wmswherther.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wmsRemote.databinding.FragmentMoveBinding
import com.example.wmswherther.Adapters.MoveAdapter
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.data.db.SyncWorker
import com.example.wmswherther.viewModel.MainViewModel
import com.example.wmswherther.viewModel.MoveViewModel

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
        var adapter = MoveAdapter(mutableListOf(), this, viewModel)
        var recyclerView: RecyclerView = binding.rwIncomeMenu
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        localViewModel.Suppliers.observe(requireActivity(), { items ->
            adapter.updateData(items)
        })

        localViewModel.LoadSuppliersFromLocal(requireActivity())
        with(binding){
            swipe.setOnRefreshListener {
                pullChanges(requireActivity())
                swipe.isRefreshing = false
            }
        }

        return  binding.root
    }
}
private fun pullChanges(requireActivity: FragmentActivity) {
    val data = Data.Builder()
        .putString("sync_type", "FULL")
        .build()

    val request =
        OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(data)
            .build()

    WorkManager.getInstance(requireActivity)
        .enqueue(request)
}