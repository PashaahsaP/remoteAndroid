package com.example.wmswherther.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.wmsRemote.R
import com.example.wmsRemote.databinding.FragmentMainBinding
import com.example.wmswherther.Classes.UiState
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
        var mainContext : Context = requireActivity()
        //viewModel.setCurrFragment(this)
       /* lifecycleScope.launch {
            withContext(Dispatchers.IO){//for testing income ui
                val db = MainDB.getDB(mainContext)
                var dao = db.getDao()
                var supplier = Supplier(UUID.randomUUID().toString(), "Bork", null)
                dao.insertSupplier(supplier)
                var pickerCellType = CellType(UUID.randomUUID().toString(), "Picker","####", null)
                var inCellType = CellType(UUID.randomUUID().toString(), "Income","#####", null)
                var firstCell = Cell(UUID.randomUUID().toString(), inCellType.id, "IN-01")
                var incomeCell = Cell(UUID.randomUUID().toString(), pickerCellType.id, "Z000")
                dao.insertCellType(pickerCellType)
                dao.insertCellType(inCellType)
                dao.insertCell(firstCell)
                dao.insertCell(incomeCell)
                var incomeSession = SessionIncome(UUID.randomUUID().toString(), supplier.id, null, firstCell.id, StatusType.Created.ordinal, System.currentTimeMillis(),null, null, null )
                var catalog = Catalog(UUID.randomUUID().toString(), "Kettle k534", "2342343", supplier.id, null)
                var goods = Goods(UUID.randomUUID().toString(), 3, incomeCell.id, catalog.id, System.currentTimeMillis(),null)
                var incomeItem = IncomeItem(UUID.randomUUID().toString(), incomeSession.id, goods.id, StatusType.Created.ordinal, null)
                dao.insertCatalog(catalog)
                dao.insertGoods(goods)
                dao.insertIncomeSession(incomeSession)
                dao.insertIncomeItem(incomeItem)
            }
        }*/
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

                viewModel.setActiveUi(UiState.IncomeMenu(prevState = viewModel.uiState.value))
            }
            btnMove.setOnClickListener {
                parentFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.slide_in_right, // enter
                        R.anim.slide_out_left,  // exit
                        R.anim.slide_in_left,   // popEnter
                        R.anim.slide_out_right  // popExit
                    )
                    replace<MoveFragment>(R.id.fragmentContainer)
                    addToBackStack(null)
                }

                viewModel.setActiveUi(UiState.MoveMenu(prevState = viewModel.uiState.value))
            }
        }
        return binding.root
    }
}