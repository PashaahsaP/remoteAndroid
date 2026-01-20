package com.example.wmswherther.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.Adapters.MoveSessionAdapter
import com.example.wmsRemote.MoveActivity
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.databinding.FragmentMoveSessionBinding
import com.example.wmsRemote.viewModel.MoveSessionViewModel
import com.example.wmswherther.viewModel.MainViewModel

class MoveSessionFragment: Fragment() {

    private var _binding: FragmentMoveSessionBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentIncome")
    private val viewModel: MainViewModel by activityViewModels()
    val localViewModel = ViewModelProvider(requireActivity()).get(MoveSessionViewModel::class)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMoveSessionBinding.inflate(inflater)
        var adapter = MoveSessionAdapter(listOf(), requireActivity(), localViewModel)
        var recyclerView: RecyclerView = binding.rwContainer
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        localViewModel.isMoving.observe(requireActivity(), {isMove->
            if (isMove) {
                binding.btnMove.visibility = View.GONE
                binding.btnCancel.visibility = View.VISIBLE
            }else {
                binding.btnMove.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.GONE
            }
        })
        localViewModel.myData.observe(requireActivity(), {data ->
            adapter.updateData(data)
        })
        localViewModel.cell.observe(requireActivity(), {str ->
            if (str != ""){
                binding.btnMove.visibility = View.VISIBLE
            }else{
                binding.btnMove.visibility = View.GONE
            }
            binding.tvCellName.text = str
        })



        with(binding){
            /*etCell.setOnEditorActionListener { textView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    // Выполнить действие
                    val text = etCell.text.toString()
                    if(text != "") {
                        handleTextChange(
                            text,
                            etCell.text.toString().trim(),
                            this@MoveActivity,
                            db,
                            adapter
                        )
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
            btnSearch.setOnClickListener {

                handleTextChange(tvCellName.text.toString(), etCell.text.toString().trim(), this@MoveActivity, db, adapter)

            }*/
            //TODO make that cell displayed in top, move btn collapsed, cancel btn appear, changing flag moving
            btnMove.setOnClickListener{
                localViewModel.updateIsMoving(true)
            }
            btnCancel.setOnClickListener {
                localViewModel.updateIsMoving(false)
            }
            /*etCell.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {
                    textChangesChannel.trySend(editable.toString())
                }
            })
            btnBork.setOnClickListener {
                llMenu.visibility = View.GONE
                CLContainer.visibility = View.VISIBLE
                viewModel.supplier = SupplierType.Bork.ordinal
            }
            btnAtomy.setOnClickListener {
                llMenu.visibility = View.GONE
                CLContainer.visibility = View.VISIBLE
                viewModel.supplier = SupplierType.Atomy.ordinal
            }*/
        }
        return binding.root
    }



    fun handleTextChange(
        text: String,
        trim: String,
        moveActivity: MoveActivity,
        db: MainDB,
        adapter: MoveSessionAdapter
    ) {
        if (text != "") {
            localViewModel.searchBtnHandler(trim, moveActivity, db, adapter, binding)
            /* binding.etCell.text.clear()
             binding.etCell.requestFocus()*/
        }
    }



}


private fun isCell(cell: String): Boolean {
    if (cell.length == 4 && cell[0] in 'A' .. 'Z' && cell[1].isDigit() && cell[2].isDigit() && cell[3].isDigit()){
        return true
    }
    return false
}
fun convertToInt(nullableInt: Int?): Int {
    return nullableInt ?: 0  // If nullableInt is null, use 0 as default
}
