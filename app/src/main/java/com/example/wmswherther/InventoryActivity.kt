package com.example.wmsRemote

import android.content.Context
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.databinding.ActivityInventoryBinding
import com.example.wmsRemote.Adapters.InventoryAdapter
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.SupplierType
import com.example.wmsRemote.viewModel.InventoryViewModel

data class CellWrapper(var cell: Cell?)
class InventoryActivity : AppCompatActivity() {
    override fun onBackPressed() {
        with(binding){
            if(llMenu.visibility == View.VISIBLE){
                super.onBackPressed()
            }else{
                llMenu.visibility = View.VISIBLE
                llContainer.visibility = View.GONE
            }

        }

    }
    private lateinit var viewModel: InventoryViewModel
    lateinit var soundPool: SoundPool
    private var _binding: ActivityInventoryBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for ActivityMain")
    var cell : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()//TODO experement with this that collapse navigation bar


        _binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(InventoryViewModel::class.java)
        var adapter = InventoryAdapter(this, lifecycleScope,viewModel, listOf())
        var recyclerView : RecyclerView = binding.rwContainer
        val db = MainDB.getDB(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        var successSoundId: Int = 0
        var errorSoundId: Int = 0

        fun initSounds(context: Context) {
            soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .build()

            successSoundId = soundPool.load(context, R.raw.positive, 1)
            errorSoundId = soundPool.load(context, R.raw.error, 1)
        }
        initSounds(this)


        viewModel.items.observe(this, Observer{ newItems -> adapter.updateData(newItems) })
        viewModel.cell.observe(this,Observer{ newCell -> binding.tvCellName.text = newCell })
        viewModel.isInventoryActive.observe(this,{isNewInventoryActive ->
            if (isNewInventoryActive){
                binding.btnSave.visibility = View.VISIBLE
            }else {
                binding.btnSave.visibility = View.GONE
            }
        })


        with(binding){
            etCell.setOnEditorActionListener { textView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    // Выполнить действие
                    val text = etCell.text.toString()
                    if (text != ""){
                        handleTextChange(text)
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
            btnSearch.setOnClickListener{
                viewModel.handleSearchEvent(etCell.text.toString(), db, this@InventoryActivity, soundPool, successSoundId, errorSoundId, binding.swipe)
            }
            btnSave.setOnClickListener {
                viewModel.handelSaveEvent(db, this@InventoryActivity, binding.swipe)
            }
            btnBork.setOnClickListener {
                llMenu.visibility = View.GONE
                llContainer.visibility = View.VISIBLE
                viewModel.supplier = SupplierType.Bork.ordinal
            }
            btnAtomy.setOnClickListener {
                llMenu.visibility = View.GONE
                llContainer.visibility = View.VISIBLE
                viewModel.supplier = SupplierType.Atomy.ordinal
            }
        }

    }

    private fun handleTextChange(text: String) {
        binding.btnSearch.performClick()
        binding.etCell.text.clear()
        binding.etCell.requestFocus()
    }

    fun convertToInt(nullableInt: Int?): Int {
        return nullableInt ?: 0  // If nullableInt is null, use 0 as default
    }


}