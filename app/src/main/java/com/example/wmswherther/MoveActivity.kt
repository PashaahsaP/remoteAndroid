package com.example.wmsRemote

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wmsRemote.databinding.ActivityMoveBinding
import com.example.wmsRemote.viewModel.MovingViewModel
import com.example.wmsRemote.Adapters.MoveSessionAdapter
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.SupplierType
import kotlinx.coroutines.channels.Channel

class MoveActivity : AppCompatActivity() {
    override fun onBackPressed() {
        with(binding){
            if(llMenu.visibility == View.VISIBLE){
                super.onBackPressed()
            }else{
                llMenu.visibility = View.VISIBLE
                CLContainer.visibility = View.GONE
            }

        }

    }
    private lateinit var viewModel: MovingViewModel
    private val textChangesChannel = Channel<String>()
    private var _binding: ActivityMoveBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for ActivityMain")
    //TODO maybe remove cellFrom.
    //TODO move  the code resposible for camere into method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMoveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //TODO remake that can use repository patter for different db type
        val db = MainDB.getDB(this)
        var recyclerView = binding.llContainer
        viewModel = ViewModelProvider(this).get(MovingViewModel::class.java)
        recyclerView.layoutManager = LinearLayoutManager(this)
        var adapter = MoveSessionAdapter(mutableListOf(), this, viewModel)

        recyclerView.adapter = adapter

        viewModel.isMoving.observe(this, Observer{isMove->
            if (isMove) {
                binding.btnMove.visibility = View.GONE
                binding.btnCancel.visibility = View.VISIBLE
            }else {
                binding.btnMove.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.GONE
            }
        })
        viewModel.myData.observe(this, Observer{data ->
            adapter.updateData(data)
        })
        viewModel.cell.observe(this, Observer{str ->
            if (str != ""){
                binding.btnMove.visibility = View.VISIBLE
            }else{
                binding.btnMove.visibility = View.GONE
            }
            binding.tvCellName.text = str
        })



        with(binding){
            etCell.setOnEditorActionListener { textView, actionId, event ->
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

            }
            //TODO make that cell displayed in top, move btn collapsed, cancel btn appear, changing flag moving
            btnMove.setOnClickListener{
                viewModel.updateIsMoving(true)
            }
            btnCancel.setOnClickListener {
                viewModel.updateIsMoving(false)
            }
            etCell.addTextChangedListener(object : TextWatcher {
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
            }
            }

    }


    fun handleTextChange(
        text: String,
        trim: String,
        moveActivity: MoveActivity,
        db: MainDB,
        adapter: MoveSessionAdapter,
    ) {
        if (text != "") {
            viewModel.searchBtnHandler(trim, moveActivity, db, adapter, binding)
            binding.etCell.text.clear()
            binding.etCell.requestFocus()
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
}


