package com.example.wmswherther.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.FragmentIncomeBinding
import com.example.wmsRemote.databinding.FragmentMainBinding
import com.example.wmswherther.Adapters.IncomeMenuAdapter
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.getValue

class IncomeFragment : Fragment() {

    private var _binding: FragmentIncomeBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentIncome")
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainContext = requireContext()
        _binding = FragmentIncomeBinding.inflate(inflater)
        var data : List<TaskMenuItem> = listOf()
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                var dao = MainDB.getDB(mainContext).getDao()
                var suppliers  = dao.getAllSuppliers()
                dao.getAllIncomeSession().forEach{ item ->
                    data += TaskMenuItem(
                        supplier = suppliers.firstOrNull { inner -> inner.id == item.supplierId }!!.name,
                        progress = "0/1",
                        number = "",
                        date = LocalDate.now().toString()
                    )
                }
            }
            withContext(Dispatchers.Main) {
                var adapter = IncomeMenuAdapter(data)
                var recyclerView: RecyclerView = binding.rwIncomeMenu
                recyclerView.layoutManager = LinearLayoutManager(requireActivity())
                recyclerView.adapter = adapter
            }
        }



        return  binding.root
    }
}