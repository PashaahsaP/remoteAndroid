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
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.FragmentAssemblyBinding
import com.example.wmsRemote.viewModel.AssemblyViewModel
import com.example.wmswherther.Adapters.AssemblyMenuAdapter
import com.example.wmswherther.data.db.SyncWorker
import com.example.wmswherther.viewModel.MainViewModel
import timber.log.Timber

class PickerFragment: Fragment() {
    private var _binding: FragmentAssemblyBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentAssemblyBinding")
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var dao = MainDB.getDB(requireActivity()).getDao()
        val localViewModel = ViewModelProvider(requireActivity()).get(AssemblyViewModel::class)
        _binding = FragmentAssemblyBinding.inflate(inflater)

        var adapter = AssemblyMenuAdapter(listOf(), this, viewModel, localViewModel, dao)
        var recyclerView: RecyclerView = binding.rwAssemblyMenu
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter


        localViewModel.items.observe(viewLifecycleOwner, { items ->
            adapter.updateMenuItems(items)
        })


        with(binding){
            swipe.setOnRefreshListener {
                pullChanges(requireActivity())
                swipe.isRefreshing = false
            }
        }


        localViewModel.loadSessions(dao)
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