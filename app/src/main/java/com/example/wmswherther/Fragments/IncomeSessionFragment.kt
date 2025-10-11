package com.example.wmswherther.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wmsRemote.databinding.FragmentIncomeSessionBinding
import com.example.wmswherther.viewModel.MainViewModel

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
            //val localViewModel = ViewModelProvider(requireActivity()).get(IncomeSessionViewModel::class)
            _binding = FragmentIncomeSessionBinding.inflate(inflater)





            return  binding.root
        }

}