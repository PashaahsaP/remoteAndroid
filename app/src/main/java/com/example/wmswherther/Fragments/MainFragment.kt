package com.example.wmswherther.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentMain")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        with(binding){
            btnIncome.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, IncomeFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
        return binding.root
    }
}