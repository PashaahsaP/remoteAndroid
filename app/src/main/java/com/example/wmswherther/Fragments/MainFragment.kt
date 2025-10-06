package com.example.wmswherther.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.wmsRemote.AssemblyActivity
import com.example.wmsRemote.InventoryActivity
import com.example.wmsRemote.MainActivity
import com.example.wmsRemote.MoveActivity
import com.example.wmsRemote.PickerActivity
import com.example.wmsRemote.R
import com.example.wmsRemote.SearchActivity
import com.example.wmsRemote.databinding.ActivityMainBinding
import com.example.wmsRemote.databinding.FragmentMainBinding
import com.example.wmswherther.LogActivity
import com.example.wmswherther.viewModel.MainViewModel

class MainFragment : Fragment(R.layout.fragment_main) {
    private var _binding: FragmentMainBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentMain")
    private val viewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        with(binding){
            btnIncome.setOnClickListener {
                parentFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.slide_in_right, // enter
                        R.anim.slide_out_left,  // exit
                        R.anim.slide_in_left,   // popEnter
                        R.anim.slide_out_right  // popExit
                    )
                    replace<IncomeFragment>(R.id.fragmentContainer)
                    addToBackStack(null)
                }
                viewModel.closeMenu()

            }
        }
        return binding.root
    }
}