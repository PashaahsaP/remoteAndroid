package com.example.wmswherther.Fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wmsRemote.databinding.FragmentIncomeBinding
import com.example.wmsRemote.databinding.FragmentMoveBinding
import com.example.wmswherther.viewModel.MainViewModel

class MoveFragment : Fragment() {
    private var _binding: FragmentMoveBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentMove")
    private val viewModel: MainViewModel by activityViewModels()
}