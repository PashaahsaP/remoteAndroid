package com.example.wmsRemote

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.Adapters.AssemblySessionAdapter
import com.example.wmsRemote.databinding.ActivityAssemblyBinding
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.viewModel.AssemblyViewModel
import java.util.UUID

class AssemblyActivity : AppCompatActivity() {
    private var _binding: ActivityAssemblyBinding? = null
    private lateinit var viewModel: AssemblyViewModel
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for AssemblyMain")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var color = ContextCompat.getColor(this, R.color.regularBlue)
        val id = UUID.randomUUID().toString()
        val db = MainDB.getDB(this)
        _binding = ActivityAssemblyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(AssemblyViewModel::class.java)
        var adapter = AssemblySessionAdapter(this, lifecycleScope, viewModel, listOf())
        var recyclerView: RecyclerView = binding.rwListItem
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        /* viewModel.sessions.observe(this, Observer { newCollection ->
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    updateMenu(newCollection, db)
                }
            }

        })*/
        /* viewModel.activeElement.observe(this, Observer{newItem ->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    updateActiveElement(newItem)
                }
            }
        })
        viewModel.items.observe(this, Observer{ items ->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    adapter.updateData(items)
                }
            }
        })
        viewModel.assemblyStatus.observe(this, Observer {status->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    updateAssemblyStyle(status)
                }
            }
        })
        viewModel.menuStatus.observe(this, Observer {status->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    updateMenuStyle(status)
                }
            }
        })

        viewModel.loadCollection(db)

        with(binding){
            etInput.setOnEditorActionListener { textView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    // Выполнить действие
                    val text = etInput.text.toString()
                    if(text != "") {
                        handleTextChange(text, text.trim(), this@AssemblyActivity, db )
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
            etCount.setOnEditorActionListener { textView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    viewModel.changeAssemblyStatus(StatusType.EnterCell.ordinal)
                    //TODO как то нужно обновить коллекцию, сменить активный элемент. И предупреждающее окно нужно при вводе другого количества
                    // Выполнить действие || actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                    val text = etCount.text.toString()
                    if(text != "") {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                handleTextChangeCount(text, text.trim(), this@AssemblyActivity, db)
                            }
                        }
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
            }

        }*/

        /* private fun updateMenuStyle(status: Int?) {
        if(status == 0){
            binding.llMenuContainer.visibility = View.VISIBLE
            binding.llAssemblyContainer.visibility = View.GONE
        }
        else if(status == 1){
            binding.llMenuContainer.visibility = View.GONE
            binding.llAssemblyContainer.visibility = View.VISIBLE
        }
    }

    private fun updateAssemblyStyle(status: Int?) {
        with(binding){
            if(StatusType.EnterCell.ordinal == status){
                tvCell.setTextColor(resources.getColor(R.color.white))
                tvGoodsName.setTextColor(resources.getColor(R.color.regularGrey))
                tvBarcode.setTextColor(resources.getColor(R.color.regularGrey))
                etCount.setTextColor(resources.getColor(R.color.regularGrey))
                etInput.isEnabled = true
                etInput.requestFocus()
                etCount.isEnabled = false
            }else if(StatusType.EnterBarcode.ordinal == status){
                tvGoodsName.setTextColor(resources.getColor(R.color.white))
                tvBarcode.setTextColor(resources.getColor(R.color.white ))
                tvCell.setTextColor(resources.getColor(R.color.regularGrey))
            }else{
                tvGoodsName.setTextColor(resources.getColor(R.color.regularGrey))
                tvBarcode.setTextColor(resources.getColor(R.color.regularGrey))
                etCount.setTextColor(resources.getColor(R.color.white))
                etCount.isEnabled = true
                etInput.isEnabled = false
                etCount.requestFocus()
            }
        }
    }*/
        /*private fun updateListItems(items: List<AssemblyItem>?, ) {
        val paint = binding.tvListNames.paint
        val maxWidth = binding.tvListNames.width
        val text = "A".repeat(200) // Можно любой длинный текст
        val count = paint.breakText(
            text, 0, text.length, true, maxWidth.toFloat(), null
        )
        var (names, cells) = items!!.fold(Pair(StringBuilder(""),StringBuilder(""))){acc, element ->
            binding.tvListNames.text
            var n = "\n".repeat((element.name.length/ (count+4)) + 1)
            acc.first.append("${element.name}\n")
            acc.second.append("${element.cell}${n}")
            acc
        }
        with(binding){
            tvListNames.text = names
            tvListCells.text = cells
        }

    }*/
        /*  private fun updateActiveElement(newItem: AssemblyItem?) {
        with(binding){
            tvCell.text = newItem!!.cell
            //tvBarcode.text = newItem.barcode
            tvGoodsName.text = newItem.name
            etCount.setText(newItem.amount.toString())
        }
    }*/
        /*private fun updateMenu(sessions: List<AssemblySession>, db: MainDB) {
        viewModel.changeAssemblyStatus(StatusType.EnterCell.ordinal)
        binding.llMenuContainer.removeAllViews()
        for (session in sessions){
            var view = AssemblyMenu(this)
            var supplierTv = view.findViewById<TextView>(R.id.tvSupplier)
            var numberTv = view.findViewById<TextView>(R.id.tvNumber)
            var linesTv = view.findViewById<TextView>(R.id.tvProgress)
            var dateTV = view.findViewById<TextView>(R.id.tvDate)

            supplierTv.text = "${getSupplier(session.supplier)}"
            numberTv.text = "Заказ №${session.id}"
            linesTv.text = "Кол-во: ${session.amount}"
            dateTV.text = session.created_at
            if(session.status == StatusType.Work.ordinal || session.status == StatusType.Created.ordinal) {
                view.setOnClickListener {
                    viewModel.loadItems(convertToInt(session.id), db = db, session.supplier)
                    viewModel.changeMenuStatus(1);
                }
            }
            binding.llMenuContainer.addView(view)
        }


}*/
        /* private fun getSupplier(supplier: Int): String {
        if(SupplierType.Bork.ordinal == supplier)
            return SupplierType.Bork.name
        else if(SupplierType.Atomy.ordinal == supplier)
            return SupplierType.Atomy.name
        else
            return SupplierType.FeedConsalt.name
    }*/
        /*private fun handleTextChange(text: String) {
        binding.btnSearch.performClick()
        binding.etCell.text.clear()
        binding.etCell.requestFocus()
    }*/
        /* private fun requestFocusAndHideKeyboard() {
        currentFocus?.clearFocus()
        binding.etCell.requestFocus() // Request focus

        Handler(Looper.getMainLooper()).postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isAcceptingText) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0) // show the keyboard
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0) // Hide the keyboard
            }
        }, 100) // Delay for 100ms
    }*/
        /*fun convertToInt(nullableInt: Int?): Int {
        return nullableInt ?: 0  // If nullableInt is null, use 0 as default
    }
    fun handleTextChange(
        text: String,
        trim: String,
        assemblyActivity: AssemblyActivity,
        db: MainDB
    ) {
        if (text != "") {
            viewModel.searchBtnHandler(trim, assemblyActivity, db)
            binding.etInput.text.clear()
            binding.etInput.requestFocus()
        }
    }
    suspend fun handleTextChangeCount(
        text: String,
        trim: String,
        assemblyActivity: AssemblyActivity,
        db: MainDB
    ) {
        if (text != "") {
            viewModel.searchBtnHandlerCount(trim, assemblyActivity, db)
            binding.etCount.text.clear()
            binding.etInput.requestFocus()
        }
    }*/
    }
}