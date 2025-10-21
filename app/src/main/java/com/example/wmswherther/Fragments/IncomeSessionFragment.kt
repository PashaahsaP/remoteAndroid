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
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.ActivityMainBinding
import com.example.wmsRemote.databinding.FragmentIncomeSessionBinding
import com.example.wmswherther.Adapters.IncomeSessionAdapter
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.data.db.Barcode
import com.example.wmswherther.viewModel.IncomeSessionViewModel
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IncomeSessionFragment : Fragment() {

        private var _binding: FragmentIncomeSessionBinding? = null
        private val binding
            get() = _binding ?: throw IllegalStateException("Binding for FragmentIncome")
        private val viewModel: MainViewModel by activityViewModels()


        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val localViewModel = ViewModelProvider(requireActivity()).get(IncomeSessionViewModel::class)
            _binding = FragmentIncomeSessionBinding.inflate(inflater)


            var recyclerView: RecyclerView = binding.rwIncomeSessionList
            var adapter = IncomeSessionAdapter(listOf(), recyclerView, localViewModel, requireActivity())
            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
            recyclerView.adapter = adapter
            val sessionId = arguments?.getString("id")

            localViewModel.items.observe(viewLifecycleOwner,{ items ->
                adapter.updateCollection(items, localViewModel.getSelectedItem())
                recyclerView.smoothScrollToPosition(localViewModel.getSelectedItem())

            })
            viewModel.Barcode.observe(viewLifecycleOwner,{ barcode ->
                if(barcode != "") {
                    lifecycleScope.launch {
                        var newItems: List<IncomeItem> = listOf()
                        var bar =
                            MainDB.getDB(requireActivity()).getDao().getBarcodeByName(barcode)
                        if (bar != null && bar is Barcode) {
                            withContext(Dispatchers.IO) {
                                localViewModel.items.value?.forEach { item ->
                                    if (item.catalogId == bar.catalogId) {
                                        newItems += IncomeItem(item.name, item.catalogId, (item.haveCount + 1), item.allCount, false)
                                    }else {
                                        newItems += item
                                    }

                                }

                            }
                            withContext(Dispatchers.Main) {
                                localViewModel.updateItems(newItems)
                                var binding = viewModel.getMainBinding()
                                if(viewModel.IsScanningActive.value == true) {
                                    binding?.etIncomeBarcode?.requestFocus()

                                }else{
                                    binding?.etIncomeBarcodeScan?.requestFocus()
                                }
                            }
                        }
                    }
                }

            })

            lifecycleScope.launch {
                var data: List<IncomeItem> = listOf()
                withContext(Dispatchers.IO){
                    data = localViewModel.loadItems(MainDB.getDB(requireActivity()), sessionId.toString())
                }
                withContext(Dispatchers.Main){
                    localViewModel.updateItems(data)
                    localViewModel.setSelectedItem(0)
                }
            }





            return  binding.root
        }

}
