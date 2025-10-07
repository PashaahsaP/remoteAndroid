package com.example.wmswherther.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.databinding.FragmentIncomeBinding
import com.example.wmsRemote.databinding.FragmentMainBinding
import com.example.wmswherther.Adapters.IncomeMenuAdapter
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.viewModel.MainViewModel
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
        _binding = FragmentIncomeBinding.inflate(inflater)
        var data : List<TaskMenuItem> = listOf(TaskMenuItem(
            supplier = "Bork",
            date = "22.12.2025",
            number = "5",
            progress = "0/34"
        ),
            TaskMenuItem(
                supplier = "Bork",
                date = "22.12.2025",
                number = "5",
                progress = "0/34"
            ),
            TaskMenuItem(
                supplier = "Bork",
                date = "22.12.2025",
                number = "5",
                progress = "0/34"
            ),
            TaskMenuItem(
                supplier = "Bork",
                date = "22.12.2025",
                number = "5",
                progress = "0/34"
            ),
            TaskMenuItem(
                supplier = "Bork",
                date = "22.12.2025",
                number = "5",
                progress = "0/34"
            ),
            TaskMenuItem(
                supplier = "Bork",
                date = "22.12.2025",
                number = "5",
                progress = "0/34"
            ),
            TaskMenuItem(
                supplier = "Bork",
                date = "22.12.2025",
                number = "5",
                progress = "0/34"
            ),
            TaskMenuItem(
                supplier = "Bork",
                date = "22.12.2025",
                number = "5",
                progress = "0/34"
            ),
            TaskMenuItem(
                supplier = "Bork",
                date = "22.12.2025",
                number = "5",
                progress = "0/34"
            ))
        var adapter = IncomeMenuAdapter(data)
        var recyclerView: RecyclerView = binding.rwIncomeMenu
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        return  binding.root
    }
}