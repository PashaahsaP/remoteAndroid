package com.example.wmswherther.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wmsRemote.databinding.FragmentIncomeBinding
import com.example.wmsRemote.databinding.FragmentMainBinding
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
        with(binding){
            btnExit.setOnClickListener {
                viewModel.changeMenuStatus(true)
                parentFragmentManager.popBackStack()
            }
        }
        return  binding.root
    }
}